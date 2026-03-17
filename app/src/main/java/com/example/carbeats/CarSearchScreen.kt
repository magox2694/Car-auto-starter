package com.example.carbeats

import android.content.Intent
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.Row
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.Template
import com.example.carbeats.search.DemoTrackSearchProvider

class CarSearchScreen(carContext: CarContext) : Screen(carContext) {

    private val searchProvider = DemoTrackSearchProvider()
    private var currentQuery: String = ""

    override fun onGetTemplate(): Template {
        val itemListBuilder = ItemList.Builder()
        val results = if (currentQuery.isBlank()) {
            emptyList()
        } else {
            searchProvider.search(currentQuery).take(6)
        }

        if (currentQuery.isBlank()) {
            itemListBuilder.addItem(
                Row.Builder()
                    .setTitle("Inserisci una ricerca")
                    .addText("Esempio: Night, Acoustic")
                    .build()
            )
        } else if (results.isEmpty()) {
            itemListBuilder.addItem(
                Row.Builder()
                    .setTitle("Nessun risultato")
                    .addText("Nessun brano demo trovato per: $currentQuery")
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
                    invalidate()
                }

                override fun onSearchSubmitted(searchText: String) {
                    currentQuery = searchText
                    invalidate()
                }
            }
        )
            .setHeaderAction(Action.BACK)
            .setSearchHint("Cerca brano demo")
            .setItemList(itemListBuilder.build())
            .build()
    }
}
