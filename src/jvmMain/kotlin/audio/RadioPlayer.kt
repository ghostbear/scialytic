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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Singleton
class RadioPlayer @Inject constructor(
    private val playerManager: AudioPlayerManager,
    private val audioPlayer: AudioPlayer
) {

    fun play() {

        GlobalScope.launch(Dispatchers.IO) {
            val format: AudioDataFormat = playerManager.configuration.outputFormat
            val stream = AudioPlayerInputStream.createStream(audioPlayer, format, 10000L, false)
            val info = DataLine.Info(SourceDataLine::class.java, stream.format)
            val line = AudioSystem.getLine(info) as SourceDataLine

            line.open(stream.format)
            line.start()

            val buffer = ByteArray(StandardAudioDataFormats.COMMON_PCM_S16_BE.maximumChunkSize())
            var chunkSize: Int

            while (stream.read(buffer).also { chunkSize = it } >= 0) {
                line.write(buffer, 0, chunkSize)
            }
        }

    }

}