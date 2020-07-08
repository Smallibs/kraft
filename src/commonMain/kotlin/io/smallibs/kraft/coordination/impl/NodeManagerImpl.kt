package io.smallibs.kraft.coordination.impl

import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Insert.Item
import io.smallibs.kraft.common.Insert.Mark
import io.smallibs.kraft.coordination.NodeManager
import io.smallibs.kraft.coordination.service.Connector
import io.smallibs.kraft.coordination.service.Executor
import io.smallibs.kraft.election.Transition
import io.smallibs.kraft.election.data.*
import io.smallibs.kraft.election.data.Action.*
import io.smallibs.kraft.election.data.NodeKind.*
import io.smallibs.kraft.election.data.Reaction.*
import io.smallibs.kraft.log.LeaderManager
import io.smallibs.kraft.log.Log
import io.smallibs.kraft.log.LogManager
import io.smallibs.kraft.log.data.Append
import io.smallibs.kraft.log.data.Appended

class NodeManagerImpl<Command>(
        private val connector: Connector<Command>,
        private val executor: Executor<Command>,
        private val behavior: NodeKind,
        private val logManager: LogManager<Command>,
        private val leaderManager: LeaderManager<Command>? = null
) : NodeManager<Command> {

    override fun insert(a: Command): NodeManager<Command> =
            leaderManager?.let {
                this(leaderManager = it.accept(Entry(behavior.term, Item(a))))
            } ?: when (behavior) {
                is Follower -> connector.insert(behavior.leader, a).let { this }
                else -> this
            }

    override fun accept(action: Action<Command>) = Transition.run {
        behavior.perform(::hasUpToDateLog, action)
    }.let {
        this(it.first, leaderManager = mayBeLeaderManager).execute(it.second)
    }

    private fun execute(reactions: List<Reaction<Command>>) =
            reactions.fold(this) { coordination, action ->
                when (action) {
                    is ArmElectionTimeout -> coordination.armTimeout(TimoutType.Election)
                    is ArmHeartbeatTimeout -> coordination.armTimeout(TimoutType.Heartbeat)
                    is StartElection -> coordination.startElection()
                    is AcceptVote -> coordination.acceptVote()
                    is InsertMarkInLog -> coordination.insertMarkInLog()
                    is SynchroniseLog -> coordination.synchroniseLog()
                    is AppendRequested -> coordination.appendRequest(action.requestAppend)
                    is AppendAccepted -> coordination.appendAccepted(action.appendResponse)
                }
            }

    // -----------------------------------------------------------------------------------------------------------------

    private fun armTimeout(timoutType: TimoutType) =
            connector.scheduleTimeOut(
                    timoutType
            ).let { this }

    private fun startElection() =
            behavior.livingNodes.forEach {
                connector.requestVote(it, behavior.term, logManager.last())
            }.let { this }

    private fun acceptVote() =
            connector.acceptVote(
                    behavior.self, behavior.term
            ).let { this }

    private fun insertMarkInLog() =
            leaderManager?.let {
                this(leaderManager = it.accept(Entry(behavior.term, Mark())))
            } ?: this

    private fun synchroniseLog() =
            leaderManager?.prepareAppend()?.forEach {
                connector.appendEntries(it.key, requestAppend(it.value))
            }.let { this }

    private fun appendRequest(requestAppend: RequestAppend<Command>) =
            logManager.append(
                    Append(requestAppend.previous, requestAppend.leaderCommit, requestAppend.entries)
            ).let {
                connector.appendResult(requestAppend.leader, appendResponse(requestAppend, it))
                executeLog(it.second.entries)(logManager = it.first)
            }

    // -----------------------------------------------------------------------------------------------------------------

    private operator fun invoke(
            behavior: NodeKind = this.behavior,
            executor: Executor<Command> = this.executor,
            logManager: LogManager<Command> = this.logManager,
            leaderManager: LeaderManager<Command>? = this.leaderManager
    ) =
            NodeManagerImpl(connector, executor, behavior, logManager, leaderManager)

    private val mayBeLeaderManager: LeaderManager<Command>?
        get() = when (behavior) {
            is Leader -> leaderManager ?: LeaderManager(behavior.self, logManager, behavior.livingNodes)
            else -> leaderManager
        }

    private fun appendAccepted(response: AppendResponse<Command>) =
            when {
                response.success -> leaderManager?.appended(response.follower, response.matchIndex)
                else -> leaderManager?.rejected(response.follower)
            }?.updateCommitIndex()?.let {
                executeLog(it.second)(leaderManager = it.first)
            } ?: this

    private fun requestAppend(value: Append<Command>) =
            RequestAppend(behavior.self, behavior.term, value.previous, value.leaderCommit, value.entries)

    private fun appendResponse(requestAppend: RequestAppend<Command>, it: Pair<LogManager<Command>, Appended<Command>>) =
            AppendResponse<Command>(behavior.self, requestAppend.term, true, it.second.matchIndex)

    private fun executeLog(entries: List<Entry<Command>>) =
            entries.map { it.value }.fold(executor) { database, a ->
                when (a) {
                    is Item -> database.accept(a.value)
                    is Mark -> database
                }
            }.let {
                this(executor = it)
            }

    private fun hasUpToDateLog(action: Action<Command>): Boolean =
            when (action) {
                is RequestVote -> {
                    val log = logManager.last()
                    val logTerm = logManager.termAt(log.first)
                    val lastLog = action.lastLog
                    lastLog.second > logTerm || (lastLog.first >= log.first && lastLog.second == logTerm)
                }
                else -> true
            }

    /**
     * Companion
     */
    companion object {

        operator fun <Command> invoke(connector: Connector<Command>, executor: Executor<Command>, context: Context, log: Log<Command>) =
                NodeManagerImpl(connector, executor, Elector(context), LogManager(log))
                        .execute(listOf(ArmElectionTimeout()))

    }
}
