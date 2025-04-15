package com.example.tango.dataClasses

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color


object QueensCellValue {
    const val BLANK = 1
    const val CROSS = 2
    const val QUEEN = 3
}

class QueensCellData(
    value: Int = QueensCellValue.BLANK,
    val color: Int = 0,
    leftColorId: Int = -1,
    topColorId: Int = -1
) {
    var value by mutableIntStateOf(value)
    var leftColorId by mutableIntStateOf(leftColorId)
    var topColorId by mutableIntStateOf(topColorId)
    var containsError by mutableStateOf(false)
    var autoX = false

    fun getColor(): Color {
        return COLORS[color]
    }

    override fun toString(): String {
        return "left: $leftColorId, top$topColorId"
    }

    companion object {
        val COLORS = arrayOf(
            Color(0xffbba3e2),
            Color(0xffffc992),
            Color(0xff96beff),
            Color(0xffb3dfa0),
            Color(0xffdfdfdf),
            Color(0xffff7b60),
            Color(0xffe6f388),
            Color(0xffb9b29e),
            Color(0xffdfa0bf),
            Color(0xffa3d2d8),
            Color(0xff62efea),
            Color(0xffff93f3),
            Color(0xff8acc6d),
            Color(0xff729aec),
            Color(0xffc387e0),
            Color(0xffffe04b),
        )
    }
}
