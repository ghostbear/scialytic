package di

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object AudioModule {

    @Provides
    @Singleton
    fun providePlayerManager(): AudioPlayerManager {
        val playerManager = DefaultAudioPlayerManager()
        AudioSourceManagers.registerRemoteSources(playerManager)
        playerManager.configuration.outputFormat = StandardAudioDataFormats.COMMON_PCM_S16_BE
        return playerManager
    }

    @Provides
    @Singleton
    fun provideAudioPlayer(
        playerManager: AudioPlayerManager
    ): AudioPlayer {
        return playerManager.createPlayer()
    }

}
