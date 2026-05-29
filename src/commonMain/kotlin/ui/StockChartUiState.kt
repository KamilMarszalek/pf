package ui

import analysis.CandleTimeframe
import data.TrendLine

data class StockChartUiState(
    val candleTimeframe: CandleTimeframe = CandleTimeframe.DAILY,
    val smaPeriod: Int = 20,
    val emaPeriod: Int = 20,
    val rsiPeriod: Int = 14,
    val smaVisible: Boolean = true,
    val emaVisible: Boolean = true,
    val rsiVisible: Boolean = true,
    val chartWidthPx: Float = 0f,
    val isDrawingMode: Boolean = false,
    val isMeasuringMode: Boolean = false,
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
    data object ToggleDrawingMode : StockChartAction
    data object ToggleMeasuringMode : StockChartAction
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
            isDrawingMode = false,
        )

        StockChartAction.ToggleDrawingMode -> {
            val drawingMode = !state.isDrawingMode
            state.copy(
                isDrawingMode = drawingMode,
                isMeasuringMode = if (drawingMode) false else state.isMeasuringMode,
            )
        }

        StockChartAction.ToggleMeasuringMode -> {
            val measuringMode = !state.isMeasuringMode
            state.copy(
                isMeasuringMode = measuringMode,
                isDrawingMode = if (measuringMode) false else state.isDrawingMode,
            )
        }

        StockChartAction.ClearTrendLines -> state.copy(trendLines = emptyList())
    }

