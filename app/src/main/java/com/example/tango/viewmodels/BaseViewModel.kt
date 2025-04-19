package com.example.tango.viewmodels

import androidx.lifecycle.ViewModel
import com.example.tango.BuildConfig
import com.example.tango.dataClasses.User
import com.example.tango.utils.FirestoreUtils
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.properties.Delegates

open class BaseViewModel(preview: Boolean = false) : ViewModel() {
    lateinit var gridId: String
    var gridNumber by Delegates.notNull<Int>()
    var attemptedGridNumbers = hashSetOf<Int>()

    internal val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    internal val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    internal val _isDeprecated = MutableStateFlow<Boolean>(false)
    val isDeprecated = _isDeprecated.asStateFlow()

    internal val _config = MutableStateFlow<Map<String, Any>?>(null)
    val config = _config.asStateFlow()

    internal val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    internal val _completed = MutableStateFlow(false)
    val completed: StateFlow<Boolean> = _completed.asStateFlow()

    internal val _started = MutableStateFlow(false)
    val started: StateFlow<Boolean> = _started.asStateFlow()

    internal val _ticks = MutableStateFlow(0)
    val ticks = _ticks.asStateFlow()

    internal val _updateAvailable = MutableStateFlow(false)
    val updateAvailable = _updateAvailable.asStateFlow()

    init {
        if (!preview) {
            updateLoggedIn()
            FirestoreUtils.getLatestConfig { config ->
                _config.value = config
                checkAppDeprecated()
                checkForUpdates()
            }
        }
    }

    fun updateLoggedIn() {
        val user = FirestoreUtils.getCurrentUser()
        if (user != null) {
            _currentUser.value = User.fromFirebaseUser(user)
            _isLoggedIn.value = true
        }
    }

    fun checkAppDeprecated() {
        if (BuildConfig.VERSION_NAME in ((_config.value?.get("deprecatedVersions")
                ?: emptyList<String>()) as List<*>)
        ) {
            _isDeprecated.value = true
        }
    }

    fun checkForUpdates() {
        if (BuildConfig.VERSION_CODE < ((config.value?.get("latestVersionCode")
                ?: 0) as Long).toInt()
        ) {
            _updateAvailable.value = true
        }
    }

    fun onStart() {
        _started.value = true
    }

    fun onComplete() {
        _completed.value = true
        attemptedGridNumbers.add(gridNumber)
        pushScore()
        saveState()
    }

    open fun pushScore() {
        throw RuntimeException("Need to override push score function")
    }

    open fun getGrid(): Array<Array<Any>>? {
        throw RuntimeException("Need to override getGrid function")
    }

    fun saveState() {
        val user = _currentUser.value
        val grid = getGrid()
        if (user != null && grid != null) {
            FirestoreUtils.pushGridState(
                gridId = gridId,
                userId = user.id,
                state = mapOf(
                    "grid" to FirestoreUtils.convertGridToStr(grid),
                    "completed" to _completed.value,
                    "timeTaken" to _ticks.value,
                    "updatedOn" to Timestamp.now()
                )
            )
        }
    }

    fun onTick() {
        _ticks.value++
    }

    open fun fetchUserStateAndStopLoading() {
        throw RuntimeException("Need to override fetchUserStateAndStopLoading function")
    }

    open fun fetchAttemptedGrids() {
        throw RuntimeException("Need to override fetchAttemptedGrids function")
    }

    fun onSignUpCompleted(user: FirebaseUser) {
        _loading.value = true
        FirestoreUtils.addUser(user)
        updateLoggedIn()
        if (_completed.value) {
            pushScore()
        } else {
            fetchUserStateAndStopLoading()
        }
        fetchAttemptedGrids()
    }

}