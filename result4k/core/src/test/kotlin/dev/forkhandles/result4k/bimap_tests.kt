package dev.forkhandles.result4k

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BiMapTests {
    @Test
    fun `bimap success`() {
        val result = Success(123).bimap(
            { "This is the successful result: ${it + 1}" },
            { fail("should not be called for a success") })

        assertEquals(Success("This is the successful result: 124"), result)
    }

    @Test
    fun `bimap failure`() {
        val result = Failure(123).bimap(
            { fail("should not be called for a failure") },
            { "This is the failed result: ${it + 1}" })

        assertEquals(Failure("This is the failed result: 124"), result)
    }
}
