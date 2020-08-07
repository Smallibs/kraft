package io.smallibs.kraft.coordination.service

interface Executor<Command> {

    fun accept(a: Command): Executor<Command>
}
