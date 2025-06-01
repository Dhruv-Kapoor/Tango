package com.example.tango.dataClasses

import com.example.tango.utils.Gson
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import kotlin.random.Random

object ROOM_STATUS {
    const val CREATED = 1
    const val READY = 2
    const val STARTED = 3
    const val COMPLETED = 4
}

@IgnoreExtraProperties
data class GameRoom (
    val id: String = "",
    val number: String = "",
    val player1: String? = null,
    val player2: String? = null,
    var status: Int = ROOM_STATUS.CREATED,
    var turn: String? = null,
    var grid: String? = null,
    var firstTurnUserID: String? = null,
    var winner: String? = null,
) {
    @Exclude
    @set:Exclude
    @get:Exclude
    var parsedGridCache: Array<Array<Any>>? = null

    @Exclude
    inline fun <reified T> getParsedGrid(): Array<Array<T>> {
        if (this.grid == null) {
            this.parsedGridCache = getDefaultGrid<T>(Pair(3,3)) as Array<Array<Any>>?
            return parsedGridCache as Array<Array<T>>
        }
        if (parsedGridCache != null) {
            return parsedGridCache as Array<Array<T>>
        }
        val type: Type = object : TypeToken<Array<Array<T>>>() {}.type
        this.parsedGridCache = Gson.getGson().fromJson(this.grid, type)
        return parsedGridCache as Array<Array<T>>
    }

    companion object {
        @Exclude
        fun generateRoomId(): String {
            val randomId = Random.nextInt(100, 1000)
            return randomId.toString()
        }
        @Exclude
        inline fun <reified T> getDefaultGrid(size: Pair<Int, Int>): Array<Array<T>> {
            return Array(size.first) {
                Array(size.second) { T::class.java.getDeclaredConstructor().newInstance() }
            }
        }
    }



    override fun hashCode(): Int {
        var result = status
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + (player1?.hashCode() ?: 0)
        result = 31 * result + (player2?.hashCode() ?: 0)
        result = 31 * result + (turn?.hashCode() ?: 0)
        result = 31 * result + (grid?.hashCode() ?: 0)
        result = 31 * result + (firstTurnUserID?.hashCode() ?: 0)
        result = 31 * result + (parsedGridCache?.contentDeepHashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameRoom

        if (status != other.status) return false
        if (id != other.id) return false
        if (player1 != other.player1) return false
        if (player2 != other.player2) return false
        if (turn != other.turn) return false
        if (grid != other.grid) return false
        if (firstTurnUserID != other.firstTurnUserID) return false
        if (!parsedGridCache.contentDeepEquals(other.parsedGridCache)) return false

        return true
    }
}