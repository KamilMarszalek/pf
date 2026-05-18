import analysis.StockAnalysis
import analysis.exportAnalysisToCsv
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import data.ApiResult
import data.StockProvider
import data.createHttpClient
import file.SaveFileResult
import file.saveTextFile
import kotlinx.coroutines.launch
import ui.Analizer
import ui.AppState
import ui.StockCharts

@Composable
fun App() {
    val stockProvider = remember { StockProvider(createHttpClient(), AppConfig.API_KEY) }
    val scope = rememberCoroutineScope()

    var comparingMode by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material.Checkbox(
                    checked = comparingMode,
                    onCheckedChange = { comparingMode = it }
                )
                Text(
                    text = "Comparing mode",
                )
            }

            if (!comparingMode) {
                Analizer(stockProvider, scope)
            } else {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Analizer(stockProvider, scope)
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color.LightGray)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        Analizer(stockProvider, scope)
                    }
                }
            }
        }
    }
}

