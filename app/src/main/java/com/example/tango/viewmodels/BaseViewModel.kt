package com.example.tango.viewmodels

import androidx.lifecycle.ViewModel
import com.example.tango.BuildConfig
import com.example.tango.utils.FirestoreUtils
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class BaseViewModel : ViewModel() {

    protected val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    protected val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser = _currentUser.asStateFlow()

    protected val _isDeprecated = MutableStateFlow<Boolean>(false)
    val isDeprecated = _isDeprecated.asStateFlow()

    protected val _config = MutableStateFlow<Map<String, Any>?>(null)
    val config = _config.asStateFlow()

    init {
        updateLoggedIn()
        checkAppDeprecated()
    }

    fun updateLoggedIn() {
        val user = FirestoreUtils.getCurrentUser()
        if (user != null) {
            _currentUser.value = user
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
}