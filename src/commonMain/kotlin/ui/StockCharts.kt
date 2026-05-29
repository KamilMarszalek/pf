package ui

import analysis.CandleTimeframe
import analysis.StockAnalysis
import analysis.aggregateCandles
import analysis.analyzeCandles
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import data.Candle
import data.TrendLine
import data.calculateChartState
import ui.activeRangeUtils.VisibleRange
import ui.activeRangeUtils.detectVisibleRange
import ui.activeRangeUtils.findVisibleRangeMarks
import ui.activeRangeUtils.getVisibleRange

@Composable
fun StockCharts(
    candles: List<Candle>,
    onAnalysisReady: (StockAnalysis) -> Unit = {},
    modifier: Modifier = Modifier,
    sharedVisibleRange: IntRange? = null,
    onVisibleRangeChange: (IntRange) -> Unit = {},
    sharedMeasureState: MeasureState? = null,
    onMeasureRangeChange: (MeasureState) -> Unit = {}
) {
    var candleTimeframe by remember { mutableStateOf(CandleTimeframe.DAILY) }
    val displayCandles by remember(candles, candleTimeframe) {
        derivedStateOf { aggregateCandles(candles, candleTimeframe) }
    }
    val totalCount = displayCandles.size
    if (totalCount == 0) return

    // State definitions
    var smaPeriod by remember { mutableStateOf(20) }
    var emaPeriod by remember { mutableStateOf(20) }
    var rsiPeriod by remember { mutableStateOf(14) }
    var smaVisible by remember { mutableStateOf(true) }
    var emaVisible by remember { mutableStateOf(true) }
    var rsiVisible by remember { mutableStateOf(true) }
    var chartWidthPx by remember { mutableStateOf(0f) }
    var isDrawingMode by remember { mutableStateOf(false) }
    var isMeasuringMode by remember { mutableStateOf(false) }

    val trendLines = remember { mutableStateListOf<TrendLine>() }

    // Pure business logic triggers
    val analysis by remember(displayCandles, smaPeriod, emaPeriod, rsiPeriod) {
        derivedStateOf { analyzeCandles(displayCandles, smaPeriod, emaPeriod, rsiPeriod) }
    }

    LaunchedEffect(analysis) { onAnalysisReady(analysis) }

    // Range Management
    var localVisibleRange by remember(totalCount) {
        mutableStateOf(initialVisibleRange(totalCount, preferredCount = 100))
    }
    val isSharedMode = sharedVisibleRange != null && sharedVisibleRange != IntRange.EMPTY
    val currentVisibleRange = if (isSharedMode) sharedVisibleRange else localVisibleRange

    val updateVisibleRange: (IntRange) -> Unit = { newRange ->
        if (isSharedMode)
            onVisibleRangeChange(newRange)
        else
            localVisibleRange = newRange
    }

    val visibleRangeMarks = remember(analysis.candles) { findVisibleRangeMarks(analysis.candles) }
    var visibleRangeEnum by remember {
        mutableStateOf(
            detectVisibleRange(
                currentVisibleRange,
                visibleRangeMarks
            )
        )
    }

    LaunchedEffect(currentVisibleRange, visibleRangeMarks) {
        visibleRangeEnum = detectVisibleRange(currentVisibleRange, visibleRangeMarks)
    }

    if (sharedVisibleRange == IntRange.EMPTY) {
        SideEffect { onVisibleRangeChange(initialVisibleRange(totalCount, preferredCount = 100)) }
    }

    // Measure State Management
    var localMeasureState by remember { mutableStateOf(MeasureState()) }
    val isSharedMeasureState = sharedMeasureState != null
    val currentMeasureState = if (isSharedMeasureState) sharedMeasureState else localMeasureState
    val updateMeasureState: (MeasureState) -> Unit = { newState ->
        if (isSharedMeasureState)
            onMeasureRangeChange(newState)
        else
            localMeasureState = newState
    }

    val paddingPx = with(LocalDensity.current) { 16.dp.toPx() }

    // Functional reduction of data state
    val chartState by remember(analysis, currentVisibleRange) {
        derivedStateOf { calculateChartState(analysis, currentVisibleRange) }
    }

    // Sub-composable lambda
    val visibleRangeButton: @Composable (VisibleRange, String) -> Unit = { range, text ->
        IconButton(
            onClick = {
                updateVisibleRange(getVisibleRange(range, visibleRangeMarks))
            }) {
            Text(
                text = text,
                color = if (visibleRangeEnum == range) Color.Magenta else Color.Black
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .onSizeChanged { chartWidthPx = it.width.toFloat() }
        ) {
            // Toolbar Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    visibleRangeButton(VisibleRange.FIVE_YEAR, "5Y")
                    visibleRangeButton(VisibleRange.ONE_YEAR, "1Y")
                    visibleRangeButton(VisibleRange.SIX_MONTHS, "6M")
                    visibleRangeButton(VisibleRange.THREE_MONTHS, "3M")
                    visibleRangeButton(VisibleRange.ONE_MONTH, "1M")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CandleTimeframe.entries.forEach { timeframe ->
                        TextButton(onClick = { candleTimeframe = timeframe }) {
                            Text(
                                text = timeframe.label,
                                color = if (candleTimeframe == timeframe) Color.Magenta else Color.Black
                            )
                        }
                    }
                }
                Row {
                    IconButton(onClick = { isDrawingMode = !isDrawingMode }) {
                        Icon(
                            Icons.Default.Edit, "Edit",
                            tint = if (isDrawingMode) Color.Magenta else Color.Black
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = {
                        isMeasuringMode = !isMeasuringMode
                        if (isMeasuringMode)
                            isDrawingMode = false
                    }) {
                        Text(
                            "%",
                            style = MaterialTheme.typography.h6,
                            color = if (isMeasuringMode) Color.Magenta else Color.Black
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = {
                        trendLines.clear()
                        updateMeasureState(MeasureState())
                    }) {
                        Icon(
                            Icons.Default.Refresh,
                            "Clear"
                        )
                    }
                }
            }

            // Main Chart Canvas Area
            Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp)) {
                chartState?.let { activeState ->
                    CandlestickChart(
                        candles = analysis.candles,
                        chartState = activeState,
                        visibleRange = currentVisibleRange,
                        isDrawingMode = isDrawingMode,
                        trendLines = trendLines,
                        onLineAdded = { newLine -> trendLines.add(newLine); isDrawingMode = false },
                        interactiveModifier = Modifier
                            .chartDrag(
                                chartWidthPx = chartWidthPx,
                                visibleRange = currentVisibleRange,
                                totalCount = totalCount,
                                onRangeChange = updateVisibleRange,
                                isDrawingMode = isDrawingMode || isMeasuringMode
                            )
                            .chartZoom(
                                visibleRange = currentVisibleRange,
                                totalCount = totalCount,
                                onRangeChange = updateVisibleRange
                            )
                            .chartMeasure(
                                isMeasuringMode = isMeasuringMode,
                                chartState = activeState,
                                visibleRange = currentVisibleRange,
                                paddingPx = paddingPx,
                                onMeasureStateChanged = updateMeasureState
                            ),
                        modifier = Modifier.fillMaxSize(),
                        measureStartIdx = currentMeasureState.startIdx,
                        measureEndIdx = currentMeasureState.endIdx,
                        isMeasuringDragActive = currentMeasureState.isDragging,
                        smaVisible = smaVisible,
                        emaVisible = emaVisible
                    )
                }
            }

            // Extracted functional slider controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IndicatorControlRow(
                    "SMA",
                    smaPeriod, { smaPeriod = it },
                    smaVisible, { smaVisible = it },
                    5f..200f,
                    Color(0xFFFFA726)
                )
                Spacer(Modifier.width(16.dp))
                IndicatorControlRow(
                    "EMA",
                    emaPeriod, { emaPeriod = it },
                    emaVisible, { emaVisible = it },
                    5f..200f,
                    Color(0xFF42A5F5)
                )
            }

            IndicatorControlRow(
                "RSI",
                rsiPeriod, { rsiPeriod = it },
                rsiVisible, { rsiVisible = it },
                2f..50f
            )

            // Bottom RSI panel
            if (rsiVisible) {
                RsiPanel(rsiData = analysis.rsi, range = currentVisibleRange)
            }

            StockSummary(analysis = analysis)
        }
    }
}

