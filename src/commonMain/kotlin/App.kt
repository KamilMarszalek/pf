import analysis.analyzeCandles
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import data.ApiResult
import data.Candle
import data.StockProvider
import data.StockQuote
import data.createHttpClient
import indicators.exponentialMovingAverage
import indicators.relativeStrengthIndex
import indicators.simpleMovingAverage
import ui.AppState

@Composable
fun App() {
    val stockProvider = remember { StockProvider(createHttpClient(), AppConfig.API_KEY) }

    // Immutable state (on new state old one is overwritten)
    var state by remember { mutableStateOf<AppState>(AppState.Idle) }
    // Side effect
    LaunchedEffect(Unit) {
        state = AppState.Loading
        state = when (val result = stockProvider.fetchCandles("AAPL")) {
            is ApiResult.Success -> AppState.Success(analyzeCandles(result.data))
            is ApiResult.Failure -> AppState.Error(result.message)
        }
    }

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
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

                    Text("Candles downloaded: ${candles.size}")
                    Text("Oldest candle date: ${oldestCandle?.date}")
                    Text("Latest candle date: ${latestCandle?.date}")
                    Text("Latest close: ${latestCandle?.close}")
                    Text("Latest SMA20: ${analysis.sma20.lastOrNull { it != null }}")
                    Text("Latest EMA20: ${analysis.ema20.lastOrNull { it != null }}")
                    Text("Latest RSI14: ${analysis.rsi14.lastOrNull { it != null }}")
                }
            }
        }
    }
}