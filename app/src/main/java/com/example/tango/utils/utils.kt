package com.example.tango.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

object Utils {
    fun formatTime(seconds: Int): String {
        val pluralMin = if (seconds / 60 > 1) "s" else ""
        val pluralSec = if (seconds % 60 > 1) "s" else ""
        return if (seconds >= 60) {
            "${seconds / 60} minute$pluralMin and ${seconds % 60} second$pluralSec"
        } else {
            "${seconds % 60} second$pluralSec"
        }
    }

    @Composable
    fun Modifier.conditional(
        condition: Boolean,
        modifier: @Composable Modifier.() -> Modifier
    ): Modifier {
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