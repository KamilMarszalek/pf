package ui

import analysis.CandleTimeframe
import data.TrendLine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class StockChartUiStateTest {

    @Test
    fun `default active tool should be pan`() {
        assertEquals(ChartTool.PAN, StockChartUiState().activeTool)
    }

    @Test
    fun `should update indicator periods and timeframe with copy based state`() {
        val state = StockChartUiState()
            .let { reduceStockChartState(it, StockChartAction.SetCandleTimeframe(CandleTimeframe.WEEKLY)) }
            .let { reduceStockChartState(it, StockChartAction.SetSmaPeriod(30)) }
            .let { reduceStockChartState(it, StockChartAction.SetEmaPeriod(40)) }
            .let { reduceStockChartState(it, StockChartAction.SetRsiPeriod(10)) }

        assertEquals(CandleTimeframe.WEEKLY, state.candleTimeframe)
        assertEquals(30, state.smaPeriod)
        assertEquals(40, state.emaPeriod)
        assertEquals(10, state.rsiPeriod)
    }

    @Test
    fun `should update visibility flags`() {
        val state = StockChartUiState()
            .let { reduceStockChartState(it, StockChartAction.SetSmaVisible(false)) }
            .let { reduceStockChartState(it, StockChartAction.SetEmaVisible(false)) }
            .let { reduceStockChartState(it, StockChartAction.SetRsiVisible(false)) }

        assertFalse(state.smaVisible)
        assertFalse(state.emaVisible)
        assertFalse(state.rsiVisible)
    }

    @Test
    fun `toggle drawing tool should switch between pan and drawing`() {
        val drawingState = reduceStockChartState(StockChartUiState(), StockChartAction.ToggleDrawingTool)
        val panState = reduceStockChartState(drawingState, StockChartAction.ToggleDrawingTool)

        assertEquals(ChartTool.DRAW_TREND_LINE, drawingState.activeTool)
        assertEquals(ChartTool.PAN, panState.activeTool)
    }

    @Test
    fun `toggle measure tool should switch between pan and measure`() {
        val measureState = reduceStockChartState(StockChartUiState(), StockChartAction.ToggleMeasureTool)
        val panState = reduceStockChartState(measureState, StockChartAction.ToggleMeasureTool)

        assertEquals(ChartTool.MEASURE, measureState.activeTool)
        assertEquals(ChartTool.PAN, panState.activeTool)
    }

    @Test
    fun `toggle drawing tool should switch from measure to drawing`() {
        val measureState = StockChartUiState(activeTool = ChartTool.MEASURE)
        val drawingState = reduceStockChartState(measureState, StockChartAction.ToggleDrawingTool)

        assertEquals(ChartTool.DRAW_TREND_LINE, drawingState.activeTool)
    }

    @Test
    fun `toggle measure tool should switch from drawing to measure`() {
        val drawingState = StockChartUiState(activeTool = ChartTool.DRAW_TREND_LINE)
        val measureState = reduceStockChartState(drawingState, StockChartAction.ToggleMeasureTool)

        assertEquals(ChartTool.MEASURE, measureState.activeTool)
    }

    @Test
    fun `should append trend line immutably and leave old state unchanged`() {
        val oldState = StockChartUiState(activeTool = ChartTool.DRAW_TREND_LINE)
        val line = trendLine()

        val newState = reduceStockChartState(oldState, StockChartAction.AddTrendLine(line))

        assertEquals(emptyList(), oldState.trendLines)
        assertEquals(listOf(line), newState.trendLines)
        assertEquals(ChartTool.PAN, newState.activeTool)
    }

    @Test
    fun `should clear trend lines without changing other state`() {
        val line = trendLine()
        val oldState = StockChartUiState(
            candleTimeframe = CandleTimeframe.MONTHLY,
            trendLines = listOf(line),
            activeTool = ChartTool.MEASURE,
        )

        val newState = reduceStockChartState(oldState, StockChartAction.ClearTrendLines)

        assertEquals(emptyList(), newState.trendLines)
        assertEquals(CandleTimeframe.MONTHLY, newState.candleTimeframe)
        assertEquals(ChartTool.MEASURE, newState.activeTool)
    }

    private fun trendLine() =
        TrendLine(
            startIndex = 1,
            startPrice = 10.0,
            endIndex = 2,
            endPrice = 12.0,
        )
}

