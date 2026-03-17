package com.example.carbeats

import android.content.Intent
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.example.carbeats.search.DemoTrackSearchProvider

class CarSearchResultsScreen(
    carContext: CarContext,
    private val query: String
) : Screen(carContext) {

    private val searchProvider = DemoTrackSearchProvider()

    @Suppress("DEPRECATION")
    override fun onGetTemplate(): Template {
        val matches = searchProvider.search(query)
        val listBuilder = ItemList.Builder()

        if (matches.isEmpty()) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle("Nessun risultato")
                    .addText("Nessun brano demo trovato per: $query")
                    .build()
            )
        } else {
            matches.forEach { item ->
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
}
