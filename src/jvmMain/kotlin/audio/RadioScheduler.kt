package audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RadioScheduler @Inject constructor(
    private val audioPlayer: AudioPlayer
) : AudioEventAdapter() {

    fun changeStream(audioTrack: AudioTrack) {
        audioPlayer.startTrack(audioTrack, false)
    }
}