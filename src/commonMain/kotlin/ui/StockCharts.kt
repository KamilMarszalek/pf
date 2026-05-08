package ui

import analysis.StockAnalysis
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import data.TrendLine

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

    var chartWidthPx by remember { mutableStateOf(0f) }

    var isDrawingMode by remember { mutableStateOf(false)}
    val trendLines = remember { mutableStateListOf<TrendLine>() }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = modifier
                .onSizeChanged { chartWidthPx = it.width.toFloat() }
        ) {
            Row(
                Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            )  {
                Row {
                    //TODO onClicks
                    IconButton(
                        onClick = {}
                    ) {
                        Text("5Y")
                    }
                    IconButton(
                        onClick = {},
                    ) {
                        Text("1Y")
                    }
                    IconButton(
                        onClick = {},
                    ) {
                        Text("6M")
                    }
                    IconButton(
                        onClick = {},
                    ) {
                        Text("3M")
                    }
                    IconButton(
                        onClick = {},
                    ) {
                        Text("1M")
                    }
                }
                Row {
                    IconButton(
                        onClick = { isDrawingMode = !isDrawingMode }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = if (isDrawingMode) Color.Magenta else Color.Black
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    IconButton(
                        onClick = { trendLines.clear() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Clear",
                            tint = Color.Black
                        )
                    }
                }
            }

            CandlestickChart(
                candles = analysis.candles,
                sma20 = analysis.sma20,
                ema20 = analysis.ema20,
                visibleRange = visibleRange,
                isDrawingMode = isDrawingMode,
                trendLines = trendLines,
                onLineAdded = { newLine ->
                    trendLines.add(newLine)
                    isDrawingMode = false
                },
                interactiveModifier = Modifier
                    .chartDrag(
                        chartWidthPx = chartWidthPx,
                        visibleRange = visibleRange,
                        totalCount = totalCount,
                        onRangeChange = { visibleRange = it },
                        isDrawingMode = isDrawingMode
                    )
                    .chartZoom(
                        visibleRange = visibleRange,
                        totalCount = totalCount,
                        onRangeChange = { visibleRange = it },
                    ),
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
}
