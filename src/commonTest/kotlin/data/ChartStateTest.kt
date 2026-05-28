package data

import analysis.StockAnalysis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ChartStateTest {

    @Test
    fun `should use fallback price range for flat prices`() {
        val chartState = calculateChartState(
            analysis = StockAnalysis(
                candles = listOf(
                    candle(date = "2026-05-01", price = 100.0),
                    candle(date = "2026-05-02", price = 100.0),
                ),
                smaPeriod = 20,
                emaPeriod = 20,
                rsiPeriod = 14,
                sma = listOf(null, null),
                ema = listOf(null, null),
                rsi = listOf(null, null),
            ),
            visibleRange = 0..1,
        )

        assertNotNull(chartState)
        assertEquals(99.5, chartState.priceMin)
        assertEquals(100.5, chartState.priceMax)
        assertEquals(1.0, chartState.priceRange)
    }

    @Test
    fun `should keep normal price range unchanged`() {
        val chartState = calculateChartState(
            analysis = StockAnalysis(
                candles = listOf(
                    candle(date = "2026-05-01", price = 100.0),
                    candle(date = "2026-05-02", price = 110.0),
                ),
                smaPeriod = 20,
                emaPeriod = 20,
                rsiPeriod = 14,
                sma = listOf(null, null),
                ema = listOf(null, null),
                rsi = listOf(null, null),
            ),
            visibleRange = 0..1,
        )

        assertNotNull(chartState)
        assertEquals(100.0, chartState.priceMin)
        assertEquals(110.0, chartState.priceMax)
        assertEquals(10.0, chartState.priceRange)
    }

    private fun candle(date: String, price: Double): Candle =
        Candle(
            date = date,
            open = price,
            high = price,
            low = price,
            close = price,
            volume = 100.0,
        )
}

