package io.smallibs.kraft.log

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll
import io.smallibs.kraft.common.Index.Companion.index
import io.smallibs.kraft.log.LogArb.entryArb
import io.smallibs.kraft.log.LogArb.logArb
import kotlin.test.assertEquals

class LogLawsTest : StringSpec({
    "log.append(X).size() = log.size() + 1" {
        checkAll(logArb) { log ->
            checkAll(entryArb) { entry ->
                assertEquals(log.size() + 1, log.append(entry).size())
            }
        }
    }

    "log.append(X).deleteFrom(log.size + 1) = log" {
        checkAll(logArb) { log ->
            checkAll(entryArb) { entry ->
                assertEquals(log, log.append(entry).deleteFrom(log.size().index + 1))
            }
        }
    }

    "log.append(X).find(log.size) = X" {
        checkAll(logArb) { log ->
            checkAll(entryArb) { entry ->
                assertEquals(entry, log.append(entry).find(log.size().index))
            }
        }
    }

    "log.append(X).getFrom(log.size,1) = [X]" {
        checkAll(logArb) { log ->
            checkAll(entryArb) { entry ->
                assertEquals(listOf(entry), log.append(entry).getFrom(log.size().index, 1))
            }
        }
    }
})