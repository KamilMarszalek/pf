package ui.activeRangeUtils

import data.Candle
import java.time.LocalDate

data class VisibleDateRange(
    val startDate: LocalDate,
    val endDate: LocalDate,
)

fun visibleRangeToDateRange(
    candles: List<Candle>,
    range: IntRange,
): VisibleDateRange? {
    if (candles.isEmpty() || range.isEmpty()) return null

    val sortedCandles = candles.sortedBy { it.date }
    val startIndex = range.first.coerceIn(0, sortedCandles.lastIndex)
    val endIndex = range.last.coerceIn(0, sortedCandles.lastIndex)
    if (startIndex > endIndex) return null

    return VisibleDateRange(
        startDate = LocalDate.parse(sortedCandles[startIndex].date),
        endDate = LocalDate.parse(sortedCandles[endIndex].date),
    )
}

fun dateRangeToVisibleRange(
    candles: List<Candle>,
    dateRange: VisibleDateRange,
): IntRange? {
    if (candles.isEmpty()) return null

    val sortedCandles = candles.sortedBy { it.date }
    val firstDate = LocalDate.parse(sortedCandles.first().date)
    val lastDate = LocalDate.parse(sortedCandles.last().date)

    if (dateRange.endDate < firstDate || dateRange.startDate > lastDate) {
        return null
    }

    val clampedStartDate = maxOf(dateRange.startDate, firstDate)
    val clampedEndDate = minOf(dateRange.endDate, lastDate)

    val startIndex = sortedCandles.indexOfFirst {
        LocalDate.parse(it.date) >= clampedStartDate
    }
    val endIndex = sortedCandles.indexOfLast {
        LocalDate.parse(it.date) <= clampedEndDate
    }

    if (startIndex == -1 || endIndex == -1 || startIndex > endIndex) {
        return null
    }

    return startIndex..endIndex
}

fun presetVisibleRangeToDateRange(
    candles: List<Candle>,
    visibleRange: VisibleRange,
): VisibleDateRange? {
    if (candles.isEmpty()) return null

    val sortedCandles = candles.sortedBy { it.date }
    val marks = findVisibleRangeMarks(sortedCandles)
    return visibleRangeToDateRange(
        candles = sortedCandles,
        range = getVisibleRange(visibleRange, marks),
    )
}
