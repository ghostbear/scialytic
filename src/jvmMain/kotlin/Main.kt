import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import di.DaggerViewModelComponent
import ui.main.MainScreen

fun main() {
    application {
        Window(
            title = "Scialytic",
            state = rememberWindowState(width = 512.dp, height = 800.dp),
            onCloseRequest = ::exitApplication,
            resizable = false
        ) {
            val viewModelComponent = DaggerViewModelComponent.create()
            MainScreen(viewModelComponent.mainViewModel)
        }
    }
}

