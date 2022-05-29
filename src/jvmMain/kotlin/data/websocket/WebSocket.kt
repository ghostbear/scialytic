package data.websocket

import data.websocket.model.CurrentlyPlaying
import data.websocket.model.Message
import data.websocket.model.artistNames
import data.websocket.model.coverArt
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.wss
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import net.arikia.dev.drpc.DiscordRPC
import net.arikia.dev.drpc.DiscordRichPresence

class WebSocket @Inject constructor(
    private val json: Json,
    private val webSocketClient: HttpClient
) {

    private var heartbeat: Job? = null
    private var currentJob: Job? = null

    private val _currentlyPlaying: MutableSharedFlow<CurrentlyPlaying> = MutableSharedFlow()
    val currentlyPlaying: SharedFlow<CurrentlyPlaying> = _currentlyPlaying.asSharedFlow()

    suspend fun connect(urlString: String) {
        heartbeat?.cancel()
        currentJob?.cancel()
        webSocketClient.wss(urlString) {
            launch {
                try {
                    for (event in incoming) {
                        event as? Frame.Text ?: continue

                        val raw = event.readText()
                        println(raw)
                        val data = json.decodeFromString<JsonObject>(raw)
                        val op = data["op"]?.jsonPrimitive?.int ?: -1
                        when (op) {
                            0 -> {
                                val message = json.decodeFromJsonElement<Message>(data["d"]!!)
                                heartbeat?.cancel()
                                heartbeat = launch {
                                    while (true) {
                                        delay(message.heartbeat.toLong())
                                        println("Sending heartbeat")
                                        send(Frame.Text("{ \"op\": 9 }"))
                                    }
                                }
                            }
                            1 -> {
                                val currentlyPlaying = json.decodeFromJsonElement<CurrentlyPlaying>(data["d"]!!)
                                _currentlyPlaying.emit(currentlyPlaying)
                                val d = try {
                                    val startTime = DateTimeFormatter.ISO_INSTANT.parse(currentlyPlaying.startTime)
                                    val i: Instant = Instant.from(startTime)
                                    Date.from(i)
                                } catch (e: Exception) {
                                    Date()
                                }

                                DiscordRPC.discordUpdatePresence(
                                    DiscordRichPresence.Builder(currentlyPlaying.song.title).apply {
                                        setDetails(currentlyPlaying.song.artistNames)
                                        setBigImage(currentlyPlaying.song.coverArt, "Cover")
                                        setStartTimestamps(d.time)
                                        if (currentlyPlaying.song.duration > 0) {
                                            setEndTimestamp(d.time + (currentlyPlaying.song.duration * 1000))
                                        }
                                    }.build()
                                )
                            }
                            10 -> {
                                // Ack heartbeat
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("Error while receiving: " + e.localizedMessage)
                }
            }
            currentJob = launch {
                while (true) {
                    delay(1000)
                }
            }
            currentJob?.join()
        }
    }
}

