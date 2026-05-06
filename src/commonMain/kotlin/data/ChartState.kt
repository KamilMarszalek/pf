package data

data class ChartState(
    val visibleCandles: List<Candle>,
    val visibleSma: List<Double?>,
    val visibleEma: List<Double?>,
    val priceMin: Double,
    val priceMax: Double,
    val priceRange: Double
)
