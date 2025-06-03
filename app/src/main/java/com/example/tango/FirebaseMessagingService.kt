package com.example.tango

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.tango.utils.FirestoreUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class CustomFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FirestoreUtils.pushMessagingTokenAndUpdateUserDetails(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (message.data["type"] == "invite") {
            val intent = Intent("INVITE_RECEIVED")
            intent.putExtra("message", message.notification?.title)
            intent.putExtra("route", message.data["route"])
            intent.putExtra("inviteId", message.data["inviteId"])
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }
}