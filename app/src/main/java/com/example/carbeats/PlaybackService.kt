package com.example.carbeats

import android.content.Intent
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        val exoPlayer = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()

        player = exoPlayer
        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setId("carbeats-session")
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_SAMPLE -> playSample()
            ACTION_PLAY_DEMO_PLAYLIST -> playDemoPlaylist()
            ACTION_PLAY_QUERY -> playByQuery(intent.getStringExtra(EXTRA_QUERY))
            ACTION_NEXT -> player?.seekToNextMediaItem()
            ACTION_PREVIOUS -> player?.seekToPreviousMediaItem()
            ACTION_PAUSE -> player?.pause()
            ACTION_RESUME -> if (player?.mediaItemCount ?: 0 > 0) player?.play()
            ACTION_STOP -> {
                player?.stop()
                stopSelf()
            }
        }

        return START_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if ((player?.playWhenReady == false) || (player?.mediaItemCount == 0)) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        player = null
        super.onDestroy()
    }

    private fun playSample() {
        val exoPlayer = player ?: return
        exoPlayer.setMediaItems(AudioCatalog.demoPlaylist(), 0, 0L)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    private fun playDemoPlaylist() {
        val exoPlayer = player ?: return
        exoPlayer.setMediaItems(AudioCatalog.demoPlaylist(), 0, 0L)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    private fun playByQuery(query: String?) {
        val exoPlayer = player ?: return
        val searchText = query ?: return
        val startIndex = AudioCatalog.findIndex(searchText)
        exoPlayer.setMediaItems(AudioCatalog.demoPlaylist(), startIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    companion object {
        const val ACTION_PLAY_SAMPLE = "com.example.carbeats.action.PLAY_SAMPLE"
        const val ACTION_PLAY_DEMO_PLAYLIST = "com.example.carbeats.action.PLAY_DEMO_PLAYLIST"
        const val ACTION_PLAY_QUERY = "com.example.carbeats.action.PLAY_QUERY"
        const val ACTION_NEXT = "com.example.carbeats.action.NEXT"
        const val ACTION_PREVIOUS = "com.example.carbeats.action.PREVIOUS"
        const val ACTION_PAUSE = "com.example.carbeats.action.PAUSE"
        const val ACTION_RESUME = "com.example.carbeats.action.RESUME"
        const val ACTION_STOP = "com.example.carbeats.action.STOP"
        const val EXTRA_QUERY = "extra_query"
    }
}
