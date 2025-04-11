package com.example.tango

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.tango.composables.DeprecatedView
import com.example.tango.composables.QueensActivity
import com.example.tango.composables.TangoActivity
import com.example.tango.composables.nativeLikeComposable
import com.example.tango.ui.theme.TangoTheme
import com.example.tango.viewmodels.BaseViewModel
import kotlinx.coroutines.launch

const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainActivityView()
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
                                label = { Text(text = "Tango") },
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
                                label = { Text(text = "Queens") },
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
                                    text = if (currentScreen == Routes.Tango.route) "Tango" else "Queens",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Cursive,
                                    fontSize = 32.sp
                                )
                            }, actions = {
                                IconButton(onClick = {}) {
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
                            startDestination = Routes.Tango.route,
                            modifier = Modifier.padding(padding),
                        ) {
                            nativeLikeComposable(route = Routes.Tango.route) {
                                TangoActivity(snackbarHostState = snackbarHostState)
                            }
                            nativeLikeComposable(route = Routes.Queens.route) {
                                QueensActivity(snackbarHostState = snackbarHostState)
                            }
                        }

                    }
                }
            }

        }
    }
}
