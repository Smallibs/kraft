package io.smallibs.kraft.common

class Context(val identifier: Identifier, val numberOfNodes: Int = 0, val term: Term) {
    fun changeTerm(term: Term) = Context(identifier, numberOfNodes, term)
}

