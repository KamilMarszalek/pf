package ui

import analysis.CandleTimeframe
import data.TrendLine

enum class ChartTool {
    PAN,
    DRAW_TREND_LINE,
    MEASURE,
}

data class StockChartUiState(
    val candleTimeframe: CandleTimeframe = CandleTimeframe.DAILY,
    val smaPeriod: Int = 20,
    val emaPeriod: Int = 20,
    val rsiPeriod: Int = 14,
    val smaVisible: Boolean = true,
    val emaVisible: Boolean = true,
    val rsiVisible: Boolean = true,
    val chartWidthPx: Float = 0f,
    val activeTool: ChartTool = ChartTool.PAN,
    val trendLines: List<TrendLine> = emptyList(),
)

sealed interface StockChartAction {
    data class SetCandleTimeframe(val timeframe: CandleTimeframe) : StockChartAction
    data class SetSmaPeriod(val period: Int) : StockChartAction
    data class SetEmaPeriod(val period: Int) : StockChartAction
    data class SetRsiPeriod(val period: Int) : StockChartAction
    data class SetSmaVisible(val visible: Boolean) : StockChartAction
    data class SetEmaVisible(val visible: Boolean) : StockChartAction
    data class SetRsiVisible(val visible: Boolean) : StockChartAction
    data class SetChartWidth(val widthPx: Float) : StockChartAction
    data class AddTrendLine(val line: TrendLine) : StockChartAction
    data object ToggleDrawingTool : StockChartAction
    data object ToggleMeasureTool : StockChartAction
    data object ClearTrendLines : StockChartAction
}

fun reduceStockChartState(
    state: StockChartUiState,
    action: StockChartAction,
): StockChartUiState =
    when (action) {
        is StockChartAction.SetCandleTimeframe -> state.copy(candleTimeframe = action.timeframe)
        is StockChartAction.SetSmaPeriod -> state.copy(smaPeriod = action.period)
        is StockChartAction.SetEmaPeriod -> state.copy(emaPeriod = action.period)
        is StockChartAction.SetRsiPeriod -> state.copy(rsiPeriod = action.period)
        is StockChartAction.SetSmaVisible -> state.copy(smaVisible = action.visible)
        is StockChartAction.SetEmaVisible -> state.copy(emaVisible = action.visible)
        is StockChartAction.SetRsiVisible -> state.copy(rsiVisible = action.visible)
        is StockChartAction.SetChartWidth -> state.copy(chartWidthPx = action.widthPx)
        is StockChartAction.AddTrendLine -> state.copy(
            trendLines = state.trendLines + action.line,
            activeTool = ChartTool.PAN,
        )

        StockChartAction.ToggleDrawingTool ->
            state.copy(
                activeTool = if (state.activeTool == ChartTool.DRAW_TREND_LINE) {
                    ChartTool.PAN
                } else {
                    ChartTool.DRAW_TREND_LINE
                }
            )

        StockChartAction.ToggleMeasureTool ->
            state.copy(
                activeTool = if (state.activeTool == ChartTool.MEASURE) {
                    ChartTool.PAN
                } else {
                    ChartTool.MEASURE
                }
            )

        StockChartAction.ClearTrendLines -> state.copy(trendLines = emptyList())
    }

