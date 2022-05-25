package ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import audio.AudioManager
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import data.StreamSource
import data.websocket.WebSocket
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import util.loadItem

class MainViewModel @Inject constructor(
    private val webSocket: WebSocket,
    private val audioManager: AudioManager,
    private val playerManager: AudioPlayerManager,
) {

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    val currentlyPlaying = webSocket.currentlyPlaying

    var volume by mutableStateOf(audioManager.volume)
    var isPlaying by mutableStateOf(false)
    var source by mutableStateOf(StreamSource.J_POP)

    fun togglePlaying() {
        isPlaying = audioManager.toggle()
    }

    fun onChangeVolume() {
        audioManager.volume = volume
    }

    fun setStream(source: StreamSource) {
        this.source = source
        changeStream(source)
    }

    fun start() {
        changeStream(source)
    }

    private fun changeStream(source: StreamSource) {
        scope.launch {
            webSocket.connect(source.gateway)
        }
        playerManager.loadItem(
            identifier = source.url,
            onTrackLoaded = { audioManager.changeStream(it) }
        )
    }

}