package io.smallibs.kraft.log

import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Identifier
import io.smallibs.kraft.common.Index
import io.smallibs.kraft.common.Index.Companion.index
import io.smallibs.kraft.log.data.Append
import io.smallibs.kraft.log.data.Indexes
import io.smallibs.kraft.log.impl.LeaderManagerImpl

interface LeaderManager<Command> {

    fun accept(entry: Entry<Command>): LeaderManager<Command>

    fun prepareAppend(): Map<Identifier, Append<Command>>

    fun appended(node: Identifier, matchIndex: Index): LeaderManager<Command>

    fun rejected(node: Identifier): LeaderManager<Command>

    fun updateCommitIndex(): Pair<LeaderManager<Command>, List<Entry<Command>>>

    companion object {
        operator fun <Command> invoke(self: Identifier, logManager: LogManager<Command>, identifiers: List<Identifier>) =
            LeaderManagerImpl(
                logManager,
                identifiers
                    .filter { it != self }
                    .map { it to initialIndexes(logManager) }
                    .toMap()
            )

        fun <Command> initialIndexes(logManager: LogManager<Command>) =
            Indexes(logManager.previous().first + 1, 0.index)
    }
}
