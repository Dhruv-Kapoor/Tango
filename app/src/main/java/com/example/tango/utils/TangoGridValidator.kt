package com.example.tango.utils

import com.example.tango.dataClasses.SYMBOLS
import com.example.tango.dataClasses.TangoCellData
import com.example.tango.dataClasses.TangoCellValue

private fun checkCells(grid: Array<Array<TangoCellData>>): Set<Pair<Int, Int>> {
    val invalidCells = hashSetOf<Pair<Int, Int>>()
    for (i in grid.indices) {
        for (j in grid[i].indices) {
            if (grid[i][j].value == TangoCellValue.BLANK) continue
            if (i > 1 && grid[i - 2][j].value == grid[i - 1][j].value && grid[i - 1][j].value == grid[i][j].value) {
                invalidCells.add(Pair(i - 2, j))
                invalidCells.add(Pair(i - 1, j))
                invalidCells.add(Pair(i, j))
            }
            if (j > 1 && grid[i][j - 2].value == grid[i][j - 1].value && grid[i][j - 1].value == grid[i][j].value) {
                invalidCells.add(Pair(i, j - 2))
                invalidCells.add(Pair(i, j - 1))
                invalidCells.add(Pair(i, j))
            }

            if (grid[i][j].leftSymbol == SYMBOLS.EQUALS) {
                if (
                    grid[i][j].value != TangoCellValue.BLANK &&
                    grid[i][j - 1].value != TangoCellValue.BLANK &&
                    grid[i][j].value != grid[i][j - 1].value
                ) {
                    invalidCells.add(Pair(i, j))
                    invalidCells.add(Pair(i, j - 1))
                }
            } else if (grid[i][j].leftSymbol == SYMBOLS.CROSS) {
                if (
                    grid[i][j].value != TangoCellValue.BLANK &&
                    grid[i][j - 1].value != TangoCellValue.BLANK &&
                    grid[i][j].value == grid[i][j - 1].value
                ) {
                    invalidCells.add(Pair(i, j))
                    invalidCells.add(Pair(i, j - 1))
                }
            }

            if (grid[i][j].topSymbol == SYMBOLS.EQUALS) {
                if (
                    grid[i][j].value != TangoCellValue.BLANK &&
                    grid[i - 1][j].value != TangoCellValue.BLANK &&
                    grid[i][j].value != grid[i - 1][j].value
                ) {
                    invalidCells.add(Pair(i, j))
                    invalidCells.add(Pair(i - 1, j))
                }
            } else if (grid[i][j].topSymbol == SYMBOLS.CROSS) {
                if (
                    grid[i][j].value != TangoCellValue.BLANK &&
                    grid[i - 1][j].value != TangoCellValue.BLANK &&
                    grid[i][j].value == grid[i - 1][j].value
                ) {
                    invalidCells.add(Pair(i, j))
                    invalidCells.add(Pair(i - 1, j))
                }
            }
        }
    }
    return invalidCells
}

fun checkRows(grid: Array<Array<TangoCellData>>): Set<Int> {
    val invalidRows = hashSetOf<Int>()
    for (i in grid.indices) {
        var s = 0
        var m = 0
        for (j in grid[i].indices) {
            if (grid[i][j].value == TangoCellValue.BLANK) break
            if (grid[i][j].value == TangoCellValue.SUN) ++s
            if (grid[i][j].value == TangoCellValue.MOON) ++m
        }
        if (s != m && (s + m) == grid.size) {
            invalidRows.add(i)
        }
    }
    return invalidRows
}

fun checkCols(grid: Array<Array<TangoCellData>>): Set<Int> {
    val invalidCols = hashSetOf<Int>()
    for (j in grid[0].indices) {
        var s = 0
        var m = 0
        for (i in grid.indices) {
            if (grid[i][j].value == TangoCellValue.BLANK) break
            if (grid[i][j].value == TangoCellValue.SUN) ++s
            if (grid[i][j].value == TangoCellValue.MOON) ++m
        }
        if (s != m && (s + m) == grid.size) {
            invalidCols.add(j)
        }
    }
    return invalidCols
}

fun validateTangoGrid(grid: Array<Array<TangoCellData>>, i: Int, j: Int): Boolean {
    val invalidCells = checkCells(grid)
    val invalidRows = checkRows(grid)
    val invalidCols = checkCols(grid)

    var blanks = false
    for (i in grid.indices) {
        for (j in grid[i].indices) {
            if (!blanks && grid[i][j].value == TangoCellValue.BLANK) {
                blanks = true
            }
            grid[i][j].containsError = Pair(i, j) in invalidCells ||
                    i in invalidRows ||
                    j in invalidCols
        }
    }
    return !blanks && invalidCells.isEmpty() && invalidRows.isEmpty() && invalidCols.isEmpty()
}
