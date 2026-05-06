package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.sp
import data.Candle
import data.ChartState
import data.TrendLine

@Composable
fun CandlestickChart(
    candles: List<Candle>,
    sma20: List<Double?> = emptyList(),
    ema20: List<Double?> = emptyList(),
    visibleRange: IntRange,
    isDrawingMode: Boolean,
    trendLines: List<TrendLine>,
    onLineAdded: (TrendLine) -> Unit,
    interactiveModifier: Modifier,
    modifier: Modifier = Modifier,
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

    val chartState by remember(candles, sma20, ema20, visibleRange) {
        derivedStateOf {
            val visibleCandles = candles.slice(visibleRange)
            if (visibleCandles.isEmpty()) return@derivedStateOf null

            val priceMin = visibleCandles.minOf { it.low }
            val priceMax = visibleCandles.maxOf { it.high }
            val priceRange = (priceMax - priceMin).takeIf { it > 0.0 } ?: 1.0

            val offset = visibleRange.first
            val visibleSma = sma20.drop(offset).take(visibleCandles.size)
            val visibleEma = ema20.drop(offset).take(visibleCandles.size)

            ChartState(visibleCandles = visibleCandles,
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
            paddingPx = 30f,
            firstPoint = firstPoint,
            onFirstPointChanged = { firstPoint = it },
            onCurrentTouchPosChanged = { currentTouchPos = it },
            onLineAdded = onLineAdded
        )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .then( if (isDrawingMode) drawingModifier else interactiveModifier )
    ) {
        val state = chartState ?: return@Canvas

        val width = size.width
        val height = size.height

        val availableChartWidth = width - (2 * paddingPx)
        val availableChartHeight = height - (2 * paddingPx)

        // pure transmutation functions
        val getX = { index: Int -> paddingPx + index * (availableChartWidth / state.visibleCandles.size) + (availableChartWidth / state.visibleCandles.size / 2)}
        val getY = { price: Double -> (paddingPx + availableChartHeight * (1.0 - (price - state.priceMin)/state.priceRange)).toFloat() }

        //grid and labels
        drawYAxisLabels(state.priceMin, state.priceMax, getY, paddingPx, width, textMeasurer)
        drawXAxisLabels(state.visibleCandles, getX, paddingPx, height, textMeasurer)

        //candles
        val candleWidth = availableChartWidth / state.visibleCandles.size
        val bodyWidth = candleWidth * 0.6f
        state.visibleCandles.forEachIndexed { i, candle ->
            drawCandle(candle, getX(i), bodyWidth, getY)
        }

        //indicators
        drawIndicatorLine(state.visibleSma, Color(0xFFFFA726), getX, getY)
        drawIndicatorLine(state.visibleEma, Color(0xFF42A5F5), getX, getY)

        //trend lines
        if (firstPoint != null && currentTouchPos != null) {
            drawGhostLine(firstPoint!!, currentTouchPos!!, getX, getY, visibleRange)
        }
        drawUserLines(trendLines, getX, getY, visibleRange)
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