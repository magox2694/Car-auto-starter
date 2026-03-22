package com.example.carbeats

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carbeats.search.TrackSearchResult
import coil.load

class PlayerActivity : ComponentActivity() {

    private var exoPlayer: ExoPlayer? = null
    private lateinit var suggestedAdapter: TrackResultsAdapter
    private var suggestedTracks: List<TrackSearchResult> = emptyList()
    private var selectedTrack: TrackSearchResult? = null
    private var currentTrack: TrackSearchResult? = null
    private var playbackPosition = 0L
    private var shouldAutoPlay = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val playerView = findViewById<PlayerView>(R.id.playerView)
        val artwork = findViewById<ImageView>(R.id.playerArtwork)
        val title = findViewById<TextView>(R.id.playerTitle)
        val subtitle = findViewById<TextView>(R.id.playerSubtitle)
        val suggestedRecycler = findViewById<RecyclerView>(R.id.suggestedRecycler)
        val playPauseButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.playerPlayPauseButton)
        val backButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.playerBackButton)
        val forwardButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.playerForwardButton)

        playbackPosition = savedInstanceState?.getLong(STATE_PLAYBACK_POSITION) ?: 0L
        shouldAutoPlay = savedInstanceState?.getBoolean(STATE_PLAY_WHEN_READY) ?: true
        currentTrack = savedInstanceState?.let {
            BundleCompat.getParcelable(it, STATE_CURRENT_TRACK, TrackSearchResult::class.java)
        }
        selectedTrack = currentTrack ?: IntentCompat.getParcelableExtra(
            intent,
            EXTRA_SELECTED_TRACK,
            TrackSearchResult::class.java
        )

        val suggestedTracks = savedInstanceState?.let {
            BundleCompat.getParcelableArrayList(it, STATE_SUGGESTED_TRACKS, TrackSearchResult::class.java)
        } ?: IntentCompat.getParcelableArrayListExtra(
            intent,
            EXTRA_SUGGESTED_TRACKS,
            TrackSearchResult::class.java
        ) ?: arrayListOf()
        this.suggestedTracks = suggestedTracks

        suggestedAdapter = TrackResultsAdapter { track ->
            bindHeader(track)
            playTrack(track, 0L, true)
        }

        suggestedRecycler.layoutManager = LinearLayoutManager(this)
        suggestedRecycler.adapter = suggestedAdapter
        suggestedAdapter.submitList(suggestedTracks)

        exoPlayer = ExoPlayer.Builder(this).build().also { player ->
            playerView.player = player
        }

        playPauseButton.setOnClickListener {
            val player = exoPlayer ?: return@setOnClickListener
            if (player.isPlaying) player.pause() else player.play()
        }
        backButton.setOnClickListener {
            val player = exoPlayer ?: return@setOnClickListener
            player.seekTo((player.currentPosition - 10_000L).coerceAtLeast(0L))
        }
        forwardButton.setOnClickListener {
            val player = exoPlayer ?: return@setOnClickListener
            player.seekTo(player.currentPosition + 10_000L)
        }

        val initial = currentTrack ?: selectedTrack ?: suggestedTracks.firstOrNull { it.playable }
            ?: suggestedTracks.firstOrNull()
        if (initial != null) {
            bindHeader(initial)
            playTrack(initial, playbackPosition, shouldAutoPlay)
        }

        artwork.load(initial?.artworkUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_foreground)
            error(R.drawable.ic_launcher_foreground)
        }
        title.text = initial?.title ?: getString(R.string.player_title_fallback)
        subtitle.text = if (initial != null) {
            "${initial.artist} • ${initial.album}"
        } else {
            getString(R.string.player_subtitle_fallback)
        }
    }

    private fun bindHeader(track: TrackSearchResult) {
        findViewById<ImageView>(R.id.playerArtwork).load(track.artworkUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_foreground)
            error(R.drawable.ic_launcher_foreground)
        }
        findViewById<TextView>(R.id.playerTitle).text = track.title
        findViewById<TextView>(R.id.playerSubtitle).text = "${track.artist} • ${track.album}"
    }

    private fun playTrack(track: TrackSearchResult) {
        playTrack(track, 0L, true)
    }

    private fun playTrack(track: TrackSearchResult, startPositionMs: Long, autoplay: Boolean) {
        currentTrack = track

        val url = StreamUrlValidator.normalizeHttpUrl(track.streamUrl)
        if (!track.playable || url == null) {
            exoPlayer?.run {
                stop()
                clearMediaItems()
            }
            playbackPosition = 0L
            shouldAutoPlay = false
            Toast.makeText(this, getString(R.string.selection_metadata_only), Toast.LENGTH_SHORT).show()
            return
        }

        val player = exoPlayer ?: return

        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(url)
            .setMediaMetadata(metadata)
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.seekTo(startPositionMs.coerceAtLeast(0L))
        player.playWhenReady = autoplay
        playbackPosition = startPositionMs.coerceAtLeast(0L)
        shouldAutoPlay = autoplay
    }

    override fun onStop() {
        exoPlayer?.let { player ->
            playbackPosition = player.currentPosition
            shouldAutoPlay = player.playWhenReady
            player.pause()
        }
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        exoPlayer?.let { player ->
            playbackPosition = player.currentPosition
            shouldAutoPlay = player.playWhenReady
        }
        currentTrack?.let { outState.putParcelable(STATE_CURRENT_TRACK, it) }
        outState.putParcelableArrayList(STATE_SUGGESTED_TRACKS, ArrayList(suggestedTracks))
        outState.putLong(STATE_PLAYBACK_POSITION, playbackPosition)
        outState.putBoolean(STATE_PLAY_WHEN_READY, shouldAutoPlay)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        exoPlayer?.release()
        exoPlayer = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_SELECTED_TRACK = "extra_selected_track"
        const val EXTRA_SUGGESTED_TRACKS = "extra_suggested_tracks"
        private const val STATE_CURRENT_TRACK = "state_current_track"
        private const val STATE_SUGGESTED_TRACKS = "state_suggested_tracks"
        private const val STATE_PLAYBACK_POSITION = "state_playback_position"
        private const val STATE_PLAY_WHEN_READY = "state_play_when_ready"
    }
}
