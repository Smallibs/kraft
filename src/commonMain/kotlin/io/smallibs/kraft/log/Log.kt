package io.smallibs.kraft.log

import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Index
import io.smallibs.kraft.log.impl.LogImpl

/**
 * The <class>Log</class> is a basic component able to manage entries.
 */
interface Log<Command> {

    fun size(): Int

    fun append(entry: Entry<Command>): Log<Command>

    fun getFrom(index: Index, size: Int): List<Entry<Command>>

    fun deleteFrom(index: Index): Log<Command>

    fun find(index: Index): Entry<Command>?

    companion object {
        operator fun <Command> invoke(): Log<Command> = LogImpl(listOf())
    }
}