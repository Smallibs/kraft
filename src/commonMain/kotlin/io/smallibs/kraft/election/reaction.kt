package io.smallibs.kraft.election

import io.smallibs.kraft.common.Identifier
import io.smallibs.kraft.common.Index
import io.smallibs.kraft.common.Term
import io.smallibs.kraft.election.Action.AppendResponse
import io.smallibs.kraft.election.Action.RequestAppend

sealed class Reaction {

    object ArmElectionTimeout : Reaction()

    object ArmHeartbeatTimeout : Reaction()

    object StartElection : Reaction()

    object SynchroniseLog : Reaction()

    data class AppendRequested(
        val term: Term,
        val previousIndex: Index,
        val previousTerm: Term,
        val leaderCommit: Index,
        val entries: List<Any>
    ) : Reaction() {
        constructor(r: RequestAppend) : this(r.term, r.previousIndex, r.previousTerm, r.leaderCommit, r.entries)
    }

    data class AppendAccepted(
        val follower: Identifier,
        val term: Term,
        var success: Boolean,
        val matchIndex: Int
    ) : Reaction() {
        constructor(a: AppendResponse) : this(a.follower, a.term, a.success, a.matchIndex)
    }

}