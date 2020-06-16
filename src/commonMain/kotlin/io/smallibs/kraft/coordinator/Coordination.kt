package io.smallibs.kraft.coordinator

import io.smallibs.kraft.election.data.Action

interface Coordination<A> {

    fun accept(action: Action<A>): Coordination<A>

}