package ui

import analysis.StockAnalysis
import analysis.analyzeCandles
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import data.Candle
import data.TrendLine
import ui.ActiveRangeUtils.*

@Composable
fun StockCharts(
    candles: List<Candle>,
    onAnalysisReady: (StockAnalysis) -> Unit = {},
    modifier: Modifier = Modifier,
    sharedVisibleRange: IntRange? = null,
    onVisibleRangeChange: (IntRange) -> Unit = {}
) {
    val totalCount = candles.size
    if (totalCount == 0) return

    var smaPeriod by remember { mutableStateOf(20) }
    var emaPeriod by remember { mutableStateOf(20) }
    var rsiPeriod by remember { mutableStateOf(14) }

    var smaVisible by remember { mutableStateOf(true) }
    var emaVisible by remember { mutableStateOf(true) }
    var rsiVisible by remember { mutableStateOf(true) }

    val analysis by remember(candles, smaPeriod, emaPeriod, rsiPeriod) {
        derivedStateOf { analyzeCandles(candles, smaPeriod, emaPeriod, rsiPeriod) }
    }

    // Side effect
    LaunchedEffect(analysis) {
        onAnalysisReady(analysis)
    }

    var localVisibleRange by remember(totalCount) {
        mutableStateOf(initialVisibleRange(totalCount, preferredCount = 100))
    }
    val isSharedMode = sharedVisibleRange != null && sharedVisibleRange != IntRange.EMPTY
    val currentVisibleRange = if (isSharedMode) sharedVisibleRange!! else localVisibleRange
    val updateVisibleRange: (IntRange) -> Unit = { newRange ->
        if (isSharedMode) {
            onVisibleRangeChange(newRange)
        } else {
            localVisibleRange = newRange
        }
    }

    var chartWidthPx by remember { mutableStateOf(0f) }

    var isDrawingMode by remember { mutableStateOf(false) }
    val trendLines = remember { mutableStateListOf<TrendLine>() }

    val visibleRangeMarks = remember{ mutableStateOf(findVisibleRangeMarks(analysis.candles))}
    var visibleRangeEnum by remember { mutableStateOf(detectVisibleRange(currentVisibleRange, visibleRangeMarks.value)) }

    // Side effect
    LaunchedEffect(currentVisibleRange) {
        visibleRangeEnum = detectVisibleRange(currentVisibleRange, visibleRangeMarks.value)
    }

    // shared range init if empty
    if (sharedVisibleRange == IntRange.EMPTY) {
        SideEffect {
            onVisibleRangeChange(initialVisibleRange(totalCount, preferredCount = 100))
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .onSizeChanged { chartWidthPx = it.width.toFloat() }
        ) {
            Row(
                Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    IconButton(
                        onClick = { updateVisibleRange(getVisibleRange(VisibleRange.FIVE_YEAR, visibleRangeMarks.value)) },
                    ) {
                        Text(
                            text = "5Y",
                            color = if (visibleRangeEnum == VisibleRange.FIVE_YEAR) Color.Magenta else Color.Black
                        )
                    }
                    IconButton(
                        onClick = { updateVisibleRange(getVisibleRange(VisibleRange.ONE_YEAR, visibleRangeMarks.value)) },
                    ) {
                        Text(
                            text = "1Y",
                            color = if (visibleRangeEnum == VisibleRange.ONE_YEAR) Color.Magenta else Color.Black
                        )
                    }
                    IconButton(
                        onClick = { updateVisibleRange(getVisibleRange(VisibleRange.SIX_MONTHS, visibleRangeMarks.value)) },
                    ) {
                        Text(
                            text = "6M",
                            color = if (visibleRangeEnum == VisibleRange.SIX_MONTHS) Color.Magenta else Color.Black
                        )
                    }
                    IconButton(
                        onClick = { updateVisibleRange(getVisibleRange(VisibleRange.THREE_MONTHS, visibleRangeMarks.value)) },
                    ) {
                        Text(
                            text = "3M",
                            color = if (visibleRangeEnum == VisibleRange.THREE_MONTHS) Color.Magenta else Color.Black
                        )
                    }
                    IconButton(
                        onClick = { updateVisibleRange(getVisibleRange(VisibleRange.ONE_MONTH, visibleRangeMarks.value)) },
                    ) {
                        Text(
                            text = "1M",
                            color = if (visibleRangeEnum == VisibleRange.ONE_MONTH) Color.Magenta else Color.Black
                        )
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
                sma = if (smaVisible) analysis.sma else emptyList(),
                ema = if (emaVisible) analysis.ema else emptyList(),
                visibleRange = currentVisibleRange,
                isDrawingMode = isDrawingMode,
                trendLines = trendLines,
                onLineAdded = { newLine ->
                    trendLines.add(newLine)
                    isDrawingMode = false
                },
                interactiveModifier = Modifier
                    .chartDrag(
                        chartWidthPx = chartWidthPx,
                        visibleRange = currentVisibleRange,
                        totalCount = totalCount,
                        onRangeChange = { updateVisibleRange(it) },
                        isDrawingMode = isDrawingMode
                    )
                    .chartZoom(
                        visibleRange = currentVisibleRange,
                        totalCount = totalCount,
                        onRangeChange = { updateVisibleRange(it) },
                    ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 8.dp),
            )

            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(12.dp).background(Color(0xFFFFA726)))
                Text(
                    " SMA${analysis.smaPeriod}",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.width(50.dp)
                )
                IconButton(onClick = { smaVisible = !smaVisible }, modifier = Modifier.size(20.dp)) {
                    Icon(
                        imageVector = if (smaVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle SMA",
                        modifier = Modifier.size(14.dp)
                    )
                }
                Slider(
                    value = smaPeriod.toFloat(), onValueChange = { smaPeriod = it.toInt() },
                    valueRange = 5f..200f, modifier = Modifier.width(120.dp).height(24.dp)
                )

                Spacer(Modifier.width(16.dp))

                Box(modifier = Modifier.size(12.dp).background(Color(0xFF42A5F5)))
                Text(
                    " EMA${analysis.emaPeriod}",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.width(50.dp)
                )
                IconButton(onClick = { emaVisible = !emaVisible }, modifier = Modifier.size(20.dp)) {
                    Icon(
                        imageVector = if (emaVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle EMA",
                        modifier = Modifier.size(14.dp)
                    )
                }
                Slider(
                    value = emaPeriod.toFloat(), onValueChange = { emaPeriod = it.toInt() },
                    valueRange = 5f..200f, modifier = Modifier.width(120.dp).height(24.dp)
                )
            }

            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "RSI(${analysis.rsiPeriod})",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.width(50.dp)
                )
                IconButton(onClick = { rsiVisible = !rsiVisible }, modifier = Modifier.size(20.dp)) {
                    Icon(
                        imageVector = if (rsiVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle RSI",
                        modifier = Modifier.size(14.dp)
                    )
                }
                Slider(
                    value = rsiPeriod.toFloat(),
                    onValueChange = { rsiPeriod = it.toInt() },
                    valueRange = 2f..50f,
                    modifier = Modifier.width(120.dp).height(24.dp)
                )
            }

            if (rsiVisible) {
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
                RsiChart(
                    rsi = analysis.rsi,
                    visibleRange = currentVisibleRange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                )


            }
        }
    }
}
