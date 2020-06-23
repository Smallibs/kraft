package io.smallibs.kraft.election.data

import io.smallibs.kraft.common.Identifier
import io.smallibs.kraft.common.Term

data class Context(
        val self: Identifier,
        val term: Term,
        val livingNodes: List<Identifier>
) {
    fun changeTerm(term: Term) =
            Context(self, term, livingNodes)
}

