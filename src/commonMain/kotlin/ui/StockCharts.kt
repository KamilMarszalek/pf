package ui

import analysis.StockAnalysis
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

@Composable
fun StockCharts(
    analysis: StockAnalysis,
    modifier: Modifier = Modifier,
) {
    val totalCount = analysis.candles.size
    if (totalCount == 0) return

    var visibleRange by remember(totalCount) {
        mutableStateOf(initialVisibleRange(totalCount, preferredCount = 100))
    }

    val visibleRangeState = rememberUpdatedState(visibleRange)

    var chartWidthPx by remember { mutableStateOf(0f) }
    var dragAccumulator by remember { mutableStateOf(0f) }

    Column(
        modifier = modifier
            .onSizeChanged { chartWidthPx = it.width.toFloat() }

            // drag / pan
            .pointerInput(chartWidthPx, totalCount) {
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

                    if (requestedShift != 0) {
                        val newRange = panVisibleRange(
                            range = range,
                            totalCount = totalCount,
                            shift = requestedShift,
                        )

                        val actualShift = newRange.first - range.first

                        if (actualShift != 0) {
                            dragAccumulator += actualShift * candleWidthPx
                            visibleRange = newRange
                        } else {
                            dragAccumulator = 0f
                        }
                    }
                }
            }

            // mouse wheel zoom
            .pointerInput(totalCount) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()

                        if (event.type == PointerEventType.Scroll) {
                            val scrollDelta = event.changes
                                .firstOrNull()
                                ?.scrollDelta
                                ?.y
                                ?: 0f

                            if (scrollDelta == 0f) continue

                            val zoomFactor = if (scrollDelta > 0) 1.1 else 0.9

                            visibleRange = zoomVisibleRange(
                                range = visibleRangeState.value,
                                totalCount = totalCount,
                                zoomFactor = zoomFactor,
                            )

                            event.changes.forEach { it.consume() }
                        }
                    }
                }
            },
    ) {
        CandlestickChart(
            candles = analysis.candles,
            sma20 = analysis.sma20,
            ema20 = analysis.ema20,
            visibleRange = visibleRange,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(top = 8.dp),
        )

        Row(modifier = Modifier.padding(top = 4.dp)) {
            Box(modifier = Modifier.size(12.dp).background(Color(0xFFFFA726)))
            Text(" SMA20", style = MaterialTheme.typography.caption)

            Spacer(Modifier.width(16.dp))

            Box(modifier = Modifier.size(12.dp).background(Color(0xFF42A5F5)))
            Text(" EMA20", style = MaterialTheme.typography.caption)
        }

        Text(
            "RSI(14)",
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(top = 8.dp),
        )

        RsiChart(
            rsi14 = analysis.rsi14,
            visibleRange = visibleRange,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
        )

        Row {
            Text(
                "— 70 (overbought)",
                style = MaterialTheme.typography.caption,
                color = Color(0xFFEF5350),
            )

            Spacer(Modifier.width(16.dp))

            Text(
                "— 30 (oversold)",
                style = MaterialTheme.typography.caption,
                color = Color(0xFF26A69A),
            )
        }
    }
}