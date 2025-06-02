package com.example.tango.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tango.Routes
import com.example.tango.dataClasses.GameRoom
import com.example.tango.dataClasses.INVITE_TYPES
import com.example.tango.dataClasses.ROOM_STATUS
import com.example.tango.dataClasses.TicTacToeCellData
import com.example.tango.dataClasses.TicTacToeCellValue
import com.example.tango.dataClasses.User
import com.example.tango.utils.FirestoreUtils
import com.example.tango.utils.validateTicTacToe
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class TicTacToeActivityViewModel : ViewModel() {
    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _loggedIn = MutableStateFlow(false)
    val loggedIn = _loggedIn.asStateFlow()

    private var roomId: String? = null
    private var prevRoomId: String? = null

    private val _gameRoom = MutableStateFlow<GameRoom?>(null)
    val gameRoom = _gameRoom.asStateFlow()

    private val _player = MutableStateFlow<User?>(null)
    val player = _player.asStateFlow()

    private val _opponent = MutableStateFlow<User?>(null)
    val opponent = _opponent.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    init {
        initialize()
    }

    fun initialize() {
        val firebaseUser = Firebase.auth.currentUser
        if (firebaseUser != null) {
            _player.value = User.fromFirebaseUser(Firebase.auth.currentUser!!)
            FirestoreUtils.getCurrentRoomId(FirestoreUtils.GAME_TYPES.TIC_TAC_TOE) {
                if (it != null) {
                    roomId = it
                    addRoomListener()
                } else {
                    roomId = null
                    _gameRoom.value = null
                    _opponent.value = null
                }
                _loading.value = false
            }
            _loggedIn.value = true
        } else {
            _loading.value = false
        }
    }

    fun addRoomListener() {
        FirestoreUtils.getCurrentRoomData<TicTacToeCellData>(
            FirestoreUtils.GAME_TYPES.TIC_TAC_TOE, roomId!!
        ) {
            _gameRoom.value = it
            if (it?.player1 == Firebase.auth.uid) {
                if (it?.player2 != _opponent.value?.id) {
                    if (it?.player2 != null) {
                        FirestoreUtils.fetchUsersFromCache(listOf(it.player2!!)) { users ->
                            _opponent.value = users[it.player2]
                        }
                    } else {
                        _opponent.value = null
                    }
                }
            } else {
                if (it?.player1 != _opponent.value?.id) {
                    if (it?.player1 != null) {
                        FirestoreUtils.fetchUsersFromCache(listOf(it.player1!!)) { users ->
                            _opponent.value = users[it.player1]
                        }
                    } else {
                        _opponent.value = null
                    }
                }
            }
        }
    }

    fun getOrCreateRoom(): GameRoom {
        if (roomId == null) {
            val room = FirestoreUtils.createGameRoom(FirestoreUtils.GAME_TYPES.TIC_TAC_TOE)
            roomId = room.id
            return room
        }
        return _gameRoom.value!!
    }

    fun onInvite(user: User) {
        val room = getOrCreateRoom()
        FirestoreUtils.inviteUserToGame(
            INVITE_TYPES.TIC_TAC_TOE,
            user,
            route = Routes.TicTacToe.route + Routes.TicTacToe.params.replace("{roomId}", room.id),
            roomNumber = room.number
        )
    }

    fun onCellUpdated(cell: TicTacToeCellData, i: Int, j: Int) {
        val room = _gameRoom.value
        if (room == null) {
            return
        }
        if (room.firstTurnUserID == _player.value?.id) {
            cell.value = TicTacToeCellValue.CROSS
        } else {
            cell.value = TicTacToeCellValue.CIRCLE
        }
        room.turn = _opponent.value?.id
        val winner = validateTicTacToe(room.getParsedGrid<TicTacToeCellData>())
        if (winner != null) {
            if (winner == TicTacToeCellValue.CROSS) {
                room.winner = room.firstTurnUserID
            } else if (winner == TicTacToeCellValue.CIRCLE) {
                room.winner =
                    if (room.firstTurnUserID == room.player1) room.player2 else room.player1
            }
            room.status = ROOM_STATUS.COMPLETED
        }
        var queue = room.queue
        queue.add(i * 3 + j)
        if (queue.size > 7) {
            val pos = queue.removeAt(0)
            val cell = room.getParsedGrid<TicTacToeCellData>()[pos / 3][pos % 3]
            cell.partial = false
            cell.value = TicTacToeCellValue.BLANK
        }
        if (queue.size == 7) {
            val pos = queue[0]
            val cell = room.getParsedGrid<TicTacToeCellData>()[pos / 3][pos % 3]
            cell.partial = true
        }
        _gameRoom.value = room
        FirestoreUtils.updateRoomState(FirestoreUtils.GAME_TYPES.TIC_TAC_TOE, room)
    }

    fun exitRoom() {
        _gameRoom.value?.let {
            it.status = ROOM_STATUS.COMPLETED
            FirestoreUtils.exitRoom(FirestoreUtils.GAME_TYPES.TIC_TAC_TOE, it)
        }
    }

    fun joinRoom(newRoomId: String) {
        if (roomId != newRoomId && prevRoomId != newRoomId) {
            prevRoomId = newRoomId
            FirestoreUtils.getRoomStatus(
                FirestoreUtils.GAME_TYPES.TIC_TAC_TOE, newRoomId
            ) { status ->
                if (status == ROOM_STATUS.CREATED) {
                    if (roomId != null) {
                        exitRoom()
                    }
                    FirestoreUtils.joinRoom(FirestoreUtils.GAME_TYPES.TIC_TAC_TOE, newRoomId)
                } else {
                    viewModelScope.launch {
                        _snackbarMessage.emit("Invite expired!")
                    }
                }
            }

        }

    }

    fun startGame() {
        val room = _gameRoom.value!!
        room.status = ROOM_STATUS.STARTED
        val firstTurn = Random.nextInt(2)
        if (firstTurn == 0) {
            room.firstTurnUserID = room.player1
        } else {
            room.firstTurnUserID = room.player2
        }
        room.turn = room.firstTurnUserID
        room.winner = null
        room.queue = mutableListOf()
        room.parsedGridCache =
            GameRoom.getDefaultGrid<TicTacToeCellData>(Pair(3, 3)) as Array<Array<Any>>?
        _gameRoom.value = room
        FirestoreUtils.updateRoomState(FirestoreUtils.GAME_TYPES.TIC_TAC_TOE, room)
    }
}
