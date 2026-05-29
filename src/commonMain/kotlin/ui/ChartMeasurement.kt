package ui

import data.Candle

fun averageCandlePrice(candle: Candle): Double =
    (candle.high + candle.low + candle.close) / 3.0

fun calculatePriceChangePercent(
    startCandle: Candle,
    endCandle: Candle,
): Double {
    val startPrice = averageCandlePrice(startCandle)
    val endPrice = averageCandlePrice(endCandle)

    return if (startPrice == 0.0) {
        0.0
    } else {
        ((endPrice - startPrice) / startPrice) * 100.0
    }
}

