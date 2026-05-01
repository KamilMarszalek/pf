package indicators

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SmaTest {

    @Test
    fun `should return null before window is full`() {
        val result = simpleMovingAverage(
            values = listOf(1.0, 2.0, 3.0),
            window = 3
        )

        assertEquals(listOf(null, null, 2.0), result)
    }

    @Test
    fun `should calculate simple moving average`() {
        val result = simpleMovingAverage(
            values = listOf(1.0, 2.0, 3.0, 4.0, 5.0),
            window = 3
        )

        assertEquals(listOf(null, null, 2.0, 3.0, 4.0), result)
    }

    @Test
    fun `should work for window equal to one`() {
        val result = simpleMovingAverage(
            values = listOf(1.0, 2.0, 3.0),
            window = 1
        )

        assertEquals(listOf(1.0, 2.0, 3.0), result)
    }

    @Test
    fun `should return empty list for empty input`() {
        val result = simpleMovingAverage(
            values = emptyList(),
            window = 3
        )

        assertEquals(emptyList(), result)
    }

    @Test
    fun `should throw exception for non positive window`() {
        assertFailsWith<IllegalArgumentException> {
            simpleMovingAverage(
                values = listOf(1.0, 2.0, 3.0),
                window = 0
            )
        }
    }
}