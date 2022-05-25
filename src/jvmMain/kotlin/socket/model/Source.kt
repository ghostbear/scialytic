package socket.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Source(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("nameRomaji")
    val nameRomaji: String? = "",
    @SerialName("image")
    val image: String? = ""
)