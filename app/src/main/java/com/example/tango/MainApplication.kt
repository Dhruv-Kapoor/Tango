package com.example.tango

import android.app.Application
import android.os.Bundle
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        subscribeToDefaultTopics()
    }

    private fun subscribeToDefaultTopics() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        Firebase.messaging.subscribeToTopic("all_users")
        if (preferences.getBoolean("new_level_notifications_enabled", true)) {
            Firebase.messaging.subscribeToTopic("new_levels")
            FirebaseAnalytics.getInstance(this)
                .logEvent("subscribe_to_topic__new_levels", Bundle().apply {
                    putString("userId", Firebase.auth.uid)
                })
        }
    }
}