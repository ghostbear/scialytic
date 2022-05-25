package data.websocket.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Artist(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("nameRomaji")
    val nameRomaji: String? = null,
    @SerialName("image")
    val image: String? = null,
    @SerialName("characters")
    val characters: List<Character> = listOf()
)