package com.example.tango.viewmodels

import androidx.lifecycle.ViewModel
import com.example.tango.BuildConfig
import com.example.tango.Routes
import com.example.tango.dataClasses.User
import com.example.tango.utils.FirestoreUtils
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class BaseViewModel(preview: Boolean = false) : ViewModel() {
    lateinit var gridId: String

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

    init {
        if (!preview) {
            updateLoggedIn()
            checkAppDeprecated()
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
        FirestoreUtils.getLatestConfig { config ->
            _config.value = config
            if (BuildConfig.VERSION_NAME in (config["deprecatedVersions"] as List<*>)) {
                _isDeprecated.value = true
            }
        }
    }

    fun onStart() {
        _started.value = true
    }

    fun onComplete() {
        _completed.value = true
        if (_isLoggedIn.value) {
            FirestoreUtils.pushScore(gridId, _currentUser.value!!.id, ticks.value)
        }
    }

    fun onTick() {
        _ticks.value++
    }

    fun onSignUpCompleted(user: FirebaseUser) {
        FirestoreUtils.addUser(user)
        updateLoggedIn()
        if (_completed.value) {
            FirestoreUtils.pushScore(gridId, user.uid, ticks.value)
        }
    }

}