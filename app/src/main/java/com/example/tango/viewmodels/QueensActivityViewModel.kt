package com.example.tango.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.tango.CELL_UPDATE_THROTTLE
import com.example.tango.GRID_TYPES
import com.example.tango.dataClasses.Grid
import com.example.tango.dataClasses.QueensCellData
import com.example.tango.dataClasses.QueensCellValue
import com.example.tango.utils.FirestoreUtils
import com.example.tango.utils.QueensAction
import com.example.tango.utils.UndoStack
import com.example.tango.utils.autoPlaceX
import com.example.tango.utils.validateQueensGrid
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class QueensActivityViewModel(preview: Boolean = false) : BaseViewModel(preview) {
    private val _grid = MutableStateFlow<Array<Array<QueensCellData>>?>(null)
    val grid = _grid.asStateFlow()
    val undoStack = UndoStack<QueensAction>()
    private var validatorJob: Job? = null

    lateinit var latestGridData: Grid<QueensCellData>

    init {
        if (!preview) {
            val user = _currentUser.value

            FirestoreUtils.getLatestQueensGrid {
                _grid.value = it.grid
                gridId = it.id
                gridNumber = it.number
                latestGridData = it
                fetchUserStateAndStopLoading()
            }
            if (user != null) {
                FirestoreUtils.getAttemptedGridNumbers(GRID_TYPES.QUEENS.value, user.id) {
                    attemptedGridNumbers = it
                }
            }
        }
    }

    fun fetchUserStateAndStopLoading() {
        val user = _currentUser.value
        if (user != null) {
            FirestoreUtils.getGridState(gridId, user.id) { state ->
                if (state != null) {
                    _grid.value =
                        FirestoreUtils.parseQueensGridStr(state["grid"] as String)
                    _completed.value = (state["completed"] as Boolean?) == true
                    _ticks.value = (state["timeTaken"] as Long).toInt()
                    if (_completed.value) {
                        _started.value = true
                    } else {
                        validateQueensGrid(_grid.value!!)
                    }
                }
                _loading.value = false
            }
        } else {
            _loading.value = false
        }
    }

    override fun getGrid(): Array<Array<Any>>? {
        return _grid.value as Array<Array<Any>>?
    }

    fun resetGrid() {
        clearGrid(_grid.value!!)
        if (_completed.value) {
            _ticks.value = 0
            _completed.value = false
        }
        undoStack.clear()
    }

    fun clearGrid(grid: Array<Array<QueensCellData>>) {
        grid.forEach { row ->
            row.forEach { cell ->
                cell.value = QueensCellValue.BLANK
                cell.containsError = false
            }
        }

    }

    fun onCellUpdate(cell: QueensCellData, i: Int, j: Int) {
        undoStack.onAction(QueensAction(cell.value, Pair(i, j)))
        cell.value = (cell.value % 3) + 1
        validatorJob?.cancel()
        val grid = _grid.value
        validatorJob = viewModelScope.launch {
            autoPlaceX(grid!!, i, j)
            delay(CELL_UPDATE_THROTTLE)
            if (validateQueensGrid(grid) && !_completed.value) {
                onComplete()
            }
        }
    }

    fun onUndo() {
        val action = undoStack.onUndo()
        val grid = _grid.value
        if (action != null && grid != null) {
            grid[action.location.first][action.location.second].value = action.oldValue
            autoPlaceX(grid, action.location.first, action.location.second)
            validateQueensGrid(grid)
        }
    }

    fun onDrag(i: Int, j: Int) {
        val grid = _grid.value
        if (grid!![i][j].value == QueensCellValue.BLANK) {
            undoStack.onAction(QueensAction(grid[i][j].value, Pair(i, j)))
            grid[i][j].value = QueensCellValue.CROSS
        }
    }

    override fun pushScore() {
        if (_isLoggedIn.value) {
            FirestoreUtils.pushScore(
                gridId, _currentUser.value!!.id, ticks.value, gridNumber,
                GRID_TYPES.QUEENS.value
            )
        }
    }

    fun getMinDate(): LocalDate {
        return (_config.value?.get("minQueensDate") as Timestamp).toInstant()
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
        FirestoreUtils.getGrid<QueensCellData>(selectedGridNumber, GRID_TYPES.QUEENS.value) {
            _grid.value = it.grid
            gridId = it.id
            gridNumber = it.number

            fetchUserStateAndStopLoading()
        }
    }

}