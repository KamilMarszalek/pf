package data

import analysis.StockAnalysis

data class ChartState(
    val visibleCandles: List<Candle>,
    val visibleSma: List<Double?>,
    val visibleEma: List<Double?>,
    val priceMin: Double,
    val priceMax: Double,
    val priceRange: Double
)

fun calculateChartState(
    analysis: StockAnalysis,
    visibleRange: IntRange
): ChartState? {
    val visibleCandles = analysis.candles.filterIndexed { index, _ -> index in visibleRange }
    if (visibleCandles.isEmpty()) return null

    val visibleSma = analysis.sma.filterIndexed { index, _ -> index in visibleRange }
    val visibleEma = analysis.ema.filterIndexed { index, _ -> index in visibleRange }

    val minPrice = visibleCandles.minOf { it.low }
    val maxPrice = visibleCandles.maxOf { it.high }

    return ChartState(
        visibleCandles = visibleCandles,
        priceMin = minPrice,
        priceMax = maxPrice,
        priceRange = maxPrice - minPrice,
        visibleEma = visibleEma,
        visibleSma = visibleSma
    )
}
