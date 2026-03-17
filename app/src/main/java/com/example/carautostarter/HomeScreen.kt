package com.example.carautostarter

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Row
import androidx.car.app.model.Template

// Ispirato a CarTube: un client video non ufficiale per Android Auto
// che permette la navigazione di YouTube dall'head unit dell'auto.
class HomeScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val itemList = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle("Video popolari")
                    .addText("Esplora i video di tendenza • Tap per dettagli")
                    .setOnClickListener {
                        screenManager.push(
                            VideoDetailScreen(
                                carContext = carContext,
                                title = "Video popolari",
                                description = "Qui puoi mostrare la lista dei video di tendenza. Integra le API del tuo servizio video per popolare questa schermata dinamicamente."
                            )
                        )
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Le mie playlist")
                    .addText("Playlist personali • Tap per dettagli")
                    .setOnClickListener {
                        screenManager.push(
                            VideoDetailScreen(
                                carContext = carContext,
                                title = "Le mie playlist",
                                description = "Mostra le playlist dell'utente autenticato. Puoi integrare OAuth2 per accedere all'account del tuo servizio video."
                            )
                        )
                    }
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Impostazioni")
                    .addText("Qualità video e preferenze • Solo da fermo")
                    .setOnClickListener(ParkedOnlyOnClickListener.create {
                        screenManager.push(
                            VideoDetailScreen(
                                carContext = carContext,
                                title = "Impostazioni",
                                description = "Le impostazioni sono disponibili solo a veicolo fermo per motivi di sicurezza. Qui puoi configurare qualità video, account e preferenze."
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
