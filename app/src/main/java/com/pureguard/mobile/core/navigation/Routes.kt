package com.pureguard.mobile.core.navigation

sealed class NavRoutes(val route: String) {
    object Home: NavRoutes("home")
    object Settings: NavRoutes("settings")
    object Analytics: NavRoutes("analytics")
    object Preferences: NavRoutes("preferences")
    object Support: NavRoutes("support")
    object Faqs: NavRoutes("faqs")
}
