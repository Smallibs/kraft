package io.smallibs.kraft.coordination.service

interface Database<A> {

    fun accept(a: A): Database<A>

    fun snapshot(): A

}