package com.example.carbeats

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val headerText = TextView(this).apply {
            text = getString(R.string.phone_companion_text)
            textSize = 16f
            setPadding(0, 0, 0, 32)
        }

        val searchInput = EditText(this).apply {
            hint = getString(R.string.search_hint)
            setPadding(0, 0, 0, 24)
        }

        val resultsText = TextView(this).apply {
            textSize = 14f
            setPadding(0, 24, 0, 24)
        }

        val statusText = TextView(this).apply {
            text = getString(R.string.status_ready)
            textSize = 14f
            setPadding(0, 0, 0, 16)
        }

        val searchButton = Button(this).apply {
            this.text = getString(R.string.search_and_play)
            setOnClickListener {
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
        }

        val selectionInput = EditText(this).apply {
            hint = getString(R.string.select_result_hint)
            setPadding(0, 0, 0, 16)
        }

        val playSelectionButton = Button(this).apply {
            this.text = getString(R.string.play_selected_result)
            setOnClickListener {
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
        }

        root.addView(headerText)
        root.addView(searchInput)
        root.addView(searchButton)
        root.addView(selectionInput)
        root.addView(playSelectionButton)
        root.addView(statusText)
        root.addView(resultsText)
        root.addView(controlButton(R.string.play_playlist_demo, PlaybackService.ACTION_PLAY_DEMO_PLAYLIST, statusText))
        root.addView(controlButton(R.string.play_sample, PlaybackService.ACTION_PLAY_SAMPLE, statusText))
        root.addView(controlButton(R.string.previous_track, PlaybackService.ACTION_PREVIOUS, statusText))
        root.addView(controlButton(R.string.next_track, PlaybackService.ACTION_NEXT, statusText))
        root.addView(controlButton(R.string.pause_audio, PlaybackService.ACTION_PAUSE, statusText))
        root.addView(controlButton(R.string.resume_audio, PlaybackService.ACTION_RESUME, statusText))
        root.addView(controlButton(R.string.stop_audio, PlaybackService.ACTION_STOP, statusText))

        setContentView(root)
    }

    private fun controlButton(textResId: Int, action: String, statusText: TextView): Button {
        return Button(this).apply {
            this.text = getString(textResId)
            setOnClickListener {
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
