package com.example.tango.composables

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.tango.R
import com.example.tango.Routes
import com.example.tango.dataClasses.GameRoom
import com.example.tango.dataClasses.ROOM_STATUS
import com.example.tango.dataClasses.TicTacToeCellData
import com.example.tango.dataClasses.TicTacToeCellValue
import com.example.tango.utils.GoogleSignInUtils
import com.example.tango.utils.Utils.conditional
import com.example.tango.utils.Utils.dpToPx
import com.example.tango.viewmodels.TicTacToeActivityViewModel

//@Preview
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun TicTacToeActivityView(
    modifier: Modifier = Modifier,
    viewModel: TicTacToeActivityViewModel = viewModel(),
    snackbarHostState: SnackbarHostState? = null,
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = LocalActivity.current

    var openInviteDialog by remember { mutableStateOf(false) }
    val loading by viewModel.loading.collectAsState()
    val loggedIn by viewModel.loggedIn.collectAsState()
    val gameRoom by viewModel.gameRoom.collectAsState()
    val player by viewModel.player.collectAsState()
    val opponent by viewModel.opponent.collectAsState()

    val backStackEntry = remember { navController.currentBackStackEntry }
    val arguments = backStackEntry?.arguments

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp.dpToPx()
    val cellSize = (screenWidth - 32.dp.dpToPx()) / 3

    val roomId = arguments?.getString("roomId")
    if (roomId != null) {
        viewModel.joinRoom(roomId)
        arguments.clear()
    }
    val isHost = gameRoom?.player1 == player?.id

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (!loading && loggedIn) {
            Column(
                modifier = Modifier
                    .matchParentSize()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (gameRoom != null) {
                        Button(onClick = { }) {
                            Text("ID: ${gameRoom?.number}")
                        }
                    } else {
                        Spacer(modifier = Modifier.size(1.dp))
                    }
                    if (opponent == null) {
                        Button(onClick = { openInviteDialog = !openInviteDialog }) {
                            Text("Invite Player")
                        }
                    } else {
                        Text(
                            "${opponent?.name?.split(" ")?.get(0)}",
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(color = Color(0xffe74c3c))
                                .conditional(gameRoom?.turn != null && opponent?.id == gameRoom?.turn) {
                                    border(
                                        width = 2.dp,
                                        color = Color.Green,
                                        shape = RoundedCornerShape(50)
                                    )
                                }
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            color = Color.White
                        )
                    }
                    if (gameRoom != null) {
                        Button(onClick = {
                            viewModel.exitRoom()
                        }) {
                            Text("Exit")
                        }
                    } else {
                        Spacer(modifier = Modifier.size(1.dp))
                    }
                }
                when (gameRoom?.status) {
                    null -> {
                        BlurredPreviewBackground {
                            Text("Invite players to get started!")
                        }
                    }

                    ROOM_STATUS.CREATED -> {
                        BlurredPreviewBackground {
                            Text("Waiting for players to join!")
                        }
                    }

                    ROOM_STATUS.READY -> {
                        BlurredPreviewBackground {
                            if (isHost) {
                                Button(onClick = { viewModel.startGame() }) {
                                    Text("Start")
                                }
                            } else {
                                Text("Waiting for host to start the game!")
                            }
                        }
                    }

                    ROOM_STATUS.STARTED -> {
                        Grid<TicTacToeCellData>(
                            gameRoom?.getParsedGrid<TicTacToeCellData>()
                                ?: GameRoom.getDefaultGrid<TicTacToeCellData>(Pair(3, 3)),
                            drawBorders = false
                        ) { cell, i, j ->
                            TicTacToeCell(
                                cellData = cell,
                                disabled = (opponent == null || player == null || gameRoom?.turn != player?.id || cell.value != TicTacToeCellValue.BLANK),
                                position = Pair(i, j),
                                cellSize = cellSize.toInt(),
                                invertColors = gameRoom?.firstTurnUserID != player?.id
                            ) {
                                viewModel.onCellUpdated(cell, i, j)
                            }
                        }
                    }

                    ROOM_STATUS.COMPLETED -> {
                        BlurredPreviewBackground {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (gameRoom?.winner != null) {
                                    Text("Winner", fontSize = 24.sp)
                                    Spacer(Modifier.size(16.dp))
                                    val winner =
                                        if (gameRoom?.winner == player?.id) player else opponent
                                    GlideImage(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(shape = RoundedCornerShape(50)),
                                        model = winner?.profilePicUrl,
                                        contentDescription = null
                                    )
                                    Spacer(Modifier.size(8.dp))
                                    Text(winner?.name ?: "", fontSize = 18.sp)
                                    Spacer(Modifier.size(32.dp))
                                } else {
                                    Text("Draw", fontSize = 24.sp)
                                    Spacer(Modifier.size(32.dp))
                                }
                                if (isHost) {
                                    Button(onClick = { viewModel.startGame() }) {
                                        Text("Play Again")
                                    }
                                } else {
                                    Text("Waiting for host to start the game!")
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${player?.name?.split(" ")?.get(0)}",
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(color = Color(0xff3498db))
                            .conditional(gameRoom?.turn != null && player?.id == gameRoom?.turn) {
                                border(width = 2.dp, color = Color.Green, RoundedCornerShape(50))
                            }
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        color = Color.White
                    )

                }

            }
        }
        if (loading) {
            BlurredPreviewBackground {
                CircularProgressIndicator()
            }
        } else if (!loggedIn) {
            BlurredPreviewBackground {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("You need to login to play multiplayer games")
                    Button(onClick = {
                        GoogleSignInUtils(
                            scope, context, activity!!, onResult = { user ->
                                if (user != null) {
                                    viewModel.initialize()
                                }
                            }
                        ).signIn()
                    }) {
                        Text("Login")
                    }
                }
            }
        }
        if (openInviteDialog) {
            InviteUsersModal(onDismissRequest = { openInviteDialog = false }, onInvite = {
                viewModel.onInvite(it)
            })
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState?.showSnackbar(message)
        }
    }
}

@Composable
fun BlurredPreviewBackground(
    overlay: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.tictactoe_preview),
            contentDescription = "",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .blur(
                    radiusX = 15.dp,
                    radiusY = 15.dp,
                    edgeTreatment = BlurredEdgeTreatment.Rectangle
                )
        )
        overlay()
    }
}
