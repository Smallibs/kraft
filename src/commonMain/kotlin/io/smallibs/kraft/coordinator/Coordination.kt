package io.smallibs.kraft.coordinator

import io.smallibs.kraft.coordinator.service.Connector
import io.smallibs.kraft.election.Transition
import io.smallibs.kraft.election.data.Action
import io.smallibs.kraft.election.data.Action.AppendResponse
import io.smallibs.kraft.election.data.Action.RequestAppend
import io.smallibs.kraft.election.data.Node
import io.smallibs.kraft.election.data.Reaction
import io.smallibs.kraft.election.data.Reaction.*
import io.smallibs.kraft.election.data.Timer
import io.smallibs.kraft.log.LeaderManager
import io.smallibs.kraft.log.LogManager
import io.smallibs.kraft.log.data.Append

class Coordination<A>(
    private val connector: Connector<A>,
    private val runner: (A) -> Unit,
    private val behavior: Node,
    private val logManager: LogManager<A>,
    private val leaderManager: LeaderManager<A>?
) {

    private operator fun invoke(
        behavior: Node,
        logManager: LogManager<A> = this.logManager,
        leaderManager: LeaderManager<A>? = this.leaderManager
    ): Coordination<A> =
        Coordination(connector, runner, behavior, logManager, leaderManager)

    fun accept(action: Action<A>) = Transition.run {
        behavior.perform(action).let {
            execute(it.first, it.second)
        }
    }

    //
    // Private behaviors
    //

    private fun execute(behavior: Node, reactions: List<Reaction<A>>) =
        reactions.fold(setLeaderManager(behavior)) { coordination, action ->
            when (action) {
                is ArmElectionTimeout -> coordination.armTimeout(behavior, Timer.Election)
                is ArmHeartbeatTimeout -> coordination.armTimeout(behavior, Timer.Heartbeat)
                is StartElection -> coordination.startElection(behavior)
                is AcceptVote -> coordination.acceptVote(behavior)
                is SynchroniseLog -> coordination.synchroniseLog(behavior)
                is AppendRequested -> coordination.appendRequest(action, behavior)
                is AppendAccepted -> coordination.appendAccepted(action, behavior)
            }
        }

    private fun setLeaderManager(behavior: Node) =
        when (behavior) {
            is Node.Leader ->
                (leaderManager ?: LeaderManager(behavior.context.self, logManager, behavior.context.livingNodes)).let {
                    this(behavior, leaderManager = it)
                }
            else -> this
        }

    private fun armTimeout(behavior: Node, timer: Timer) =
        connector.scheduleTimeOut(timer).let {
            this(behavior)
        }


    private fun startElection(behavior: Node) =
        behavior.context.livingNodes.forEach {
            connector.requestVote(
                it,
                behavior.context.term,
                logManager.last()
            )
        }.let {
            this(behavior)
        }

    private fun acceptVote(behavior: Node) =
        connector.acceptVote(behavior.context.self, behavior.context.term).let {
            this(behavior)
        }

    private fun synchroniseLog(behavior: Node) =
        leaderManager?.let {
            it.prepareAppend().forEach {
                connector.appendEntries(
                    it.key,
                    RequestAppend(
                        behavior.context.self,
                        behavior.context.term,
                        it.value.previous,
                        it.value.leaderCommit,
                        it.value.entries
                    )
                )
            }
        }.let {
            this(behavior)
        }

    private fun appendRequest(action: AppendRequested<A>, behavior: Node) =
        logManager.append(
            Append(
                action.requestAppend.previous,
                action.requestAppend.leaderCommit,
                action.requestAppend.entries
            )
        ).let {
            connector.appendResult(
                action.requestAppend.leader,
                AppendResponse(
                    behavior.context.self,
                    action.requestAppend.term,
                    true,
                    it.second.matchIndex
                )
            )

            it.second.entries.map { it.value }.forEach(runner)

            this(behavior, logManager = it.first)
        }

    private fun appendAccepted(action: AppendAccepted<A>, behavior: Node) =
        when {
            action.appendResponse.success ->
                leaderManager?.appended(action.appendResponse.follower, action.appendResponse.matchIndex)
            else ->
                leaderManager?.rejected(action.appendResponse.follower)
        }.let {
            it?.updateCommitIndex()
        }.let {
            it?.let { it.second.map { it.value }.forEach(runner) }

            this(behavior, leaderManager = it?.first)
        }

}