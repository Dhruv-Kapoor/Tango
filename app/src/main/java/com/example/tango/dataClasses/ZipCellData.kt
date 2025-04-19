package com.example.tango.dataClasses

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ZipCellData(
    value: Int? = null,
    leftWall: Boolean = false,
    topWall: Boolean = false,
    pathPosition: Int? = null
) {
    val value by mutableStateOf(value)
    val leftWall by mutableStateOf(leftWall)
    val topWall by mutableStateOf(topWall)
    var pathPosition by mutableStateOf<Int?>(pathPosition)
    var containsError by mutableStateOf(false)
}
