package io.smallibs.kraft.log.impl

import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Index
import io.smallibs.kraft.log.Log

class LogImpl<A> (
    private val value: List<Entry<A>>
) : Log<A> {

    private operator fun invoke(value: List<Entry<A>>) =
        LogImpl(value)

    override fun size() =
        value.size

    override fun append(entry: Entry<A>) =
        this(value + entry)

    override fun getFrom(index: Index, size: Int) =
        value.subList(index.value, minOf(index.value + size, value.size) - 1)

    override fun deleteFrom(index: Index): Log<A> =
        if (index.value < 1) {
            this(listOf())
        } else {
            this(value.subList(0, minOf(index.value, value.size) - 1))
        }

    override fun find(index: Index) =
        value.elementAtOrNull(index.value)

}