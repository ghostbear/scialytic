package data.socket.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Album(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("nameRomaji")
    val nameRomaji: String? = null,
    @SerialName("image")
    val image: String? = ""
)