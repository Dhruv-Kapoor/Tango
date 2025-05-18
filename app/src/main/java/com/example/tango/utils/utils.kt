package com.example.tango.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    fun getDayWithSuffix(day: Int): String {
        return when {
            day in 11..13 -> "${day}th"
            day % 10 == 1 -> "${day}st"
            day % 10 == 2 -> "${day}nd"
            day % 10 == 3 -> "${day}rd"
            else -> "${day}th"
        }
    }

    fun formatDate(date: Date): String {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        val dayFormat = SimpleDateFormat("d", Locale.ENGLISH)
        val monthYearFormat = SimpleDateFormat("MMMM, yyyy", Locale.ENGLISH)

        val time = timeFormat.format(date)
        val day = getDayWithSuffix(dayFormat.format(date).toInt())
        val monthYear = monthYearFormat.format(date)

        return "$time, $day $monthYear"
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