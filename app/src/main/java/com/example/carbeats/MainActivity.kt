package com.example.carbeats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carbeats.search.SearchRepository
import com.example.carbeats.search.SearchResponse
import com.example.carbeats.search.TrackSearchResult

class MainActivity : ComponentActivity() {
    private val searchRepository = SearchRepository.default()
    private val resultsAdapter = TrackResultsAdapter(::openPlayer)
    private var lastResults: List<TrackSearchResult> = emptyList()
    private var latestSearchToken: Int = 0

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
            val query = searchInput.text?.toString().orEmpty().trim()
            if (query.isBlank()) {
                latestSearchToken += 1
                statusText.text = getString(R.string.search_enter_query)
                resultsAdapter.submitList(emptyList())
                return@setOnClickListener
            }

            val searchToken = ++latestSearchToken
            searchButton.isEnabled = false
            statusText.text = getString(R.string.search_loading)

            SearchExecutors.io.execute {
                val response = searchRepository.searchDetails(query)
                runOnUiThread {
                    if (latestSearchToken != searchToken || isFinishing || isDestroyed) {
                        return@runOnUiThread
                    }

                    lastResults = response.results
                    resultsAdapter.submitList(response.results)
                    searchButton.isEnabled = true
                    statusText.text = buildStatusMessage(response)
                }
            }
        }
    }

    private fun buildStatusMessage(response: SearchResponse): String {
        if (response.results.isEmpty()) {
            return when {
                response.hasProviderErrors -> getString(R.string.search_provider_error_only)
                response.hasDisabledProviders -> getString(R.string.search_provider_disabled_only)
                else -> getString(R.string.search_no_results)
            }
        }

        val youtubeCount = response.results.count { it.source == "youtube" }
        val streamCount = response.results.size - youtubeCount
        val baseMessage = getString(
            R.string.search_results_count_with_sources,
            response.results.size,
            streamCount,
            youtubeCount
        )

        return when {
            response.unavailableProviderCount > 0 -> {
                getString(
                    R.string.search_results_with_provider_warnings,
                    baseMessage,
                    response.unavailableProviderCount
                )
            }
            response.fromCache -> getString(R.string.search_results_from_cache, baseMessage)
            else -> baseMessage
        }
    }

    private fun openPlayer(selected: TrackSearchResult) {
        if (!selected.playable) {
            Toast.makeText(this, getString(R.string.selection_metadata_only), Toast.LENGTH_SHORT).show()
            return
        }

        val playable = ArrayList(lastResults.filter { it.playable })
        if (playable.isEmpty()) return

        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_SELECTED_TRACK, selected)
            putParcelableArrayListExtra(PlayerActivity.EXTRA_SUGGESTED_TRACKS, playable)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        latestSearchToken += 1
        super.onDestroy()
    }
}
