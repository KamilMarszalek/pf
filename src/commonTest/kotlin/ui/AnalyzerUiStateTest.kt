package ui

import data.Candle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class AnalyzerUiStateTest {

    @Test
    fun `set ticker should uppercase value and clear export message`() {
        val state = reduceAnalyzerState(
            AnalyzerUiState(exportMessage = "saved"),
            AnalyzerAction.SetTicker("pko.wa"),
        )

        assertEquals("PKO.WA", state.ticker)
        assertNull(state.exportMessage)
    }

    @Test
    fun `start loading should clear export message and analysis`() {
        val state = reduceAnalyzerState(
            AnalyzerUiState(
                exportMessage = "saved",
                currentAnalysis = fakeAnalysis(),
            ),
            AnalyzerAction.StartLoading,
        )

        assertIs<AppState.Loading>(state.appState)
        assertNull(state.exportMessage)
        assertNull(state.currentAnalysis)
    }

    @Test
    fun `load success should store success app state and clear old analysis`() {
        val candles = listOf(candle())
        val state = reduceAnalyzerState(
            AnalyzerUiState(currentAnalysis = fakeAnalysis()),
            AnalyzerAction.LoadSucceeded("PKO.WA", candles),
        )

        val appState = assertIs<AppState.Success>(state.appState)
        assertEquals("PKO.WA", appState.symbol)
        assertEquals(candles, appState.candles)
        assertNull(state.currentAnalysis)
    }

    @Test
    fun `load failure should store error and clear analysis`() {
        val state = reduceAnalyzerState(
            AnalyzerUiState(currentAnalysis = fakeAnalysis()),
            AnalyzerAction.LoadFailed("Network error"),
        )

        val appState = assertIs<AppState.Error>(state.appState)
        assertEquals("Network error", appState.message)
        assertNull(state.currentAnalysis)
    }

    @Test
    fun `validation failure should store error and clear export message`() {
        val state = reduceAnalyzerState(
            AnalyzerUiState(exportMessage = "saved"),
            AnalyzerAction.ValidationFailed("Ticker cannot be empty"),
        )

        val appState = assertIs<AppState.Error>(state.appState)
        assertEquals("Ticker cannot be empty", appState.message)
        assertNull(state.exportMessage)
    }

    private fun fakeAnalysis() =
        analysis.StockAnalysis(
            candles = emptyList(),
            smaPeriod = 20,
            emaPeriod = 20,
            rsiPeriod = 14,
            sma = emptyList(),
            ema = emptyList(),
            rsi = emptyList(),
        )

    private fun candle() =
        Candle(
            date = "2026-05-01",
            open = 1.0,
            high = 1.0,
            low = 1.0,
            close = 1.0,
            volume = 1.0,
        )
}

