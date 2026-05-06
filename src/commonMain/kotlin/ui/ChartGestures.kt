package ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun Modifier.chartDrag(
    chartWidthPx: Float,
    visibleRange: IntRange,
    totalCount: Int,
    onRangeChange: (IntRange) -> Unit,
): Modifier {
    val visibleRangeState = rememberUpdatedState(visibleRange)
    val onRangeChangeState = rememberUpdatedState(onRangeChange)

    return pointerInput(chartWidthPx, totalCount) {
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
