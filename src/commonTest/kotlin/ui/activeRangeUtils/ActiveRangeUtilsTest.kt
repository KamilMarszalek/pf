package ui.activeRangeUtils

import data.Candle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ActiveRangeUtilsTest {

    @Test
    fun `should map visible range enum to index range`() {
        val marks = VisibleRangeMarks(
            fullSize = 99,
            oneYearMark = 20,
            sixMonthsMark = 50,
            threeMonthsMark = 70,
            oneMonthMark = 90,
        )

        assertEquals(0..99, getVisibleRange(VisibleRange.FIVE_YEAR, marks))
        assertEquals(20..99, getVisibleRange(VisibleRange.ONE_YEAR, marks))
        assertEquals(50..99, getVisibleRange(VisibleRange.SIX_MONTHS, marks))
        assertEquals(70..99, getVisibleRange(VisibleRange.THREE_MONTHS, marks))
        assertEquals(90..99, getVisibleRange(VisibleRange.ONE_MONTH, marks))
    }

    @Test
    fun `should detect selected visible range from marks`() {
        val marks = VisibleRangeMarks(
            fullSize = 99,
            oneYearMark = 20,
            sixMonthsMark = 50,
            threeMonthsMark = 70,
            oneMonthMark = 90,
        )

        assertEquals(VisibleRange.FIVE_YEAR, detectVisibleRange(0..99, marks))
        assertEquals(VisibleRange.ONE_YEAR, detectVisibleRange(20..99, marks))
        assertEquals(VisibleRange.SIX_MONTHS, detectVisibleRange(50..99, marks))
        assertEquals(VisibleRange.THREE_MONTHS, detectVisibleRange(70..99, marks))
        assertEquals(VisibleRange.ONE_MONTH, detectVisibleRange(90..99, marks))
        assertEquals(VisibleRange.NONE, detectVisibleRange(20..98, marks))
        assertEquals(VisibleRange.NONE, detectVisibleRange(21..99, marks))
    }

    @Test
    fun `should return empty marks for no candles`() {
        assertEquals(
            VisibleRangeMarks(0, 0, 0, 0, 0),
            findVisibleRangeMarks(emptyList()),
        )
    }

    @Test
    fun `visible range marks should not be negative for short data`() {
        val marks = findVisibleRangeMarks(
            listOf(
                candle("2026-04-01"),
                candle("2026-05-01"),
            )
        )

        assertTrue(marks.oneMonthMark >= 0)
        assertTrue(marks.threeMonthsMark >= 0)
        assertTrue(marks.sixMonthsMark >= 0)
        assertTrue(marks.oneYearMark >= 0)
    }

    @Test
    fun `visible range marks should clamp index before first candle to zero`() {
        val marks = findVisibleRangeMarks(
            listOf(
                candle("2025-04-01"),
                candle("2026-05-01"),
            )
        )

        assertEquals(0, marks.oneYearMark)
        assertTrue(allMarks(marks).all { it >= 0 })
    }

    @Test
    fun `visible range marks should be safe for one candle`() {
        val marks = findVisibleRangeMarks(listOf(candle("2026-05-01")))

        assertEquals(VisibleRangeMarks(0, 0, 0, 0, 0), marks)
    }

    private fun allMarks(marks: VisibleRangeMarks): List<Int> =
        listOf(
            marks.fullSize,
            marks.oneYearMark,
            marks.sixMonthsMark,
            marks.threeMonthsMark,
            marks.oneMonthMark,
        )

    private fun candle(date: String) =
        Candle(
            date = date,
            open = 1.0,
            high = 1.0,
            low = 1.0,
            close = 1.0,
            volume = 1.0,
        )
}

