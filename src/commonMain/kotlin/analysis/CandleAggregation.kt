package analysis

import data.Candle
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields

enum class CandleTimeframe(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
}

fun aggregateCandles(
    candles: List<Candle>,
    timeframe: CandleTimeframe,
): List<Candle> {
    val sortedCandles = candles.sortedBy { it.date }

    return when (timeframe) {
        CandleTimeframe.DAILY -> sortedCandles
        CandleTimeframe.WEEKLY -> aggregateBy(sortedCandles) { candle ->
            val date = LocalDate.parse(candle.date)
            val weekFields = WeekFields.ISO
            "${date.get(weekFields.weekBasedYear())}-${date.get(weekFields.weekOfWeekBasedYear())}"
        }
        CandleTimeframe.MONTHLY -> aggregateBy(sortedCandles) { candle ->
            YearMonth.from(LocalDate.parse(candle.date)).toString()
        }
    }
}

private fun aggregateBy(
    sortedCandles: List<Candle>,
    periodKey: (Candle) -> String,
): List<Candle> =
    sortedCandles
        .groupBy(periodKey)
        .values
        .map { periodCandles ->
            val first = periodCandles.first()
            val last = periodCandles.last()

            Candle(
                date = first.date,
                open = first.open,
                high = periodCandles.maxOf { it.high },
                low = periodCandles.minOf { it.low },
                close = last.close,
                volume = periodCandles.sumOf { it.volume },
            )
        }
