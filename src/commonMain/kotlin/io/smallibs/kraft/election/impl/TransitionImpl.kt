package io.smallibs.kraft.election.impl

import io.smallibs.kraft.election.Transition
import io.smallibs.kraft.election.TransitionResult
import io.smallibs.kraft.election.data.Action
import io.smallibs.kraft.election.data.Action.*
import io.smallibs.kraft.election.data.NodeKind
import io.smallibs.kraft.election.data.NodeKind.*
import io.smallibs.kraft.election.data.Reaction
import io.smallibs.kraft.election.data.Reaction.*
import io.smallibs.kraft.election.data.TimoutType.Election
import io.smallibs.kraft.election.data.TimoutType.Heartbeat

class TransitionImpl : Transition {

    override fun <Command> NodeKind.perform(hasUpToDateLog: (Action<Command>) -> Boolean, action: Action<Command>) =
            when {
                isOlderTerm(action) -> this.changeNothing()
                isYoungerTerm(action) -> this.stepDown(action)
                hasUpToDateLog(action).not() -> changeNothing()
                else ->
                    when (this) {
                        is Leader -> this.perform(action)
                        is Follower -> this.perform(action)
                        is Candidate -> this.perform(action)
                        is Elector -> this.perform(action)
                    }
            }

    // Second level

    private fun <Command> Leader.perform(action: Action<Command>): TransitionResult<Command> =
            when (action) {
                is TimeOut ->
                    when {
                        action.timoutType != Heartbeat -> changeNothing()
                        else -> this to listOf(SynchroniseLog(), ArmHeartbeatTimeout())
                    }
                is AppendResponse ->
                    this to listOf(AppendAccepted(action))
                else ->
                    changeNothing()
            }

    private fun <Command> Follower.perform(action: Action<Command>): TransitionResult<Command> =
            when (action) {
                is TimeOut ->
                    when {
                        action.timoutType != Election -> changeNothing()
                        extended -> this.resetTime() to listOf(ArmElectionTimeout())
                        else -> this.becomeElector() to listOf(ArmElectionTimeout())
                    }
                is RequestAppend<Command> ->
                    this.extendTimeout() to listOf(AppendRequested(action))
                else ->
                    changeNothing()
            }

    private fun <Command> Candidate.perform(action: Action<Command>): TransitionResult<Command> =
            when (action) {
                is TimeOut ->
                    when {
                        action.timoutType != Election -> changeNothing()
                        else -> this.becomeElector().becomeCandidate() to listOf(StartElection(), ArmElectionTimeout())
                    }
                is Voted ->
                    when {
                        hasWinElection() -> this.becomeLeader() to listOf(InsertMarkInLog(), SynchroniseLog(), ArmHeartbeatTimeout())
                        else -> this.stayCandidateWithNewFollower(action.follower) to listOf()
                    }
                else ->
                    changeNothing()
            }

    private fun <Command> Elector.perform(action: Action<Command>): TransitionResult<Command> =
            when (action) {
                is TimeOut ->
                    when {
                        action.timoutType != Election -> changeNothing()
                        else -> this.becomeCandidate() to listOf(AcceptVote(self), StartElection(), ArmElectionTimeout())
                    }
                is RequestVote ->
                    this.becomeFollower(action.candidate).extendTimeout() to listOf(AcceptVote(action.candidate))
                else ->
                    changeNothing()
            }

    private fun <Command> NodeKind.stepDown(action: Action<Command>): TransitionResult<Command> =
            this.becomeElector(action.term) to when (this) {
                is Leader ->
                    listOf(ArmElectionTimeout())
                else ->
                    listOf()
            }

    private fun <Command> NodeKind.changeNothing() = this to listOf<Reaction<Command>>()

    private fun Candidate.hasWinElection() = (followers.size + 1) * 2 > livingNodes.size

    private fun <Command> NodeKind.isYoungerTerm(action: Action<Command>) = action.term > term

    private fun <Command> NodeKind.isOlderTerm(action: Action<Command>) = action.term < term

}