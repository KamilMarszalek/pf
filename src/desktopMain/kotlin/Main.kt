import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

// Czysta funkcja UI (Pure Function w kontekście Compose)
@Composable
fun App() {
    MaterialTheme {
        Text("European stocks analizer - KMP Desktop")
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Technical Analysis Tool"
    ) {
        App()
    }
}