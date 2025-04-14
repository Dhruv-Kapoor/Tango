package com.example.tango.composables

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.example.tango.LeaderboardActivity
import com.example.tango.R
import com.example.tango.dataClasses.TangoCellData
import com.example.tango.utils.GoogleSignInUtils
import com.example.tango.utils.Utils.dpToPx
import com.example.tango.viewmodels.TangoActivityViewModel
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit


@Preview(showSystemUi = true)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun TangoActivityView(
    modifier: Modifier = Modifier,
    viewModel: TangoActivityViewModel = viewModel {
        TangoActivityViewModel(preview = true)
    },
    started: Boolean = true,
    loading: Boolean = false,
    isLoggedIn: Boolean = false,
    completed: Boolean = false,
    grid: Array<Array<TangoCellData>>? = arrayOf(
        arrayOf(
            TangoCellData(),
            TangoCellData(),
            TangoCellData(),
            TangoCellData(),
            TangoCellData(),
            TangoCellData()
        ),
        arrayOf(
            TangoCellData(),
            TangoCellData(),
            TangoCellData(),
            TangoCellData(),
            TangoCellData(),
            TangoCellData()
        ),
        arrayOf(
            TangoCellData(),
            TangoCellData(),
            TangoCellData(),
            TangoCellData(),
            TangoCellData(),
            TangoCellData()
        ),
        arrayOf(
            TangoCellData(),
            TangoCellData(),
            TangoCellData(),
            TangoCellData(),
            TangoCellData(),
            TangoCellData()
        ),
        arrayOf(
            TangoCellData(),
            TangoCellData(),
            TangoCellData(),
            TangoCellData(),
            TangoCellData(),
            TangoCellData()
        ),
        arrayOf(
            TangoCellData(),
            TangoCellData(),
            TangoCellData(),
            TangoCellData(),
            TangoCellData(),
            TangoCellData()
        ),
    ),
    ticks: Int = 0,
    snackbarHostState: SnackbarHostState? = null
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp.dpToPx()

    val cellSize = (screenWidth - 32.dp.dpToPx()) / (grid?.size ?: 1)
    var openCalendarDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (!loading && started) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.1f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(modifier = Modifier.width(100.dp), onClick = {}) {
                        Timer(running = !completed, ticks = ticks) {
                            viewModel.onTick()
                        }
                    }
                    Button(contentPadding = PaddingValues(start = 24.dp, end = 20.dp), onClick = {
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
                Grid<TangoCellData>(grid) { cell, i, j ->
                    TangoCell(cell, completed, cellSize.toInt()) {
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
                                "Ye to nhi banega",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }) { Text("Stuck?") }
                }
            }
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomStart)
            ) {
                ElevatedButton(
                    modifier = Modifier
                        .fillMaxWidth(), onClick = {
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
                    painter = painterResource(R.drawable.preview),
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
fun TangoActivity(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null
) {
    val viewModel: TangoActivityViewModel = viewModel()

    val started by viewModel.started.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val completed by viewModel.completed.collectAsStateWithLifecycle()
    val grid by viewModel.grid.collectAsState()
    val ticks by viewModel.ticks.collectAsState()

    TangoActivityView(
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
