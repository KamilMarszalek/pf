package ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import data.ChartState
import data.TrendLine

@Composable
fun Modifier.chartDrag(
    isDrawingMode: Boolean,
    chartWidthPx: Float,
    visibleRange: IntRange,
    totalCount: Int,
    onRangeChange: (IntRange) -> Unit,
): Modifier {
    val visibleRangeState = rememberUpdatedState(visibleRange)
    val onRangeChangeState = rememberUpdatedState(onRangeChange)

    return pointerInput(chartWidthPx, totalCount) {
        if (isDrawingMode) return@pointerInput

        var dragAccumulator = 0f

        detectDragGestures(
            onDragStart = { dragAccumulator = 0f },
            onDragEnd = { dragAccumulator = 0f },
            onDragCancel = { dragAccumulator = 0f },
        ) { change, dragAmount ->
            change.consume()

            val range = visibleRangeState.value
            val currentCount = range.count()
            if (chartWidthPx <= 0f || currentCount <= 0) {
                return@detectDragGestures
            }

            val candleWidthPx = chartWidthPx / currentCount
            if (candleWidthPx <= 0f) {
                return@detectDragGestures
            }

            dragAccumulator += dragAmount.x

            val requestedShift = (-dragAccumulator / candleWidthPx).toInt()
            if (requestedShift == 0) {
                return@detectDragGestures
            }

            val newRange = panVisibleRange(
                range = range,
                totalCount = totalCount,
                shift = requestedShift,
            )

            val actualShift = newRange.first - range.first
            if (actualShift != 0) {
                dragAccumulator += actualShift * candleWidthPx
                onRangeChangeState.value(newRange)
            } else {
                dragAccumulator = 0f
            }
        }
    }
}

@Composable
fun Modifier.chartZoom(
    visibleRange: IntRange,
    totalCount: Int,
    onRangeChange: (IntRange) -> Unit,
): Modifier {
    val visibleRangeState = rememberUpdatedState(visibleRange)
    val onRangeChangeState = rememberUpdatedState(onRangeChange)

    return pointerInput(totalCount) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                if (event.type != PointerEventType.Scroll) {
                    continue
                }

                val scrollDelta = event.changes
                    .firstOrNull()
                    ?.scrollDelta
                    ?.y
                    ?: 0f

                if (scrollDelta == 0f) {
                    continue
                }

                val zoomFactor = if (scrollDelta > 0) 1.1 else 0.9

                onRangeChangeState.value(
                    zoomVisibleRange(
                        range = visibleRangeState.value,
                        totalCount = totalCount,
                        zoomFactor = zoomFactor,
                    )
                )

                event.changes.forEach { it.consume() }
            }
        }
    }
}

@Composable
fun Modifier.drawTrendLine(
    isDrawingMode: Boolean,
    chartState: ChartState?,
    visibleRange: IntRange,
    paddingPx: Float,
    onLineAdded: (TrendLine) -> Unit
) : Modifier = composed {
    var firstPoint by remember { mutableStateOf<Pair<Int, Double>?>(null) }

    LaunchedEffect(isDrawingMode) {
        if (!isDrawingMode) firstPoint = null
    }

    pointerInput(isDrawingMode, chartState, visibleRange) {
        if (!isDrawingMode || chartState == null) return@pointerInput

        detectTapGestures { offset ->
            val availableW = size.width - (2 * paddingPx)
            val availableH = size.height - (2 * paddingPx)

            val step = availableW / chartState.visibleCandles.size
            val localIndex = ((offset.x - paddingPx - step / 2) / step).toInt().coerceIn(0, chartState.visibleCandles.size - 1)
            val globalIndex = visibleRange.first + localIndex

            val relativeY = (offset.y - paddingPx) / availableH
            val clickedPrice = chartState.priceMin + (1.0 - relativeY.toDouble()) * chartState.priceRange

            if (firstPoint == null) {
                firstPoint = globalIndex to clickedPrice
            } else {
                onLineAdded(
                    TrendLine(
                        startIndex = firstPoint!!.first,
                        startPrice = firstPoint!!.second,
                        endIndex = globalIndex,
                        endPrice = clickedPrice
                    )
                )
                firstPoint = null
            }
        }
    }
}
