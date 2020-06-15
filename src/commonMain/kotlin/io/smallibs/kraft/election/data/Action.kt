package io.smallibs.kraft.election.data

import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Identifier
import io.smallibs.kraft.common.Index
import io.smallibs.kraft.common.Term

sealed class Action<A>(open val term: Term) {

    data class TimeOut<A>(
        val timer: Timer,
        override val term: Term
    ) : Action<A>(term)

    data class RequestVote<A>(
        val candidate: Identifier,
        override val term: Term,
        val lastLogIndex: Index,
        val lastLogTerm: Term
    ) : Action<A>(term)

    data class Voted<A>(
        val follower: Identifier,
        override val term: Term,
        val accepted: Boolean
    ) : Action<A>(term)

    data class RequestAppend<A>(
        val leader: Identifier,
        override val term: Term,
        var previousIndex: Index,
        val previousTerm: Term,
        val leaderCommit: Index,
        val entries: List<Entry<A>> = listOf()
    ) : Action<A>(term)

    data class AppendResponse<A>(
        val follower: Identifier,
        override val term: Term,
        val success: Boolean,
        var matchIndex: Int
    ) : Action<A>(term)

}