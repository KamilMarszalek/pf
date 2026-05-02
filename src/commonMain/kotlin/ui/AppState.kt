package ui

import analysis.StockAnalysis

sealed class AppState {
    data object Idle : AppState()
    data object Loading : AppState()
    data class Success(val symbol: String, val analysis: StockAnalysis) : AppState()
    data class Error(val message: String) : AppState()
}