package com.example.tango.viewmodels

import com.example.tango.dataClasses.QueensCellData
import com.example.tango.dataClasses.QueensCellValue
import com.example.tango.dataClasses.TangoCellValue
import com.example.tango.utils.FirestoreUtils
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class QueensActivityViewModel(preview: Boolean = false) : BaseViewModel(preview) {
    private val _grid = MutableStateFlow<Array<Array<QueensCellData>>?>(null)
    val grid = _grid.asStateFlow()

    init {
        if (!preview) {
            FirestoreUtils.getLatestQueensGrid {
                _grid.value = it.grid
                gridId = it.id
                val user = _currentUser.value
                if (user != null) {
                    FirestoreUtils.getGridState(it.id, user.id) { state ->
                        if (state != null) {
                            _grid.value =
                                FirestoreUtils.parseQueensGridStr(state["grid"] as String)
                            _completed.value = (state["completed"] as Boolean?) == true
                            _ticks.value = (state["timeTaken"] as Long).toInt()
                            if (_completed.value) {
                                _started.value = true
                            }
                        }
                        _loading.value = false
                    }
                } else {
                    _loading.value = false
                }
            }
        }
    }

    fun saveState() {
        val user = _currentUser.value
        val grid = _grid.value
        if (user != null && grid != null) {
            FirestoreUtils.pushGridState(
                gridId = gridId,
                userId = user.id,
                state = mapOf(
                    "grid" to FirestoreUtils.convertGridToStr(grid as Array<Array<Any>>),
                    "completed" to _completed.value,
                    "timeTaken" to _ticks.value,
                    "updatedOn" to Timestamp.now()
                )
            )
        }
    }

    fun resetGrid() {
        clearGrid(_grid.value!!)
        if (_completed.value) {
            _ticks.value = 0
            _completed.value = false
        }
    }

    fun clearGrid(grid: Array<Array<QueensCellData>>) {
        grid.forEach { row ->
            row.forEach { cell ->
                cell.value = QueensCellValue.BLANK
                cell.containsError = false
            }
        }

    }

}