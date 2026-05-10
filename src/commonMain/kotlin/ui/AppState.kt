package ui

import data.Candle

sealed class AppState {
    data object Idle : AppState()
    data object Loading : AppState()
    data class Success(val symbol: String, val candles: List<Candle>) : AppState()
    data class Error(val message: String) : AppState()
}