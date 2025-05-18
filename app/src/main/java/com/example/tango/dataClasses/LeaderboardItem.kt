package com.example.tango.dataClasses

data class LeaderboardItem(
    var user: User,
    var timeTaken: Int,
    val attempts: List<Map<String, Any>>
)
