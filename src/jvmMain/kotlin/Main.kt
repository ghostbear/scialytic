import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import data.StreamSource
import data.socket.Socket
import data.socket.model.artistNames
import data.socket.model.coverArt
import di.DaggerAudioComponent
import di.DaggerDataComponent
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.toByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.skia.Image
import util.loadItem


@Composable
@Preview
fun App(socket: Socket, onClickPlayPause: () -> Unit, onChangeStreamSource: (StreamSource) -> Unit) {
    val currentlyPlaying by socket.currentlyPlaying.collectAsState(null)
    var source by remember { mutableStateOf(StreamSource.J_POP) }

    MaterialTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (currentlyPlaying != null) {
                val image by imageFromString(currentlyPlaying!!.song.coverArt)
                image?.let {
                    Image(
                        bitmap = it,
                        contentDescription = "",
                        modifier = Modifier.size(512.dp)
                    )
                }
                Text(
                    currentlyPlaying!!.song.title,
                    style = MaterialTheme.typography.h5
                )
                Text(
                    currentlyPlaying!!.song.artistNames,
                    style = MaterialTheme.typography.subtitle1
                )
                FloatingActionButton(
                    onClick = onClickPlayPause
                ) {
                    Icon(Icons.Filled.PlayArrow, "")
                }
                StreamSource.values().forEach {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = source == it,
                            onClick = {
                                source = it
                                onChangeStreamSource(it)
                            }
                        )
                        Text(it.displayName)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        onChangeStreamSource(source)
    }
}

@Composable
fun imageFromString(stringUrl: String): State<ImageBitmap?> {
    return produceState<ImageBitmap?>(null, stringUrl) {
        val httpClient = HttpClient()
        try {
            val bytes = httpClient.get(stringUrl).bodyAsChannel()
            value = Image.makeFromEncoded(bytes.toByteArray()).toComposeImageBitmap()

        } catch (e: Exception) {
            println("Couldn't load image: ${e.localizedMessage}")
        } finally {
            httpClient.close()
        }
    }
}

suspend fun main() {
    val dataComponent = DaggerDataComponent.create()
    val audioComponent = DaggerAudioComponent.create()

    val radioScheduler = audioComponent.radioScheduler
    val playerManager = audioComponent.playerManager
    val radioPlayer = audioComponent.radioPlayer

    audioComponent.audioPlayer.addListener(radioScheduler)
    radioPlayer.play()

    val scope = CoroutineScope(Job() + Dispatchers.IO)

    val socket = dataComponent.socket
    application {
        Window(
            title = "Scialytic",
            state = rememberWindowState(width = 512.dp, height = 800.dp),
            onCloseRequest = ::exitApplication,
            resizable = false
        ) {
            App(
                socket = socket,
                onClickPlayPause = {
                    radioPlayer.toggle()
                },
                onChangeStreamSource = { source ->
                    scope.launch {
                        socket.connect(source.gateway)
                    }
                    playerManager.loadItem(
                        identifier = source.url,
                        onTrackLoaded = { radioScheduler.changeStream(it) }
                    )
                }
            )
        }
    }
}


