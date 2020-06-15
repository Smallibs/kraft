package io.smallibs.kraft.election

import io.smallibs.kraft.election.data.Action
import io.smallibs.kraft.election.data.Node
import io.smallibs.kraft.election.data.Reaction
import io.smallibs.kraft.election.impl.TransitionImpl

interface Transition {

    infix fun <A> Node.perform(action: Action<A>): Pair<Node, List<Reaction<A>>>

    companion object {
        operator fun <A> invoke(): Transition = TransitionImpl()
    }
}
