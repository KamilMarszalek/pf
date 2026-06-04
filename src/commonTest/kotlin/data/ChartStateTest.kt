package data

import analysis.StockAnalysis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class ChartStateTest {

    @Test
    fun `should return null when visible range has no matching candles`() {
        val chartState = calculateChartState(
            analysis = stockAnalysis(
                candles = listOf(
                    candle(date = "2026-05-01", price = 100.0),
                    candle(date = "2026-05-02", price = 110.0),
                ),
                sma = listOf(null, null),
                ema = listOf(null, null),
            ),
            visibleRange = 5..10,
        )

        assertNull(chartState)
    }

    @Test
    fun `should handle visible range starting before available candles`() {
        val chartState = calculateChartState(
            analysis = stockAnalysis(
                candles = listOf(
                    candle(date = "2026-05-01", price = 100.0),
                    candle(date = "2026-05-02", price = 110.0),
                    candle(date = "2026-05-03", price = 120.0),
                ),
                sma = listOf(10.0, 11.0, 12.0),
                ema = listOf(20.0, 21.0, 22.0),
            ),
            visibleRange = -5..1,
        )

        assertNotNull(chartState)
        assertEquals(listOf("2026-05-01", "2026-05-02"), chartState.visibleCandles.map { it.date })
        assertEquals(listOf(10.0, 11.0), chartState.visibleSma)
        assertEquals(listOf(20.0, 21.0), chartState.visibleEma)
    }

    @Test
    fun `should handle visible range ending after available candles`() {
        val chartState = calculateChartState(
            analysis = stockAnalysis(
                candles = listOf(
                    candle(date = "2026-05-01", price = 100.0),
                    candle(date = "2026-05-02", price = 110.0),
                    candle(date = "2026-05-03", price = 120.0),
                ),
                sma = listOf(10.0, 11.0, 12.0),
                ema = listOf(20.0, 21.0, 22.0),
            ),
            visibleRange = 1..99,
        )

        assertNotNull(chartState)
        assertEquals(listOf("2026-05-02", "2026-05-03"), chartState.visibleCandles.map { it.date })
        assertEquals(listOf(11.0, 12.0), chartState.visibleSma)
        assertEquals(listOf(21.0, 22.0), chartState.visibleEma)
    }

    @Test
    fun `should use high and low for min and max price`() {
        val chartState = calculateChartState(
            analysis = stockAnalysis(
                candles = listOf(
                    candle(date = "2026-05-01", price = 100.0, high = 120.0, low = 90.0),
                    candle(date = "2026-05-02", price = 101.0, high = 110.0, low = 95.0),
                ),
                sma = listOf(null, null),
                ema = listOf(null, null),
            ),
            visibleRange = 0..1,
        )

        assertNotNull(chartState)
        assertEquals(90.0, chartState.priceMin)
        assertEquals(120.0, chartState.priceMax)
        assertEquals(30.0, chartState.priceRange)
    }

    @Test
    fun `should use fallback price range for flat prices`() {
        val chartState = calculateChartState(
            analysis = stockAnalysis(
                candles = listOf(
                    candle(date = "2026-05-01", price = 100.0),
                    candle(date = "2026-05-02", price = 100.0),
                ),
                sma = listOf(null, null),
                ema = listOf(null, null),
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
            analysis = stockAnalysis(
                candles = listOf(
                    candle(date = "2026-05-01", price = 100.0),
                    candle(date = "2026-05-02", price = 110.0),
                ),
                sma = listOf(null, null),
                ema = listOf(null, null),
            ),
            visibleRange = 0..1,
        )

        assertNotNull(chartState)
        assertEquals(100.0, chartState.priceMin)
        assertEquals(110.0, chartState.priceMax)
        assertEquals(10.0, chartState.priceRange)
    }

    private fun stockAnalysis(
        candles: List<Candle>,
        sma: List<Double?>,
        ema: List<Double?>,
    ) = StockAnalysis(
        candles = candles,
        smaPeriod = 20,
        emaPeriod = 20,
        rsiPeriod = 14,
        sma = sma,
        ema = ema,
        rsi = List(candles.size) { null },
    )

    private fun candle(
        date: String,
        price: Double,
        high: Double = price,
        low: Double = price,
    ): Candle =
        Candle(
            date = date,
            open = price,
            high = high,
            low = low,
            close = price,
            volume = 100.0,
        )
}

