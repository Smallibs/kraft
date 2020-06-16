package io.smallibs.kraft.log

import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Identifier
import io.smallibs.kraft.common.Index
import io.smallibs.kraft.common.Index.Companion.index
import io.smallibs.kraft.log.data.Append
import io.smallibs.kraft.log.data.Indexes
import io.smallibs.kraft.log.impl.LeaderManagerImpl

interface LeaderManager<A> {

    fun accept(entry: Entry<A>): LeaderManager<A>

    fun prepareAppend(): Map<Identifier, Append<A>>

    fun appended(node: Identifier, matchIndex: Index): LeaderManager<A>

    fun rejected(node: Identifier): LeaderManager<A>

    fun updateCommitIndex(): Pair<LeaderManager<A>, List<Entry<A>>>

    companion object {
        operator fun <A> invoke(self: Identifier, logManager: LogManager<A>, identifiers: List<Identifier>) =
            LeaderManagerImpl(
                logManager,
                identifiers
                    .filter { it != self }
                    .map { it to initialIndexes(logManager) }
                    .toMap()
            )

        fun <A> initialIndexes(logManager: LogManager<A>) =
            Indexes(logManager.previous().first + 1, 0.index)
    }
}