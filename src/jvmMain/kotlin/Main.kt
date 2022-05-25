import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import audio.TrackScheduler
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.COMMON_PCM_S16_BE
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import data.socket.Socket
import data.socket.model.artistNames
import data.socket.model.coverArt
import di.DaggerAudioComponent
import di.DaggerDataComponent
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.toByteArray
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.skia.Image
import util.loadItem


@Composable
@Preview
fun App(socket: Socket) {
    val currentlyPlaying by socket.currentlyPlaying.collectAsState(null)
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
                    onClick = {}
                ) {
                    Icon(Icons.Filled.PlayArrow, "")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        socket.connect()
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

fun main() {
    val dataComponent = DaggerDataComponent.create()
    val audioComponent = DaggerAudioComponent.create()


    audioComponent.audioPlayer.addListener(audioComponent.trackScheduler)
    audioComponent.playerManager.loadItem(
        "https://listen.moe/opus",
        onTrackLoaded = { audioComponent.trackScheduler.queue(it) }
    )
    GlobalScope.launch(Dispatchers.IO) {
        val format: AudioDataFormat = audioComponent.playerManager.configuration.outputFormat
        val stream = AudioPlayerInputStream.createStream(audioComponent.audioPlayer, format, 10000L, false)
        val info = DataLine.Info(SourceDataLine::class.java, stream.format)
        val line = AudioSystem.getLine(info) as SourceDataLine

        line.open(stream.format)
        line.start()

        val buffer = ByteArray(COMMON_PCM_S16_BE.maximumChunkSize())
        var chunkSize: Int

        while (stream.read(buffer).also { chunkSize = it } >= 0) {
            line.write(buffer, 0, chunkSize)
        }
    }

    application {
        Window(
            title = "Scialytic",
            state = rememberWindowState(width = 512.dp, height = 800.dp),
            onCloseRequest = ::exitApplication,
            resizable = false
        ) {
            App(dataComponent.socket)
        }
    }
}


