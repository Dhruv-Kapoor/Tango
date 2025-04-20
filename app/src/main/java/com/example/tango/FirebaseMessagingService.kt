package com.example.tango

import com.example.tango.utils.FirestoreUtils
import com.google.firebase.messaging.FirebaseMessagingService

class CustomFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FirestoreUtils.pushMessagingToken(token)
    }
}