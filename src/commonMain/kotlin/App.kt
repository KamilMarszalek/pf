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
import indicators.simpleMovingAverage

@Composable
fun App() {
    val stockProvider = remember { StockProvider(createHttpClient(), AppConfig.API_KEY) }

    // Immutable state (on new state old one is overwritten)
    var state by remember { mutableStateOf<ApiResult<List<Candle>>?>(null) }

    // Side effect
    LaunchedEffect(Unit) {
        state = stockProvider.fetchCandles("AAPL")
    }

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            when (val current = state) {
                null -> Text("Downloading candles...")
                is ApiResult.Failure -> {
                    Text("Error: ${current.message}", color = Color.Red)
                    println(current.message)
                }
                is ApiResult.Success -> {
                    val candles = current.data
                    val closes = candles.map {it.close}
                    val sma20 = simpleMovingAverage(closes, 20)
                    Text("Candles downloaded: ${candles.size}")
                    Text("First candle date: ${candles.firstOrNull()?.date}")
                    Text("Latest close: ${candles.firstOrNull()?.close}")
                    Text("Latest SMA20: ${sma20.firstOrNull() {it != null}}")

                }
            }
        }
    }
}