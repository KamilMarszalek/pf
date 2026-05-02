package analysis

import data.Candle
import kotlin.test.Test
import kotlin.test.assertEquals

class CsvExportTest {

    @Test
    fun `should export header when analysis has no candles`() {
        val csv = exportAnalysisToCsv(
            StockAnalysis(
                candles = emptyList(),
                sma20 = emptyList(),
                ema20 = emptyList(),
                rsi14 = emptyList()
            )
        )

        assertEquals("date,open,high,low,close,volume,sma20,ema20,rsi14", csv)
    }

    @Test
    fun `should export candle and indicator values`() {
        val csv = exportAnalysisToCsv(
            StockAnalysis(
                candles = listOf(
                    candle(
                        date = "2026-05-01",
                        open = 10.0,
                        high = 12.5,
                        low = 9.75,
                        close = 11.25,
                        volume = 1000.0
                    ),
                    candle(
                        date = "2026-05-02",
                        open = 11.25,
                        high = 13.0,
                        low = 10.5,
                        close = 12.75,
                        volume = 1500.0
                    )
                ),
                sma20 = listOf(10.5, 11.5),
                ema20 = listOf(10.75, 11.75),
                rsi14 = listOf(55.0, 60.25)
            )
        )

        assertEquals(
            """
            date,open,high,low,close,volume,sma20,ema20,rsi14
            2026-05-01,10.0,12.5,9.75,11.25,1000.0,10.5,10.75,55.0
            2026-05-02,11.25,13.0,10.5,12.75,1500.0,11.5,11.75,60.25
            """.trimIndent(),
            csv
        )
    }

    @Test
    fun `should leave indicator cells empty for null or missing values`() {
        val csv = exportAnalysisToCsv(
            StockAnalysis(
                candles = listOf(
                    candle(date = "2026-05-01"),
                    candle(date = "2026-05-02")
                ),
                sma20 = listOf(null),
                ema20 = emptyList(),
                rsi14 = listOf(null, 47.5)
            )
        )

        assertEquals(
            """
            date,open,high,low,close,volume,sma20,ema20,rsi14
            2026-05-01,1.0,2.0,0.5,1.5,100.0,,,
            2026-05-02,1.0,2.0,0.5,1.5,100.0,,,47.5
            """.trimIndent(),
            csv
        )
    }

    private fun candle(
        date: String,
        open: Double = 1.0,
        high: Double = 2.0,
        low: Double = 0.5,
        close: Double = 1.5,
        volume: Double = 100.0
    ) = Candle(
        date = date,
        open = open,
        high = high,
        low = low,
        close = close,
        volume = volume
    )
}
