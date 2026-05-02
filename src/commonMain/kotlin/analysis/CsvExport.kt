package analysis

fun exportAnalysisToCsv(analysis: StockAnalysis): String {
    val header = "date,open,high,low,close,volume,sma20,ema20,rsi14"
    val rows = analysis.candles.mapIndexed { index, candle ->
        val sma = analysis.sma20.getOrNull(index) ?: ""
        val ema = analysis.ema20.getOrNull(index) ?: ""
        val rsi = analysis.rsi14.getOrNull(index) ?: ""

        listOf(
            candle.date, candle.open, candle.high, candle.low, candle.close, candle.volume, sma, ema, rsi
        ).joinToString(",")
    }
    return (listOf(header) + rows).joinToString("\n")
}