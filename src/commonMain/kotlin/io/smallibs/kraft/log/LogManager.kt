package io.smallibs.kraft.log

import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Index
import io.smallibs.kraft.common.Index.Companion.index
import io.smallibs.kraft.common.Term
import io.smallibs.kraft.log.data.Append
import io.smallibs.kraft.log.data.Appended
import io.smallibs.kraft.log.impl.LogManagerImpl

interface LogManager<Command> {

    fun logSize(): Int

    fun previous(): Pair<Index, Term>

    fun last(): Pair<Index, Term>

    fun commitIndex(): Index

    fun termAt(index: Index): Term

    fun append(entry: Entry<Command>): LogManager<Command>

    fun entriesFrom(index: Index, size: Int): List<Entry<Command>>

    fun append(append: Append<Command>): Pair<LogManager<Command>, Appended<Command>>

    companion object {
        operator fun <Command> invoke(log: Log<Command>) = LogManagerImpl(log, 0.index, 0.index)
    }
}
