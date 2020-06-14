package io.smallibs.kraft.election

import io.smallibs.kraft.common.Identifier
import io.smallibs.kraft.common.Index
import io.smallibs.kraft.common.Term

sealed class Action(open val term: Term) {

    data class TimeOut(
        val timer: Timer,
        override val term: Term
    ) : Action(term)

    data class RequestVote(
        val candidate: Identifier,
        override val term: Term,
        val lastLogIndex: Index,
        val lastLogTerm: Term
    ) : Action(term)

    data class Voted(
        val follower: Identifier,
        override val term: Term,
        val accepted: Boolean
    ) : Action(term)

    data class RequestAppend(
        val leader: Identifier,
        override val term: Term,
        var previousIndex: Index,
        val previousTerm: Term,
        val leaderCommit: Index,
        val entries: List<Any> = listOf()
    ) : Action(term)

    data class AppendResponse(
        val follower: Identifier,
        override val term: Term,
        val success: Boolean,
        var matchIndex: Int
    ) : Action(term)

}