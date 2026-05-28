package analysis

import data.Candle
import kotlin.math.pow
import kotlin.math.sqrt

data class StockStatistics(
    val totalReturnPercent: Double,
    val minClose: Double,
    val maxClose: Double,
    val averageClose: Double,
    val averageVolume: Double,
    val volatilityPercent: Double,
)

fun calculateStockStatistics(candles: List<Candle>): StockStatistics? {
    if (candles.isEmpty()) return null

    val sortedCandles = candles.sortedBy { it.date }
    val closes = sortedCandles.map { it.close }
    val volumes = sortedCandles.map { it.volume }

    val firstClose = closes.first()
    val lastClose = closes.last()
    val totalReturnPercent = if (firstClose == 0.0) {
        0.0
    } else {
        ((lastClose - firstClose) / firstClose) * 100.0
    }

    return StockStatistics(
        totalReturnPercent = totalReturnPercent,
        minClose = closes.min(),
        maxClose = closes.max(),
        averageClose = closes.average(),
        averageVolume = volumes.average(),
        volatilityPercent = standardDeviation(dailyReturnPercentages(closes)),
    )
}

fun dailyReturnPercentages(closes: List<Double>): List<Double> =
    closes.zipWithNext().mapNotNull { (previous, current) ->
        if (previous == 0.0) null else ((current - previous) / previous) * 100.0
    }

private fun standardDeviation(values: List<Double>): Double {
    if (values.isEmpty()) return 0.0

    val average = values.average()
    val variance = values.map { (it - average).pow(2) }.average()
    return sqrt(variance)
}

