package io.smallibs.kraft.log

import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Index
import io.smallibs.kraft.log.impl.LogImpl

/**
 * The <class>Log</class> is a basic component able to manage entries.
 */
interface Log<E> {

    fun size(): Int

    fun append(entry: Entry<E>): Log<E>

    fun getFrom(index: Index, size: Int): List<Entry<E>>

    fun deleteFrom(index: Index): Log<E>

    fun find(index: Index): Entry<E>?

    companion object {
        operator fun <E> invoke(): Log<E> = LogImpl(listOf())
    }
}
