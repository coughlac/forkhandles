package dev.forkhandles.result4k

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FoldTests {
    @Test
    fun `fold success`() {
        val result = Success(123).fold({ "A value of: ${(it + 1)}" }, { fail("should not be called for a success") })
        assertEquals("A value of: 124", result)
    }

    @Test
    fun `fold failure`() {
        val result = Failure(123).fold({ fail("should not be called for a failure") }, { "A value of: ${(it + 1)}" })
        assertEquals("A value of: 124", result)
    }
}
