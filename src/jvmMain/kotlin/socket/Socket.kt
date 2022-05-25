package socket

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.plugins.websocket.wss
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import java.io.InputStream
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import socket.model.CurrentlyPlaying
import socket.model.Message

class Socket @Inject constructor(
    private val json: Json,
    private val webSocketClient: HttpClient
) {

    private val _currentlyPlaying: MutableSharedFlow<CurrentlyPlaying> = MutableSharedFlow()
    val currentlyPlaying: SharedFlow<CurrentlyPlaying> = _currentlyPlaying.asSharedFlow()

    suspend fun connect() {
        webSocketClient.wss("wss://listen.moe/gateway_v2") {
            var heartbeat: Job? = null
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
            launch {
                while (true) {
                    delay(1000)
                }
            }.join()
        }
    }
}

