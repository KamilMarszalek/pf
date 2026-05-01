package indicators

fun exponentialMovingAverage(values: List<Double>, period: Int): List<Double?> {
    require(period > 0) {"period must be positive"}
    if (values.size < period) {
        return List(values.size) { null }
    }

    val alpha = 2.0 / (period + 1.0)

    val initialEma = values.take(period).average()

    val emaValues = values.drop(period).runningFold(initialEma) {
        previousEma, currentValue -> alpha * currentValue + (1.0 - alpha) * previousEma
    }

    val leadingNulls: List<Double?> = List(period - 1) { null }
    return leadingNulls + emaValues
}