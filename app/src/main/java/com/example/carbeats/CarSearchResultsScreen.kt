package com.example.carbeats

import android.content.Intent
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.example.carbeats.search.SearchRepository
import com.example.carbeats.search.TrackSearchResult
import java.util.concurrent.Executors

class CarSearchResultsScreen(
    carContext: CarContext,
    private val query: String
) : Screen(carContext) {

    private val searchRepository = SearchRepository.default()
    private val searchExecutor = Executors.newSingleThreadExecutor()
    private var hasRequestedSearch = false
    private var isLoading = false
    private var results: List<TrackSearchResult> = emptyList()

    @Suppress("DEPRECATION")
    override fun onGetTemplate(): Template {
        maybeStartSearch()
        val listBuilder = ItemList.Builder()

        if (isLoading) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle("Ricerca in corso")
                    .addText("Sto cercando: $query")
                    .build()
            )
        } else if (results.isEmpty()) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle("Nessun risultato")
                    .addText("Nessun brano trovato per: $query")
                    .build()
            )
        } else {
            results.forEach { item ->
                listBuilder.addItem(
                    Row.Builder()
                        .setTitle(item.title)
                        .addText("${item.artist} - ${item.album}")
                        .setOnClickListener {
                            if (item.playable && item.source == "demo") {
                                val playIntent = Intent(carContext, PlaybackService::class.java).apply {
                                    action = PlaybackService.ACTION_PLAY_MEDIA_ID
                                    putExtra(PlaybackService.EXTRA_MEDIA_ID, item.id)
                                }
                                carContext.startService(playIntent)
                            }
                            screenManager.push(
                                PlaceDetailScreen(
                                    carContext = carContext,
                                    title = item.title,
                                    description = "Riproduzione avviata: ${item.artist} (${item.source})."
                                )
                            )
                        }
                        .build()
                )
            }
        }

        return ListTemplate.Builder()
            .setTitle("Risultati: $query")
            .setSingleList(listBuilder.build())
            .setHeaderAction(Action.BACK)
            .build()
    }

    private fun maybeStartSearch() {
        if (hasRequestedSearch || query.isBlank()) return

        hasRequestedSearch = true
        isLoading = true

        searchExecutor.execute {
            val loaded = searchRepository.search(query).take(10)
            carContext.mainExecutor.execute {
                results = loaded
                isLoading = false
                invalidate()
            }
        }
    }
}
