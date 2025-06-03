package com.example.tango.utils

import com.example.tango.dataClasses.TicTacToeCellData
import com.example.tango.dataClasses.TicTacToeCellValue


fun validateTicTacToe(grid: Array<Array<TicTacToeCellData>>): Int? {
    var blanks = 0
    // Rows
    for (i in grid.indices) {
        var cross = 0
        var circle = 0
        for (j in grid[i].indices) {
            if (grid[i][j].value == TicTacToeCellValue.BLANK) {
                ++blanks
                break
            }
            if (grid[i][j].value == TicTacToeCellValue.CIRCLE) ++circle
            if (grid[i][j].value == TicTacToeCellValue.CROSS) ++cross
        }
        if (circle == grid.size) {
            return TicTacToeCellValue.CIRCLE
        }
        if (cross == grid.size) {
            return TicTacToeCellValue.CROSS
        }
    }

    // Cols
    for (j in grid[0].indices) {
        var cross = 0
        var circle = 0
        for (i in grid.indices) {
            if (grid[i][j].value == TicTacToeCellValue.BLANK) break
            if (grid[i][j].value == TicTacToeCellValue.CIRCLE) ++circle
            if (grid[i][j].value == TicTacToeCellValue.CROSS) ++cross
        }
        if (circle == grid.size) {
            return TicTacToeCellValue.CIRCLE
        }
        if (cross == grid.size) {
            return TicTacToeCellValue.CROSS
        }
    }

    // Diagonal 1
    if (grid[0][0].value == grid[1][1].value && grid[1][1].value == grid[2][2].value && grid[0][0].value != TicTacToeCellValue.BLANK) {
        return grid[0][0].value
    }

    // Diagonal 2
    if (grid[0][2].value == grid[1][1].value && grid[1][1].value == grid[2][0].value && grid[2][0].value != TicTacToeCellValue.BLANK) {
        return grid[0][2].value
    }

    return if (blanks > 0) null else 0
}
