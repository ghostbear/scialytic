package audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackScheduler @Inject constructor(
    private val audioPlayer: AudioPlayer
) : AudioEventAdapter() {
    private val queue: BlockingQueue<AudioTrack> = LinkedBlockingQueue()

    fun queue(audioTrack: AudioTrack) {
        if (!audioPlayer.startTrack(audioTrack, true)) {
            queue.offer(audioTrack)
        }
    }

    fun nextTrack() {
        audioPlayer.startTrack(queue.poll(), false)
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            nextTrack()
        }
    }
}