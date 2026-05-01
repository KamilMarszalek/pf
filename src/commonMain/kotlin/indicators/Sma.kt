package indicators

fun simpleMovingAverage(values: List<Double>, window: Int): List<Double?> {
    require(window > 0) { "window must be positive" }

    return values.indices.map { index ->
        if (index + 1 < window) {
            null
        } else {
            values.subList(index + 1 - window, index + 1).average()
        }
    }
}