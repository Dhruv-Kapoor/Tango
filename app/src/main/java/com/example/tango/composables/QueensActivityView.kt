package com.example.tango.composables

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.preference.PreferenceManager
import com.example.tango.LeaderboardActivity
import com.example.tango.QUEENS_EDGE_THICKNESS_FACTOR
import com.example.tango.R
import com.example.tango.dataClasses.QueensCellData
import com.example.tango.utils.GoogleSignInUtils
import com.example.tango.utils.Utils.dpToPx
import com.example.tango.utils.Utils.pxToDp
import com.example.tango.viewmodels.QueensActivityViewModel
import kotlinx.coroutines.launch
import me.zhanghai.compose.preference.getPreferenceFlow
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Preview
@Composable
fun QueensActivityView(
    modifier: Modifier = Modifier,
    viewModel: QueensActivityViewModel = viewModel {
        QueensActivityViewModel(preview = true)
    },
    started: Boolean = true,
    loading: Boolean = false,
    isLoggedIn: Boolean = false,
    completed: Boolean = false,
    grid: Array<Array<QueensCellData>>? = arrayOf(

    ),
    ticks: Int = 0,
    snackbarHostState: SnackbarHostState? = null
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = LocalActivity.current

    val preferencesFlow = PreferenceManager.getDefaultSharedPreferences(context).getPreferenceFlow()
    val preferences = preferencesFlow.collectAsState()

    var openCalendarDialog by remember { mutableStateOf(false) }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp.dpToPx()
    val cellSize = (screenWidth - 32.dp.dpToPx()) / (grid?.size ?: 1)

    viewModel.enableAutoPlaceX(preferences.value.get<Boolean>("auto_place_x") != false)

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (!loading && started) {
            Column {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (preferences.value.get<Boolean>("show_timer") != false) {
                                Button(modifier = Modifier.width(100.dp), onClick = {}) {
                                    Timer(running = !completed, ticks = ticks) {
                                        viewModel.onTick()
                                    }
                                }
                            } else {
                                Timer(running = !completed, ticks = ticks, hideClock = true) {
                                    viewModel.onTick()
                                }
                            }
                            Button(
                                contentPadding = PaddingValues(start = 24.dp, end = 20.dp),
                                onClick = {
                                    openCalendarDialog = true
                                }) {
                                Text("#${viewModel.gridNumber}")
                                Spacer(Modifier.size(12.dp))
                                Icon(
                                    modifier = Modifier.size(16.dp),
                                    painter = painterResource(R.drawable.switch_icon),
                                    contentDescription = null
                                )
                            }
                            Button(modifier = Modifier.width(100.dp), onClick = {
                                viewModel.resetGrid()
                            }) {
                                Text(if (completed) "Reset" else "Clear")
                            }
                        }
                        Grid<QueensCellData>(
                            grid,
                            Modifier.border(
                                width = 0.5.dp * QUEENS_EDGE_THICKNESS_FACTOR,
                                color = Color.Black,
                                shape = RoundedCornerShape(4.dp)
                            ),
                            cellSize = cellSize.toInt().pxToDp(),
                            enableDragging = true,
                            onDrag = {
                                viewModel.onDrag(it.first, it.second)
                            }) { cell, i, j ->
                            QueensCell(cell, completed, cellSize.toInt()) {
                                viewModel.onCellUpdate(cell, i, j)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                modifier = Modifier.weight(1f),
                                enabled = !viewModel.undoStack.isEmpty() && !completed,
                                onClick = {
                                    viewModel.onUndo()
                                }) { Text("Undo") }
                            Spacer(modifier = Modifier.size(8.dp))
                            Button(modifier = Modifier.weight(1f), onClick = {
                                scope.launch {
                                    snackbarHostState?.currentSnackbarData?.dismiss()
                                    snackbarHostState?.showSnackbar(
                                        "bola na nhi banega",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }) { Text("Stuck?") }
                        }
                    }
                }
                ElevatedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp), onClick = {
                        if (isLoggedIn) {
                            context.startActivity(
                                Intent(
                                    context, LeaderboardActivity::class.java
                                ).apply {
                                    putExtra("gridId", viewModel.gridId)
                                })
                        } else {
                            GoogleSignInUtils(
                                scope, context, activity!!, onResult = { user ->
                                    if (user != null) {
                                        viewModel.onSignUpCompleted(user)
                                    }
                                }
                            ).signIn()
                        }
                    }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            ImageVector.vectorResource(R.drawable.leaderboard),
                            contentDescription = "",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "View Leaderboard  ->",
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }
        if (started && completed) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(), parties = listOf(
                    Party(
                        speed = 15f,
                        maxSpeed = 25f,
                        damping = 0.9f,
                        angle = Angle.BOTTOM,
                        spread = Spread.ROUND,
                        emitter = Emitter(
                            duration = 3, TimeUnit.SECONDS
                        ).perSecond(100),
                        position = Position.Relative(0.0, 0.3)
                            .between(Position.Relative(1.0, 0.3))
                    )
                )
            )
        }
        if (loading || !started) {
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.queens_preview),
                    contentDescription = "",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .matchParentSize()
                        .blur(
                            radiusX = 30.dp,
                            radiusY = 30.dp,
                            edgeTreatment = BlurredEdgeTreatment.Rectangle
                        )
                )
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Button(onClick = {
                        viewModel.onStart()
                    }) { Text(if (ticks == 0) "Start" else "Resume") }
                }
            }
        }
        if (openCalendarDialog) {
            CalendarModal(
                minDate = viewModel.getMinDate(),
                maxDate = viewModel.latestGridData.date,
                maxNumber = viewModel.latestGridData.number,
                selectedNumber = viewModel.gridNumber,
                attemptedNumbers = viewModel.attemptedGridNumbers,
                onDismissRequest = {
                    openCalendarDialog = false
                },
                onDayClick = {
                    openCalendarDialog = false
                    viewModel.setGridForDate(it)
                }
            )
        }
    }
}

@Composable
fun QueensActivity(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null
) {
    val viewModel: QueensActivityViewModel = viewModel()

    val started by viewModel.started.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val completed by viewModel.completed.collectAsStateWithLifecycle()
    val grid by viewModel.grid.collectAsState()
    val ticks by viewModel.ticks.collectAsState()

    QueensActivityView(
        modifier,
        viewModel,
        started,
        loading,
        isLoggedIn,
        completed,
        grid,
        ticks,
        snackbarHostState
    )
    OnLifecycleEvent { owner, event ->
        when (event) {
            Lifecycle.Event.ON_PAUSE -> {
                viewModel.saveState()
            }

            else -> {}
        }
    }
}
