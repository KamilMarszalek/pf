package analysis

import data.Candle
import kotlin.test.Test
import kotlin.test.assertEquals

class CandleAggregationTest {

    @Test
    fun `should keep sorted daily candles for daily timeframe`() {
        val result = aggregateCandles(
            candles = listOf(
                candle(date = "2026-05-02", open = 20.0),
                candle(date = "2026-05-01", open = 10.0),
            ),
            timeframe = CandleTimeframe.DAILY,
        )

        assertEquals(listOf("2026-05-01", "2026-05-02"), result.map { it.date })
    }

    @Test
    fun `should aggregate candles by week`() {
        val result = aggregateCandles(
            candles = listOf(
                candle(date = "2026-05-04", open = 10.0, high = 13.0, low = 9.0, close = 12.0, volume = 100.0),
                candle(date = "2026-05-05", open = 12.0, high = 15.0, low = 11.0, close = 14.0, volume = 200.0),
                candle(date = "2026-05-11", open = 20.0, high = 23.0, low = 19.0, close = 22.0, volume = 300.0),
            ),
            timeframe = CandleTimeframe.WEEKLY,
        )

        assertEquals(2, result.size)
        assertEquals(
            candle(date = "2026-05-04", open = 10.0, high = 15.0, low = 9.0, close = 14.0, volume = 300.0),
            result[0],
        )
        assertEquals(
            candle(date = "2026-05-11", open = 20.0, high = 23.0, low = 19.0, close = 22.0, volume = 300.0),
            result[1],
        )
    }

    @Test
    fun `should aggregate candles by month`() {
        val result = aggregateCandles(
            candles = listOf(
                candle(date = "2026-05-01", open = 10.0, high = 12.0, low = 9.0, close = 11.0, volume = 100.0),
                candle(date = "2026-05-29", open = 11.0, high = 16.0, low = 10.0, close = 15.0, volume = 200.0),
                candle(date = "2026-06-01", open = 20.0, high = 22.0, low = 18.0, close = 19.0, volume = 300.0),
            ),
            timeframe = CandleTimeframe.MONTHLY,
        )

        assertEquals(2, result.size)
        assertEquals(
            candle(date = "2026-05-01", open = 10.0, high = 16.0, low = 9.0, close = 15.0, volume = 300.0),
            result[0],
        )
        assertEquals(
            candle(date = "2026-06-01", open = 20.0, high = 22.0, low = 18.0, close = 19.0, volume = 300.0),
            result[1],
        )
    }

    @Test
    fun `should aggregate weeks across calendar year using ISO week rules`() {
        val result = aggregateCandles(
            candles = listOf(
                candle(date = "2020-12-28", open = 10.0, high = 12.0, low = 9.0, close = 11.0, volume = 100.0),
                candle(date = "2021-01-01", open = 11.0, high = 15.0, low = 10.0, close = 14.0, volume = 200.0),
                candle(date = "2021-01-04", open = 20.0, high = 22.0, low = 19.0, close = 21.0, volume = 300.0),
            ),
            timeframe = CandleTimeframe.WEEKLY,
        )

        assertEquals(2, result.size)
        assertEquals(
            candle(date = "2020-12-28", open = 10.0, high = 15.0, low = 9.0, close = 14.0, volume = 300.0),
            result[0],
        )
        assertEquals(
            candle(date = "2021-01-04", open = 20.0, high = 22.0, low = 19.0, close = 21.0, volume = 300.0),
            result[1],
        )
    }

    private fun candle(
        date: String,
        open: Double,
        high: Double = open,
        low: Double = open,
        close: Double = open,
        volume: Double = 100.0,
    ) = Candle(
        date = date,
        open = open,
        high = high,
        low = low,
        close = close,
        volume = volume,
    )
}

