package com.example.tango.utils

import android.util.Log
import com.example.tango.BuildConfig
import com.example.tango.GRID_TYPES
import com.example.tango.dataClasses.Grid
import com.example.tango.dataClasses.LeaderboardItem
import com.example.tango.dataClasses.QueensCellData
import com.example.tango.dataClasses.TangoCellData
import com.example.tango.dataClasses.User
import com.example.tango.dataClasses.ZipCellData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.ZoneId

object FirestoreUtils {

    private const val TAG = "FirestoreUtils"

    enum class COLLECTIONS(val value: String) {
        GRIDS("grids"), USERS("users"), PARTICIPANTS("participants"), CONFIG("config"),
        ANONYMOUS_USERS("anonymousUsers")
    }

    private fun getDb(): FirebaseFirestore {
        return Firebase.firestore
    }

    fun parseTangoGridStr(gridStr: String): Array<Array<TangoCellData>> {
        return Gson.getGson().fromJson(gridStr, Array<Array<TangoCellData>>::class.java)
    }

    fun parseQueensGridStr(gridStr: String): Array<Array<QueensCellData>> {
        val grid = Gson.getGson().fromJson(gridStr, Array<Array<QueensCellData>>::class.java)
        populateQueensGrid(grid)
        return grid
    }

    fun parseZipGridStr(gridStr: String): Array<Array<ZipCellData>> {
        return Gson.getGson().fromJson(gridStr, Array<Array<ZipCellData>>::class.java)
    }

    fun convertGridToStr(grid: Array<Array<Any>>): String {
        return Gson.getGson().toJson(grid)
    }

    fun getLatestTangoGrid(callback: (Grid<TangoCellData>) -> Unit) {
        getDb().collection(COLLECTIONS.GRIDS.value).whereEqualTo("type", GRID_TYPES.TANGO.value)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(1).get().addOnCompleteListener() {
                val gridDoc = it.result.documents[0]
                val gridStr = gridDoc.data?.get("grid") as String
                val grid = parseTangoGridStr(gridStr)
                callback(
                    Grid<TangoCellData>(
                        id = gridDoc.id,
                        grid = grid,
                        date = (gridDoc.data?.get("date") as Timestamp).toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate(),
                        number = (gridDoc.data?.get("number") as Long).toInt()
                    )
                )
            }
    }

    fun populateQueensGrid(grid: Array<Array<QueensCellData>>) {
        grid.forEachIndexed { i, row ->
            row.forEachIndexed { j, cell ->
                if (i == 0) {
                    cell.topColorId = cell.color
                } else {
                    cell.topColorId = grid[i - 1][j].color
                }
                if (j == 0) {
                    cell.leftColorId = cell.color
                } else {
                    cell.leftColorId = grid[i][j - 1].color
                }
            }
        }
    }

    fun getLatestQueensGrid(callback: (Grid<QueensCellData>) -> Unit) {
        getDb().collection(COLLECTIONS.GRIDS.value).whereEqualTo("type", GRID_TYPES.QUEENS.value)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(1).get().addOnCompleteListener() {
                val gridDoc = it.result.documents[0]
                val gridStr = gridDoc.data?.get("grid") as String
                val grid = parseQueensGridStr(gridStr)
                callback(
                    Grid<QueensCellData>(
                        id = gridDoc.id,
                        grid = grid,
                        date = (gridDoc.data?.get("date") as Timestamp).toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate(),
                        number = (gridDoc.data?.get("number") as Long).toInt()
                    )
                )
            }
    }

    fun getLatestZipGrid(callback: (Grid<ZipCellData>) -> Unit) {
        getDb().collection(COLLECTIONS.GRIDS.value).whereEqualTo("type", GRID_TYPES.ZIP.value)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(1).get().addOnCompleteListener() {
                val gridDoc = it.result.documents[0]
                val gridStr = gridDoc.data?.get("grid") as String
                val grid = parseZipGridStr(gridStr)
                callback(
                    Grid<ZipCellData>(
                        id = gridDoc.id,
                        grid = grid,
                        date = (gridDoc.data?.get("date") as Timestamp).toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate(),
                        number = (gridDoc.data?.get("number") as Long).toInt()
                    )
                )
            }
    }

    fun addUser(user: FirebaseUser) {
        getDb().collection(COLLECTIONS.USERS.value).document(user.uid).set(
            mapOf(
                Pair("id", user.uid),
                Pair("name", user.displayName),
                Pair("profilePic", user.photoUrl),
                Pair("email", user.email),
            ),
            SetOptions.merge()
        )
    }

    fun getCurrentUser(): FirebaseUser? {
        return Firebase.auth.currentUser
    }

    fun getUserScore(gridId: String, userId: String, onResult: (Map<String, Any>?) -> Unit) {
        getDb().collection(COLLECTIONS.GRIDS.value).document(gridId)
            .collection(COLLECTIONS.PARTICIPANTS.value).document(userId).get()
            .addOnSuccessListener {
                onResult(it.data)
            }
    }

    fun pushScore(gridId: String, userId: String, timeTaken: Int, gridNumber: Int, gridType: Int) {
        getUserScore(gridId, userId) {
            var bestAttempt = timeTaken
            var firstAttempt = timeTaken
            var firstAttemptOn = Timestamp.now()
            var bestAttemptOn: Any? = Timestamp.now()
            var attempts = listOf(
                mapOf(
                    "timeTaken" to timeTaken,
                    "attemptedOn" to Timestamp.now()
                )
            )
            if (it != null && it["firstAttempt"] != null) {
                firstAttempt = (it["firstAttempt"] as Long).toInt()
                firstAttemptOn = it["firstAttemptOn"] as Timestamp
                val prevBestAttempt = (it["bestAttempt"] as Long).toInt()
                if (prevBestAttempt < bestAttempt) {
                    bestAttempt = prevBestAttempt
                    bestAttemptOn = it["bestAttemptOn"]
                }
                attempts = (it["attempts"] as List<Map<String, Comparable<Any>>>).plus(attempts)
            }
            getDb().collection(COLLECTIONS.GRIDS.value).document(gridId)
                .collection(COLLECTIONS.PARTICIPANTS.value).document(userId).set(
                    mapOf(
                        Pair("id", userId),
                        Pair("firstAttempt", firstAttempt),
                        Pair("bestAttempt", bestAttempt),
                        Pair("firstAttemptOn", firstAttemptOn),
                        Pair("bestAttemptOn", bestAttemptOn),
                        Pair("attempts", attempts),
                        Pair("gridId", gridId),
                        Pair("gridNumber", gridNumber),
                        Pair("gridType", gridType)
                    ),
                    SetOptions.merge()
                )
        }
    }

    fun fetchUsers(userIds: List<String>, onResult: (Map<String, Map<String, Any>?>) -> Unit) {
        val users = mutableMapOf<String, Map<String, Any>?>()
        getDb().collection(COLLECTIONS.USERS.value).whereIn(FieldPath.documentId(), userIds).get()
            .addOnSuccessListener {
                it.documents.forEach { doc ->
                    users.put(doc.id, doc.data)
                }
                onResult(users)
            }
    }

    fun getLeaderboardData(
        gridId: String,
        orderByBestTime: Boolean = true,
        onResult: (List<LeaderboardItem>) -> Unit
    ): ListenerRegistration {
        val orderingField = if (orderByBestTime) "bestAttempt" else "firstAttempt"
        return getDb().collection(COLLECTIONS.GRIDS.value).document(gridId)
            .collection(COLLECTIONS.PARTICIPANTS.value).orderBy(orderingField).limit(10)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                val userIds = arrayListOf<String>()
                snapshot?.documents?.forEach {
                    userIds.add(it.id)
                }
                if (userIds.isEmpty()) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }
                fetchUsers(userIds) { users ->
                    val data = arrayListOf<LeaderboardItem>()
                    snapshot?.documents?.forEach { participant ->
                        val userData = users[participant.data?.get("id")]
                        val attempts = arrayListOf<Map<String, Any>>()
                        (participant.data?.get("attempts") as List<Map<String, Any>>).forEach { attempt ->
                            attempts.add(
                                mapOf<String, Any>(
                                    "attemptedOn" to (attempt["attemptedOn"] as Timestamp).toDate(),
                                    "timeTaken" to (attempt["timeTaken"] as Long).toInt()
                                )
                            )
                        }
                        data.add(
                            LeaderboardItem(
                                timeTaken = ((participant.data?.get(orderingField)
                                    ?: 0) as Long).toInt(),
                                user = User(
                                    id = userData?.get("id").toString(),
                                    name = userData?.get("name").toString(),
                                    profilePicUrl = userData?.get("profilePic").toString(),
                                    email = userData?.get("email").toString(),
                                ),
                                attempts = attempts
                            )
                        )
                        onResult(data)
                    }
                }
            }
    }

    fun getLatestConfig(onResult: (Map<String, Any>) -> Unit) {
        getDb().collection(COLLECTIONS.CONFIG.value).orderBy("addedOn", Query.Direction.DESCENDING)
            .limit(1).get().addOnSuccessListener {
                onResult(it.documents[0].data!!)
            }
    }

    fun pushGridState(gridId: String, userId: String, state: Map<String, Any>) {
        getDb().collection(COLLECTIONS.GRIDS.value).document(gridId)
            .collection(COLLECTIONS.PARTICIPANTS.value).document(userId).set(
                mapOf(
                    "currentState" to state
                ), SetOptions.merge()
            )

    }

    fun getGridState(gridId: String, userId: String, onResult: (Map<String, Any>?) -> Unit) {
        getDb().collection(COLLECTIONS.GRIDS.value).document(gridId)
            .collection(COLLECTIONS.PARTICIPANTS.value).document(userId).get()
            .addOnSuccessListener {
                if (it == null || it["currentState"] == null) {
                    onResult(null)
                } else {
                    onResult(it["currentState"] as Map<String, Any>)
                }
            }
    }

    fun getAttemptedGridNumbers(type: Int, userId: String, onResult: (HashSet<Int>) -> Unit) {
        getDb().collectionGroup(COLLECTIONS.PARTICIPANTS.value)
            .whereEqualTo("id", userId)
            .whereEqualTo("gridType", type)
            .whereNotEqualTo("firstAttempt", null).get()
            .addOnSuccessListener {
                val attemptedNumbers = hashSetOf<Int>()
                it.documents.forEach { doc ->
                    attemptedNumbers.add(doc.get("gridNumber", Int::class.java)!!)
                }
                onResult(attemptedNumbers)
            }
    }

    fun <T> getGrid(gridNumber: Int, type: Int, onResult: (Grid<T>) -> Unit) {
        getDb().collection(COLLECTIONS.GRIDS.value)
            .whereEqualTo("type", type)
            .whereEqualTo("number", gridNumber)
            .get()
            .addOnCompleteListener() {
                val gridDoc = it.result.documents[0]
                val gridStr = gridDoc.data?.get("grid") as String
                val grid = when (type) {
                    GRID_TYPES.QUEENS.value -> parseQueensGridStr(gridStr)
                    GRID_TYPES.ZIP.value -> parseZipGridStr(gridStr)
                    else -> parseTangoGridStr(gridStr)
                } as Array<Array<T>>
                onResult(
                    Grid<T>(
                        id = gridDoc.id,
                        grid = grid,
                        date = (gridDoc.data?.get("date") as Timestamp).toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate(),
                        number = (gridDoc.data?.get("number") as Long).toInt()
                    )
                )
            }
    }

    fun pushMessagingTokenAndUpdateUserDetails(token: String) {
        val user = Firebase.auth.currentUser
        val otherDetails = mapOf(
            "lastAccessedAt" to Timestamp.now(),
            "currentAppVersion" to "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        )
        if (user != null) {
            getDb().collection(COLLECTIONS.USERS.value).document(user.uid).set(
                mapOf(
                    "fcmToken" to token,
                    "name" to user.displayName,
                    "profilePic" to user.photoUrl
                ).plus(otherDetails),
                SetOptions.merge()
            )
        } else {
            getDb().collection(COLLECTIONS.ANONYMOUS_USERS.value).document(token).set(
                mapOf(
                    "fcmToken" to token
                ).plus(otherDetails),
            )
        }
    }

    fun updateUserPreferences(preferences: MutableMap<String, *>?) {
        val user = Firebase.auth.currentUser
        if (user != null && preferences != null) {
            getDb().collection(COLLECTIONS.USERS.value).document(user.uid).set(
                mapOf(
                    "preferences" to preferences
                ),
                SetOptions.merge()
            )
        }
    }
}
