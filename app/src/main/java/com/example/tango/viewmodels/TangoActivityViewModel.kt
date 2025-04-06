package com.example.tango.viewmodels

import com.example.tango.dataClasses.TangoCellData
import com.example.tango.dataClasses.TangoCellValue
import com.example.tango.utils.FirestoreUtils
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TangoActivityViewModel : BaseViewModel() {
    lateinit var gridId: String

    private val _grid = MutableStateFlow<Array<Array<TangoCellData>>?>(null)
    val grid = _grid.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _completed = MutableStateFlow(false)
    val completed: StateFlow<Boolean> = _completed.asStateFlow()

    private val _started = MutableStateFlow(false)
    val started: StateFlow<Boolean> = _started.asStateFlow()

    private val _ticks = MutableStateFlow(0)
    val ticks = _ticks.asStateFlow()

    init {
        FirestoreUtils.getLatestTangoGrid {
            _grid.value = it.grid
            gridId = it.id
            val user = _currentUser.value
            if (user != null) {
                FirestoreUtils.getGridState(it.id, user.uid) { state ->
                    if (state != null) {
                        _grid.value = FirestoreUtils.parseGridStr(state["grid"] as String)
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

    fun saveState() {
        val user = _currentUser.value
        val grid = _grid.value
        if (user != null && grid != null) {
            FirestoreUtils.pushGridState(
                gridId = gridId,
                userId = user.uid,
                state = mapOf(
                    "grid" to FirestoreUtils.convertGridToStr(grid),
                    "completed" to _completed.value,
                    "timeTaken" to _ticks.value,
                    "updatedOn" to Timestamp.now()
                )
            )
        }
    }

    fun onStart() {
        _started.value = true
    }

    fun onComplete() {
        _completed.value = true
        if (_isLoggedIn.value) {
            FirestoreUtils.pushScore(gridId, _currentUser.value!!.uid, ticks.value)
        }
    }

    fun onTick() {
        _ticks.value++
    }

    fun resetGrid() {
        clearGrid(_grid.value!!)
        if (_completed.value) {
            _ticks.value = 0
            _completed.value = false
        }
    }

    fun clearGrid(grid: Array<Array<TangoCellData>>) {
        grid.forEach { row ->
            row.forEach { cell ->
                if (!cell.disabled) {
                    cell.value = TangoCellValue.BLANK
                    cell.containsError = false
                }
            }
        }

    }

    fun onSignUpCompleted(user: FirebaseUser) {
        FirestoreUtils.addUser(user)
        updateLoggedIn()
        if (_completed.value) {
            FirestoreUtils.pushScore(gridId, user.uid, ticks.value)
        }
    }

}