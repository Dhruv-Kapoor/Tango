package com.example.tango.viewmodels

import com.example.tango.GRID_TYPES
import com.example.tango.dataClasses.Grid
import com.example.tango.dataClasses.ZipCellData
import com.example.tango.utils.FirestoreUtils
import com.example.tango.utils.UndoStack
import com.example.tango.utils.ZipAction
import com.example.tango.utils.validateZipGrid
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

class ZipActivityViewModel(preview: Boolean = false) : BaseViewModel(preview) {
    private val _grid = MutableStateFlow<Array<Array<ZipCellData>>?>(null)
    val grid = _grid.asStateFlow()

    val undoStack = UndoStack<ZipAction>(100)

    lateinit var latestGridData: Grid<ZipCellData>
    private val _path = MutableStateFlow(arrayListOf<Pair<Int, Int>>())
    val path = _path.asStateFlow()

    var currentZipAction: ZipAction? = null
    var dragStartedAt: Pair<Int, Int>? = null

    init {
        if (!preview) {

            FirestoreUtils.getLatestZipGrid {
                _grid.value = it.grid
                gridId = it.id
                gridNumber = it.number
                latestGridData = it
                clearGrid()
                fetchUserStateAndStopLoading()
            }

            fetchAttemptedGrids()
        }
    }

    override fun fetchAttemptedGrids() {
        val user = _currentUser.value
        if (user != null) {
            FirestoreUtils.getAttemptedGridNumbers(GRID_TYPES.ZIP.value, user.id) {
                attemptedGridNumbers = it
            }
        }
    }

    fun makePathUsingGrid() {
        val grid = _grid.value!!
        var size = 0
        grid.forEach { row ->
            row.forEach { cell ->
                if (cell.pathPosition != null) {
                    ++size
                }
            }
        }

        val path = arrayListOf<Pair<Int, Int>>()
        for (i in 1..size) {
            path.add(Pair(0, 0))
        }

        grid.forEachIndexed { i, row ->
            row.forEachIndexed { j, cell ->
                val position = cell.pathPosition
                if (position != null) {
                    path[position] = Pair(i, j)
                }
            }
        }

        _path.value = path
    }

    override fun fetchUserStateAndStopLoading() {
        val user = _currentUser.value
        if (user != null) {
            FirestoreUtils.getGridState(gridId, user.id) { state ->
                if (state != null) {
                    _grid.value = FirestoreUtils.parseZipGridStr(state["grid"] as String)
                    _completed.value = (state["completed"] as Boolean?) == true
                    _ticks.value = (state["timeTaken"] as Long).toInt()
                    if (_completed.value) {
                        _started.value = true
                    }
                    makePathUsingGrid()
                }
                _loading.value = false
            }
        } else {
            _loading.value = false
        }
    }

    fun resetGrid() {
        clearGrid()
        if (_completed.value) {
            _ticks.value = 0
            _completed.value = false
        }
        undoStack.clear()
    }

    fun clearGrid() {
        val grid = _grid.value
        if (grid != null) {
            for (i in grid.indices) {
                for (j in grid[i].indices) {
                    if (grid[i][j].value == 1) {
                        _path.value = arrayListOf(Pair(i, j))
                        grid[i][j].pathPosition = 0
                    } else {
                        grid[i][j].pathPosition = null
                    }
                }
            }
        }
    }

    fun onCellUpdate(cell: ZipCellData, i: Int, j: Int) {
        val path = _path.value
        val grid = _grid.value
        if (cell.pathPosition != null && grid != null &&
            Pair(i, j) != path.last()
        ) {
            undoStack.onAction(ZipAction(path))
            for (i in (cell.pathPosition!! + 1)..(path.size - 1)) {
                grid[path[i].first][path[i].second].containsError = false
                grid[path[i].first][path[i].second].pathPosition = null
            }
            _path.value =
                path.dropLast(path.size - cell.pathPosition!! - 1) as ArrayList<Pair<Int, Int>>
        }
    }

    fun onUndo() {
        val action = undoStack.onUndo()
        val grid = _grid.value
        if (action != null && grid != null) {
            val currentPath = _path.value
            currentPath.forEach { coordinates ->
                grid[coordinates.first][coordinates.second].pathPosition = null
                grid[coordinates.first][coordinates.second].containsError = false
            }
            action.path.forEachIndexed { i, coordinates ->
                grid[coordinates.first][coordinates.second].pathPosition = i
            }
            _path.value = action.path as ArrayList<Pair<Int, Int>>
        }
    }

    override fun pushScore() {
        if (_isLoggedIn.value) {
            FirestoreUtils.pushScore(
                gridId, _currentUser.value!!.id, ticks.value, gridNumber,
                GRID_TYPES.ZIP.value
            )
        }
    }

    override fun getGrid(): Array<Array<Any>>? {
        return _grid.value as Array<Array<Any>>?
    }

    fun getMinDate(): LocalDate {
        return (_config.value?.get("minZipDate") as Timestamp).toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun setGridForDate(selectedGridNumber: Int) {
        if (selectedGridNumber == gridNumber) {
            return
        }
        saveState()

        _loading.value = true
        _started.value = false
        _completed.value = false
        _ticks.value = 0
        FirestoreUtils.getGrid<ZipCellData>(selectedGridNumber, GRID_TYPES.ZIP.value) {
            _grid.value = it.grid
            gridId = it.id
            gridNumber = it.number
            clearGrid()
            fetchUserStateAndStopLoading()
        }
    }

    private fun canConnect(start: Pair<Int, Int>, end: Pair<Int, Int>): Boolean {
        val grid = _grid.value

        val distance = abs(start.first - end.first) + abs(start.second - end.second)
        if (distance > 1) return false
        if (grid!![end.first][end.second].pathPosition != null) return false
        if (start.first - end.first == 1) {
            return !grid[start.first][start.second].topWall
        } else if (start.first - end.first == -1) {
            return !grid[end.first][end.second].topWall
        } else if (start.second - end.second == 1) {
            return !grid[start.first][start.second].leftWall
        } else if (start.second - end.second == -1) {
            return !grid[end.first][end.second].leftWall
        }
        return true
    }

    fun onDragStart(coordinates: Pair<Int, Int>) {
        currentZipAction = ZipAction(_path.value.toList())
        dragStartedAt = coordinates
    }

    fun onDrag(coordinates: Pair<Int, Int>) {
        val path = _path.value
        val grid = _grid.value
        if (path.size > 1 && path[path.size - 2] == coordinates) {
            val pair = path.removeAt(path.size - 1)
            val cell = grid!![pair.first][pair.second]
            cell.pathPosition = null
            cell.containsError = false
        } else if (canConnect(path[path.size - 1], coordinates)) {
            path.add(coordinates)
            grid!![coordinates.first][coordinates.second].pathPosition = path.size - 1
        }
    }

    fun onDragEnd() {
        val grid = _grid.value
        val action = currentZipAction
        if (grid != null) {
            if (validateZipGrid(grid, _path.value) && !completed.value) {
                onComplete()
            }
        }
        if (dragStartedAt != _path.value.last() && action != null) {
            undoStack.onAction(action)
            dragStartedAt = null
            currentZipAction = null
        }
    }

}