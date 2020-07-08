package io.smallibs.kraft.log.data

import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Index

class Appended<Command> private constructor(
        val success: Boolean,
        var matchIndex: Index,
        val entries: List<Entry<Command>> = listOf()
) {

    companion object {
        fun <Command> success(matchIndex: Index, entries: List<Entry<Command>>) =
                Appended(true, matchIndex, entries)

        fun <Command> failure(matchIndex: Index) =
                Appended(false, matchIndex, listOf<Entry<Command>>())
    }

}
