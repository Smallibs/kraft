package io.smallibs.kraft.log

import io.smallibs.kraft.common.Index.Companion.index
import io.smallibs.kraft.common.Term.Companion.term
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LogManagerTest {

    // Construction

    @Test
    fun `Should create a fresh log with size equals to 0`() {
        val logManager = LogManager<Int>(Log());

        assertEquals(0, logManager.logSize())
    }

    @Test
    fun `Should a term at index 0 equals to 0 when the log is empty`() {
        val logManager = LogManager<Int>(Log());

        assertEquals(0.index to 0.term, logManager.last())
    }

}