package com.example.carautostarter

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = TextView(this).apply {
            text = getString(R.string.phone_companion_text)
            textSize = 18f
            setPadding(48, 96, 48, 48)
        }

        setContentView(text)
    }
}
