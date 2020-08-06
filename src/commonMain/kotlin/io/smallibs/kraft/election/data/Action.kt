package io.smallibs.kraft.election.data

import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Identifier
import io.smallibs.kraft.common.Index
import io.smallibs.kraft.common.Term

sealed class Action<Command>(open val term: Term) {

    data class TimeOut<Command>(
        val timoutType: TimoutType,
        override val term: Term
    ) : Action<Command>(term)

    data class RequestVote<Command>(
        val candidate: Identifier,
        override val term: Term,
        val lastLog: Pair<Index, Term>
    ) : Action<Command>(term)

    data class Voted<Command>(
        val follower: Identifier,
        override val term: Term
    ) : Action<Command>(term)

    data class RequestAppend<Command>(
        val leader: Identifier,
        override val term: Term,
        var previous: Pair<Index, Term>,
        val leaderCommit: Index,
        val entries: List<Entry<Command>> = listOf()
    ) : Action<Command>(term)

    data class AppendResponse<Command>(
        val follower: Identifier,
        override val term: Term,
        val success: Boolean,
        var matchIndex: Index
    ) : Action<Command>(term)
}
