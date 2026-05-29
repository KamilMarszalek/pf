package ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
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

fun Modifier.drawTrendLine(
    isDrawingMode: Boolean,
    chartState: ChartState?,
    visibleRange: IntRange,
    paddingPx: Float,
    firstPoint: Pair<Int, Double>?,
    onFirstPointChanged: (Pair<Int, Double>?) -> Unit,
    onCurrentTouchPosChanged: (Offset?) -> Unit,
    onLineAdded: (TrendLine) -> Unit
): Modifier = composed {
    val currentFirstPoint by rememberUpdatedState(firstPoint)
    val currentRange by rememberUpdatedState(visibleRange)
    val currentChartState by rememberUpdatedState(chartState)

    pointerInput(isDrawingMode, chartState) {
        if (!isDrawingMode || chartState == null) {
            onCurrentTouchPosChanged(null)
            return@pointerInput
        }

        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.first()
                val position = change.position

                when {
                    change.changedToDown() -> {
                        val state = currentChartState ?: return@awaitPointerEventScope
                        val point = pointerPositionToChartPoint(
                            size,
                            paddingPx,
                            state,
                            position,
                            currentRange
                        )

                        onFirstPointChanged(point.candleIndex to point.price)
                        onCurrentTouchPosChanged(position)
                        change.consume()
                    }

                    change.pressed -> {
                        if (currentFirstPoint != null) {
                            onCurrentTouchPosChanged(position)
                            change.consume()
                        }
                    }

                    change.changedToUp() -> {
                        val state = currentChartState
                        val startPt = currentFirstPoint

                        if (state != null && startPt != null) {
                            val point = pointerPositionToChartPoint(
                                size,
                                paddingPx,
                                state,
                                position,
                                currentRange
                            )

                            onLineAdded(
                                TrendLine(
                                    startIndex = startPt.first,
                                    startPrice = startPt.second,
                                    endIndex = point.candleIndex,
                                    endPrice = point.price
                                )
                            )
                        }
                        onFirstPointChanged(null)
                        onCurrentTouchPosChanged(null)
                        change.consume()
                    }
                }
            }
        }
    }
}

fun Modifier.chartMeasure(
    isMeasuringMode: Boolean,
    chartState: ChartState?,
    visibleRange: IntRange,
    paddingPx: Float,
    onMeasureStateChanged: (MeasureState) -> Unit
): Modifier = composed {
    val currentRange by rememberUpdatedState(visibleRange)
    val currentChartState by rememberUpdatedState(chartState)

    pointerInput(isMeasuringMode, chartState) {
        if (!isMeasuringMode || chartState == null) return@pointerInput

        fun getCandleIndex(pointerX: Float): Int? {
            val state = currentChartState ?: return null
            val availableW = size.width - (2 * paddingPx)
            if (availableW <= 0 || state.visibleCandles.isEmpty()) return null
            val step = availableW / state.visibleCandles.size
            return currentRange.first + ((pointerX - paddingPx) / step).toInt()
                .coerceIn(0, state.visibleCandles.size - 1)
        }

        var activeStartIdx: Int? = null
        var activeEndIdx: Int? = null

        detectDragGestures(
            onDragStart = { startOffset ->
                val idx = getCandleIndex(startOffset.x)
                if (idx != null) {
                    activeStartIdx = idx
                    activeEndIdx = idx
                    onMeasureStateChanged(MeasureState(startIdx = idx, endIdx = idx, isDragging = true))
                }
            },
            onDragEnd = {
                if (activeStartIdx != null && activeEndIdx != null) {
                    onMeasureStateChanged(
                        MeasureState(
                            startIdx = activeStartIdx,
                            endIdx = activeEndIdx,
                            isDragging = false
                        )
                    )
                }
            },
            onDragCancel = {
                activeStartIdx = null
                activeEndIdx = null
                onMeasureStateChanged(MeasureState(startIdx = null, endIdx = null, isDragging = false))
            },
            onDrag = { change, _ ->
                change.consume()
                val currentIdx = getCandleIndex(change.position.x)
                if (activeStartIdx != null && currentIdx != null) {
                    activeEndIdx = currentIdx
                    onMeasureStateChanged(
                        MeasureState(
                            startIdx = activeStartIdx,
                            endIdx = currentIdx,
                            isDragging = true
                        )
                    )
                }
            }
        )
    }
}

private fun pointerPositionToChartPoint(
    size: IntSize,
    paddingPx: Float,
    state: ChartState,
    position: Offset,
    currentRange: IntRange
): ChartPoint {
    val availableW = size.width - (2 * paddingPx)
    val availableH = size.height - (2 * paddingPx)
    val step = availableW / state.visibleCandles.size

    val localIndex = ((position.x - paddingPx) / step)
        .toInt()
        .coerceIn(0, state.visibleCandles.size - 1)
    
    val globalIndex = currentRange.first + localIndex
    val relativeY = (position.y - paddingPx) / availableH
    val price = state.priceMin + (1.0 - relativeY.toDouble()) * state.priceRange
    return ChartPoint(price, globalIndex)
}

private data class ChartPoint(
    val price: Double,
    val candleIndex: Int,
)

