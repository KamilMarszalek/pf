package indicators

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EmaTest {

    @Test
    fun `should return null before period is full`() {
        val result = exponentialMovingAverage(
            values = listOf(1.0, 2.0, 3.0),
            period = 3
        )

        assertEquals(listOf(null, null, 2.0), result)
    }

    @Test
    fun `should calculate exponential moving average`() {
        val result = exponentialMovingAverage(
            values = listOf(1.0, 2.0, 3.0, 4.0, 5.0),
            period = 3
        )

        assertEquals(listOf(null, null, 2.0, 3.0, 4.0), result)
    }

    @Test
    fun `should work for period equal to one`() {
        val result = exponentialMovingAverage(
            values = listOf(1.0, 2.0, 3.0),
            period = 1
        )

        assertEquals(listOf(1.0, 2.0, 3.0), result)
    }

    @Test
    fun `should return empty list for empty input`() {
        val result = exponentialMovingAverage(
            values = emptyList(),
            period = 3
        )

        assertEquals(emptyList(), result)
    }

    @Test
    fun `should throw exception for non positive period`() {
        assertFailsWith<IllegalArgumentException> {
            exponentialMovingAverage(
                values = listOf(1.0, 2.0, 3.0),
                period = 0
            )
        }
    }
}
