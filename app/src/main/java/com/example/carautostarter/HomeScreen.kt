package com.example.carautostarter

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
                    .setTitle("CarTube - Novità motori")
                    .addText("Video: test drive e anteprime")
                    .setOnClickListener {
                        screenManager.push(
                            PlaceDetailScreen(
                                carContext = carContext,
                                title = "Novità motori",
                                description = "Le ultime uscite dal mondo auto, raccontate in formato video."
                            )
                        )
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("CarTube - Tutorial")
                    .addText("Video: guida e consigli pratici")
                    .setOnClickListener {
                        screenManager.push(
                            PlaceDetailScreen(
                                carContext = carContext,
                                title = "Tutorial",
                                description = "Contenuti utili su manutenzione, sicurezza e funzioni dell'auto."
                            )
                        )
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("CarTube - Playlist parcheggio")
                    .addText("Azione consentita solo da fermo")
                    .setOnClickListener(ParkedOnlyOnClickListener.create {
                        screenManager.push(
                            PlaceDetailScreen(
                                carContext = carContext,
                                title = "Playlist parcheggio",
                                description = "Contenuti disponibili solo a veicolo fermo per rispettare la sicurezza."
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
