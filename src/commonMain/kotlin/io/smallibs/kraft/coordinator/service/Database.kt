package io.smallibs.kraft.coordinator.service

interface Database<A> {

    fun accept(a: A): Database<A>

}