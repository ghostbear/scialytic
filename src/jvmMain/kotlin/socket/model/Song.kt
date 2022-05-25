package socket.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Song(
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

val Song.coverArt: String
    get() {
        val image = albums.firstNotNullOfOrNull(Album::image) ?: return "https://listen.moe/_nuxt/img/blank-dark.cd1c044.png"
        return "https://cdn.listen.moe/covers/${image}"
    }

val Song.artistNames: String
    get() {
        return artists.joinToString(", ", transform = Artist::name)
    }