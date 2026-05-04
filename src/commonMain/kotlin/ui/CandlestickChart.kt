package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import data.Candle

@Composable
fun CandlestickChart(
    candles: List<Candle>,
    sma20: List<Double?> = emptyList(),
    ema20: List<Double?> = emptyList(),
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) return

    val totalCount = candles.size
    val visibleCandles = candles.takeLast(100)
    val offset = totalCount - visibleCandles.size

    val visibleSma = sma20.drop(offset).take(visibleCandles.size)
    val visibleEma = ema20.drop(offset).take(visibleCandles.size)
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val priceMin = visibleCandles.minOf { it.low }
        val priceMax = visibleCandles.maxOf { it.high }
        val priceRange = priceMax - priceMin

        fun priceToY(price: Double): Float = (height * (1.0 - (price - priceMin)/priceRange)).toFloat()

        val candleWidth = width / visibleCandles.size
        val bodyWidth = candleWidth * 0.6f

        visibleCandles.forEachIndexed { i, candle ->
            val x = i * candleWidth + candleWidth / 2
            val isGreen = candle.close >= candle.open
            val color = if (isGreen) Color(0xFF26A69A) else Color(0xFFEF5350)

            drawLine(color = color,
                start = Offset(x, priceToY(candle.high)),
                end = Offset(x, priceToY(candle.low)),
                strokeWidth = 1.5f
            )

            val top = priceToY(maxOf(candle.open, candle.close))
            val bottom = priceToY(minOf(candle.open, candle.close))
            val bodyHeight = (bottom - top).coerceAtLeast(1f)

            drawRect(
                color = color,
                topLeft = Offset(x - bodyWidth / 2, top),
                size = Size(bodyWidth, bodyHeight)
            )
        }

        fun drawIndicatorLine(values: List<Double?>, color: Color) {
            val offsets = values.mapIndexed { i, v ->
                if (v != null) Offset(i * candleWidth + candleWidth / 2, priceToY(v)) else null
            }
            segmentOffsets(offsets).forEach { segment ->
                segment.zipWithNext { a, b ->
                    drawLine(color = color, start = a, end = b, strokeWidth = 1.5f)
                }
            }
        }
        drawIndicatorLine(visibleSma, Color(0xFFFFA726))
        drawIndicatorLine(visibleEma, Color(0xFF42A5F5))
    }
}