package com.example.tango

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.tango.dataClasses.User
import com.example.tango.ui.theme.TangoTheme
import com.example.tango.utils.GoogleSignInUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.getPreferenceFlow
import me.zhanghai.compose.preference.switchPreference

class SettingsActivity : ComponentActivity(), OnSharedPreferenceChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SettingsActivityView()
        }

        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    @Preview
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
    @Composable
    fun SettingsActivityView() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        val snackbarHostState = remember { SnackbarHostState() }
        var menuExpanded by remember { mutableStateOf(false) }

        val firebaseUser = Firebase.auth.currentUser
        val user = when (firebaseUser) {
            null -> null
            else -> User.fromFirebaseUser(firebaseUser)
        }

        val preferencesFlow =
            PreferenceManager.getDefaultSharedPreferences(context).getPreferenceFlow()

        TangoTheme {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(navigationIcon = {
                        IconButton(onClick = {
                            onBackPressedDispatcher.onBackPressed()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }, title = {
                        Text(
                            text = "Settings",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Cursive,
                            fontSize = 32.sp
                        )
                    }, actions = {
                        if (user == null) {
                            return@CenterAlignedTopAppBar
                        }
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = {
                                    GoogleSignInUtils(
                                        scope,
                                        context,
                                        this@SettingsActivity,
                                        onResult = {
                                            val intent = Intent(
                                                this@SettingsActivity,
                                                MainActivity::class.java
                                            )
                                            intent.flags =
                                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                        }).signOut()
                                }
                            )

                        }

                    })
                },
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    ProvidePreferenceLocals(
                        flow = preferencesFlow
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            if (user != null) {
                                item {
                                    Spacer(Modifier.size(16.dp))
                                }
                                item {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        GlideImage(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(shape = RoundedCornerShape(36.dp)),
                                            model = user.profilePicUrl,
                                            contentDescription = "profile"
                                        )
                                        Column(
                                            modifier = Modifier
                                                .padding(horizontal = 16.dp)
                                        ) {
                                            Text(
                                                text = user.name,
                                                fontSize = 24.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = user.email,
                                                fontSize = 14.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                                item {
                                    Spacer(Modifier.size(32.dp))
                                }
                            }

                            item {
                                Text("General", fontSize = 12.sp, color = Color.Gray)
                                HorizontalDivider()
                            }
                            switchPreference(
                                key = "dark_mode",
                                defaultValue = false,
                                title = { Text(text = "Dark Mode") },
                                summary = { Text(text = if (it) "Enabled (not supported)" else "Disabled") }
                            )
                            switchPreference(
                                key = "show_timer",
                                defaultValue = true,
                                title = { Text(text = "Show Timer") },
                                summary = { Text(text = if (it) "Shown" else "Hidden") }
                            )
                            item {
                                Spacer(Modifier.size(16.dp))
                            }
                            item {
                                Text("Queens", fontSize = 12.sp, color = Color.Gray)
                                HorizontalDivider()
                            }
                            switchPreference(
                                key = "auto_place_x",
                                defaultValue = true,
                                title = { Text(text = "Auto Place X") },
                                summary = { Text(text = if (it) "Enabled" else "Disabled") }
                            )

                            item {
                                Spacer(Modifier.size(16.dp))
                            }
                            item {
                                Text("Notifications", fontSize = 12.sp, color = Color.Gray)
                                HorizontalDivider()
                            }
                            switchPreference(
                                key = "new_level_notifications_enabled",
                                defaultValue = true,
                                title = { Text(text = "New Levels") },
                                summary = { Text(text = if (it) "Enabled" else "Receive notifications when new levels are available") }
                            )

                            item {
                                Spacer(Modifier.size(32.dp))
                            }
                            item {
                                Text(
                                    "App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onSharedPreferenceChanged(
        preferences: SharedPreferences?,
        key: String?
    ) {
        when (key) {
            "new_level_notifications_enabled" -> {
                if (preferences?.getBoolean("new_level_notifications_enabled", true) == false) {
                    Firebase.messaging.unsubscribeFromTopic("new_levels")
                    FirebaseAnalytics.getInstance(this)
                        .logEvent("unsubscribed_from_topic__new_levels", Bundle().apply {
                            putString("userId", Firebase.auth.uid)
                        })
                } else {
                    Firebase.messaging.subscribeToTopic("new_levels")
                    FirebaseAnalytics.getInstance(this)
                        .logEvent("subscribe_to_topic__new_levels", Bundle().apply {
                            putString("userId", Firebase.auth.uid)
                        })
                }
            }
        }
    }
}
