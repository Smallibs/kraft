package io.smallibs.kraft.election

import io.smallibs.kraft.election.data.Action
import io.smallibs.kraft.election.data.Node
import io.smallibs.kraft.election.data.Reaction
import io.smallibs.kraft.election.impl.TransitionImpl

interface Transition {

    fun <A> Node.perform(
        hasNotLeaderCompleteness: (Action<A>) -> Boolean,
        action: Action<A>
    ): Pair<Node, List<Reaction<A>>>

    companion object {
        fun <R> run(block: Transition.() -> R): R = Transition().run(block)

        operator fun invoke(): Transition = TransitionImpl()
    }
}
