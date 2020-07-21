package io.smallibs.kraft.log.impl

import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Identifier
import io.smallibs.kraft.common.Index
import io.smallibs.kraft.common.Index.Companion.index
import io.smallibs.kraft.common.Index.Companion.max
import io.smallibs.kraft.common.Index.Companion.min
import io.smallibs.kraft.log.LeaderManager
import io.smallibs.kraft.log.LeaderManager.Companion.initialIndexes
import io.smallibs.kraft.log.LogManager
import io.smallibs.kraft.log.data.Append
import io.smallibs.kraft.log.data.Indexes

data class LeaderManagerImpl<Command>(
    private val logManager: LogManager<Command>,
    private val indexes: Map<Identifier, Indexes>
) : LeaderManager<Command> {

    override fun accept(entry: Entry<Command>) =
        LeaderManagerImpl(logManager.append(entry), indexes)

    override fun prepareAppend() =
        indexes.map { it.key to prepareAppend(it.value) }.toMap()

    override fun appended(node: Identifier, matchIndex: Index) = run {
        val index = this.indexes[node] ?: initialIndexes(this.logManager)
        val match = max(matchIndex, index.match)

        if (match > index.match) {
            LeaderManagerImpl(logManager, indexes + (node to Indexes(matchIndex + 1, match)))
        } else {
            this
        }
    }

    override fun rejected(node: Identifier) =
        (this.indexes[node] ?: initialIndexes(this.logManager)).let {
            Indexes(max(1.index, it.next - 1), it.match)
        }.let {
            LeaderManagerImpl(logManager, indexes + (node to it))
        }

    override fun updateCommitIndex(): Pair<LeaderManager<Command>, List<Entry<Command>>> = run {
        this.logManager.append(commit(commitIndex())).let {
            LeaderManagerImpl(it.first, indexes) to it.second.entries
        }
    }

    private fun prepareAppend(indexes: Indexes): Append<Command> = run {
        val previous = indexes.next - 1
        val term = logManager.termAt(previous)
        val size = messageSize(indexes, previous)
        val commitIndex = min(indexes.next, logManager.commitIndex())

        logManager.entriesFrom(previous, size).let {
            Append(previous to term, commitIndex, it)
        }
    }

    private fun messageSize(indexes: Indexes, previous: Index): Int {
        return if (indexes.match + 1 < indexes.next) {
            previous.value
        } else {
            minOf(5, logManager.logSize() - previous.value)
        }
    }

    private fun commitIndex() =
        when {
            indexes.isEmpty() ->
                logManager.logSize().index
            else ->
                indexes
                    .map { it.value.match.value }
                    .sortedDescending()
                    .get(this.indexes.size / 2)
                    .index
        }


    private fun commit(commit: Index): Append<Command> =
        max(logManager.commitIndex(), commit).let {
            Append(logManager.previous(), it, listOf())
        }
}