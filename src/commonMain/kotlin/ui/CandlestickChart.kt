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
    sma: List<Double?> = emptyList(),
    ema: List<Double?> = emptyList(),
    visibleRange: IntRange,
    isDrawingMode: Boolean,
    trendLines: List<TrendLine>,
    onLineAdded: (TrendLine) -> Unit,
    interactiveModifier: Modifier,
    modifier: Modifier = Modifier,
    measureStartIdx: Int?,
    measureEndIdx: Int?,
    isMeasuringDragActive: Boolean
) {
    if (candles.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val paddingPx = 30f

    var firstPoint by remember { mutableStateOf<Pair<Int, Double>?>(null) }
    var currentTouchPos by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(isDrawingMode) {
        if (!isDrawingMode) {
            firstPoint = null
            currentTouchPos = null
        }
    }

    val chartState by remember(candles, sma, ema, visibleRange) {
        derivedStateOf {
            val visibleCandles = candles.slice(visibleRange)
            if (visibleCandles.isEmpty()) return@derivedStateOf null

            val priceMin = visibleCandles.minOf { it.low }
            val priceMax = visibleCandles.maxOf { it.high }
            val priceRange = (priceMax - priceMin).takeIf { it > 0.0 } ?: 1.0

            val offset = visibleRange.first
            val visibleSma = sma.drop(offset).take(visibleCandles.size)
            val visibleEma = ema.drop(offset).take(visibleCandles.size)

            ChartState(
                visibleCandles = visibleCandles,
                visibleSma = visibleSma,
                visibleEma = visibleEma,
                priceMin = priceMin,
                priceMax = priceMax,
                priceRange = priceRange
            )
        }
    }

    val drawingModifier = Modifier
        .drawTrendLine(
            isDrawingMode = isDrawingMode,
            chartState = chartState,
            visibleRange = visibleRange,
            paddingPx = paddingPx,
            firstPoint = firstPoint,
            onFirstPointChanged = { firstPoint = it },
            onCurrentTouchPosChanged = { currentTouchPos = it },
            onLineAdded = onLineAdded
        )

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isDrawingMode) drawingModifier else interactiveModifier)
        ) {
            val state = chartState ?: return@Canvas

            val width = size.width
            val height = size.height

            val availableChartWidth = width - (2 * paddingPx)
            val availableChartHeight = height - (2 * paddingPx)

            val getX = { index: Int ->
                paddingPx + index * (availableChartWidth / state.visibleCandles.size) + (availableChartWidth / state.visibleCandles.size / 2)
            }
            val getY = { price: Double ->
                (paddingPx + availableChartHeight * (1.0 - (price - state.priceMin) / state.priceRange)).toFloat()
            }

            drawYAxisLabels(state.priceMin, state.priceMax, getY, paddingPx, width, textMeasurer)
            drawXAxisLabels(state.visibleCandles, getX, paddingPx, height, textMeasurer)

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

            val candleWidth = availableChartWidth / state.visibleCandles.size
            val bodyWidth = candleWidth * 0.6f
            state.visibleCandles.forEachIndexed { i, candle ->
                drawCandle(candle, getX(i), bodyWidth, getY)
            }

            drawIndicatorLine(state.visibleSma, Color(0xFFFFA726), getX, getY)
            drawIndicatorLine(state.visibleEma, Color(0xFF42A5F5), getX, getY)

            if (firstPoint != null && currentTouchPos != null) {
                drawGhostLine(firstPoint!!, currentTouchPos!!, getX, getY, visibleRange)
            }
            drawUserLines(trendLines, getX, getY, visibleRange)
        }

        if (measureStartIdx != null && measureEndIdx != null) {
            val leftIdx = minOf(measureStartIdx, measureEndIdx)
            val rightIdx = maxOf(measureStartIdx, measureEndIdx)

            val startCandle = candles.getOrNull(leftIdx)
            val endCandle = candles.getOrNull(rightIdx)

            if (startCandle != null && endCandle != null) {
                val priceStart = (startCandle.high + startCandle.low + startCandle.close) / 3.0
                val priceEnd = (endCandle.high + endCandle.low + endCandle.close) / 3.0

                val priceChange = priceEnd - priceStart
                val percentageChange = if (priceStart != 0.0) (priceChange / priceStart) * 100 else 0.0

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
    firstPoint: Pair<Int, Double>,
    currentTouchPos: Offset,
    getX: (Int) -> Float,
    getY: (Double) -> Float,
    visibleRange: IntRange
) {
    val startX = getX(firstPoint.first - visibleRange.first)
    val startY = getY(firstPoint.second)

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