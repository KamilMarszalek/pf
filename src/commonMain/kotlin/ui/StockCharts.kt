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
    val (uiState, dispatch) = rememberReducer(
        initialState = StockChartUiState(),
        reducer = ::reduceStockChartState
    )

    val displayCandles by remember(candles, uiState.candleTimeframe) {
        derivedStateOf { aggregateCandles(candles, uiState.candleTimeframe) }
    }
    val totalCount = displayCandles.size
    if (totalCount == 0) return

    // Pure business logic triggers
    val analysis by remember(displayCandles, uiState.smaPeriod, uiState.emaPeriod, uiState.rsiPeriod) {
        derivedStateOf {
            analyzeCandles(
                candles = displayCandles,
                smaPeriod = uiState.smaPeriod,
                emaPeriod = uiState.emaPeriod,
                rsiPeriod = uiState.rsiPeriod,
            )
        }
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
                .onSizeChanged { dispatch(StockChartAction.SetChartWidth(it.width.toFloat())) }
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
                        TextButton(onClick = { dispatch(StockChartAction.SetCandleTimeframe(timeframe)) }) {
                            Text(
                                text = timeframe.label,
                                color = if (uiState.candleTimeframe == timeframe) Color.Magenta else Color.Black
                            )
                        }
                    }
                }
                Row {
                    IconButton(onClick = { dispatch(StockChartAction.ToggleDrawingTool) }) {
                        Icon(
                            Icons.Default.Edit, "Edit",
                            tint = if (uiState.activeTool == ChartTool.DRAW_TREND_LINE) Color.Magenta else Color.Black
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = {
                        dispatch(StockChartAction.ToggleMeasureTool)
                    }) {
                        Text(
                            "%",
                            style = MaterialTheme.typography.h6,
                            color = if (uiState.activeTool == ChartTool.MEASURE) Color.Magenta else Color.Black
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = {
                        dispatch(StockChartAction.ClearTrendLines)
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
                    val activeInteractionModifier = when (uiState.activeTool) {
                        ChartTool.PAN -> Modifier
                            .chartDrag(
                                chartWidthPx = uiState.chartWidthPx,
                                visibleRange = currentVisibleRange,
                                totalCount = totalCount,
                                onRangeChange = updateVisibleRange,
                                isPanDisabled = false
                            )
                            .chartZoom(
                                visibleRange = currentVisibleRange,
                                totalCount = totalCount,
                                onRangeChange = updateVisibleRange
                            )

                        ChartTool.DRAW_TREND_LINE -> Modifier

                        ChartTool.MEASURE -> Modifier
                            .chartMeasure(
                                isMeasuringMode = true,
                                chartState = activeState,
                                visibleRange = currentVisibleRange,
                                paddingPx = paddingPx,
                                onMeasureStateChanged = updateMeasureState
                            )
                    }

                    CandlestickChart(
                        candles = analysis.candles,
                        chartState = activeState,
                        visibleRange = currentVisibleRange,
                        isDrawingMode = uiState.activeTool == ChartTool.DRAW_TREND_LINE,
                        trendLines = uiState.trendLines,
                        onLineAdded = { newLine -> dispatch(StockChartAction.AddTrendLine(newLine)) },
                        interactiveModifier = activeInteractionModifier,
                        modifier = Modifier.fillMaxSize(),
                        measureStartIdx = currentMeasureState.startIdx,
                        measureEndIdx = currentMeasureState.endIdx,
                        isMeasuringDragActive = currentMeasureState.isDragging,
                        smaVisible = uiState.smaVisible,
                        emaVisible = uiState.emaVisible
                    )
                }
            }

            // Extracted functional slider controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IndicatorControlRow(
                    "SMA",
                    uiState.smaPeriod, { dispatch(StockChartAction.SetSmaPeriod(it)) },
                    uiState.smaVisible, { dispatch(StockChartAction.SetSmaVisible(it)) },
                    5f..200f,
                    Color(0xFFFFA726)
                )
                Spacer(Modifier.width(16.dp))
                IndicatorControlRow(
                    "EMA",
                    uiState.emaPeriod, { dispatch(StockChartAction.SetEmaPeriod(it)) },
                    uiState.emaVisible, { dispatch(StockChartAction.SetEmaVisible(it)) },
                    5f..200f,
                    Color(0xFF42A5F5)
                )
            }

            IndicatorControlRow(
                "RSI",
                uiState.rsiPeriod, { dispatch(StockChartAction.SetRsiPeriod(it)) },
                uiState.rsiVisible, { dispatch(StockChartAction.SetRsiVisible(it)) },
                2f..50f
            )

            // Bottom RSI panel
            if (uiState.rsiVisible) {
                RsiPanel(rsiData = analysis.rsi, range = currentVisibleRange)
            }

            StockSummary(analysis = analysis)
        }
    }
}

