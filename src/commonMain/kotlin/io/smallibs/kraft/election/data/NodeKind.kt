package io.smallibs.kraft.election.data

import io.smallibs.kraft.common.Identifier
import io.smallibs.kraft.common.Term
import io.smallibs.kraft.common.Term.Companion.term

/**
 * NodeKind denotes the behaviors linked to a node. We have:
 * - an Elector i.e. a Follower without a Leader in the Raft specification
 * - a Candidate
 * - a Follower and
 * - a Leader.
 *
 * Each oriented arrow is equipped by the related message, conditional is necessary and
 * reactions to be performed by the system.
 *
 * For instance, when an Elector accepts a RequestVote message it becomes a Follower and
 * an AcceptVote reaction should be performed. This reaction highlights the acceptance to
 * be send to the corresponding Candidate. This AcceptVote is managed by a Candidate node.
 *
 * <pre>
 *                                         TimeOut | ArmElectionTimeOut
 *                  +-------------------------------------------------------------------------+
 *                  |                                                                         |
 *                  v                   RequestVote | AcceptVote                              |                                                  |
 *           +-> Elector ------------------------------------------------------------+        |
 *           |      |                                                                |        |
 *           |      |  TimeOut | ArmElectionTimeOut StartElection                    v        |
 *           |      V                                                             Follower ---+
 *           +–+ Candidate <-±                                                       |
 *  Higher   |      |    |   | TimeOut | ArmElection TimeOut StartElection           +--------+
 *   Term    |      |    |   | AcceptVote if No Quorum                                        |
 *           |      |    +---+                                                                |
 *           |      |                                                                         |
 *           |      | AcceptVote if Quorum | ArmHeartBeatTime SynchronizeLog InsertMarkInLog  |
 *           |      V                                                                         |
 *           +-+ Leader <-+                                                                   |
 *           |        |   | TimeOut | ArmHeartBeatTime SynchronizeLog                         |
 *           |        +---+                                                                   |
 *           +--------------------------------------------------------------------------------+
 * </pre>
 */

sealed class NodeKind(protected open val context: Context) {

    val self: Identifier get() = context.self
    val term: Term get() = context.term
    val livingNodes: List<Identifier> get() = context.livingNodes

    fun becomeElector(term: Term = context.term) = Elector(context.changeTerm(term))

    open fun becomeFollower(leader: Identifier) = Follower(context, leader, false)

    data class Elector(override val context: Context) : NodeKind(context) {

        constructor(
            self: Identifier,
            term: Term,
            otherNodes: List<Identifier>
        ) : this(Context(self, term, otherNodes))

        fun becomeCandidate() = Candidate(context.changeTerm(context.term.next()))
    }

    data class Candidate(override val context: Context, val followers: List<Identifier> = listOf()) :
        NodeKind(context) {

        constructor(
            self: Identifier,
            term: Term,
            livingNodes: List<Identifier>,
            followers: List<Identifier> = listOf()
        ) : this(Context(self, term, livingNodes), followers)

        fun becomeLeader() = Leader(context)

        fun stayCandidateWithNewFollower(follower: Identifier) = Candidate(context, followers + follower)
    }

    data class Follower(override val context: Context, val leader: Identifier, val extended: Boolean) :
        NodeKind(context) {

        constructor(
            self: Identifier,
            term: Term,
            livingNodes: List<Identifier>,
            leader: Identifier,
            extended: Boolean = false
        ) : this(Context(self, term, livingNodes), leader, extended)

        override fun becomeFollower(leader: Identifier) = Follower(context, leader, extended)

        fun extendTimeout() = Follower(context, leader, true)

        fun resetTime() = Follower(context, leader, false)
    }

    data class Leader(override val context: Context) : NodeKind(context) {

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
