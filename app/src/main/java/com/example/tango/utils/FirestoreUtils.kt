package com.example.tango.utils

import android.util.Log
import com.example.tango.dataClasses.LeaderboardItem
import com.example.tango.dataClasses.TangoCellData
import com.example.tango.dataClasses.TangoGrid
import com.example.tango.dataClasses.User
import com.example.tango.utils.Gson
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

object FirestoreUtils {

    private const val TAG = "FirestoreUtils"

    enum class COLLECTIONS(val value: String) {
        GRIDS("grids"), USERS("users"), PARTICIPANTS("participants"), CONFIG("config")
    }

    private fun getDb(): FirebaseFirestore {
        return Firebase.firestore
    }

    fun parseGridStr(gridStr: String): Array<Array<TangoCellData>> {
        return Gson.getGson().fromJson(gridStr, Array<Array<TangoCellData>>::class.java)
    }

    fun convertGridToStr(grid: Array<Array<TangoCellData>>): String {
        return Gson.getGson().toJson(grid)
    }

    fun getLatestTangoGrid(callback: (TangoGrid) -> Unit) {
        getDb().collection(COLLECTIONS.GRIDS.value).orderBy("date", Query.Direction.DESCENDING)
            .limit(1).get().addOnCompleteListener() {
                val gridDoc = it.result.documents[0]
                val gridStr = gridDoc.data?.get("grid") as String
                val grid = parseGridStr(gridStr)
                callback(
                    TangoGrid(
                        id = gridDoc.id,
                        grid = grid,
                        date = (gridDoc.data?.get("date") as Timestamp).toDate()
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
            )
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

    fun pushScore(gridId: String, userId: String, timeTaken: Int) {
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
                        data.add(
                            LeaderboardItem(
                                timeTaken = ((participant.data?.get(orderingField)
                                    ?: 0) as Long).toInt(),
                                user = User(
                                    id = userData?.get("id").toString(),
                                    name = userData?.get("name").toString(),
                                    profilePicUrl = userData?.get("profilePic").toString(),
                                    email = userData?.get("email").toString(),
                                )
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
            .collection(COLLECTIONS.PARTICIPANTS.value).document(userId).get().addOnSuccessListener {
                if (it == null || it["currentState"] == null) {
                    onResult(null)
                } else {
                    onResult(it["currentState"] as Map<String, Any>)
                }
            }
    }
}