package com.example.tango.dataClasses

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


object TicTacToeCellValue {
    const val BLANK = 1
    const val CROSS = 2
    const val CIRCLE = 3
}

class TicTacToeCellData (
    value: Int = TicTacToeCellValue.BLANK,
    partial: Boolean = false
) {
    var value by mutableIntStateOf(value)
    var partial by mutableStateOf(partial)
}
