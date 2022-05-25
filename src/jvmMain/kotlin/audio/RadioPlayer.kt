package audio

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import javax.inject.Inject
import javax.inject.Singleton
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Singleton
class RadioPlayer @Inject constructor(
    private val playerManager: AudioPlayerManager,
    private val audioPlayer: AudioPlayer
) {

    private var job: Job? = null
    private var line: SourceDataLine? = null

    fun isPlaying(): Boolean {
        return job != null
    }

    fun play() {
        val format: AudioDataFormat = playerManager.configuration.outputFormat
        val stream = AudioPlayerInputStream.createStream(audioPlayer, format, 10000L, false)
        val info = DataLine.Info(SourceDataLine::class.java, stream.format)
        line = AudioSystem.getLine(info) as SourceDataLine

        line?.open(stream.format)
        line?.start()

        val buffer = ByteArray(StandardAudioDataFormats.COMMON_PCM_S16_BE.maximumChunkSize())
        var chunkSize: Int

        job = CoroutineScope(Job() + Dispatchers.IO).launch {
            while (stream.read(buffer).also { chunkSize = it } >= 0) {
                line?.write(buffer, 0, chunkSize)
            }
        }
    }

    fun pause() {
        job?.cancel()
        job = null
        line?.close()
    }

}