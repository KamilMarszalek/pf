@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

actual object AppConfig {
    actual val API_KEY: String = System.getProperty("fmp.api.key") ?: ""
}


fun main() = application {
    val windowState = rememberWindowState(
        size = DpSize(1200.dp, 900.dp)
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "European stocks analyzer",
        state = windowState,
    ) {
        window.minimumSize = java.awt.Dimension(900, 700)
        App()
    }
}