package com.example.tango.viewmodels

import androidx.lifecycle.ViewModel
import com.example.tango.dataClasses.User
import com.example.tango.utils.FirestoreUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InviteUsersModalViewModel : ViewModel() {

    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    init {
        FirestoreUtils.fetchAllUsers {
            _users.value = it
            _loading.value = false
        }
    }

}