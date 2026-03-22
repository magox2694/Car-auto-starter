package com.example.carbeats

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.IntentCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import android.webkit.WebChromeClient
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.carbeats.search.TrackSearchResult

class PlayerActivity : ComponentActivity() {

    private var exoPlayer: ExoPlayer? = null
    private var youtubeWebView: WebView? = null
    private lateinit var suggestedAdapter: TrackResultsAdapter
    private var suggestedTracks: List<TrackSearchResult> = emptyList()
    private var selectedTrack: TrackSearchResult? = null
    private var currentTrack: TrackSearchResult? = null
    private var isYoutubeMode = false
    private val blockedYoutubeIds = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val playerView = findViewById<PlayerView>(R.id.playerView)
        val webView = findViewById<WebView>(R.id.youtubeWebView)
        youtubeWebView = webView
        val artwork = findViewById<ImageView>(R.id.playerArtwork)
        val title = findViewById<TextView>(R.id.playerTitle)
        val subtitle = findViewById<TextView>(R.id.playerSubtitle)
        val suggestedRecycler = findViewById<RecyclerView>(R.id.suggestedRecycler)
        val playPauseButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.playerPlayPauseButton)
        val backButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.playerBackButton)
        val forwardButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.playerForwardButton)

        selectedTrack = IntentCompat.getParcelableExtra(
            intent,
            EXTRA_SELECTED_TRACK,
            TrackSearchResult::class.java
        )

        val suggestedTracks = IntentCompat.getParcelableArrayListExtra(
            intent,
            EXTRA_SUGGESTED_TRACKS,
            TrackSearchResult::class.java
        ) ?: arrayListOf()
        this.suggestedTracks = suggestedTracks

        suggestedAdapter = TrackResultsAdapter { track ->
            playTrack(track)
            bindHeader(track)
        }

        suggestedRecycler.layoutManager = LinearLayoutManager(this)
        suggestedRecycler.adapter = suggestedAdapter
        suggestedAdapter.submitList(suggestedTracks)

        exoPlayer = ExoPlayer.Builder(this).build().also { player ->
            playerView.player = player
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.addJavascriptInterface(YouTubeEmbedBridge(), "AndroidBridge")
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()

        playPauseButton.setOnClickListener {
            if (isYoutubeMode) {
                sendYoutubeCommand("togglePlayPause")
            } else {
                val player = exoPlayer ?: return@setOnClickListener
                if (player.isPlaying) player.pause() else player.play()
            }
        }
        backButton.setOnClickListener {
            if (isYoutubeMode) {
                sendYoutubeCommand("seekRelative", -10)
            } else {
                val player = exoPlayer ?: return@setOnClickListener
                player.seekTo((player.currentPosition - 10_000L).coerceAtLeast(0L))
            }
        }
        forwardButton.setOnClickListener {
            if (isYoutubeMode) {
                sendYoutubeCommand("seekRelative", 10)
            } else {
                val player = exoPlayer ?: return@setOnClickListener
                player.seekTo(player.currentPosition + 10_000L)
            }
        }

        val initial = selectedTrack ?: suggestedTracks.firstOrNull()
        if (initial != null) {
            bindHeader(initial)
            playTrack(initial)
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
        currentTrack = track

        val playerView = findViewById<PlayerView>(R.id.playerView)
        val webView = youtubeWebView ?: return

        if (track.source == "youtube") {
            isYoutubeMode = true
            exoPlayer?.pause()
            playerView.visibility = View.GONE
            webView.visibility = View.VISIBLE
            loadYoutubeVideo(track.id)
            return
        }

        val url = track.streamUrl
        if (url.isNullOrBlank()) {
            Toast.makeText(this, getString(R.string.selection_not_playable), Toast.LENGTH_SHORT).show()
            return
        }

        isYoutubeMode = false
        webView.visibility = View.GONE
        playerView.visibility = View.VISIBLE

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
        player.play()
    }

    private fun loadYoutubeVideo(videoId: String) {
        val webView = youtubeWebView ?: return
        val html = """
            <!doctype html>
            <html>
            <body style="margin:0;background:black;">
                <div id="player"></div>
                <script>
                    var tag = document.createElement('script');
                    tag.src = 'https://www.youtube.com/iframe_api';
                    var firstScriptTag = document.getElementsByTagName('script')[0];
                    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
                    var player;
                    function onYouTubeIframeAPIReady() {
                        player = new YT.Player('player', {
                            width: '100%',
                            height: '220',
                            videoId: '${videoId}',
                            playerVars: {
                                autoplay: 1,
                                controls: 1,
                                rel: 0,
                                modestbranding: 1,
                                playsinline: 1
                            },
                            events: {
                                'onReady': function(event) {
                                    event.target.playVideo();
                                },
                                'onError': function(event) {
                                    if (window.AndroidBridge && window.AndroidBridge.onYouTubeError) {
                                        window.AndroidBridge.onYouTubeError(String(event.data));
                                    }
                                }
                            }
                        });
                    }
                    function togglePlayPause() {
                        if (!player) return;
                        var state = player.getPlayerState();
                        if (state === 1) { player.pauseVideo(); } else { player.playVideo(); }
                    }
                    function seekRelative(seconds) {
                        if (!player) return;
                        var current = player.getCurrentTime();
                        player.seekTo(Math.max(0, current + seconds), true);
                    }
                    function pausePlayback() {
                        if (!player) return;
                        player.pauseVideo();
                    }
                </script>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "utf-8", null)
    }

    private fun sendYoutubeCommand(command: String, value: Int? = null) {
        val webView = youtubeWebView ?: return
        val js = if (value == null) {
            "javascript:$command()"
        } else {
            "javascript:$command($value)"
        }
        webView.evaluateJavascript(js, null)
    }

    private fun playNextYouTubeCandidate(currentId: String): Boolean {
        val next = suggestedTracks.firstOrNull {
            it.source == "youtube" && it.id != currentId && !blockedYoutubeIds.contains(it.id)
        } ?: return false

        bindHeader(next)
        playTrack(next)
        return true
    }

    private inner class YouTubeEmbedBridge {
        @JavascriptInterface
        fun onYouTubeError(errorCode: String) {
            val failedId = currentTrack?.id ?: return
            runOnUiThread {
                blockedYoutubeIds.add(failedId)

                val movedToNext = playNextYouTubeCandidate(failedId)
                Toast.makeText(
                    this@PlayerActivity,
                    if (movedToNext) {
                        getString(R.string.youtube_embed_retrying, errorCode)
                    } else {
                        getString(R.string.youtube_embed_blocked_no_fallback, errorCode)
                    },
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onStop() {
        if (isYoutubeMode) {
            sendYoutubeCommand("pausePlayback")
        } else {
            exoPlayer?.pause()
        }
        super.onStop()
    }

    override fun onDestroy() {
        exoPlayer?.release()
        exoPlayer = null
        youtubeWebView?.apply {
            stopLoading()
            loadUrl("about:blank")
            destroy()
        }
        youtubeWebView = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_SELECTED_TRACK = "extra_selected_track"
        const val EXTRA_SUGGESTED_TRACKS = "extra_suggested_tracks"
    }
}
