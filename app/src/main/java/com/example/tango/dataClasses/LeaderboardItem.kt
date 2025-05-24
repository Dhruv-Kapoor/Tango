package com.example.tango.dataClasses

import java.util.Objects

data class LeaderboardItem(
    var user: User,
    var timeTaken: Int,
    val attempts: List<Map<String, Any>>
) {
    override fun hashCode(): Int {
        return Objects.hash(user, timeTaken)
    }
}
