package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.Candle
import data.ChartState
import data.TrendLine

@Composable
fun CandlestickChart(
    candles: List<Candle>,
    chartState: ChartState,
    visibleRange: IntRange,
    isDrawingMode: Boolean,
    trendLines: List<TrendLine>,
    onLineAdded: (TrendLine) -> Unit,
    interactiveModifier: Modifier,
    modifier: Modifier = Modifier,
    measureStartIdx: Int?,
    measureEndIdx: Int?,
    isMeasuringDragActive: Boolean,
    smaVisible: Boolean,
    emaVisible: Boolean
) {
    if (candles.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val paddingPx = 30f

    var drawingLineState by remember { mutableStateOf(DrawingLineState()) }

    LaunchedEffect(isDrawingMode) {
        if (!isDrawingMode) {
            drawingLineState = DrawingLineState()
        }
    }

    val drawingModifier = Modifier
        .drawTrendLine(
            isDrawingMode = isDrawingMode,
            chartState = chartState,
            visibleRange = visibleRange,
            paddingPx = paddingPx,
            drawingLineState = drawingLineState,
            onDrawingLineStateChanged = { drawingLineState = it },
            onLineAdded = onLineAdded
        )

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isDrawingMode) drawingModifier else interactiveModifier)
        ) {
            val width = size.width
            val height = size.height

            val availableChartWidth = width - (2 * paddingPx)
            val availableChartHeight = height - (2 * paddingPx)

            val getX = { index: Int ->
                paddingPx + index * (availableChartWidth / chartState.visibleCandles.size) + (availableChartWidth / chartState.visibleCandles.size / 2)
            }
            val getY = { price: Double ->
                (paddingPx + availableChartHeight * (1.0 - (price - chartState.priceMin) / chartState.priceRange)).toFloat()
            }

            drawYAxisLabels(chartState.priceMin, chartState.priceMax, getY, paddingPx, width, textMeasurer)
            drawXAxisLabels(chartState.visibleCandles, getX, paddingPx, height, textMeasurer)

            if (measureStartIdx != null && measureEndIdx != null) {
                val leftIdx = minOf(measureStartIdx, measureEndIdx)
                val rightIdx = maxOf(measureStartIdx, measureEndIdx)

                val leftX = getX(leftIdx - visibleRange.first)
                val rightX = getX(rightIdx - visibleRange.first)

                if (leftIdx != rightIdx) {
                    val fillLeft = leftX.coerceIn(paddingPx, width - paddingPx)
                    val fillRight = rightX.coerceIn(paddingPx, width - paddingPx)
                    drawRect(
                        color = Color.Gray.copy(alpha = 0.15f),
                        topLeft = Offset(fillLeft, paddingPx),
                        size = Size(fillRight - fillLeft, height - (2 * paddingPx))
                    )
                }

                if (leftIdx in visibleRange) {
                    drawLine(
                        color = Color.Gray,
                        start = Offset(leftX, paddingPx),
                        end = Offset(leftX, height - paddingPx),
                        strokeWidth = 2f
                    )
                }

                if (rightIdx in visibleRange) {
                    drawLine(
                        color = Color.Gray,
                        start = Offset(rightX, paddingPx),
                        end = Offset(rightX, height - paddingPx),
                        strokeWidth = 2f,
                        pathEffect = if (isMeasuringDragActive) {
                            PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        } else null
                    )
                }
            }

            val candleWidth = availableChartWidth / chartState.visibleCandles.size
            val bodyWidth = candleWidth * 0.6f
            chartState.visibleCandles.forEachIndexed { i, candle ->
                drawCandle(candle, getX(i), bodyWidth, getY)
            }

            if (smaVisible) {
                drawIndicatorLine(chartState.visibleSma, Color(0xFFFFA726), getX, getY)
            }
            if (emaVisible) {
                drawIndicatorLine(chartState.visibleEma, Color(0xFF42A5F5), getX, getY)
            }

            val ghostStart = drawingLineState.firstPoint
            val ghostEnd = drawingLineState.currentTouchPos
            if (ghostStart != null && ghostEnd != null) {
                drawGhostLine(ghostStart, ghostEnd, getX, getY, visibleRange)
            }
            drawUserLines(trendLines, getX, getY, visibleRange)
        }

        if (measureStartIdx != null && measureEndIdx != null) {
            val leftIdx = minOf(measureStartIdx, measureEndIdx)
            val rightIdx = maxOf(measureStartIdx, measureEndIdx)

            val startCandle = candles.getOrNull(leftIdx)
            val endCandle = candles.getOrNull(rightIdx)

            if (startCandle != null && endCandle != null) {
                val percentageChange = calculatePriceChangePercent(startCandle, endCandle)

                val isPositive = percentageChange >= 0
                val badgeColor = if (isPositive) Color(0xFF26A69A) else Color(0xFFEF5350)
                val sign = if (isPositive) "▲ +" else "▼ "

                Card(
                    backgroundColor = badgeColor.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    elevation = 4.dp
                ) {
                    Text(
                        text = String.format(java.util.Locale.US, "%s%.2f%%", sign, percentageChange),
                        color = Color.White,
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// helpers (DrawScope extensions)

fun DrawScope.drawUserLines(
    trendLines: List<TrendLine>,
    getX: (Int) -> Float,
    getY: (Double) -> Float,
    visibleRange: IntRange
) {
    trendLines.forEach { line ->
        val xStart = getX(line.startIndex - visibleRange.first)
        val xEnd = getX(line.endIndex - visibleRange.first)
        val yStart = getY(line.startPrice)
        val yEnd = getY(line.endPrice)

        drawLine(
            color = Color.Magenta,
            start = Offset(xStart, yStart),
            end = Offset(xEnd, yEnd),
            strokeWidth = 3f
        )
    }
}

fun DrawScope.drawGhostLine(
    firstPoint: DrawingPoint,
    currentTouchPos: Offset,
    getX: (Int) -> Float,
    getY: (Double) -> Float,
    visibleRange: IntRange
) {
    val startX = getX(firstPoint.candleIndex - visibleRange.first)
    val startY = getY(firstPoint.price)

    drawLine(
        color = Color.Yellow.copy(alpha = 0.2f),
        start = Offset(startX, startY),
        end = currentTouchPos,
        strokeWidth = 15f,
        cap = StrokeCap.Round
    )

    drawLine(
        color = Color.Yellow,
        start = Offset(startX, startY),
        end = currentTouchPos,
        strokeWidth = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
    )
}

private fun DrawScope.drawYAxisLabels(
    min: Double,
    max: Double,
    getY: (Double) -> Float,
    padding: Float,
    width: Float,
    textMeasurer: TextMeasurer
) {
    val steps = 5
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

    val labelCount = 5
    val interval = (candles.size / labelCount).coerceAtLeast(1)

    candles.forEachIndexed { i, candle ->
        if (i % interval == 0) {
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

    drawLine(
        color = color,
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
