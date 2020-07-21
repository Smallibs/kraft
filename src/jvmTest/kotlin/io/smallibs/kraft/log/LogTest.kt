package io.smallibs.kraft.log

import io.smallibs.kraft.common.Index.Companion.index
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LogTest {

    // Construction

    @Test
    fun `Should create a fresh log with size equals to 0`() {
        val log = Log<Int>();

        assertEquals(0, log.size())
    }

    @Test
    fun `Should find nothing when the log is empty`() {
        val log = Log<Int>();

        assertEquals(null, log.find(0.index))
    }

    @Test
    fun `Should remove nothing when the log is empty`() {
        val log = Log<Int>();

        assertEquals(log, log.deleteFrom(0.index))
    }

    @Test
    fun `Should get nothing when the log is empty`() {
        val log = Log<Int>();

        assertEquals(listOf(), log.getFrom(0.index, 10))
    }

}