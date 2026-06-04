package analysis

import data.Candle
import kotlin.test.Test
import kotlin.test.assertEquals

class StockAnalysisTest {

    @Test
    fun `should sort candles and preserve indicator metadata`() {
        val candles = listOf(
            candle(date = "2026-05-03", close = 30.0),
            candle(date = "2026-05-01", close = 10.0),
            candle(date = "2026-05-02", close = 20.0),
        )

        val analysis = analyzeCandles(
            candles = candles,
            smaPeriod = 2,
            emaPeriod = 2,
            rsiPeriod = 2,
        )

        assertEquals(listOf("2026-05-01", "2026-05-02", "2026-05-03"), analysis.candles.map { it.date })
        assertEquals(analysis.candles.size, analysis.sma.size)
        assertEquals(analysis.candles.size, analysis.ema.size)
        assertEquals(analysis.candles.size, analysis.rsi.size)
        assertEquals(2, analysis.smaPeriod)
        assertEquals(2, analysis.emaPeriod)
        assertEquals(2, analysis.rsiPeriod)
    }

    private fun candle(
        date: String,
        close: Double,
    ) = Candle(
        date = date,
        open = close,
        high = close,
        low = close,
        close = close,
        volume = 100.0,
    )
}
