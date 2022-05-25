package audio

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import javax.inject.Inject
import javax.inject.Singleton
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class AudioManager @Inject constructor(
    private val playerManager: AudioPlayerManager,
    private val audioPlayer: AudioPlayer
) : AudioEventAdapter() {

    init {
        audioPlayer.addListener(this)
    }

    private var job: Job? = null
    private var line: SourceDataLine? = null

    fun changeStream(audioTrack: AudioTrack) {
        audioPlayer.startTrack(audioTrack, false)
    }

    override fun onTrackStart(player: AudioPlayer?, track: AudioTrack?) {
        openAudioOutput()
    }

    override fun onPlayerResume(player: AudioPlayer?) {
        openAudioOutput()
    }

    override fun onPlayerPause(player: AudioPlayer?) {
        closeAudioOutput()
    }

    override fun onTrackEnd(player: AudioPlayer?, track: AudioTrack?, endReason: AudioTrackEndReason?) {
        closeAudioOutput()
    }

    private fun openAudioOutput() {
        val format: AudioDataFormat = playerManager.configuration.outputFormat
        val stream = AudioPlayerInputStream.createStream(audioPlayer, format, 10000L, false)
        val info = DataLine.Info(SourceDataLine::class.java, stream.format)
        line = AudioSystem.getLine(info) as SourceDataLine

        line?.open(stream.format)
        line?.start()

        val buffer = ByteArray(StandardAudioDataFormats.COMMON_PCM_S16_BE.maximumChunkSize())
        var chunkSize: Int

        job = CoroutineScope(Job() + Dispatchers.IO).launch {
            withContext(Dispatchers.IO) {
                while (stream.read(buffer).also { chunkSize = it } >= 0) {
                    line?.write(buffer, 0, chunkSize)
                }
            }
        }
    }

    private fun closeAudioOutput() {
        job?.cancel()
        job = null
        line?.close()
    }

    fun toggle(): Boolean {
        audioPlayer.isPaused = !audioPlayer.isPaused
        return !audioPlayer.isPaused
    }

    var volume: Int = audioPlayer.volume
        set(value) {
            field = value
            audioPlayer.volume = value
        }

}