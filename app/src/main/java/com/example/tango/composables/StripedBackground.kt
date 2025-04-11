package com.example.tango.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun stripedBackground(bgColor: Color = Color.White): Brush {
    val stripeSize = with(LocalDensity.current) {
        4.dp.toPx()
    }
    val brush = Brush.linearGradient(
        0.0f to Color.Red,
        0.3f to Color.Red,
        0.3f to bgColor,
        1.0f to bgColor,
        start = Offset(0f, 0f),
        end = Offset(stripeSize, stripeSize),
        tileMode = TileMode.Repeated
    )
    return brush
}
