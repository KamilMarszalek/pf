import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import data.StockProvider
import data.createHttpClient
import ui.Analizer
import ui.MeasureState

@Composable
fun App() {
    val stockProvider = remember { StockProvider(createHttpClient(), AppConfig.API_KEY) }
    val scope = rememberCoroutineScope()

    var comparingMode by remember { mutableStateOf(false) }

    var sharedVisibleRange by remember { mutableStateOf(IntRange.EMPTY) }
    var sharedMeasureState by remember { mutableStateOf(MeasureState()) }

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
                Analizer(
                    stockProvider,
                    scope,
                    sharedVisibleRange = null,
                    onVisibleRangeChange = {},
                    sharedMeasureState = null,
                    onMeasureStateChange = {}
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Analizer(
                            stockProvider,
                            scope,
                            sharedVisibleRange = sharedVisibleRange,
                            onVisibleRangeChange = { sharedVisibleRange = it },
                            sharedMeasureState = sharedMeasureState,
                            onMeasureStateChange = { sharedMeasureState = it }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color.LightGray)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        Analizer(
                            stockProvider,
                            scope,
                            sharedVisibleRange = sharedVisibleRange,
                            onVisibleRangeChange = { sharedVisibleRange = it },
                            sharedMeasureState = sharedMeasureState,
                            onMeasureStateChange = { sharedMeasureState = it }
                        )
                    }
                }
            }
        }
    }
}

