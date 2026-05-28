package com.pureguard.mobile.core.navigation

sealed class NavRoutes(val route: String) {
    object Home: NavRoutes("home")
    object Settings: NavRoutes("settings")
    object Analytics: NavRoutes("analytics")
}
