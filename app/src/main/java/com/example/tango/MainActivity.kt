package com.example.tango

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.preference.PreferenceManager
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.tango.composables.DeprecatedView
import com.example.tango.composables.QueensActivity
import com.example.tango.composables.TangoActivity
import com.example.tango.composables.TicTacToeActivityView
import com.example.tango.composables.ZipActivity
import com.example.tango.composables.nativeLikeComposable
import com.example.tango.ui.theme.TangoTheme
import com.example.tango.utils.FirestoreUtils
import com.example.tango.utils.GoogleSignInUtils
import com.example.tango.viewmodels.BaseViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch


const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private var inviteReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainActivityView()
        }
        updateUserDetails()
        checkSignInAndPermissions()
    }

    private fun checkSignInAndPermissions() {
        if (Firebase.auth.currentUser == null && PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("first_open", true)
        ) {
            PreferenceManager.getDefaultSharedPreferences(this).edit {
                putBoolean("first_open", false)
            }
            GoogleSignInUtils(lifecycleScope, this, this) {
                if (it != null) {
                    val intent = Intent(
                        this@MainActivity, MainActivity::class.java
                    )
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    askNotificationPermission()
                }
            }.signIn()
        } else {
            askNotificationPermission()
        }
    }


    //    @Preview(showSystemUi = true)
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
    @Composable
    fun MainActivityView() {
        val viewModel: BaseViewModel = viewModel()
        val isDeprecated by viewModel.isDeprecated.collectAsStateWithLifecycle()
        val currentUser by viewModel.currentUser.collectAsState()
        val config by viewModel.config.collectAsState()
        val updateAvailable by viewModel.updateAvailable.collectAsState()
        val inviteData by viewModel.inviteData.collectAsState()
        val showInvite by viewModel.showInvite.collectAsState()
        val uriHandler = LocalUriHandler.current
        val context = LocalContext.current

        val snackbarHostState = remember { SnackbarHostState() }
        val navController = rememberNavController()
        val scope = rememberCoroutineScope()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentScreen by remember { derivedStateOf { navBackStackEntry?.destination?.route } }

        TangoTheme {
            if (isDeprecated) {
                DeprecatedView(config!!)
            } else {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                BackHandler(enabled = drawerState.isOpen) {
                    scope.launch {
                        drawerState.close()
                    }
                }
                ModalNavigationDrawer(
                    drawerState = drawerState, drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier.fillMaxWidth(0.75f),
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                            )
                            NavigationDrawerItem(
                                label = { Text(text = Routes.Tango.label) },
                                selected = currentScreen == Routes.Tango.route,
                                shape = RoundedCornerShape(8.dp),
                                onClick = {
                                    navController.navigate(Routes.Tango.route) {
                                        launchSingleTop = true
                                    }
                                    scope.launch {
                                        drawerState.close()
                                    }
                                })
                            NavigationDrawerItem(
                                label = { Text(text = Routes.Queens.label) },
                                selected = currentScreen == Routes.Queens.route,
                                shape = RoundedCornerShape(8.dp),
                                onClick = {
                                    navController.navigate(Routes.Queens.route) {
                                        launchSingleTop = true
                                    }
                                    scope.launch {
                                        drawerState.close()
                                    }
                                })
                            NavigationDrawerItem(
                                label = { Text(text = Routes.Zip.label) },
                                selected = currentScreen == Routes.Zip.route,
                                shape = RoundedCornerShape(8.dp),
                                onClick = {
                                    navController.navigate(Routes.Zip.route) {
                                        launchSingleTop = true
                                    }
                                    scope.launch {
                                        drawerState.close()
                                    }
                                })
                            NavigationDrawerItem(
                                label = { Text(text = Routes.TicTacToe.label) },
                                selected = currentScreen == Routes.TicTacToe.route,
                                shape = RoundedCornerShape(8.dp),
                                onClick = {
                                    navController.navigate(Routes.TicTacToe.route) {
                                        launchSingleTop = true
                                    }
                                    scope.launch {
                                        drawerState.close()
                                    }
                                })
                        }
                    }) {
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(navigationIcon = {
                                IconButton(onClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu, contentDescription = "Menu"
                                    )
                                }
                            }, title = {
                                Text(
                                    text = Routes.getRoute(currentScreen ?: "").label,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Cursive,
                                    fontSize = 32.sp
                                )
                            }, actions = {
                                IconButton(
                                    modifier = Modifier.padding(horizontal = 8.dp), onClick = {
                                        context.startActivity(
                                            Intent(
                                                context, SettingsActivity::class.java
                                            )
                                        )
                                    }) {
                                    if (currentUser?.profilePicUrl != null) {
                                        GlideImage(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(shape = RoundedCornerShape(18.dp)),
                                            model = currentUser?.profilePicUrl,
                                            contentDescription = null
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.Settings,
                                            contentDescription = "Settings"
                                        )
                                    }
                                }
                            })
                        },
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
                        NavHost(
                            navController = navController,
                            startDestination = intent?.getStringExtra("route")
                                ?: Routes.Tango.route,
                            modifier = Modifier.padding(padding),
                        ) {
                            nativeLikeComposable(route = Routes.Tango.route) {
                                TangoActivity(snackbarHostState = snackbarHostState)
                            }
                            nativeLikeComposable(route = Routes.Queens.route) {
                                QueensActivity(snackbarHostState = snackbarHostState)
                            }
                            nativeLikeComposable(route = Routes.Zip.route) {
                                ZipActivity(snackbarHostState = snackbarHostState)
                            }
                            nativeLikeComposable(
                                route = Routes.TicTacToe.route + Routes.TicTacToe.params,
                                arguments = listOf(
                                    navArgument("roomId") {
                                        defaultValue = null
                                        nullable = true
                                    }
                                )
                            ) { backStackEntry ->
                                TicTacToeActivityView(
                                    snackbarHostState = snackbarHostState,
                                    navController = navController
                                )
                            }
                        }


                    }
                }
                AnimatedVisibility(
                    visible = showInvite,
                    enter = slideInVertically(),// + expandVertically(),
                    exit = slideOutVertically() //+ shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .windowInsetsPadding(
                                WindowInsets.systemBars.only(
                                    WindowInsetsSides.Vertical
                                )
                            )
                            .padding(horizontal = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(inviteData?.message ?: "", color = Color.Black)
                            Spacer(modifier = Modifier.size(8.dp))
                            Row() {
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.declineInvite() }) {
                                    Text("Decline")
                                }
                                Spacer(modifier = Modifier.size(8.dp))
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        println(navBackStackEntry?.destination?.route)
                                        println(inviteData?.route)
                                        navController.navigate(inviteData?.route ?: "") {
                                            launchSingleTop = true
                                        }
                                        viewModel.acceptInvite()
                                    }) {
                                    Text("Join")
                                }
                            }
                        }

                    }
                }
                if (updateAvailable) {
                    LaunchedEffect(Unit) {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        val result = snackbarHostState.showSnackbar(
                            message = "New update available",
                            actionLabel = "Update",
                            duration = SnackbarDuration.Long
                        )
                        when (result) {
                            SnackbarResult.Dismissed -> {}
                            SnackbarResult.ActionPerformed -> {
                                uriHandler.openUri(config!!["appLink"].toString())
                            }
                        }
                    }
                }
            }

        }

        inviteReceiver = remember {
            val obj = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent) {
                    println("onReceive")
                    val inviteId = intent.getStringExtra("inviteId")!!
                    FirestoreUtils.getValidInvite(inviteId) { invite ->
                        invite?.let {
                            invite.route = intent.getStringExtra("route")!!
                            invite.message = intent.getStringExtra("message")!!
                            viewModel.showInvite(invite)
                        }
                    }
                }
            }
            LocalBroadcastManager.getInstance(this)
                .registerReceiver(obj, IntentFilter("INVITE_RECEIVED"))
            obj
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun updateUserDetails() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            FirestoreUtils.pushMessagingTokenAndUpdateUserDetails(
                it.result
            )
        }
    }

    override fun onResume() {
        super.onResume()
        println("onResume")
        // Register receiver when activity is visible
        inviteReceiver?.let {
            LocalBroadcastManager.getInstance(this)
                .registerReceiver(it, IntentFilter("INVITE_RECEIVED"))
        }

    }

    override fun onPause() {
        super.onPause()
        // Unregister receiver when activity is not visible
        inviteReceiver?.let {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(inviteReceiver!!)
        }

    }
}
