import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import analysis.CandleTimeframe
import data.StockProvider
import data.createHttpClient
import ui.Analyzer
import ui.MeasureState
import ui.activeRangeUtils.VisibleDateRange

@Composable
fun App() {
    val stockProvider = remember { StockProvider(createHttpClient(), AppConfig.API_KEY) }
    val scope = rememberCoroutineScope()

    var comparingMode by remember { mutableStateOf(false) }

    var sharedVisibleDateRange by remember { mutableStateOf<VisibleDateRange?>(null) }
    var sharedCandleTimeframe by remember { mutableStateOf(CandleTimeframe.DAILY) }
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
                Analyzer(
                    stockProvider,
                    scope,
                    sharedVisibleRange = null,
                    onVisibleRangeChange = {},
                    sharedVisibleDateRange = null,
                    onVisibleDateRangeChange = {},
                    sharedCandleTimeframe = null,
                    onCandleTimeframeChange = {},
                    showRangeControls = true,
                    showTimeframeControls = true,
                    sharedMeasureState = null,
                    onMeasureStateChange = {}
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Analyzer(
                            stockProvider,
                            scope,
                            sharedVisibleRange = null,
                            onVisibleRangeChange = {},
                            sharedVisibleDateRange = sharedVisibleDateRange,
                            onVisibleDateRangeChange = { sharedVisibleDateRange = it },
                            sharedCandleTimeframe = sharedCandleTimeframe,
                            onCandleTimeframeChange = { sharedCandleTimeframe = it },
                            showRangeControls = true,
                            showTimeframeControls = true,
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
                        Analyzer(
                            stockProvider,
                            scope,
                            sharedVisibleRange = null,
                            onVisibleRangeChange = {},
                            sharedVisibleDateRange = sharedVisibleDateRange,
                            onVisibleDateRangeChange = { sharedVisibleDateRange = it },
                            sharedCandleTimeframe = sharedCandleTimeframe,
                            onCandleTimeframeChange = { sharedCandleTimeframe = it },
                            showRangeControls = false,
                            showTimeframeControls = false,
                            sharedMeasureState = sharedMeasureState,
                            onMeasureStateChange = { sharedMeasureState = it }
                        )
                    }
                }
            }
        }
    }
}

