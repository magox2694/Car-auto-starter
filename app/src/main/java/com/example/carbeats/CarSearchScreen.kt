package com.example.carbeats

import android.content.Intent
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.Row
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.Template
import com.example.carbeats.search.SearchRepository
import com.example.carbeats.search.SearchResponse
import com.example.carbeats.search.TrackSearchResult

class CarSearchScreen(carContext: CarContext) : Screen(carContext) {

    private val searchRepository = SearchRepository.default()
    private var currentQuery: String = ""
    private var results: List<TrackSearchResult> = emptyList()
    private var isLoading: Boolean = false
    private var latestSearchToken: Int = 0
    private var statusMessage: String? = null

    override fun onGetTemplate(): Template {
        val itemListBuilder = ItemList.Builder()

        if (currentQuery.isBlank()) {
            itemListBuilder.addItem(
                Row.Builder()
                    .setTitle("Inserisci una ricerca")
                    .addText("Esempio: Night, Acoustic, Chill")
                    .build()
            )
        } else if (isLoading) {
            itemListBuilder.addItem(
                Row.Builder()
                    .setTitle("Ricerca in corso")
                    .addText("Sto cercando: $currentQuery")
                    .build()
            )
        } else if (results.isEmpty()) {
            if (!statusMessage.isNullOrBlank()) {
                itemListBuilder.addItem(
                    Row.Builder()
                        .setTitle(statusMessage!!)
                        .addText("Controlla connessione e provider disponibili")
                        .build()
                )
            } else {
                itemListBuilder.addItem(
                    Row.Builder()
                        .setTitle("Nessun risultato")
                        .addText("Nessun brano trovato per: $currentQuery")
                        .build()
                )
            }
        } else {
            if (!statusMessage.isNullOrBlank()) {
                itemListBuilder.addItem(
                    Row.Builder()
                        .setTitle(statusMessage!!)
                        .addText("Controlla connessione e provider disponibili")
                        .build()
                )
            }
            results.forEach { item ->
                itemListBuilder.addItem(
                    Row.Builder()
                        .setTitle(item.title)
                        .addText("${item.artist} - ${item.album}")
                        .setOnClickListener {
                            val description = if (item.playable) {
                                "Riproduzione avviata: ${item.artist} (${item.source})."
                            } else {
                                "Risultato informativo (${item.source}): il brano non e disponibile per la riproduzione diretta."
                            }

                            if (item.playable) {
                                val playIntent = Intent(carContext, PlaybackService::class.java).apply {
                                    if (!item.streamUrl.isNullOrBlank()) {
                                        action = PlaybackService.ACTION_PLAY_STREAM
                                        putExtra(PlaybackService.EXTRA_STREAM_URL, item.streamUrl)
                                        putExtra(PlaybackService.EXTRA_TITLE, item.title)
                                        putExtra(PlaybackService.EXTRA_ARTIST, item.artist)
                                        putExtra(PlaybackService.EXTRA_ALBUM, item.album)
                                        putExtra(PlaybackService.EXTRA_STREAM_MEDIA_ID, item.id)
                                    } else {
                                        action = PlaybackService.ACTION_PLAY_MEDIA_ID
                                        putExtra(PlaybackService.EXTRA_MEDIA_ID, item.id)
                                    }
                                }
                                carContext.startService(playIntent)
                            }
                            screenManager.push(
                                PlaceDetailScreen(
                                    carContext = carContext,
                                    title = item.title,
                                    description = description
                                )
                            )
                        }
                        .build()
                )
            }
        }

        return SearchTemplate.Builder(
            object : SearchTemplate.SearchCallback {
                override fun onSearchTextChanged(searchText: String) {
                    currentQuery = searchText.trim()
                    if (searchText.isBlank()) {
                        latestSearchToken += 1
                        results = emptyList()
                        isLoading = false
                        statusMessage = null
                    }
                    invalidate()
                }

                override fun onSearchSubmitted(searchText: String) {
                    currentQuery = searchText.trim()
                    performSearch(currentQuery)
                }
            }
        )
            .setHeaderAction(Action.BACK)
            .setSearchHint("Cerca brano")
            .setItemList(itemListBuilder.build())
            .build()
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            results = emptyList()
            isLoading = false
            invalidate()
            return
        }

        isLoading = true
        results = emptyList()
        statusMessage = null
        invalidate()
        val searchToken = ++latestSearchToken

        SearchExecutors.io.execute {
            val response = searchRepository.searchDetails(query)
            carContext.mainExecutor.execute {
                if (latestSearchToken == searchToken && currentQuery == query) {
                    results = response.results.take(10)
                    isLoading = false
                    statusMessage = buildStatusMessage(response)
                    invalidate()
                }
            }
        }
    }

    private fun buildStatusMessage(response: SearchResponse): String? {
        if (response.results.isNotEmpty()) {
            return if (response.unavailableProviderCount > 0) {
                "Alcuni provider non sono disponibili ora"
            } else {
                null
            }
        }

        return when {
            response.hasProviderErrors -> "Provider temporaneamente non raggiungibili"
            response.hasDisabledProviders -> "Provider opzionali non configurati"
            else -> null
        }
    }
}
