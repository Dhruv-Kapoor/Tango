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

fun autooPlaceX(grid: Array<Array<QueensCellData>>, placedI: Int, placedJ: Int) {
    val toCheck = QueensCellValue.BLANK
    val newVal = QueensCellValue.CROSS
    val autoX = true
    if (grid[placedI][placedJ].value != QueensCellValue.QUEEN) {
        return
    }
    for (i in grid.indices) {
        if (grid[i][placedJ].value == toCheck) {
            grid[i][placedJ].value = newVal
            grid[i][placedJ].autoX = autoX
        }
    }
    for (j in grid[placedI].indices) {
        if (grid[placedI][j].value == toCheck) {
            grid[placedI][j].value = newVal
            grid[placedI][j].autoX = autoX
        }
    }

    if (placedI > 0) {
        if (placedJ > 0 && grid[placedI - 1][placedJ - 1].value == toCheck) {
            grid[placedI - 1][placedJ - 1].value = newVal
            grid[placedI - 1][placedJ - 1].autoX = autoX
        }
        if (placedJ < grid[0].size - 1 && grid[placedI - 1][placedJ + 1].value == toCheck) {
            grid[placedI - 1][placedJ + 1].value = newVal
            grid[placedI - 1][placedJ + 1].autoX = autoX
        }
    }
    if (placedI < grid.size - 1) {
        if (placedJ > 0 && grid[placedI + 1][placedJ - 1].value == toCheck) {
            grid[placedI + 1][placedJ - 1].value = newVal
            grid[placedI + 1][placedJ - 1].autoX = autoX
        }
        if (placedJ < grid[0].size - 1 && grid[placedI + 1][placedJ + 1].value == toCheck) {
            grid[placedI + 1][placedJ + 1].value = newVal
            grid[placedI + 1][placedJ + 1].autoX = autoX
        }
    }


    val visited = hashSetOf<Pair<Int, Int>>()
    fun dfs(i: Int, j: Int, color: Int) {
        if (i < 0 || j < 0 || i >= grid.size || j >= grid[0].size || grid[i][j].color != color ||
            Pair(i, j) in visited
        ) {
            return
        }
        visited.add(Pair(i, j))
        if (grid[i][j].value == toCheck) {
            grid[i][j].value = newVal
            grid[i][j].autoX = autoX
        }
        dfs(i + 1, j, color)
        dfs(i, j + 1, color)
        dfs(i - 1, j, color)
        dfs(i, j - 1, color)
    }

    dfs(placedI, placedJ, grid[placedI][placedJ].color)
}

fun autoRemoveX(grid: Array<Array<QueensCellData>>, placedI: Int, placedJ: Int) {
    val toCheck = QueensCellValue.CROSS
    val newVal = QueensCellValue.BLANK
    val autoX = false
    if (grid[placedI][placedJ].value != QueensCellValue.QUEEN) {
        return
    }
    for (i in grid.indices) {
        if (grid[i][placedJ].value == toCheck && grid[i][placedJ].autoX) {
            grid[i][placedJ].value = newVal
            grid[i][placedJ].autoX = autoX
        }
    }
    for (j in grid[placedI].indices) {
        if (grid[placedI][j].value == toCheck && grid[placedI][j].autoX) {
            grid[placedI][j].value = newVal
            grid[placedI][j].autoX = autoX
        }
    }

    if (placedI > 0) {
        if (placedJ > 0 && grid[placedI - 1][placedJ - 1].value == toCheck && grid[placedI - 1][placedJ - 1].autoX) {
            grid[placedI - 1][placedJ - 1].value = newVal
            grid[placedI - 1][placedJ - 1].autoX = autoX
        }
        if (placedJ < grid[0].size - 1 && grid[placedI - 1][placedJ + 1].value == toCheck && grid[placedI - 1][placedJ + 1].autoX) {
            grid[placedI - 1][placedJ + 1].value = newVal
            grid[placedI - 1][placedJ + 1].autoX = autoX
        }
    }
    if (placedI < grid.size - 1) {
        if (placedJ > 0 && grid[placedI + 1][placedJ - 1].value == toCheck && grid[placedI + 1][placedJ - 1].autoX) {
            grid[placedI + 1][placedJ - 1].value = newVal
            grid[placedI + 1][placedJ - 1].autoX = autoX
        }
        if (placedJ < grid[0].size - 1 && grid[placedI + 1][placedJ + 1].value == toCheck && grid[placedI + 1][placedJ + 1].autoX) {
            grid[placedI + 1][placedJ + 1].value = newVal
            grid[placedI + 1][placedJ + 1].autoX = autoX
        }
    }


    val visited = hashSetOf<Pair<Int, Int>>()
    fun dfs(i: Int, j: Int, color: Int) {
        if (i < 0 || j < 0 || i >= grid.size || j >= grid[0].size || grid[i][j].color != color ||
            Pair(i, j) in visited
        ) {
            return
        }
        visited.add(Pair(i, j))
        if (grid[i][j].value == toCheck && grid[i][j].autoX) {
            grid[i][j].value = newVal
            grid[i][j].autoX = autoX
        }
        dfs(i + 1, j, color)
        dfs(i, j + 1, color)
        dfs(i - 1, j, color)
        dfs(i, j - 1, color)
    }

    dfs(placedI, placedJ, grid[placedI][placedJ].color)
}


fun autoPlaceX(grid: Array<Array<QueensCellData>>, placedI: Int, placedJ: Int) {
    val orgValue = grid[placedI][placedJ].value
    val queens = hashSetOf<Pair<Int, Int>>()
    val queenRows = hashSetOf<Int>()
    val queenCols = hashSetOf<Int>()
    for (i in grid.indices) {
        for (j in grid[i].indices) {
            if (grid[i][j].value == QueensCellValue.QUEEN) {
                queens.add(Pair(i, j))
                queenRows.add(i)
                queenCols.add(j)
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
                                Pair(i + 1, j + 1) in queens
                        )
            ) {
                grid[i][j].value = QueensCellValue.CROSS
                grid[i][j].autoX = true
            }
        }
    }
}
