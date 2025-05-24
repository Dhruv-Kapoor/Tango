package com.example.tango.dataClasses

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Objects

data class LeaderboardItem(
    var user: User,
    var timeTaken: Int,
    val attempts: List<Map<String, Any>>
) {
    var expanded by mutableStateOf(false)

    override fun hashCode(): Int {
        return Objects.hash(user, timeTaken)
    }
}
