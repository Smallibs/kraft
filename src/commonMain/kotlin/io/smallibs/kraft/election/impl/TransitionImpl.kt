package io.smallibs.kraft.election.impl

import io.smallibs.kraft.election.Transition
import io.smallibs.kraft.election.TransitionResult
import io.smallibs.kraft.election.data.Action
import io.smallibs.kraft.election.data.Action.AppendResponse
import io.smallibs.kraft.election.data.Action.RequestAppend
import io.smallibs.kraft.election.data.Action.RequestVote
import io.smallibs.kraft.election.data.Action.TimeOut
import io.smallibs.kraft.election.data.NodeKind
import io.smallibs.kraft.election.data.NodeKind.Candidate
import io.smallibs.kraft.election.data.NodeKind.Elector
import io.smallibs.kraft.election.data.NodeKind.Follower
import io.smallibs.kraft.election.data.NodeKind.Leader
import io.smallibs.kraft.election.data.Reaction
import io.smallibs.kraft.election.data.Reaction.AcceptVote
import io.smallibs.kraft.election.data.Reaction.AppendAccepted
import io.smallibs.kraft.election.data.Reaction.AppendRequested
import io.smallibs.kraft.election.data.Reaction.ArmTimeout
import io.smallibs.kraft.election.data.Reaction.InsertMarkInLog
import io.smallibs.kraft.election.data.Reaction.StartElection
import io.smallibs.kraft.election.data.Reaction.SynchroniseLog
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
                    else -> this to listOf(SynchroniseLog(), ArmTimeout(Heartbeat))
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
                    extended -> this.resetTime() to listOf(ArmTimeout(Election))
                    else -> this.becomeElector() to listOf(ArmTimeout(Election))
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
                    else -> this.becomeElector().becomeCandidate() to listOf(StartElection(), ArmTimeout(Election))
                }
            is Action.Voted ->
                when {
                    hasWinElection() -> this.becomeLeader() to listOf(
                        InsertMarkInLog(),
                        SynchroniseLog(),
                        ArmTimeout(Heartbeat)
                    )
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
                    else -> this.becomeCandidate() to listOf(AcceptVote(self), StartElection(), ArmTimeout(Election))
                }
            is RequestVote ->
                this.becomeFollower(action.candidate).extendTimeout() to listOf(AcceptVote(action.candidate))
            else ->
                changeNothing()
        }

    private fun <Command> NodeKind.stepDown(action: Action<Command>): TransitionResult<Command> =
        this.becomeElector(action.term) to when (this) {
            is Leader ->
                listOf(ArmTimeout(Election))
            else ->
                listOf()
        }

    private fun <Command> NodeKind.changeNothing() = this to listOf<Reaction<Command>>()

    private fun Candidate.hasWinElection() = (followers.size + 1) * 2 > livingNodes.size

    private fun <Command> NodeKind.isYoungerTerm(action: Action<Command>) = action.term > term

    private fun <Command> NodeKind.isOlderTerm(action: Action<Command>) = action.term < term
}
