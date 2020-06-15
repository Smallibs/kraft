package io.smallibs.kraft.log.data

import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Index

class Appended<A> private constructor(
    val success: Boolean,
    var matchIndex: Index,
    val entries: List<Entry<A>> = listOf()
) {

    companion object {
        fun <A> success(matchIndex: Index, entries: List<Entry<A>>) =
            Appended(true, matchIndex, entries)

        fun <A> failure(matchIndex: Index) =
            Appended(false, matchIndex, listOf<Entry<A>>())
    }

}
