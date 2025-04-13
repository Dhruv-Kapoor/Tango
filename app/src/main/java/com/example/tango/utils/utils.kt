package com.example.tango.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

object Utils {
    fun formatTime(seconds: Int): String {
        return if (seconds >= 60) {
            "${seconds / 60} minutes and ${seconds % 60} seconds"
        } else {
            "${seconds % 60} seconds"
        }
    }

    @Composable
    fun Modifier.conditional(condition : Boolean, modifier : @Composable Modifier.() -> Modifier) : Modifier {
        return if (condition) {
            then(modifier(Modifier))
        } else {
            this
        }
    }

    @Composable
    fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }


    @Composable
    fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }
}