package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp

@Composable
fun RsiPanel(rsiData: List<Double?>, range: IntRange) {
    Column {
        Row(modifier = Modifier.padding(top = 4.dp)) {
            Text("70 (overbought)", style = MaterialTheme.typography.caption, color = Color(0xFFEF5350))
            Spacer(Modifier.width(16.dp))
            Text("30 (oversold)", style = MaterialTheme.typography.caption, color = Color(0xFF26A69A))
        }
        RsiChart(rsi = rsiData, visibleRange = range, modifier = Modifier.fillMaxWidth().height(200.dp))
    }
}

@Composable
fun RsiChart(
    rsi: List<Double?>,
    visibleRange: IntRange,
    modifier: Modifier = Modifier,
) {
    if (rsi.isEmpty()) return

    val visibleRsi = rsi.drop(visibleRange.first).take(visibleRange.count())

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        fun rsiToY(value: Double): Float = (height * (1.0 - value / 100.0)).toFloat()

        val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))

        listOf(30.0 to Color(0xFF26A69A), 70.0 to Color(0xFFEF5350)).forEach { (level, color) ->
            drawLine(
                color = color.copy(alpha = 0.6f),
                start = Offset(0f, rsiToY(level)),
                end = Offset(width, rsiToY(level)),
                strokeWidth = 1f,
                pathEffect = dash
            )
        }

        val candleWidth = width / visibleRsi.size
        val offsets = visibleRsi.mapIndexed { i, v ->
            if (v != null) Offset(i * candleWidth + candleWidth / 2, rsiToY(v)) else null
        }
        segmentOffsets(offsets).forEach { segment ->
            segment.zipWithNext { a, b ->
                drawLine(color = Color(0xFFAB47BC), start = a, end = b, strokeWidth = 1.5f)
            }
        }
    }
}

