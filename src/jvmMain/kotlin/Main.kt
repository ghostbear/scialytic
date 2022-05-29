import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import di.DaggerViewModelComponent
import net.arikia.dev.drpc.DiscordEventHandlers
import net.arikia.dev.drpc.DiscordRPC
import ui.main.MainScreen

fun main() {
    val handlers = DiscordEventHandlers.Builder()
        .setReadyEventHandler { user ->
            println("Welcome ${user.username}#${user.discriminator}!")
        }
        .build()
    DiscordRPC.discordInitialize("980464950420062268", handlers, true)

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

