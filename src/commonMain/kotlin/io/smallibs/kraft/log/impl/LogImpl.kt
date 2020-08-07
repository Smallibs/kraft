package io.smallibs.kraft.log.impl

import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Index
import io.smallibs.kraft.log.Log

data class LogImpl<Command>(
    private val value: List<Entry<Command>>
) : Log<Command> {

    private operator fun invoke(value: List<Entry<Command>>) =
        LogImpl(value)

    override fun size() =
        value.size

    override fun append(entry: Entry<Command>) =
        this(value + entry)

    override fun getFrom(index: Index, size: Int) =
        value.subList(index.value, minOf(index.value + size, value.size))

    override fun deleteFrom(index: Index): Log<Command> =
        if (index.value < 1) {
            this(listOf())
        } else {
            this(value.subList(0, minOf(index.value, value.size) - 1))
        }

    override fun find(index: Index) =
        value.elementAtOrNull(index.value)
}
