package io.smallibs.kraft.election

import io.smallibs.kraft.election.data.Action
import io.smallibs.kraft.election.data.Node
import io.smallibs.kraft.election.data.Reaction
import io.smallibs.kraft.election.impl.TransitionImpl

typealias TransitionResult<A> = Pair<Node, List<Reaction<A>>>

interface Transition {

    fun <A> Node.perform(hasUpToDateLog: (Action<A>) -> Boolean, action: Action<A>): TransitionResult<A>

    companion object {
        fun <A> run(block: Transition.() -> TransitionResult<A>) = TransitionImpl().run(block)
    }
}
