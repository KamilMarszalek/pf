package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import data.Candle

@Composable
fun CandlestickChart(
    candles: List<Candle>,
    sma20: List<Double?> = emptyList(),
    ema20: List<Double?> = emptyList(),
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()

    val totalCount = candles.size
    val visibleCandles = candles.takeLast(101)
    val offset = totalCount - visibleCandles.size

    val paddingPx = 30f

    val visibleSma = sma20.drop(offset).take(visibleCandles.size)
    val visibleEma = ema20.drop(offset).take(visibleCandles.size)
    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {
        val width = size.width
        val height = size.height

        val availableChartWidth = width - (2 * paddingPx)
        val availableChartHeight = height - (2 * paddingPx)

        val priceMin = visibleCandles.minOf { it.low }
        val priceMax = visibleCandles.maxOf { it.high }
        val priceRange = priceMax - priceMin

        // pure transmutation functions
        val getX = { index: Int -> paddingPx + index * (availableChartWidth / visibleCandles.size) + (availableChartWidth / visibleCandles.size / 2)}
        val getY = { price: Double -> (paddingPx + availableChartHeight * (1.0 - (price - priceMin)/priceRange)).toFloat() }

        //grid and labels
        drawYAxisLabels(priceMin, priceMax, getY, paddingPx, width, textMeasurer)
        drawXAxisLabels(visibleCandles, getX, paddingPx, height, textMeasurer)

        //candles
        val candleWidth = availableChartWidth / visibleCandles.size
        val bodyWidth = candleWidth * 0.6f
        visibleCandles.forEachIndexed { i, candle ->
            drawCandle(candle, getX(i), bodyWidth, getY)
        }

        //indicators
        drawIndicatorLine(visibleSma, Color(0xFFFFA726), getX, getY)
        drawIndicatorLine(visibleEma, Color(0xFF42A5F5), getX, getY)
    }
}

// helpers (DrawScope extensions)

private fun DrawScope.drawYAxisLabels(
    min: Double,
    max: Double,
    getY: (Double) -> Float,
    padding: Float,
    width: Float,
    textMeasurer: TextMeasurer
) {
    val steps  = 5
    val stepValue = (max - min) / steps
    val textStyle = TextStyle(color = Color.Gray, fontSize = 10.sp)

    (0..steps).forEach { i ->
        val price = min + (stepValue * i)
        val y = getY(price)
        val priceText = String.format("%.1f", price)

        drawLine(
            color = Color.LightGray.copy(alpha = 0.3f),
            start = Offset(padding, y),
            end = Offset(width - padding, y),
            strokeWidth = 1f
        )

        val textLayoutResult = textMeasurer.measure(priceText, textStyle)

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                x = width - textLayoutResult.size.width - 5f,
                y = y - (textLayoutResult.size.height / 2)
            )
        )
    }
}

private fun DrawScope.drawXAxisLabels(
    candles: List<Candle>,
    getX: (Int) -> Float,
    padding: Float,
    height: Float,
    textMeasurer: TextMeasurer
) {
    val textStyle = TextStyle(color = Color.Gray, fontSize = 10.sp)

    candles.forEachIndexed { i, candle ->
        if (i % 20 == 0) {
            val x = getX(i)
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(x, padding),
                end = Offset(x, height - padding),
                strokeWidth = 1f
            )

            val textLayoutResult = textMeasurer.measure(candle.date, textStyle)

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = x - (textLayoutResult.size.width / 2),
                    y = height - textLayoutResult.size.height - 5f
                )
            )
        }
    }

}

private fun DrawScope.drawCandle(
    candle: Candle,
    x: Float,
    bodyWidth: Float,
    getY: (Double) -> Float
) {
    val isGreen = candle.close >= candle.open
    val color = if (isGreen) Color(0xFF26A69A) else Color(0xFFEF5350)

    drawLine(color = color,
        start = Offset(x, getY(candle.high)),
        end = Offset(x, getY(candle.low)),
        strokeWidth = 1.5f
    )

    val top = getY(maxOf(candle.open, candle.close))
    val bottom = getY(minOf(candle.open, candle.close))
    val bodyHeight = (bottom - top).coerceAtLeast(1f)

    drawRect(
        color = color,
        topLeft = Offset(x - bodyWidth / 2, top),
        size = Size(bodyWidth, bodyHeight)
    )
}

private fun DrawScope.drawIndicatorLine(
    values: List<Double?>,
    color: Color,
    getX: (Int) -> Float,
    getY: (Double) -> Float
) {
    values.zipWithNext().forEachIndexed { i, pair ->
        if (pair.first != null && pair.second != null) {
            drawLine(
                color = color,
                start = Offset(getX(i), getY(pair.first!!)),
                end = Offset(getX(i + 1), getY(pair.second!!)),
                strokeWidth = 1.5f
            )
        }
    }
}