package ui.activeRangeUtils

import analysis.CandleTimeframe
import analysis.aggregateCandles
import data.Candle
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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

    @Test
    fun `should map same date range to different valid index ranges`() {
        val sparseCandles = listOf(
            candle("2026-01-01"),
            candle("2026-03-01"),
            candle("2026-06-01"),
        )
        val denseCandles = listOf(
            candle("2026-01-01"),
            candle("2026-02-01"),
            candle("2026-03-01"),
            candle("2026-04-01"),
            candle("2026-05-01"),
            candle("2026-06-01"),
        )
        val dateRange = VisibleDateRange(
            startDate = LocalDate.parse("2026-03-01"),
            endDate = LocalDate.parse("2026-06-01"),
        )

        assertEquals(1..2, dateRangeToVisibleRange(sparseCandles, dateRange))
        assertEquals(2..5, dateRangeToVisibleRange(denseCandles, dateRange))
    }

    @Test
    fun `should clamp date range start before available candles`() {
        val candles = listOf(
            candle("2026-03-01"),
            candle("2026-04-01"),
            candle("2026-05-01"),
        )
        val dateRange = VisibleDateRange(
            startDate = LocalDate.parse("2025-01-01"),
            endDate = LocalDate.parse("2026-04-01"),
        )

        assertEquals(0..1, dateRangeToVisibleRange(candles, dateRange))
    }

    @Test
    fun `should clamp date range end after available candles`() {
        val candles = listOf(
            candle("2026-03-01"),
            candle("2026-04-01"),
            candle("2026-05-01"),
        )
        val dateRange = VisibleDateRange(
            startDate = LocalDate.parse("2026-04-01"),
            endDate = LocalDate.parse("2027-01-01"),
        )

        assertEquals(1..2, dateRangeToVisibleRange(candles, dateRange))
    }

    @Test
    fun `visible range to date range should clamp partially out of bounds indexes`() {
        val candles = listOf(
            candle("2026-03-01"),
            candle("2026-04-01"),
            candle("2026-05-01"),
        )

        assertEquals(
            VisibleDateRange(
                startDate = LocalDate.parse("2026-03-01"),
                endDate = LocalDate.parse("2026-04-01"),
            ),
            visibleRangeToDateRange(candles, -5..1),
        )
        assertEquals(
            VisibleDateRange(
                startDate = LocalDate.parse("2026-04-01"),
                endDate = LocalDate.parse("2026-05-01"),
            ),
            visibleRangeToDateRange(candles, 1..99),
        )
    }

    @Test
    fun `visible range to date range should return null for empty range`() {
        val candles = listOf(candle("2026-03-01"))

        assertNull(visibleRangeToDateRange(candles, IntRange.EMPTY))
    }

    @Test
    fun `date range to visible range should handle unsorted candles`() {
        val candles = listOf(
            candle("2026-05-01"),
            candle("2026-03-01"),
            candle("2026-04-01"),
        )
        val range = assertNotNull(
            dateRangeToVisibleRange(
                candles,
                VisibleDateRange(
                    startDate = LocalDate.parse("2026-04-01"),
                    endDate = LocalDate.parse("2026-05-01"),
                )
            )
        )

        assertEquals(1..2, range)
        assertTrue(range.first >= 0)
        assertTrue(range.last >= 0)
    }

    @Test
    fun `should return null when date range has no overlap`() {
        val candles = listOf(
            candle("2026-03-01"),
            candle("2026-04-01"),
        )

        assertNull(
            dateRangeToVisibleRange(
                candles,
                VisibleDateRange(
                    startDate = LocalDate.parse("2025-01-01"),
                    endDate = LocalDate.parse("2025-12-31"),
                )
            )
        )
        assertNull(
            dateRangeToVisibleRange(
                candles,
                VisibleDateRange(
                    startDate = LocalDate.parse("2027-01-01"),
                    endDate = LocalDate.parse("2027-12-31"),
                )
            )
        )
    }

    @Test
    fun `date range utilities should handle empty candles`() {
        val dateRange = VisibleDateRange(
            startDate = LocalDate.parse("2026-01-01"),
            endDate = LocalDate.parse("2026-12-31"),
        )

        assertNull(dateRangeToVisibleRange(emptyList(), dateRange))
        assertNull(visibleRangeToDateRange(emptyList(), 0..1))
        assertNull(presetVisibleRangeToDateRange(emptyList(), VisibleRange.ONE_MONTH))
    }

    @Test
    fun `date range utilities should handle one candle`() {
        val candles = listOf(candle("2026-05-01"))
        val dateRange = VisibleDateRange(
            startDate = LocalDate.parse("2026-01-01"),
            endDate = LocalDate.parse("2026-12-31"),
        )

        assertEquals(0..0, dateRangeToVisibleRange(candles, dateRange))
        assertEquals(
            VisibleDateRange(
                startDate = LocalDate.parse("2026-05-01"),
                endDate = LocalDate.parse("2026-05-01"),
            ),
            visibleRangeToDateRange(candles, 0..0),
        )
    }

    @Test
    fun `preset range should return dates instead of reusable raw indexes`() {
        val candles = listOf(
            candle("2025-01-01"),
            candle("2025-06-01"),
            candle("2026-01-01"),
            candle("2026-06-01"),
        )

        val dateRange = assertNotNull(presetVisibleRangeToDateRange(candles, VisibleRange.ONE_YEAR))

        assertEquals(LocalDate.parse("2025-01-01"), dateRange.startDate)
        assertEquals(LocalDate.parse("2026-06-01"), dateRange.endDate)
    }

    @Test
    fun `date range should map safely after timeframe aggregation`() {
        val dailyCandles = listOf(
            candle("2026-01-01"),
            candle("2026-01-02"),
            candle("2026-01-08"),
            candle("2026-02-01"),
            candle("2026-03-01"),
        )
        val monthlyCandles = aggregateCandles(dailyCandles, CandleTimeframe.MONTHLY)
        val dateRange = VisibleDateRange(
            startDate = LocalDate.parse("2026-01-02"),
            endDate = LocalDate.parse("2026-03-31"),
        )

        assertEquals(1..4, dateRangeToVisibleRange(dailyCandles, dateRange))
        assertEquals(1..2, dateRangeToVisibleRange(monthlyCandles, dateRange))
    }

    @Test
    fun `date range utility should not produce negative indexes`() {
        val candles = listOf(
            candle("2026-03-01"),
            candle("2026-04-01"),
        )
        val range = assertNotNull(
            dateRangeToVisibleRange(
                candles,
                VisibleDateRange(
                    startDate = LocalDate.parse("2025-01-01"),
                    endDate = LocalDate.parse("2026-04-01"),
                )
            )
        )

        assertTrue(range.first >= 0)
        assertTrue(range.last >= 0)
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

