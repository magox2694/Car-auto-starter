package com.example.carbeats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carbeats.search.SearchRepository
import com.example.carbeats.search.TrackSearchResult
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    private val searchRepository = SearchRepository.default()
    private val resultsAdapter = TrackResultsAdapter(::openPlayer)
    private var lastResults: List<TrackSearchResult> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val headerText = findViewById<TextView>(R.id.headerText)
        val searchInput = findViewById<EditText>(R.id.searchInput)
        val searchButton = findViewById<Button>(R.id.searchButton)
        val statusText = findViewById<TextView>(R.id.statusText)
        val resultsRecycler = findViewById<RecyclerView>(R.id.resultsRecycler)

        headerText.text = getString(R.string.phone_companion_text)
        statusText.text = getString(R.string.search_idle)

        resultsRecycler.layoutManager = LinearLayoutManager(this)
        resultsRecycler.adapter = resultsAdapter

        searchButton.setOnClickListener {
            val query = searchInput.text?.toString().orEmpty()
            if (query.isBlank()) {
                statusText.text = getString(R.string.search_enter_query)
                resultsAdapter.submitList(emptyList())
                return@setOnClickListener
            }

            statusText.text = getString(R.string.search_loading)

            thread {
                val results = searchRepository.search(query)
                runOnUiThread {
                    lastResults = results
                    resultsAdapter.submitList(results)
                    statusText.text = if (results.isEmpty()) {
                        getString(R.string.search_no_results)
                    } else {
                        val youtubeCount = results.count { it.source == "youtube" }
                        val streamCount = results.size - youtubeCount
                        getString(
                            R.string.search_results_count_with_sources,
                            results.size,
                            streamCount,
                            youtubeCount
                        )
                    }
                }
            }
        }
    }

    private fun openPlayer(selected: TrackSearchResult) {
        if (!selected.playable) return

        val playable = ArrayList(lastResults.filter { it.playable })
        if (playable.isEmpty()) return

        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_SELECTED_TRACK, selected)
            putParcelableArrayListExtra(PlayerActivity.EXTRA_SUGGESTED_TRACKS, playable)
        }
        startActivity(intent)
    }
}
