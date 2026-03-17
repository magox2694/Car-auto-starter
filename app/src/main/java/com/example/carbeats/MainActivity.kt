package com.example.carbeats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.example.carbeats.search.SearchRepository
import com.example.carbeats.search.TrackSearchResult
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    private val searchRepository = SearchRepository.default()
    private var lastResults: List<TrackSearchResult> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val headerText = findViewById<TextView>(R.id.headerText)
        val searchInput = findViewById<EditText>(R.id.searchInput)
        val selectionInput = findViewById<EditText>(R.id.selectionInput)
        val searchButton = findViewById<Button>(R.id.searchButton)
        val playSelectionButton = findViewById<Button>(R.id.playSelectionButton)
        val statusText = findViewById<TextView>(R.id.statusText)
        val resultsText = findViewById<TextView>(R.id.resultsText)

        val playPlaylistButton = findViewById<Button>(R.id.playPlaylistButton)
        val playSampleButton = findViewById<Button>(R.id.playSampleButton)
        val previousButton = findViewById<Button>(R.id.previousButton)
        val nextButton = findViewById<Button>(R.id.nextButton)
        val pauseButton = findViewById<Button>(R.id.pauseButton)
        val resumeButton = findViewById<Button>(R.id.resumeButton)
        val stopButton = findViewById<Button>(R.id.stopButton)

        headerText.text = getString(R.string.phone_companion_text)
        statusText.text = getString(R.string.status_ready)

        searchButton.setOnClickListener {
            val query = searchInput.text?.toString().orEmpty()
            resultsText.text = getString(R.string.search_loading)

            thread {
                val results = searchRepository.search(query)
                runOnUiThread {
                    lastResults = results
                    val summary = if (results.isEmpty()) {
                        getString(R.string.search_no_results)
                    } else {
                        results.mapIndexed { index, item ->
                            val state = if (item.playable) "play" else "meta"
                            "${index + 1}. ${item.title} (${item.artist}) [${item.source}][$state]"
                        }.joinToString(separator = "\n", prefix = "Risultati:\n")
                    }
                    resultsText.text = summary

                    val firstPlayable = results.firstOrNull { it.playable }
                    if (firstPlayable != null && firstPlayable.source == "demo") {
                        val intent = Intent(this@MainActivity, PlaybackService::class.java).apply {
                            action = PlaybackService.ACTION_PLAY_MEDIA_ID
                            putExtra(PlaybackService.EXTRA_MEDIA_ID, firstPlayable.id)
                        }
                        dispatchServiceIntent(intent)
                        statusText.text = getString(R.string.status_playing_selected)
                    }
                }
            }
        }

        playSelectionButton.setOnClickListener {
            val value = selectionInput.text?.toString()?.trim().orEmpty()
            val index = value.toIntOrNull()?.minus(1)
            if (index == null || index !in lastResults.indices) {
                resultsText.text = getString(R.string.invalid_selection)
                return@setOnClickListener
            }

            val selected = lastResults[index]
            if (!selected.playable || selected.source != "demo") {
                resultsText.text = getString(R.string.selection_not_playable)
                return@setOnClickListener
            }

            val intent = Intent(this@MainActivity, PlaybackService::class.java).apply {
                action = PlaybackService.ACTION_PLAY_MEDIA_ID
                putExtra(PlaybackService.EXTRA_MEDIA_ID, selected.id)
            }
            dispatchServiceIntent(intent)
            statusText.text = getString(R.string.status_playing_selected)
        }

        bindControlButton(playPlaylistButton, PlaybackService.ACTION_PLAY_DEMO_PLAYLIST, statusText)
        bindControlButton(playSampleButton, PlaybackService.ACTION_PLAY_SAMPLE, statusText)
        bindControlButton(previousButton, PlaybackService.ACTION_PREVIOUS, statusText)
        bindControlButton(nextButton, PlaybackService.ACTION_NEXT, statusText)
        bindControlButton(pauseButton, PlaybackService.ACTION_PAUSE, statusText)
        bindControlButton(resumeButton, PlaybackService.ACTION_RESUME, statusText)
        bindControlButton(stopButton, PlaybackService.ACTION_STOP, statusText)
    }

    private fun bindControlButton(button: Button, action: String, statusText: TextView) {
        button.setOnClickListener {
            val intent = Intent(this@MainActivity, PlaybackService::class.java).apply {
                this.action = action
            }
            dispatchServiceIntent(intent)
            statusText.text = when (action) {
                PlaybackService.ACTION_PLAY_SAMPLE,
                PlaybackService.ACTION_PLAY_DEMO_PLAYLIST -> getString(R.string.status_playing)

                PlaybackService.ACTION_PAUSE -> getString(R.string.status_paused)
                PlaybackService.ACTION_RESUME -> getString(R.string.status_playing)
                PlaybackService.ACTION_STOP -> getString(R.string.status_stopped)
                PlaybackService.ACTION_NEXT,
                PlaybackService.ACTION_PREVIOUS -> getString(R.string.status_navigating)

                else -> getString(R.string.status_ready)
            }
        }
    }

    private fun dispatchServiceIntent(intent: Intent) {
        val action = intent.action.orEmpty()
        val shouldStartForeground = action == PlaybackService.ACTION_PLAY_SAMPLE ||
            action == PlaybackService.ACTION_PLAY_DEMO_PLAYLIST ||
            action == PlaybackService.ACTION_PLAY_QUERY ||
            action == PlaybackService.ACTION_PLAY_MEDIA_ID

        if (shouldStartForeground) {
            ContextCompat.startForegroundService(this, intent)
        } else {
            startService(intent)
        }
    }
}
