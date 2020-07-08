package io.smallibs.kraft.election.data

import io.smallibs.kraft.common.Identifier
import io.smallibs.kraft.common.Term
import io.smallibs.kraft.common.Term.Companion.term

sealed class Node(open val context: Context) {

    val self: Identifier get() = context.self
    val term: Term get() = context.term
    val livingNodes: List<Identifier> get() = context.livingNodes

    fun becomeElector() = Elector(context)

    // In the initial Spec' an Elector is a Follower without leader
    data class Elector(override val context: Context) : Node(context) {

        constructor(
                self: Identifier,
                term: Term,
                otherNodes: List<Identifier>
        ) : this(Context(self, term, otherNodes))

        fun changeTerm(term: Term): Node = Elector(context.changeTerm(term))

        fun becomeFollower(candidate: Identifier) = Follower(context, candidate, false)

        fun becomeCandidate() = Candidate(context.changeTerm(context.term.next()))
    }

    data class Candidate(override val context: Context, val followers: List<Identifier> = listOf()) : Node(context) {

        constructor(
                self: Identifier,
                term: Term,
                livingNodes: List<Identifier>,
                followers: List<Identifier> = listOf()
        ) : this(Context(self, term, livingNodes), followers)

        fun becomeLeader() = Leader(context)

        fun stayCandidateWithNewFollower(follower: Identifier) = Candidate(context, followers + follower)
    }

    data class Follower(override val context: Context, val leader: Identifier, val extended: Boolean) : Node(context) {

        constructor(
                self: Identifier,
                term: Term,
                livingNodes: List<Identifier>,
                leader: Identifier,
                extended: Boolean = false
        ) : this(Context(self, term, livingNodes), leader, extended)

        fun extendTimeout() = Follower(context, leader, true)

        fun resetTime() = Follower(context, leader, false)
    }

    data class Leader(override val context: Context) : Node(context) {

        constructor(
                self: Identifier,
                term: Term,
                livingNodes: List<Identifier>
        ) : this(Context(self, term, livingNodes))

    }

    companion object {

        operator fun invoke(self: Identifier, livingNodes: List<Identifier>) =
                Elector(Context(self, 0.term, livingNodes))

    }

}