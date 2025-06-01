package com.example.tango

sealed class Routes(val route: String, val label: String, var params: String = "") {
    object Tango : Routes("tango", "Tango")
    object Queens : Routes("queens", "Queens")
    object Zip : Routes("zip", "Zip")
    object TicTacToe : Routes("tictactoe", "Tic Tac Toe", "?roomId={roomId}")

    companion object {
        fun getRoute(route: String): Routes {
            return when (route) {
                Queens.route -> Queens
                Tango.route -> Tango
                Zip.route -> Zip
                TicTacToe.route -> TicTacToe
                else -> Tango
            }
        }
    }
}
