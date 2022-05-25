package di

import dagger.Component
import dagger.Module
import dagger.Provides
import data.socket.Socket
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

@Module
object DataModule {

    @Provides
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    @Provides
    fun provideHttpClient(json: Json): HttpClient {
        return HttpClient {
            install(WebSockets)
            install(ContentNegotiation) {
                json(json)
            }
        }
    }
}

@Component(modules = [DataModule::class])
interface DataComponent {
    val socket: Socket
}