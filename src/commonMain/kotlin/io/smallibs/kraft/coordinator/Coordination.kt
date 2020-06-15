package io.smallibs.kraft.coordinator

import io.smallibs.kraft.common.Identifier
import io.smallibs.kraft.coordinator.service.Connector
import io.smallibs.kraft.election.Transition
import io.smallibs.kraft.election.data.Action
import io.smallibs.kraft.election.data.Action.AppendResponse
import io.smallibs.kraft.election.data.Action.RequestAppend
import io.smallibs.kraft.election.data.Node
import io.smallibs.kraft.election.data.Node.Leader
import io.smallibs.kraft.election.data.Reaction
import io.smallibs.kraft.election.data.Reaction.*
import io.smallibs.kraft.election.data.Timer
import io.smallibs.kraft.log.LeaderManager
import io.smallibs.kraft.log.LogManager
import io.smallibs.kraft.log.data.Append
import io.smallibs.kraft.log.data.Appended

class Coordination<A>(
    private val connector: Connector<A>,
    private val runner: (A) -> Unit,
    private val behavior: Node,
    private val logManager: LogManager<A>,
    private val leaderManager: LeaderManager<A>?
) {

    private operator fun invoke(
        behavior: Node = this.behavior,
        logManager: LogManager<A> = this.logManager,
        leaderManager: LeaderManager<A>? = this.leaderManager
    ): Coordination<A> =
        Coordination(connector, runner, behavior, logManager, leaderManager)

    fun accept(action: Action<A>) = Transition.run {
        behavior.perform(action).let { execute(it.first, it.second) }
    }

    //
    // Private behaviors
    //

    private fun execute(behavior: Node, reactions: List<Reaction<A>>) =
        reactions.fold(this(behavior).setLeaderManager()) { coordination, action ->
            when (action) {
                is ArmElectionTimeout -> coordination.armTimeout(Timer.Election)
                is ArmHeartbeatTimeout -> coordination.armTimeout(Timer.Heartbeat)
                is StartElection -> coordination.startElection()
                is AcceptVote -> coordination.acceptVote()
                is SynchroniseLog -> coordination.synchroniseLog()
                is AppendRequested -> coordination.appendRequest(action.requestAppend)
                is AppendAccepted -> coordination.appendAccepted(action.appendResponse)
            }
        }

    private fun setLeaderManager() =
        when (behavior) {
            is Leader ->
                (leaderManager ?: LeaderManager(behavior.context.self, logManager, behavior.context.livingNodes)).let {
                    this(leaderManager = it)
                }
            else -> this
        }

    private fun armTimeout(timer: Timer) =
        connector.scheduleTimeOut(timer).let { this }


    private fun startElection() =
        behavior.context.livingNodes.forEach {
            connector.requestVote(
                it,
                behavior.context.term,
                logManager.last()
            )
        }.let { this }

    private fun acceptVote() =
        connector.acceptVote(behavior.context.self, behavior.context.term).let { this }

    private fun synchroniseLog() =
        leaderManager?.let {
            it.prepareAppend().forEach {
                connector.appendEntries(it.key, requestAppend(it.key, it.value))
            }
        }.let { this }

    private fun requestAppend(it: Identifier, value: Append<A>) =
        RequestAppend(
            behavior.context.self,
            behavior.context.term,
            value.previous,
            value.leaderCommit,
            value.entries
        )

    private fun appendRequest(requestAppend: RequestAppend<A>) =
        logManager.append(
            Append(requestAppend.previous, requestAppend.leaderCommit, requestAppend.entries)
        ).let {
            connector.appendResult(requestAppend.leader, appendResponse(requestAppend, it))

            it.second.entries.map { it.value }.forEach(runner)

            this(logManager = it.first)
        }

    private fun appendResponse(requestAppend: RequestAppend<A>, it: Pair<LogManager<A>, Appended<A>>) =
        AppendResponse<A>(
            behavior.context.self,
            requestAppend.term,
            true,
            it.second.matchIndex
        )

    private fun appendAccepted(appendResponse: AppendResponse<A>) =
        when {
            appendResponse.success ->
                leaderManager?.appended(appendResponse.follower, appendResponse.matchIndex)
            else ->
                leaderManager?.rejected(appendResponse.follower)
        }.let {
            it?.updateCommitIndex()
        }.let {
            it?.let { it.second.map { it.value }.forEach(runner) }

            this(leaderManager = it?.first)
        }

}