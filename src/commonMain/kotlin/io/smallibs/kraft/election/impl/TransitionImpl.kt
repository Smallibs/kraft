package io.smallibs.kraft.election.impl

import io.smallibs.kraft.election.Transition
import io.smallibs.kraft.election.data.Action
import io.smallibs.kraft.election.data.Action.*
import io.smallibs.kraft.election.data.Node
import io.smallibs.kraft.election.data.Node.*
import io.smallibs.kraft.election.data.Reaction
import io.smallibs.kraft.election.data.Reaction.*
import io.smallibs.kraft.election.data.Timer.Election
import io.smallibs.kraft.election.data.Timer.Heartbeat

class TransitionImpl : Transition {

    override infix fun <A> Node.perform(action: Action<A>): Pair<Node, List<Reaction<A>>> =
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

    private fun <A> Leader.perform(action: Action<A>): Pair<Node, List<Reaction<A>>> =
        when (action) {
            is TimeOut ->
                when {
                    action.timer != Heartbeat -> changeNothing()
                    else -> this to listOf(SynchroniseLog(), ArmHeartbeatTimeout())
                }
            is AppendResponse -> this to listOf(AppendAccepted(action))
            else -> changeNothing()
        }

    private fun <A> Follower.perform(action: Action<A>): Pair<Node, List<Reaction<A>>> =
        when (action) {
            is TimeOut ->
                when {
                    action.timer != Election -> changeNothing()
                    this.extended -> this.resetTime() to listOf(ArmElectionTimeout())
                    else -> this.becomeElector().becomeCandidate() to listOf(StartElection(), ArmElectionTimeout())
                }
            is RequestAppend<A> ->
                this.extendTime() to listOf(AppendRequested(action))
            else -> changeNothing()
        }

    private fun <A> Candidate.perform(action: Action<A>): Pair<Node, List<Reaction<A>>> =
        when (action) {
            is TimeOut ->
                when {
                    action.timer != Election -> changeNothing()
                    else -> this.becomeElector().becomeCandidate() to listOf(StartElection(), ArmElectionTimeout())
                }
            is Voted ->
                when {
                    winElection() -> this.becomeLeader() to listOf(StartElection(), ArmHeartbeatTimeout())
                    else -> this.becomeCandidate(action.follower) to listOf()
                }
            else -> changeNothing()
        }

    private fun <A> Elector.perform(action: Action<A>): Pair<Node, List<Reaction<A>>> =
        when (action) {
            is TimeOut ->
                when {
                    action.timer != Election -> changeNothing()
                    else -> this.becomeCandidate() to listOf(StartElection(), ArmElectionTimeout())
                }
            is RequestVote -> this.becomeFollower(action.candidate).extendTime() to listOf()
            else -> changeNothing()
        }

    private fun <A> Node.stepDown(action: Action<A>): Pair<Node, List<Reaction<A>>> =
        this.becomeElector().changeTerm(action.term) to when (this) {
            is Leader -> listOf(ArmElectionTimeout())
            else -> listOf()
        }

    private fun <A> Node.changeNothing(): Pair<Node, List<Reaction<A>>> = this to listOf()

    private fun Candidate.winElection() = followers.size * 2 > context.numberOfNodes

}