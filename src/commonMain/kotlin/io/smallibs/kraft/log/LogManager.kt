package io.smallibs.kraft.log

import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Index
import io.smallibs.kraft.common.Index.Companion.index
import io.smallibs.kraft.common.Term
import io.smallibs.kraft.log.data.Append
import io.smallibs.kraft.log.data.Appended
import io.smallibs.kraft.log.impl.LogManagerImpl

interface LogManager<A> {

    fun logSize(): Int

    fun previous(): Pair<Index, Term>

    fun last(): Pair<Index, Term>

    fun commitIndex(): Index

    fun termAt(index: Index): Term

    fun append(entry: Entry<A>): LogManager<A>

    fun entriesFrom(index: Index, size: Int): List<Entry<A>>

    fun append(append: Append<A>): Pair<LogManager<A>, Appended<A>>

    companion object {
        operator fun <A> invoke() = LogManagerImpl<A>(Log(), 0.index, 0.index)
    }
}
