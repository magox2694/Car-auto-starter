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
                    .setTitle("YouTube su Android Auto")
                    .addText("La riproduzione video sul display auto non è supportata")
                    .setOnClickListener {
                        screenManager.push(
                            PlaceDetailScreen(
                                carContext = carContext,
                                title = "Limite piattaforma",
                                description = "Android Auto non consente player video YouTube sullo schermo dell'auto. Usa il telefono per la visione."
                            )
                        )
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Contenuti consentiti in auto")
                    .addText("In auto mostra solo informazioni sicure e conformi")
                    .setOnClickListener {
                        screenManager.push(
                            PlaceDetailScreen(
                                carContext = carContext,
                                title = "Sicurezza",
                                description = "Per rispettare i requisiti Android Auto, il video deve restare sul telefono e non sul display dell'auto."
                            )
                        )
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Uso da fermo")
                    .addText("Quando sei parcheggiato puoi passare al telefono")
                    .setOnClickListener(ParkedOnlyOnClickListener.create {
                        screenManager.push(
                            PlaceDetailScreen(
                                carContext = carContext,
                                title = "Parcheggio",
                                description = "Da fermo puoi continuare su smartphone per aprire e riprodurre YouTube."
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
