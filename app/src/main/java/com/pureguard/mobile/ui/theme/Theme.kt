package com.pureguard.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PureGuardColors = darkColorScheme(
    primary = PgAccentBlue,
    secondary = PgAccentViolet,
    background = PgBgStart,
    surface = PgBgEnd,
    onPrimary = PgBgStart,
    onSecondary = PgBgStart,
    onBackground = PgText,
    onSurface = PgText
)

@Composable
fun PureGuardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PureGuardColors,
        content = content
    )
}
