package indicators

fun relativeStrengthIndex(values: List<Double>, period: Int): List<Double?> {
    require(period > 0) {"period must be positive"}
    if (values.size <= period) {
        return List(values.size) { null }
    }

    val changes = values.zipWithNext { first, second ->
        second - first
    }

    val gains = changes.map { value -> maxOf(value, 0.0)}
    val losses = changes.map { value -> maxOf(-value, 0.0)}

    val avgGain = gains.subList(0, period).average()
    val avgLoss = losses.subList(0, period).average()

    val avgGains = gains.drop(period).runningFold(avgGain) {
        prev, curr -> (prev * (period - 1) + curr) / period
    }

    val avgLosses = losses.drop(period).runningFold(avgLoss) {
            prev, curr -> (prev * (period - 1) + curr) / period
    }

    val rsiVals = avgGains.zip(avgLosses).map { (gain, loss) ->
        when {
            gain == 0.0 && loss == 0.0 -> 50.0
            loss == 0.0 -> 100.0
            else -> {
                val rs = gain / loss
                100.0 - (100.0 / (1.0 + rs))
            }
        }
    }
    val leadingNulls: List<Double?> = List(period) { null }
    return leadingNulls + rsiVals
}