package com.example.tango.dataClasses

import com.google.firebase.auth.FirebaseUser

data class User(
    val id: String,
    val name: String,
    val profilePicUrl: String,
    val email: String
) {
    companion object {
        fun fromFirebaseUser(user: FirebaseUser): User {
            return User(
                user.uid, user.displayName?:"", user.photoUrl.toString(), user.email?:""
            )
        }
    }
}