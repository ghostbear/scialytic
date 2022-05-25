package util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.toByteArray
import org.jetbrains.skia.Image

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