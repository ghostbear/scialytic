package data.websocket.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LastPlayed(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("title")
    val title: String = "",
    @SerialName("sources")
    val sources: List<Source> = listOf(),
    @SerialName("artists")
    val artists: List<Artist> = listOf(),
    @SerialName("characters")
    val characters: List<Character> = listOf(),
    @SerialName("albums")
    val albums: List<Album> = listOf(),
    @SerialName("duration")
    val duration: Int = 0
)