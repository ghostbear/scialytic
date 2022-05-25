package util

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

fun AudioPlayerManager.loadItem(
    identifier: String,
    onTrackLoaded: (AudioTrack) -> Unit = {},
    onPlaylistLoaded: (AudioPlaylist) -> Unit = {},
    onNoMatches: () -> Unit = {},
    onLoadFailed: (FriendlyException) -> Unit = {},
) {
    loadItem(
        identifier,
        object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                onTrackLoaded(track)
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                onPlaylistLoaded(playlist)
            }

            override fun noMatches() {
                onNoMatches()
            }

            override fun loadFailed(exception: FriendlyException) {
                onLoadFailed(exception)
            }
        }
    )
}