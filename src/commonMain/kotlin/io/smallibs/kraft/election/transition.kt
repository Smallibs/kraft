package io.smallibs.kraft.election

import io.smallibs.kraft.election.Action.*
import io.smallibs.kraft.election.Node.*
import io.smallibs.kraft.election.Reaction.*
import io.smallibs.kraft.election.Timer.Election
import io.smallibs.kraft.election.Timer.Heartbeat

// First level

infix fun Node.perform(action: Action): Pair<Node, List<Reaction>> =
    when {
        action.term < context.term -> this.changeNothing()
        action.term > context.term -> this.stepDown(action)
        else -> when (this) {
            is Leader -> this.perform(action)
            is Follower -> this.perform(action)
            is Candidate -> this.perform(action)
            is Elector -> this.perform(action)
        }
    }

// Second level

private fun Leader.perform(action: Action) =
    when (action) {
        is TimeOut ->
            when {
                action.timer != Heartbeat -> changeNothing()
                else -> this to listOf(SynchroniseLog, ArmHeartbeatTimeout)
            }
        is AppendResponse -> this to listOf(AppendAccepted(action))
        else -> changeNothing()
    }

private fun Follower.perform(action: Action) =
    when (action) {
        is TimeOut ->
            when {
                action.timer != Election -> changeNothing()
                this.extended -> this.resetTime() to listOf(ArmElectionTimeout)
                else -> this.becomeElector().becomeCandidate() to listOf(StartElection, ArmElectionTimeout)
            }
        is RequestAppend ->
            this.extendTime() to listOf(AppendRequested(action))
        else -> changeNothing()
    }

private fun Candidate.perform(action: Action) =
    when (action) {
        is TimeOut ->
            when {
                action.timer != Election -> changeNothing()
                else -> this.becomeElector().becomeCandidate() to listOf(StartElection, ArmElectionTimeout)
            }
        is Voted ->
            when {
                winElection() -> this.becomeLeader() to listOf(StartElection, ArmHeartbeatTimeout)
                else -> this.becomeCandidate(action.follower) to listOf()
            }
        else -> changeNothing()
    }

private fun Elector.perform(action: Action) =
    when (action) {
        is TimeOut ->
            when {
                action.timer != Election -> changeNothing()
                else -> this.becomeCandidate() to listOf(StartElection, ArmElectionTimeout)
            }
        is RequestVote -> this.becomeFollower(action.candidate).extendTime() to listOf()
        else -> changeNothing()
    }

// Third level

private fun Node.stepDown(action: Action): Pair<Node, List<Reaction>> =
    this.becomeElector().changeTerm(action.term) to when (this) {
        is Leader -> listOf(ArmElectionTimeout)
        else -> listOf()
    }

private fun Node.changeNothing(): Pair<Node, List<Reaction>> = this to listOf()

private fun Candidate.winElection() = followers.size * 2 > context.numberOfNodes