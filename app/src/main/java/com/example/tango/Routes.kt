package com.example.tango

sealed class Routes(val route: String, val label: String) {
    object Tango : Routes("tango", "Tango")
    object Queens : Routes("queens", "Queens")
    object Zip : Routes("zip", "Zip")

    companion object {
        fun getRoute(route: String): Routes {
            return when (route) {
                Queens.route -> Queens
                Tango.route -> Tango
                Zip.route -> Zip
                else -> Tango
            }
        }
    }
}
