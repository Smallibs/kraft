package io.smallibs.kraft.election.data

import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Identifier
import io.smallibs.kraft.common.Index
import io.smallibs.kraft.common.Term
import io.smallibs.kraft.election.data.Action.AppendResponse
import io.smallibs.kraft.election.data.Action.RequestAppend

sealed class Reaction<A> {

    class ArmElectionTimeout<A> : Reaction<A>()

    class ArmHeartbeatTimeout<A> : Reaction<A>()

    class StartElection<A> : Reaction<A>()

    class SynchroniseLog<A> : Reaction<A>()

    class AppendRequested<A>(
        val term: Term,
        val previousIndex: Index,
        val previousTerm: Term,
        val leaderCommit: Index,
        val entries: List<Entry<A>>
    ) : Reaction<A>() {
        constructor(r: RequestAppend<A>) : this(r.term, r.previousIndex, r.previousTerm, r.leaderCommit, r.entries)
    }

    class AppendAccepted<A>(
        val follower: Identifier,
        val term: Term,
        var success: Boolean,
        val matchIndex: Int
    ) : Reaction<A>() {
        constructor(a: AppendResponse<A>) : this(a.follower, a.term, a.success, a.matchIndex)
    }

}