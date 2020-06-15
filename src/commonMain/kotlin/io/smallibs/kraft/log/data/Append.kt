package io.smallibs.kraft.log.data

import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Index
import io.smallibs.kraft.common.Term

class Append<A>(
    var previous: Pair<Index,Term>,
    val leaderCommit: Index,
    val entries: List<Entry<A>> = listOf()
)
