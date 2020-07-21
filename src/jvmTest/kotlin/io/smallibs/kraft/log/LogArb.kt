package io.smallibs.kraft.log

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.take
import io.smallibs.kraft.common.Entry
import io.smallibs.kraft.common.Insert
import io.smallibs.kraft.common.Term.Companion.term

object LogArb {

    val entryArb = arb { rs ->
        val values = Arb.int().values(rs)
        var term = 0 // UGLY: Term should always increase | Don't see to Arb can help in such case (for the moment)
        values.map { (value) ->
            term += 1
            Entry(term.term, Insert.Item(value))
        }
    }

    val logArb = arb { rs ->
        val size = Arb.int(0, 10).values(rs)
        size.map { l ->
            val entries = entryArb
            entries.take(l.value, rs).fold(Log<Int>()) { log, entry ->
                log.append(entry)
            }

        }
    }

    val logManagerArb = arb { rs ->
        val log = logArb.values(rs)

        log.map { LogManager(it.value) }
    }

}