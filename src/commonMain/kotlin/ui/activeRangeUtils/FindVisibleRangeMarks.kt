package ui.activeRangeUtils

import data.Candle
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun findVisibleRangeMarks(candles: List<Candle>): VisibleRangeMarks {
    if (candles.isEmpty()) return VisibleRangeMarks(0, 0, 0, 0, 0)

    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val lastCandleDate = LocalDate.parse(candles.last().date, formatter)

    fun findMarkFor(monthsBack: Long): Int {
        val targetDate = lastCandleDate.minusMonths(monthsBack)

        val index = candles.indexOfLast {
            LocalDate.parse(it.date, formatter).isBefore(targetDate)
        }

        return if (index == -1) 0 else index - 1
    }

    return VisibleRangeMarks(
        fullSize = candles.size - 1,
        oneYearMark = findMarkFor(12),
        sixMonthsMark = findMarkFor(6),
        threeMonthsMark = findMarkFor(3),
        oneMonthMark = findMarkFor(1)
    )
}