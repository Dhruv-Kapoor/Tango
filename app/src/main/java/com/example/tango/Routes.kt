package com.example.tango

sealed class Routes(val route: String) {
    object Tango: Routes("tango")
    object Queens: Routes("queens")
}
