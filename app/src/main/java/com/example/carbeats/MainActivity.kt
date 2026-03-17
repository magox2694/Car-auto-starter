package com.example.carbeats

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.carbeats.search.SearchRepository

class MainActivity : ComponentActivity() {
    private val searchRepository = SearchRepository.default()

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

        val text = TextView(this).apply {
            text = getString(R.string.phone_companion_text)
            textSize = 16f
            setPadding(0, 0, 0, 32)
        }

        val searchInput = EditText(this).apply {
            hint = getString(R.string.search_hint)
            setPadding(0, 0, 0, 24)
        }

        val searchButton = Button(this).apply {
            text = getString(R.string.search_and_play)
            setOnClickListener {
                val query = searchInput.text?.toString().orEmpty()
                val results = searchRepository.search(query)
                val summary = if (results.isEmpty()) {
                    getString(R.string.search_no_results)
                } else {
                    results.joinToString(separator = "\n", prefix = "Risultati:\n") {
                        "- ${it.title} (${it.artist}) [${it.source}]"
                    }
                }
                resultsText.text = summary

                val intent = Intent(this@MainActivity, PlaybackService::class.java).apply {
                    action = PlaybackService.ACTION_PLAY_QUERY
                    putExtra(PlaybackService.EXTRA_QUERY, query)
                }
                startService(intent)
            }
        }

        val resultsText = TextView(this).apply {
            textSize = 14f
            setPadding(0, 24, 0, 24)
        }

        root.addView(text)
        root.addView(searchInput)
        root.addView(searchButton)
        root.addView(resultsText)
        root.addView(controlButton(R.string.play_playlist_demo, PlaybackService.ACTION_PLAY_DEMO_PLAYLIST))
        root.addView(controlButton(R.string.play_sample, PlaybackService.ACTION_PLAY_SAMPLE))
        root.addView(controlButton(R.string.previous_track, PlaybackService.ACTION_PREVIOUS))
        root.addView(controlButton(R.string.next_track, PlaybackService.ACTION_NEXT))
        root.addView(controlButton(R.string.pause_audio, PlaybackService.ACTION_PAUSE))
        root.addView(controlButton(R.string.resume_audio, PlaybackService.ACTION_RESUME))
        root.addView(controlButton(R.string.stop_audio, PlaybackService.ACTION_STOP))

        setContentView(root)
    }

    private fun controlButton(textResId: Int, action: String): Button {
        return Button(this).apply {
            text = getString(textResId)
            setOnClickListener {
                val intent = Intent(this@MainActivity, PlaybackService::class.java).apply {
                    this.action = action
                }
                startService(intent)
            }
        }
    }
}
