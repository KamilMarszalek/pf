package ui

import analysis.CandleTimeframe
import data.TrendLine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StockChartUiStateTest {

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
    fun `should disable drawing mode when measuring mode is enabled`() {
        val drawingState = reduceStockChartState(
            StockChartUiState(),
            StockChartAction.ToggleDrawingMode,
        )

        val measuringState = reduceStockChartState(
            drawingState,
            StockChartAction.ToggleMeasuringMode,
        )

        assertTrue(drawingState.isDrawingMode)
        assertTrue(measuringState.isMeasuringMode)
        assertFalse(measuringState.isDrawingMode)
    }

    @Test
    fun `should append trend line immutably and leave old state unchanged`() {
        val oldState = StockChartUiState(isDrawingMode = true)
        val line = trendLine()

        val newState = reduceStockChartState(oldState, StockChartAction.AddTrendLine(line))

        assertEquals(emptyList(), oldState.trendLines)
        assertEquals(listOf(line), newState.trendLines)
        assertFalse(newState.isDrawingMode)
    }

    @Test
    fun `should clear trend lines without changing other state`() {
        val line = trendLine()
        val oldState = StockChartUiState(
            candleTimeframe = CandleTimeframe.MONTHLY,
            trendLines = listOf(line),
            isMeasuringMode = true,
        )

        val newState = reduceStockChartState(oldState, StockChartAction.ClearTrendLines)

        assertEquals(emptyList(), newState.trendLines)
        assertEquals(CandleTimeframe.MONTHLY, newState.candleTimeframe)
        assertTrue(newState.isMeasuringMode)
    }

    private fun trendLine() =
        TrendLine(
            startIndex = 1,
            startPrice = 10.0,
            endIndex = 2,
            endPrice = 12.0,
        )
}

