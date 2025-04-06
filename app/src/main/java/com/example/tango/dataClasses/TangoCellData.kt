package com.example.tango.dataClasses

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object TangoCellValue {
    const val BLANK = 1
    const val SUN = 2
    const val MOON = 3
}

object SYMBOLS {
    const val NONE = 1
    const val SIMPLE = 2
    const val CROSS = 3
    const val EQUALS = 4
}

class TangoCellData(
    value: Int = TangoCellValue.BLANK,
    disabled: Boolean = false,
    leftSymbol: Int = SYMBOLS.SIMPLE,
    topSymbol: Int = SYMBOLS.SIMPLE
) {
    var value by mutableIntStateOf(value)
    var disabled by mutableStateOf(disabled)
    val leftSymbol by mutableIntStateOf(leftSymbol)
    val topSymbol by mutableIntStateOf(topSymbol)
    var containsError by mutableStateOf(false)
}
