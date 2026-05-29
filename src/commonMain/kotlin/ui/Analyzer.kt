package ui

import analysis.exportAnalysisToCsv
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import data.ApiResult
import data.StockProvider
import file.SaveFileResult
import file.saveTextFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun Analyzer(
    stockProvider: StockProvider,
    scope: CoroutineScope,
    sharedVisibleRange: IntRange?,
    onVisibleRangeChange: (IntRange) -> Unit,
    sharedMeasureState: MeasureState?,
    onMeasureStateChange: (MeasureState) -> Unit,
) {
    val (uiState, dispatch) = rememberReducer(
        initialState = AnalyzerUiState(),
        reducer = ::reduceAnalyzerState,
    )

    suspend fun loadAnalysis(symbol: String) {
        dispatch(AnalyzerAction.StartLoading)

        val action = when (val result = stockProvider.fetchCandles(symbol)) {
            is ApiResult.Success ->
                AnalyzerAction.LoadSucceeded(symbol, result.data)

            is ApiResult.Failure ->
                AnalyzerAction.LoadFailed(result.message)
        }

        dispatch(action)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row {
            OutlinedTextField(
                value = uiState.ticker,
                onValueChange = { dispatch(AnalyzerAction.SetTicker(it)) },
                label = { Text("Ticker") },
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                enabled = uiState.appState !is AppState.Loading,
                onClick = {
                    val symbol = uiState.ticker.trim().uppercase()

                    if (symbol.isBlank()) {
                        dispatch(AnalyzerAction.ValidationFailed("Ticker cannot be empty"))
                        return@Button
                    }

                    scope.launch {
                        loadAnalysis(symbol)
                    }
                },
            ) {
                Text("Analyze")
            }

            Button(
                enabled = uiState.appState is AppState.Success && uiState.currentAnalysis != null,
                onClick = {
                    val successState = uiState.appState as? AppState.Success ?: return@Button
                    val analysis = uiState.currentAnalysis ?: return@Button

                    val csv = exportAnalysisToCsv(analysis)
                    val filename = "${successState.symbol}_analysis.csv"

                    val message = when (val result = saveTextFile(filename, csv)) {
                        is SaveFileResult.Success -> "CSV saved to ${result.path}"
                        SaveFileResult.Cancelled -> "CSV export canceled"
                        is SaveFileResult.Failure -> "CSV export failed: ${result.message}"
                    }

                    dispatch(AnalyzerAction.SetExportMessage(message))
                },
            ) {
                Text("Export to csv")
            }
        }

        uiState.exportMessage?.let {
            Text(text = it, color = Color.Blue)
        }

        when (val current = uiState.appState) {
            AppState.Idle ->
                Text("Enter ticker and click Analyze")

            AppState.Loading ->
                Text("Downloading candles...")

            is AppState.Error ->
                Text("Error: ${current.message}", color = Color.Red)

            is AppState.Success -> {
                StockCharts(
                    candles = current.candles,
                    onAnalysisReady = {
                        dispatch(AnalyzerAction.SetCurrentAnalysis(it))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    sharedVisibleRange = sharedVisibleRange,
                    onVisibleRangeChange = onVisibleRangeChange,
                    sharedMeasureState = sharedMeasureState,
                    onMeasureRangeChange = onMeasureStateChange,
                )
            }
        }
    }
}