package com.example.carbeats

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Row
import androidx.car.app.model.Template

class HomeScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val itemList = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle("CarBeats - Novita motori")
                    .addText("Audio: novita, test drive e anteprime")
                    .setOnClickListener {
                        screenManager.push(
                            PlaceDetailScreen(
                                carContext = carContext,
                                title = "Novità motori",
                                description = "Le ultime uscite dal mondo auto in formato audio, ottimizzate per Android Auto."
                            )
                        )
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("CarBeats - Tutorial")
                    .addText("Audio: guida e consigli pratici")
                    .setOnClickListener {
                        screenManager.push(
                            PlaceDetailScreen(
                                carContext = carContext,
                                title = "Tutorial",
                                description = "Contenuti utili su manutenzione, sicurezza e funzioni dell'auto in ascolto." 
                            )
                        )
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("CarBeats - Playlist parcheggio")
                    .addText("Playlist speciali disponibili solo da fermo")
                    .setOnClickListener(ParkedOnlyOnClickListener.create {
                        screenManager.push(
                            PlaceDetailScreen(
                                carContext = carContext,
                                title = "Playlist parcheggio",
                                description = "Playlist speciali da consultare solo a veicolo fermo, nel rispetto della sicurezza."
                            )
                        )
                    })
                    .build()
            )
            .build()

        return ListTemplate.Builder()
            .setTitle(carContext.getString(R.string.car_home_title))
            .setSingleList(itemList)
            .setHeaderAction(Action.APP_ICON)
            .build()
    }
}
