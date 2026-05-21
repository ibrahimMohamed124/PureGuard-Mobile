package com.pureguard.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.pureguard.mobile.ui.theme.PgBgEnd
import com.pureguard.mobile.ui.theme.PgBgStart
import com.pureguard.mobile.ui.theme.PgCard
import com.pureguard.mobile.ui.theme.PgCardBorder

@Composable
fun GradientBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(PgBgStart, PgBgEnd)
                )
            )
    ) { content() }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = PgCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, PgCardBorder)
    ) {
        content()
    }
}

