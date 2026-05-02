package analysis

import data.Candle
import indicators.exponentialMovingAverage
import indicators.relativeStrengthIndex
import indicators.simpleMovingAverage

data class StockAnalysis(
    val candles: List<Candle>,
    val sma20: List<Double?>,
    val ema20: List<Double?>,
    val rsi14: List<Double?>,
)

fun analyzeCandles(candles: List<Candle>): StockAnalysis {
    val candlesAscending = candles.sortedBy { it.date }
    val closes = candlesAscending.map { it.close }

    return StockAnalysis(
        candles = candlesAscending,
        sma20 = simpleMovingAverage(closes, 20),
        ema20 = exponentialMovingAverage(closes, 20),
        rsi14 = relativeStrengthIndex(closes, 14),
    )
}