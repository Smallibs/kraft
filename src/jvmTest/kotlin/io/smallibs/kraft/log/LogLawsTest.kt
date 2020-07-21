package io.smallibs.kraft.log

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.take
import io.kotest.property.checkAll
import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Index.Companion.index
import io.smallibs.kraft.common.Insert.Item
import io.smallibs.kraft.common.Term.Companion.term
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
}) {

    companion object {
        val entryArb = arb { rs ->
            val values = Arb.int().values(rs)
            val terms = Arb.int().values(rs).filter { it.value > 0 }
            values.zip(terms).map { (value, term) ->
                Entry(term.value.term, Item(value.value))
            }
        }

        var logArb = arb { rs ->
            val size = Arb.int(0, 10).values(rs)
            size.map { l ->
                val entries = entryArb
                entries.take(l.value, rs).fold(Log<Int>()) { log, entry ->
                    log.append(entry)
                }

            }
        }
    }

}