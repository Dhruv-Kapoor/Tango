package com.example.tango.utils

import androidx.compose.ui.Modifier

object Utils {
    fun formatTime(seconds: Int): String {
        return if (seconds >= 60) {
            "${seconds / 60} minutes and ${seconds % 60} seconds"
        } else {
            "${seconds % 60} seconds"
        }
    }

    fun Modifier.conditional(condition : Boolean, modifier : Modifier.() -> Modifier) : Modifier {
        return if (condition) {
            then(modifier(Modifier))
        } else {
            this
        }
    }
}