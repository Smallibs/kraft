package io.smallibs.kraft.election

import io.smallibs.kraft.election.data.Action
import io.smallibs.kraft.election.data.Node
import io.smallibs.kraft.election.data.Reaction
import io.smallibs.kraft.election.impl.TransitionImpl

typealias TransitionResult<Command> = Pair<Node, List<Reaction<Command>>>

interface Transition {

    fun <Command> Node.perform(hasUpToDateLog: (Action<Command>) -> Boolean, action: Action<Command>): TransitionResult<Command>

    companion object {
        fun <Command> run(block: Transition.() -> TransitionResult<Command>) = TransitionImpl().run(block)
    }
}
