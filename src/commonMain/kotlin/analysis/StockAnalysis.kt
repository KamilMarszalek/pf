package analysis

import data.Candle
import indicators.exponentialMovingAverage
import indicators.relativeStrengthIndex
import indicators.simpleMovingAverage

data class StockAnalysis(
    val candles: List<Candle>,
    val smaPeriod: Int,
    val emaPeriod: Int,
    val rsiPeriod: Int,
    val sma: List<Double?>,
    val ema: List<Double?>,
    val rsi: List<Double?>,
)

fun analyzeCandles(
    candles: List<Candle>,
    smaPeriod: Int = 20,
    emaPeriod: Int = 20,
    rsiPeriod: Int = 14,
): StockAnalysis {
    val candlesAscending = candles.sortedBy { it.date }
    val closes = candlesAscending.map { it.close }

    return StockAnalysis(
        candles = candlesAscending,
        sma = simpleMovingAverage(closes, smaPeriod),
        ema = exponentialMovingAverage(closes, emaPeriod),
        rsi = relativeStrengthIndex(closes, rsiPeriod),
        smaPeriod = smaPeriod,
        emaPeriod = emaPeriod,
        rsiPeriod = rsiPeriod,
    )
}