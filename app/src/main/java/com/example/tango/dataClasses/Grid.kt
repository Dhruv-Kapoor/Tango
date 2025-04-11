package com.example.tango.dataClasses

import java.util.Date

data class Grid <T> (
    val id: String,
    val grid: Array<Array<T>>,
    val date: Date
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Grid<*>
        return id == other.id
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + grid.contentDeepHashCode()
        result = 31 * result + date.hashCode()
        return result
    }
}