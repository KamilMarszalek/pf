import analysis.analyzeCandles
import analysis.exportAnalysisToCsv
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.Button
import androidx.compose.ui.unit.dp
import data.ApiResult
import data.StockProvider
import data.createHttpClient
import file.SaveFileResult
import file.saveTextFile
import kotlinx.coroutines.launch
import ui.AppState

@Composable
fun App() {
    var ticker by remember { mutableStateOf("")}
    val stockProvider = remember { StockProvider(createHttpClient(), AppConfig.API_KEY) }
    val scope = rememberCoroutineScope()
    var exportMessage by remember { mutableStateOf<String?>(null) }
    // Immutable state (on new state old one is overwritten)
    var state by remember { mutableStateOf<AppState>(AppState.Idle) }

    suspend fun loadAnalysis(symbol: String) {
        state = AppState.Loading

        state = when (val result = stockProvider.fetchCandles(symbol)) {
            is ApiResult.Success -> AppState.Success(symbol,analyzeCandles(result.data))
            is ApiResult.Failure -> AppState.Error(result.message)
        }
    }
//    // Side effect
//    LaunchedEffect(Unit) {
//        loadAnalysis(ticker)
//    }

    fun formatNullable(value: Double?): String = value?.let {"%.2f".format(it)} ?: "-"
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                OutlinedTextField(
                    value = ticker,
                    onValueChange = { ticker = it.uppercase() },
                    label = { Text("Ticker") },
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    enabled = state !is AppState.Loading,
                    onClick = {
                        val symbol = ticker.trim().uppercase()

                        if (symbol.isBlank()) {
                            state = AppState.Error("Ticker cannot be empty")
                            return@Button
                        }

                        scope.launch {
                            loadAnalysis(symbol)
                        }
                    }
                ) {
                    Text("Analyze")
                }
                Button(
                    enabled = state is AppState.Success,
                    onClick = {
                        val successState = state as? AppState.Success ?: return@Button
                        val csv = exportAnalysisToCsv(successState.analysis)
                        val filename = "${successState.symbol}_analysis.csv"

                        exportMessage = when (val result = saveTextFile(filename, csv)) {
                            is SaveFileResult.Success -> "CSV saved to ${result.path}"
                            SaveFileResult.Cancelled -> "CSV export canceled"
                            is SaveFileResult.Failure -> "CSV export failed: ${result.message}"
                        }
                    }
                ) {
                    Text("Export to csv")
                }
            }
            exportMessage?.let {
                Text(text = it, color = Color.Blue)
            }
            when (val current = state) {
                AppState.Idle -> Text("Enter ticker and click Analyze")
                AppState.Loading -> Text("Downloading candles...")

                is AppState.Error -> {
                    Text("Error: ${current.message}", color = Color.Red)
                }

                is AppState.Success -> {
                    val analysis = current.analysis
                    val candles = analysis.candles

                    val oldestCandle = candles.firstOrNull()
                    val latestCandle = candles.lastOrNull()

                    Text("Ticker: ${current.symbol}", color = Color.Blue)
                    Text("Candles downloaded: ${candles.size}")
                    Text("Oldest candle date: ${oldestCandle?.date}")
                    Text("Latest candle date: ${latestCandle?.date}")
                    Text("Latest close: ${formatNullable(latestCandle?.close)}")
                    Text("Latest SMA20: ${formatNullable(analysis.sma20.lastOrNull { it != null })}")
                    Text("Latest EMA20: ${formatNullable(analysis.ema20.lastOrNull { it != null })}")
                    Text("Latest RSI14: ${formatNullable(analysis.rsi14.lastOrNull { it != null })}")
                }
            }
        }
    }
}

