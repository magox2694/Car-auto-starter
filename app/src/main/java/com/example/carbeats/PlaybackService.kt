package com.example.carbeats

import android.content.Intent
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

private typealias LibrarySession = MediaLibraryService.MediaLibrarySession
private typealias LibrarySessionBuilder = MediaLibraryService.MediaLibrarySession.Builder
private typealias LibrarySessionCallback = MediaLibraryService.MediaLibrarySession.Callback

class PlaybackService : MediaLibraryService() {

    private val rootItemId = "root"

    private var player: ExoPlayer? = null
    private var mediaLibrarySession: LibrarySession? = null

    override fun onCreate() {
        super.onCreate()

        val exoPlayer = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()

        player = exoPlayer
        mediaLibrarySession = LibrarySessionBuilder(
            this,
            exoPlayer,
            object : LibrarySessionCallback {
                override fun onGetLibraryRoot(
                    session: LibrarySession,
                    browser: MediaSession.ControllerInfo,
                    params: LibraryParams?
                ): ListenableFuture<LibraryResult<MediaItem>> {
                    val root = MediaItem.Builder()
                        .setMediaId(rootItemId)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle("CarBeats")
                                .setIsBrowsable(true)
                                .setIsPlayable(false)
                                .build()
                        )
                        .build()

                    return Futures.immediateFuture(LibraryResult.ofItem(root, params))
                }

                override fun onGetChildren(
                    session: LibrarySession,
                    browser: MediaSession.ControllerInfo,
                    parentId: String,
                    page: Int,
                    pageSize: Int,
                    params: LibraryParams?
                ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
                    if (parentId != rootItemId) {
                        return Futures.immediateFuture(
                            LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
                        )
                    }

                    val allItems = AudioCatalog.allTracks().map { track ->
                        MediaItem.Builder()
                            .setMediaId(track.mediaId)
                            .setUri(track.url)
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle(track.title)
                                    .setArtist(track.artist)
                                    .setAlbumTitle(track.album)
                                    .setIsBrowsable(false)
                                    .setIsPlayable(true)
                                    .build()
                            )
                            .build()
                    }

                    val safePageSize = if (pageSize <= 0) allItems.size else pageSize
                    val fromIndex = page * safePageSize
                    if (fromIndex >= allItems.size) {
                        return Futures.immediateFuture(
                            LibraryResult.ofItemList(ImmutableList.of(), params)
                        )
                    }

                    val toIndex = minOf(fromIndex + safePageSize, allItems.size)
                    val slice = allItems.subList(fromIndex, toIndex)
                    return Futures.immediateFuture(
                        LibraryResult.ofItemList(ImmutableList.copyOf(slice), params)
                    )
                }

                override fun onGetItem(
                    session: LibrarySession,
                    browser: MediaSession.ControllerInfo,
                    mediaId: String
                ): ListenableFuture<LibraryResult<MediaItem>> {
                    val track = AudioCatalog.allTracks().firstOrNull { it.mediaId == mediaId }
                        ?: return Futures.immediateFuture(
                            LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
                        )

                    val item = MediaItem.Builder()
                        .setMediaId(track.mediaId)
                        .setUri(track.url)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(track.title)
                                .setArtist(track.artist)
                                .setAlbumTitle(track.album)
                                .setIsBrowsable(false)
                                .setIsPlayable(true)
                                .build()
                        )
                        .build()

                    return Futures.immediateFuture(LibraryResult.ofItem(item, null))
                }
            }
        )
            .setId("carbeats-session")
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val superResult = super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_PLAY_SAMPLE -> playSample()
            ACTION_PLAY_DEMO_PLAYLIST -> playDemoPlaylist()
            ACTION_PLAY_QUERY -> playByQuery(intent.getStringExtra(EXTRA_QUERY))
            ACTION_PLAY_MEDIA_ID -> playByMediaId(intent.getStringExtra(EXTRA_MEDIA_ID))
            ACTION_PLAY_STREAM -> playStream(
                url = intent.getStringExtra(EXTRA_STREAM_URL),
                title = intent.getStringExtra(EXTRA_TITLE),
                artist = intent.getStringExtra(EXTRA_ARTIST),
                album = intent.getStringExtra(EXTRA_ALBUM),
                mediaId = intent.getStringExtra(EXTRA_STREAM_MEDIA_ID)
            )
            ACTION_NEXT -> player?.seekToNextMediaItem()
            ACTION_PREVIOUS -> player?.seekToPreviousMediaItem()
            ACTION_PAUSE -> player?.pause()
            ACTION_RESUME -> if ((player?.mediaItemCount ?: 0) > 0) player?.play()
            ACTION_STOP -> {
                player?.stop()
                stopSelf()
            }
            else -> return superResult
        }

        return START_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): LibrarySession? {
        return mediaLibrarySession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if ((player?.playWhenReady == false) || (player?.mediaItemCount == 0)) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaLibrarySession?.run {
            player.release()
            release()
        }
        mediaLibrarySession = null
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

    private fun playByMediaId(mediaId: String?) {
        val exoPlayer = player ?: return
        val id = mediaId ?: return
        val startIndex = AudioCatalog.indexByMediaId(id)
        if (startIndex < 0) return
        exoPlayer.setMediaItems(AudioCatalog.demoPlaylist(), startIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    private fun playStream(
        url: String?,
        title: String?,
        artist: String?,
        album: String?,
        mediaId: String?
    ) {
        val exoPlayer = player ?: return
        val streamUrl = StreamUrlValidator.normalizeHttpUrl(url) ?: return

        val metadata = MediaMetadata.Builder()
            .setTitle(title ?: "Track")
            .setArtist(artist ?: "Unknown")
            .setAlbumTitle(album ?: "Unknown")
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(streamUrl)
            .setMediaId(mediaId ?: streamUrl)
            .setMediaMetadata(metadata)
            .build()

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    companion object {
        const val ACTION_PLAY_SAMPLE = "com.example.carbeats.action.PLAY_SAMPLE"
        const val ACTION_PLAY_DEMO_PLAYLIST = "com.example.carbeats.action.PLAY_DEMO_PLAYLIST"
        const val ACTION_PLAY_QUERY = "com.example.carbeats.action.PLAY_QUERY"
        const val ACTION_PLAY_MEDIA_ID = "com.example.carbeats.action.PLAY_MEDIA_ID"
        const val ACTION_PLAY_STREAM = "com.example.carbeats.action.PLAY_STREAM"
        const val ACTION_NEXT = "com.example.carbeats.action.NEXT"
        const val ACTION_PREVIOUS = "com.example.carbeats.action.PREVIOUS"
        const val ACTION_PAUSE = "com.example.carbeats.action.PAUSE"
        const val ACTION_RESUME = "com.example.carbeats.action.RESUME"
        const val ACTION_STOP = "com.example.carbeats.action.STOP"
        const val EXTRA_QUERY = "extra_query"
        const val EXTRA_MEDIA_ID = "extra_media_id"
        const val EXTRA_STREAM_URL = "extra_stream_url"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_ALBUM = "extra_album"
        const val EXTRA_STREAM_MEDIA_ID = "extra_stream_media_id"
    }
}
