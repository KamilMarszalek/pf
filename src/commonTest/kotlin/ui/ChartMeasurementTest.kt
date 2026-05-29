package ui

import data.Candle
import kotlin.test.Test
import kotlin.test.assertEquals

class ChartMeasurementTest {

    @Test
    fun `should calculate positive price change percent`() {
        val result = calculatePriceChangePercent(
            startCandle = candle(high = 12.0, low = 9.0, close = 9.0),
            endCandle = candle(high = 18.0, low = 12.0, close = 15.0),
        )

        assertEquals(50.0, result, 1e-10)
    }

    @Test
    fun `should calculate negative price change percent`() {
        val result = calculatePriceChangePercent(
            startCandle = candle(high = 18.0, low = 12.0, close = 15.0),
            endCandle = candle(high = 12.0, low = 9.0, close = 9.0),
        )

        assertEquals(-33.33333333333333, result, 1e-10)
    }

    @Test
    fun `should return zero when start price is zero`() {
        val result = calculatePriceChangePercent(
            startCandle = candle(high = 0.0, low = 0.0, close = 0.0),
            endCandle = candle(high = 12.0, low = 9.0, close = 9.0),
        )

        assertEquals(0.0, result)
    }

    private fun candle(
        high: Double,
        low: Double,
        close: Double,
    ) = Candle(
        date = "2026-05-01",
        open = close,
        high = high,
        low = low,
        close = close,
        volume = 100.0,
    )
}

