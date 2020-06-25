package io.smallibs.kraft.coordination.impl

import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Insert.Item
import io.smallibs.kraft.common.Insert.Mark
import io.smallibs.kraft.coordination.NodeManager
import io.smallibs.kraft.coordination.service.Connector
import io.smallibs.kraft.coordination.service.Database
import io.smallibs.kraft.election.Transition
import io.smallibs.kraft.election.data.*
import io.smallibs.kraft.election.data.Action.*
import io.smallibs.kraft.election.data.Node.*
import io.smallibs.kraft.election.data.Reaction.*
import io.smallibs.kraft.log.LeaderManager
import io.smallibs.kraft.log.Log
import io.smallibs.kraft.log.LogManager
import io.smallibs.kraft.log.data.Append
import io.smallibs.kraft.log.data.Appended

class NodeManagerImpl<A>(
        private val connector: Connector<A>,
        private val database: Database<A>,
        private val behavior: Node,
        private val logManager: LogManager<A>,
        private val leaderManager: LeaderManager<A>?
) : NodeManager<A> {

    override fun insert(a: A): NodeManager<A> =
            leaderManager?.let {
                this(leaderManager = it.accept(Entry(behavior.term, Item(a))))
            } ?: when (behavior) {
                is Follower -> connector.insert(behavior.leader, a).let { this }
                else -> this
            }

    override fun accept(action: Action<A>) = Transition.run {
        behavior.perform(::hasUpToDateLog, action)
    }.let {
        this(it.first, leaderManager = mayBeLeaderManager).execute(it.second)
    }

    private fun execute(reactions: List<Reaction<A>>) =
            reactions.fold(this) { coordination, action ->
                when (action) {
                    is ArmElectionTimeout -> coordination.armTimeout(Timer.Election)
                    is ArmHeartbeatTimeout -> coordination.armTimeout(Timer.Heartbeat)
                    is StartElection -> coordination.startElection()
                    is AcceptVote -> coordination.acceptVote()
                    is InsertMarkInLog -> coordination.insertMarkInLog()
                    is SynchroniseLog -> coordination.synchroniseLog()
                    is AppendRequested -> coordination.appendRequest(action.requestAppend)
                    is AppendAccepted -> coordination.appendAccepted(action.appendResponse)
                }
            }

    // -----------------------------------------------------------------------------------------------------------------

    private fun armTimeout(timer: Timer) =
            connector.scheduleTimeOut(
                    timer
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
                this(leaderManager = it.accept(Entry(behavior.term)))
            } ?: this

    private fun synchroniseLog() =
            leaderManager?.prepareAppend()?.forEach {
                connector.appendEntries(it.key, requestAppend(it.value))
            }.let { this }

    private fun appendRequest(requestAppend: RequestAppend<A>) =
            logManager.append(
                    Append(requestAppend.previous, requestAppend.leaderCommit, requestAppend.entries)
            ).let {
                connector.appendResult(requestAppend.leader, appendResponse(requestAppend, it))
                executeLog(it.second.entries)(logManager = it.first)
            }

    // -----------------------------------------------------------------------------------------------------------------

    private operator fun invoke(
            behavior: Node = this.behavior,
            database: Database<A> = this.database,
            logManager: LogManager<A> = this.logManager,
            leaderManager: LeaderManager<A>? = this.leaderManager
    ) =
            NodeManagerImpl(connector, database, behavior, logManager, leaderManager)

    private val mayBeLeaderManager: LeaderManager<A>?
        get() = when (behavior) {
            is Leader -> leaderManager ?: LeaderManager(behavior.self, logManager, behavior.livingNodes)
            else -> leaderManager
        }

    private fun appendAccepted(response: AppendResponse<A>) =
            when {
                response.success -> leaderManager?.appended(response.follower, response.matchIndex)
                else -> leaderManager?.rejected(response.follower)
            }?.updateCommitIndex()?.let {
                executeLog(it.second)(leaderManager = it.first)
            } ?: this

    private fun requestAppend(value: Append<A>) =
            RequestAppend(behavior.self, behavior.term, value.previous, value.leaderCommit, value.entries)

    private fun appendResponse(requestAppend: RequestAppend<A>, it: Pair<LogManager<A>, Appended<A>>) =
            AppendResponse<A>(behavior.self, requestAppend.term, true, it.second.matchIndex)

    private fun executeLog(entries: List<Entry<A>>) =
            entries.map { it.value }.fold(database) { database, a ->
                when (a) {
                    is Item -> database.accept(a.value)
                    is Mark -> database
                }
            }.let {
                this(database = it)
            }

    private fun hasUpToDateLog(action: Action<A>): Boolean =
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

        operator fun <A> invoke(connector: Connector<A>, database: Database<A>, context: Context, log: Log<A>) =
                NodeManagerImpl(connector, database, Elector(context), LogManager(log), null)
                        .execute(listOf(ArmElectionTimeout()))

    }
}
