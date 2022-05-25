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
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.COMMON_PCM_S16_BE
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import dagger.Component
import dagger.Module
import dagger.Provides
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.toByteArray
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.skia.Image
import socket.Socket
import socket.model.artistNames
import socket.model.coverArt


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
    val playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    AudioSourceManagers.registerRemoteSources(playerManager)
    playerManager.configuration.outputFormat = COMMON_PCM_S16_BE;
    val audioPlayer: AudioPlayer = playerManager.createPlayer()
    val trackScheduler = TrackScheduler(audioPlayer)
    audioPlayer.addListener(trackScheduler)
    playerManager.loadItem("https://listen.moe/m3u8/jpop.m3u", object : AudioLoadResultHandler {
        override fun trackLoaded(track: AudioTrack) {
            trackScheduler.queue(track)
        }

        override fun playlistLoaded(playlist: AudioPlaylist) {

        }

        override fun noMatches() {

        }

        override fun loadFailed(exception: FriendlyException) {

        }

    })
    GlobalScope.launch(Dispatchers.IO) {
        val format: AudioDataFormat = playerManager.configuration.outputFormat
        val stream = AudioPlayerInputStream.createStream(audioPlayer, format, 10000L, false)
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
            val component = DaggerDataComponent.create()
            App(component.socket)
        }
    }
}

class TrackScheduler(
    private val audioPlayer: AudioPlayer
) : AudioEventAdapter() {
    private val queue: BlockingQueue<AudioTrack> = LinkedBlockingQueue()

    fun queue(audioTrack: AudioTrack) {
        if (!audioPlayer.startTrack(audioTrack, true)) {
            queue.offer(audioTrack)
        }
    }

    fun nextTrack() {
        audioPlayer.startTrack(queue.poll(), false)
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            nextTrack()
        }
    }
}

@Module
object DataModule {

    @Provides
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    @Provides
    fun provideHttpClient(json: Json): HttpClient {
        return HttpClient {
            install(WebSockets)
            install(ContentNegotiation) {
                json(json)
            }
        }
    }
}

@Component(modules = [DataModule::class])
interface DataComponent {
    val socket: Socket
}
