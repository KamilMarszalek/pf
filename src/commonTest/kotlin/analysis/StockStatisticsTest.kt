package analysis

import data.Candle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StockStatisticsTest {

    @Test
    fun `should return null for empty candles`() {
        assertNull(calculateStockStatistics(emptyList()))
    }

    @Test
    fun `should calculate basic stock statistics`() {
        val statistics = calculateStockStatistics(
            listOf(
                candle(date = "2026-05-01", close = 100.0, volume = 1_000.0),
                candle(date = "2026-05-02", close = 110.0, volume = 2_000.0),
                candle(date = "2026-05-03", close = 104.5, volume = 3_000.0),
            )
        )

        requireNotNull(statistics)
        assertEquals(4.5, statistics.totalReturnPercent, 1e-10)
        assertEquals(100.0, statistics.minClose)
        assertEquals(110.0, statistics.maxClose)
        assertEquals(104.83333333333333, statistics.averageClose, 1e-10)
        assertEquals(2_000.0, statistics.averageVolume)
        assertEquals(7.5, statistics.volatilityPercent, 1e-10)
    }

    @Test
    fun `should sort candles by date before calculating statistics`() {
        val statistics = calculateStockStatistics(
            listOf(
                candle(date = "2026-05-03", close = 121.0),
                candle(date = "2026-05-01", close = 100.0),
                candle(date = "2026-05-02", close = 110.0),
            )
        )

        requireNotNull(statistics)
        assertEquals(21.0, statistics.totalReturnPercent, 1e-10)
        assertEquals(0.0, statistics.volatilityPercent, 1e-10)
    }

    @Test
    fun `should calculate single candle statistics with zero volatility`() {
        val statistics = calculateStockStatistics(
            listOf(candle(date = "2026-05-01", close = 42.0, volume = 500.0))
        )

        requireNotNull(statistics)
        assertEquals(0.0, statistics.totalReturnPercent)
        assertEquals(42.0, statistics.minClose)
        assertEquals(42.0, statistics.maxClose)
        assertEquals(42.0, statistics.averageClose)
        assertEquals(500.0, statistics.averageVolume)
        assertEquals(0.0, statistics.volatilityPercent)
    }

    @Test
    fun `daily returns should skip previous zero close`() {
        val returns = dailyReturnPercentages(listOf(0.0, 10.0, 12.0))

        assertEquals(listOf(20.0), returns)
    }

    private fun candle(
        date: String,
        close: Double,
        volume: Double = 100.0,
    ) = Candle(
        date = date,
        open = close,
        high = close,
        low = close,
        close = close,
        volume = volume,
    )
}
