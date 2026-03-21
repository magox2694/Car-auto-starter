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
import com.example.carbeats.search.TrackSearchResult
import java.util.concurrent.Executors

class CarSearchScreen(carContext: CarContext) : Screen(carContext) {

    private val searchRepository = SearchRepository.default()
    private val searchExecutor = Executors.newSingleThreadExecutor()
    private var currentQuery: String = ""
    private var results: List<TrackSearchResult> = emptyList()
    private var isLoading: Boolean = false

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
            itemListBuilder.addItem(
                Row.Builder()
                    .setTitle("Nessun risultato")
                    .addText("Nessun brano trovato per: $currentQuery")
                    .build()
            )
        } else {
            results.forEach { item ->
                itemListBuilder.addItem(
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

        return SearchTemplate.Builder(
            object : SearchTemplate.SearchCallback {
                override fun onSearchTextChanged(searchText: String) {
                    currentQuery = searchText
                    if (searchText.isBlank()) {
                        results = emptyList()
                        isLoading = false
                    }
                    invalidate()
                }

                override fun onSearchSubmitted(searchText: String) {
                    currentQuery = searchText
                    performSearch(searchText)
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
        invalidate()

        searchExecutor.execute {
            val loaded = searchRepository.search(query).take(10)
            carContext.mainExecutor.execute {
                if (currentQuery == query) {
                    results = loaded
                    isLoading = false
                    invalidate()
                }
            }
        }
    }
}
