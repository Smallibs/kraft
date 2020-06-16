package io.smallibs.kraft.log.impl

import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Index
import io.smallibs.kraft.common.Index.Companion.index
import io.smallibs.kraft.common.Index.Companion.max
import io.smallibs.kraft.common.Term
import io.smallibs.kraft.common.Term.Companion.term
import io.smallibs.kraft.log.Log
import io.smallibs.kraft.log.LogManager
import io.smallibs.kraft.log.data.Append
import io.smallibs.kraft.log.data.Appended
import kotlin.math.max

class LogManagerImpl<A>(
    private val log: Log<A>,
    private val commitIndex: Index,
    private val lastApplied: Index
) : LogManager<A> {

    private operator fun invoke(
        log: Log<A> = this.log,
        commitIndex: Index = this.commitIndex,
        lastApplied: Index = this.lastApplied
    ) =
        LogManagerImpl(log, commitIndex, lastApplied)

    override fun logSize() =
        log.size()

    override fun previous() =
        max(0, logSize() - 1).index.let {
            it to termAt(it)
        }

    override fun last() =
        logSize().index.let {
            it to termAt(it)
        }

    override fun commitIndex(): Index {
        return commitIndex
    }

    override fun termAt(index: Index): Term {
        return log.find(index - 1)?.term ?: 0.term
    }

    override fun append(entry: Entry<A>): LogManager<A> {
        return this(log.append(entry))
    }

    override fun entriesFrom(index: Index, size: Int): List<Entry<A>> {
        return log.getFrom(index, size)
    }

    override fun append(append: Append<A>) =
        when {
            canAppend(append.previous) -> {
                synchronizeEntries(append.previous.first, append.entries).let {
                    it.updateCommitIndex(append.leaderCommit)
                }.let {
                    it.collectedActionsToApply()
                }.let {
                    it.first to Appended.success(last().first, it.second)
                }
            }
            else -> {
                this to Appended.failure(last().first)
            }
        }

    private fun canAppend(previous: Pair<Index, Term>) =
        previous.first.value == 0 || previous.first.value <= log.size() && termAt(previous.first) == previous.second

    private fun synchronizeEntries(previousIndex: Index, entries: List<Entry<A>>) =
        IntRange(0, entries.size).fold(log) { r, i ->
            r.synchronizeEntry(previousIndex + 1 + i, entries[i])
        }.let {
            this(it)
        }

    private fun <A> Log<A>.synchronizeEntry(index: Index, entry: Entry<A>) =
        // Is entry already in the log ?
        when {
            termAt(index) != entry.term -> this.deleteFrom(index - 1).append(entry)
            else -> this
        }

    private fun updateCommitIndex(leaderCommit: Index) =
        this(commitIndex = max(this.commitIndex, leaderCommit))

    private fun collectedActionsToApply() =
        IntRange(lastApplied.value, commitIndex.value).map {
            log.find(it.index)!!
        }.let {
            this(lastApplied = commitIndex) to it
        }
}