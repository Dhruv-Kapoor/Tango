package com.example.tango

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tango.composables.LeaderboardRow
import com.example.tango.ui.theme.TangoTheme
import com.example.tango.viewmodels.LeaderboardActivityViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class LeaderboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LeaderboardActivityView()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview(showSystemUi = true)
    @Composable
    fun LeaderboardActivityView(
        viewModel: LeaderboardActivityViewModel = viewModel()
    ) {
        if (!LocalInspectionMode.current) {
            viewModel.loadData(intent?.getStringExtra("gridId") ?: "")
        }

        val leaderboardData by viewModel.leaderboardData.collectAsStateWithLifecycle()
        val loading by viewModel.loading.collectAsState()
        val currentViewType by viewModel.currentViewType.collectAsState()

        var menuExpanded by remember { mutableStateOf(false) }
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

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
                            text = "Leaderboard",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Cursive,
                            fontSize = 32.sp
                        )
                    }, actions = {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }) {
                            if (currentViewType != LeaderboardActivityViewModel.VIEWS.FIRST_ATTEMPTS) {
                                DropdownMenuItem(
                                    text = { Text("Switch to First Attempts") },
                                    onClick = {
                                        viewModel.switchToFirstAttemptsView()
                                        menuExpanded = false
                                        scope.launch {
                                            snackbarHostState.currentSnackbarData?.dismiss()
                                            snackbarHostState.showSnackbar(
                                                "Showing results of users when they first attempted this level",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                )
                            }
                            if (currentViewType != LeaderboardActivityViewModel.VIEWS.BEST_ATTEMPTS) {
                                DropdownMenuItem(
                                    text = { Text("Switch to Best Attempts") },
                                    onClick = {
                                        viewModel.switchToBestAttemptsView()
                                        menuExpanded = false
                                        scope.launch {
                                            snackbarHostState.currentSnackbarData?.dismiss()
                                            snackbarHostState.showSnackbar(
                                                "Showing best results of users",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    })
                },
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
                if (loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (leaderboardData.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .padding(padding)
                                .padding(12.dp)
                        ) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                itemsIndexed(leaderboardData) { index, item ->
                                    LeaderboardRow(
                                        item,
                                        index + 1,
                                        currentUser = item.user.id == (Firebase.auth.currentUser?.uid
                                            ?: "")
                                    )
                                }
                            }

                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painterResource(R.drawable.null_state), contentDescription = "null"
                            )
                        }
                    }

                }
            }
        }
    }
}