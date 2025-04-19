package com.example.tango.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.tango.CELL_UPDATE_THROTTLE
import com.example.tango.GRID_TYPES
import com.example.tango.dataClasses.Grid
import com.example.tango.dataClasses.TangoCellData
import com.example.tango.dataClasses.TangoCellValue
import com.example.tango.utils.FirestoreUtils
import com.example.tango.utils.TangoAction
import com.example.tango.utils.UndoStack
import com.example.tango.utils.validateTangoGrid
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class TangoActivityViewModel(preview: Boolean = false) : BaseViewModel(preview) {
    private val _grid = MutableStateFlow<Array<Array<TangoCellData>>?>(null)
    val grid = _grid.asStateFlow()
    val undoStack = UndoStack<TangoAction>()
    private var validatorJob: Job? = null

    lateinit var latestGridData: Grid<TangoCellData>

    init {
        if (!preview) {
            FirestoreUtils.getLatestTangoGrid {
                _grid.value = it.grid
                gridId = it.id
                gridNumber = it.number
                latestGridData = it
                fetchUserStateAndStopLoading()
            }
            fetchAttemptedGrids()
        }
    }

    override fun fetchUserStateAndStopLoading() {
        val user = _currentUser.value
        if (user != null) {
            FirestoreUtils.getGridState(gridId, user.id) { state ->
                if (state != null) {
                    _grid.value = FirestoreUtils.parseTangoGridStr(state["grid"] as String)
                    _completed.value = (state["completed"] as Boolean?) == true
                    _ticks.value = (state["timeTaken"] as Long).toInt()
                    if (_completed.value) {
                        _started.value = true
                    } else {
                        validateTangoGrid(_grid.value!!)
                    }
                }
                _loading.value = false
            }
        } else {
            _loading.value = false
        }
    }

    override fun fetchAttemptedGrids() {
        val user = _currentUser.value
        if (user != null) {
            FirestoreUtils.getAttemptedGridNumbers(GRID_TYPES.TANGO.value, user.id) {
                attemptedGridNumbers = it
            }
        }
    }

    fun resetGrid() {
        clearGrid(_grid.value!!)
        if (_completed.value) {
            _ticks.value = 0
            _completed.value = false
        }
        undoStack.clear()
    }

    fun clearGrid(grid: Array<Array<TangoCellData>>) {
        grid.forEach { row ->
            row.forEach { cell ->
                if (!cell.disabled) {
                    cell.value = TangoCellValue.BLANK
                }
                cell.containsError = false
            }
        }

    }

    fun onCellUpdate(cell: TangoCellData, i: Int, j: Int) {
        undoStack.onAction(TangoAction(cell.value, Pair(i, j)))
        cell.value = (cell.value % 3) + 1
        if (cell.value == TangoCellValue.SUN) {
            validatorJob?.cancel()
            validatorJob = viewModelScope.launch {
                delay(CELL_UPDATE_THROTTLE)
                if (validateTangoGrid(_grid.value!!) && !completed.value) {
                    onComplete()
                }
            }
        } else {
            if (validateTangoGrid(_grid.value!!) && !completed.value) {
                onComplete()
            }
        }
    }

    fun onUndo() {
        val action = undoStack.onUndo()
        val grid = _grid.value
        if (action != null && grid != null) {
            grid[action.location.first][action.location.second].value = action.oldValue
            validateTangoGrid(grid)
        }
    }

    override fun pushScore() {
        if (_isLoggedIn.value) {
            FirestoreUtils.pushScore(
                gridId, _currentUser.value!!.id, ticks.value, gridNumber,
                GRID_TYPES.TANGO.value
            )
        }
    }

    override fun getGrid(): Array<Array<Any>>? {
        return _grid.value as Array<Array<Any>>?
    }

    fun getMinDate(): LocalDate {
        return (_config.value?.get("minTangoDate") as Timestamp).toInstant()
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
        FirestoreUtils.getGrid<TangoCellData>(selectedGridNumber, GRID_TYPES.TANGO.value) {
            _grid.value = it.grid
            gridId = it.id
            gridNumber = it.number
            fetchUserStateAndStopLoading()
        }
    }

}