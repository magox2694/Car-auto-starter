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
                    .setTitle("Veterinario di zona")
                    .addText("Esempio POI • Tap per dettagli")
                    .setOnClickListener {
                        screenManager.push(
                            PlaceDetailScreen(
                                carContext = carContext,
                                title = "Veterinario di zona",
                                description = "Qui puoi mostrare dettagli, indirizzo, orari o azioni."
                            )
                        )
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Farmacia aperta")
                    .addText("Esempio demo • Tap per dettagli")
                    .setOnClickListener {
                        screenManager.push(
                            PlaceDetailScreen(
                                carContext = carContext,
                                title = "Farmacia aperta",
                                description = "Questo starter è pensato come base da personalizzare."
                            )
                        )
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Info sicurezza")
                    .addText("Azione consentita solo da fermo")
                    .setOnClickListener(ParkedOnlyOnClickListener.create {
                        screenManager.push(
                            PlaceDetailScreen(
                                carContext = carContext,
                                title = "Sicurezza",
                                description = "Qui puoi inserire schermate disponibili solo a veicolo fermo."
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
