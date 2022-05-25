package socket.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class CurrentlyPlaying(
    @SerialName("song")
    val song: Song = Song(),
    @SerialName("requester")
    val requester: JsonObject? = null,
    @SerialName("event")
    val event: JsonObject? = null,
    @SerialName("startTime")
    val startTime: String = "",
    @SerialName("lastPlayed")
    val lastPlayed: List<LastPlayed> = listOf(),
    @SerialName("listeners")
    val listeners: Int = 0
)