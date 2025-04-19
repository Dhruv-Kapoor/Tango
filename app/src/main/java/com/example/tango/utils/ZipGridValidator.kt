package com.example.tango.utils

import com.example.tango.dataClasses.ZipCellData

fun validateZipGrid(grid: Array<Array<ZipCellData>>, path: List<Pair<Int, Int>>): Boolean {
    var errorAtPosition: Int? = null
    var lastValue = 0
    var blanks = grid.size * grid[0].size != path.size
    path.forEach {
        val cell = grid[it.first][it.second]
        if (errorAtPosition == null && cell.value != null) {
            if (cell.value!! - lastValue > 1) {
                errorAtPosition = cell.pathPosition
            } else {
                lastValue = cell.value!!
            }
        }
    }
    if (!blanks && errorAtPosition != null) {
        path.forEach {
            val cell = grid[it.first][it.second]
            cell.containsError = cell.pathPosition!! >= errorAtPosition
        }
    }
    return !blanks && errorAtPosition == null
}
