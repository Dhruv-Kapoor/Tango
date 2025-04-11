package com.example.tango.viewmodels

import com.example.tango.dataClasses.LeaderboardItem
import com.example.tango.utils.FirestoreUtils
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LeaderboardActivityViewModel : BaseViewModel() {

    enum class VIEWS {
        BEST_ATTEMPTS,
        FIRST_ATTEMPTS
    }

    private var _leaderboardData = MutableStateFlow<List<LeaderboardItem>>(listOf())
    val leaderboardData = _leaderboardData.asStateFlow()

    private val _currentViewType = MutableStateFlow(VIEWS.BEST_ATTEMPTS)
    val currentViewType = _currentViewType.asStateFlow()

    private var leaderboardDataListener: ListenerRegistration? = null

    fun switchToBestAttemptsView() {
        _currentViewType.value = VIEWS.BEST_ATTEMPTS
        loadData()
    }

    fun switchToFirstAttemptsView() {
        _currentViewType.value = VIEWS.FIRST_ATTEMPTS
        loadData()
    }

    fun loadData(gridId: String = this.gridId) {
        this.gridId = gridId
        leaderboardDataListener?.remove()
        leaderboardDataListener = FirestoreUtils.getLeaderboardData(
            gridId,
            orderByBestTime = _currentViewType.value == VIEWS.BEST_ATTEMPTS
        ) {
            _leaderboardData.value = it
            if (_loading.value) {
                _loading.value = false
            }
        }
    }

}