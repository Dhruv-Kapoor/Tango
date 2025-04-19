package com.example.tango

import androidx.compose.ui.unit.dp

val CELL_SIZE = 60.dp
val SYMBOL_SIZE = 16.dp
val SYMBOL_ICON_SIZE = 16.dp
const val EDGE_STROKE = 1f
const val CELL_UPDATE_THROTTLE: Long = 500
const val QUEENS_EDGE_THICKNESS_FACTOR = 5
const val ZIP_EDGE_THICKNESS_FACTOR = 8

enum class GRID_TYPES(val value: Int) {
    TANGO(1),
    QUEENS(2),
    ZIP(3)
}
