import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import data.ApiResult
import data.StockProvider
import data.StockQuote
import data.createHttpClient

@Composable
fun App() {
    val stockProvider = remember { StockProvider(createHttpClient()) }

    // Immutable state (on new state old one is overwritten)
    var state by remember { mutableStateOf<ApiResult<StockQuote>?>(null) }

    // Side effect
    LaunchedEffect(Unit) {
        state = stockProvider.fetchQuote("NVDA")
    }

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            when (val current = state) {
                null -> Text("Downloading data...")
                is ApiResult.Failure -> {
                    Text("Error: ${current.message}", color = Color.Red)
                    println(current.message)
                }
                is ApiResult.Success -> {
                    val stock = current.data
                    Text("Ticker: ${stock.symbol}", style = MaterialTheme.typography.h5)
                    Text("Price: ${stock.price}")
                    Text("Change percentage: ${stock.changePercentage}%",
                        color = if (stock.changePercentage >= 0) Color.Green else Color.Red)
                }
            }
        }
    }
}