package data.websocket.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    @SerialName("message")
    val message: String = "",
    @SerialName("heartbeat")
    val heartbeat: Int = 0
)