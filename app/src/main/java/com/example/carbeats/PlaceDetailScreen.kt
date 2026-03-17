package com.example.carbeats

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template

class PlaceDetailScreen(
    carContext: CarContext,
    private val title: String,
    private val description: String
) : Screen(carContext) {

    @Suppress("DEPRECATION")
    override fun onGetTemplate(): Template {
        return MessageTemplate.Builder(description)
            .setTitle(title)
            .setHeaderAction(Action.BACK)
            .build()
    }
}
