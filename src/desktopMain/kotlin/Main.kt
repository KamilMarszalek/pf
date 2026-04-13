@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

actual object AppConfig {
    actual val API_KEY: String = System.getProperty("fmp.api.key") ?: ""
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "European stocks analyzer"
    ) {
        val keyStatus = if (AppConfig.API_KEY.isNotBlank()) "Loaded" else "API KEY missing"
        Text("Status API: $keyStatus")
        App()
    }
}