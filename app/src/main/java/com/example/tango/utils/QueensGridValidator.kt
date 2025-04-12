package com.example.tango.utils

import com.example.tango.dataClasses.QueensCellData
import com.example.tango.dataClasses.QueensCellValue

fun checkSurroundingCells(grid: Array<Array<QueensCellData>>, i: Int, j: Int): Set<Pair<Int, Int>> {
    val invalidCells = hashSetOf<Pair<Int, Int>>()
    for (i in grid.indices) {
        for (j in grid[i].indices) {
            if (i > 0 && grid[i][j].value == QueensCellValue.QUEEN) {
                if (j > 0 && grid[i - 1][j - 1].value == QueensCellValue.QUEEN) {
                    invalidCells.add(Pair(i, j))
                    invalidCells.add(Pair(i - 1, j - 1))
                }
                if (j < grid[i].size - 1 && grid[i - 1][j + 1].value == QueensCellValue.QUEEN) {
                    invalidCells.add(Pair(i, j))
                    invalidCells.add(Pair(i - 1, j + 1))
                }
            }
        }
    }
    return invalidCells
}

fun checkRows(grid: Array<Array<QueensCellData>>): Set<Int> {
    val invalidRows = hashSetOf<Int>()
    for (i in grid.indices) {
        var queensCount = 0
        for (j in grid[i].indices) {
            if (grid[i][j].value == QueensCellValue.QUEEN) {
                ++queensCount
            }
        }
        if (queensCount > 1) {
            invalidRows.add(i)
        }
    }
    return invalidRows
}

fun checkCols(grid: Array<Array<QueensCellData>>): Set<Int> {
    val invalidCols = hashSetOf<Int>()
    for (j in grid[0].indices) {
        var queensCount = 0
        for (i in grid.indices) {
            if (grid[i][j].value == QueensCellValue.QUEEN) {
                ++queensCount
            }
        }
        if (queensCount > 1) {
            invalidCols.add(j)
        }
    }
    return invalidCols
}

fun checkColors(grid: Array<Array<QueensCellData>>): Set<Int> {
    val colorsCount = hashMapOf<Int, Int>().withDefault { _ -> 0 }
    val invalidColors = hashSetOf<Int>()
    for (i in grid.indices) {
        for (j in grid[i].indices) {
            if (grid[i][j].value == QueensCellValue.QUEEN) {
                colorsCount[grid[i][j].color] = colorsCount.getValue(grid[i][j].color) + 1
                if (colorsCount[grid[i][j].color]!! > 1) {
                    invalidColors.add(grid[i][j].color)
                }
            }
        }
    }
    return invalidColors
}

fun validateQueensGrid(grid: Array<Array<QueensCellData>>, i: Int, j: Int): Boolean {
    val invalidCells = checkSurroundingCells(grid, i, j)
    val invalidRows = checkRows(grid)
    val invalidCols = checkCols(grid)
    val invalidColors = checkColors(grid)

    var blanks = false
    for (i in grid.indices) {
        for (j in grid[i].indices) {
            if (!blanks && grid[i][j].value == QueensCellValue.BLANK) {
                blanks = true
            }
            grid[i][j].containsError = Pair(i, j) in invalidCells ||
                    i in invalidRows ||
                    j in invalidCols ||
                    grid[i][j].color in invalidColors
        }
    }
    return !blanks && invalidCells.isEmpty() && invalidRows.isEmpty() && invalidCols.isEmpty()
}

fun autoPlaceX(grid: Array<Array<QueensCellData>>, placedI: Int, placedJ: Int) {
    val queens = hashSetOf<Pair<Int, Int>>()
    val queenRows = hashSetOf<Int>()
    val queenCols = hashSetOf<Int>()
    val colors = hashSetOf<Int>()
    for (i in grid.indices) {
        for (j in grid[i].indices) {
            if (grid[i][j].value == QueensCellValue.QUEEN) {
                queens.add(Pair(i, j))
                queenRows.add(i)
                queenCols.add(j)
                colors.add(grid[i][j].color)
            } else if (grid[i][j].value == QueensCellValue.CROSS && grid[i][j].autoX) {
                grid[i][j].value = QueensCellValue.BLANK
            }
            grid[i][j].autoX = false
        }
    }
    for (i in grid.indices) {
        for (j in grid[i].indices) {
            if (grid[i][j].value == QueensCellValue.BLANK && (
                        i in queenRows || j in queenCols ||
                                Pair(i - 1, j - 1) in queens ||
                                Pair(i - 1, j + 1) in queens ||
                                Pair(i + 1, j - 1) in queens ||
                                Pair(i + 1, j + 1) in queens ||
                                grid[i][j].color in colors
                        )
            ) {
                grid[i][j].value = QueensCellValue.CROSS
                grid[i][j].autoX = true
            }
        }
    }
}
