package indicators

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RsiTest {

    @Test
    fun `should return null before period is full`() {
        val result = relativeStrengthIndex(
            values = listOf(1.0, 2.0, 3.0),
            period = 3
        )

        assertEquals(listOf(null, null, null), result)
    }

    @Test
    fun `should calculate relative strength index`() {
        val result = relativeStrengthIndex(
            values = listOf(1.0, 2.0, 3.0, 2.0, 4.0, 3.0),
            period = 3
        )

        assertNullableDoubleListEquals(
            expected = listOf(null, null, null, 66.66666666666666, 83.33333333333333, 60.6060606060606),
            actual = result
        )
    }

    @Test
    fun `should return one hundred when there are no losses`() {
        val result = relativeStrengthIndex(
            values = listOf(1.0, 2.0, 3.0, 4.0),
            period = 3
        )

        assertEquals(listOf(null, null, null, 100.0), result)
    }

    @Test
    fun `should return zero when there are no gains`() {
        val result = relativeStrengthIndex(
            values = listOf(4.0, 3.0, 2.0, 1.0),
            period = 3
        )

        assertEquals(listOf(null, null, null, 0.0), result)
    }

    @Test
    fun `should return fifty when there are no gains or losses`() {
        val result = relativeStrengthIndex(
            values = listOf(2.0, 2.0, 2.0, 2.0),
            period = 3
        )

        assertEquals(listOf(null, null, null, 50.0), result)
    }

    @Test
    fun `should work for period equal to one`() {
        val result = relativeStrengthIndex(
            values = listOf(1.0, 2.0, 1.0),
            period = 1
        )

        assertEquals(listOf(null, 100.0, 0.0), result)
    }

    @Test
    fun `should return empty list for empty input`() {
        val result = relativeStrengthIndex(
            values = emptyList(),
            period = 3
        )

        assertEquals(emptyList(), result)
    }

    @Test
    fun `should throw exception for non positive period`() {
        assertFailsWith<IllegalArgumentException> {
            relativeStrengthIndex(
                values = listOf(1.0, 2.0, 3.0),
                period = 0
            )
        }
    }

    private fun assertNullableDoubleListEquals(
        expected: List<Double?>,
        actual: List<Double?>,
        absoluteTolerance: Double = 1e-10
    ) {
        assertEquals(expected.size, actual.size)
        expected.zip(actual).forEach { (expectedValue, actualValue) ->
            if (expectedValue == null || actualValue == null) {
                assertEquals(expectedValue, actualValue)
            } else {
                assertEquals(expectedValue, actualValue, absoluteTolerance)
            }
        }
    }
}
