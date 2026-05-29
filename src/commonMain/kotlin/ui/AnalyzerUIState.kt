package ui

import analysis.StockAnalysis
import data.Candle

data class AnalyzerUiState(
    val ticker: String = "",
    val exportMessage: String? = null,
    val appState: AppState = AppState.Idle,
    val currentAnalysis: StockAnalysis? = null,
)

sealed interface AnalyzerAction {
    data class SetTicker(val ticker: String) : AnalyzerAction
    data object StartLoading : AnalyzerAction
    data class LoadSucceeded(val symbol: String, val candles: List<Candle>) : AnalyzerAction
    data class LoadFailed(val message: String) : AnalyzerAction
    data class SetCurrentAnalysis(val analysis: StockAnalysis) : AnalyzerAction
    data class SetExportMessage(val message: String?) : AnalyzerAction
    data class ValidationFailed(val error: String) : AnalyzerAction
}

fun reduceAnalyzerState(
    state: AnalyzerUiState,
    action: AnalyzerAction,
): AnalyzerUiState =
    when (action) {
        is AnalyzerAction.SetTicker ->
            state.copy(
                ticker = action.ticker.uppercase(),
                exportMessage = null,
            )

        AnalyzerAction.StartLoading ->
            state.copy(
                appState = AppState.Loading,
                exportMessage = null,
                currentAnalysis = null,
            )

        is AnalyzerAction.LoadSucceeded ->
            state.copy(
                appState = AppState.Success(action.symbol, action.candles),
                exportMessage = null,
                currentAnalysis = null,
            )

        is AnalyzerAction.LoadFailed ->
            state.copy(
                appState = AppState.Error(action.message),
                currentAnalysis = null,
            )

        is AnalyzerAction.SetCurrentAnalysis ->
            state.copy(currentAnalysis = action.analysis)

        is AnalyzerAction.SetExportMessage ->
            state.copy(exportMessage = action.message)

        is AnalyzerAction.ValidationFailed ->
            state.copy(
                appState = AppState.Error(action.error),
                exportMessage = null,
                currentAnalysis = null,
            )
    }