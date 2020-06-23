package io.smallibs.kraft.log

import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Index
import io.smallibs.kraft.log.impl.LogImpl

/**
 * The <class>Log</class> is a basic component able to manage entries.
 */
interface Log<A> {

    fun size(): Int

    fun append(entry: Entry<A>): Log<A>

    fun getFrom(index: Index, size: Int): List<Entry<A>>

    fun deleteFrom(index: Index): Log<A>

    fun find(index: Index): Entry<A>?

    companion object {
        operator fun <A> invoke(): Log<A> = LogImpl(listOf())
    }
}