package io.smallibs.kraft.log

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll
import io.smallibs.kraft.common.Index.Companion.index
import io.smallibs.kraft.log.LogArb.entryArb
import io.smallibs.kraft.log.LogArb.logManagerArb
import kotlin.test.assertEquals

class LogManagerLawsTest : StringSpec({
    "logManager.last() = logManager.append(X).previous()" {
        checkAll(logManagerArb) { log ->
            checkAll(entryArb) { entry ->
                assertEquals(log.last(), log.append(entry).previous())
            }
        }
    }

    "logManager.append(X).logSize() = logManager.logSize() + 1" {
        checkAll(logManagerArb) { log ->
            checkAll(entryArb) { entry ->
                assertEquals(log.logSize() + 1, log.append(entry).logSize())
            }
        }
    }

    "[X] = logManager.append(X).entriesFrom(logManager.logSize(), 1)" {
        checkAll(logManagerArb) { logManager ->
            checkAll(entryArb) { entry ->
                assertEquals(listOf(entry), logManager.append(entry).entriesFrom(logManager.logSize().index, 1))
            }
        }
    }

    "X.term = logManager.append(X).termAt(logManager.last().first + 1)" {
        checkAll(logManagerArb) { logManager ->
            checkAll(entryArb) { entry ->
                assertEquals(entry.term, logManager.append(entry).termAt(logManager.last().first + 1))
            }
        }
    }
})