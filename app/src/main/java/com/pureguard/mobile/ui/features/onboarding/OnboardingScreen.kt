package com.pureguard.mobile.ui.features.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import com.pureguard.mobile.R
import com.pureguard.mobile.ui.theme.PgAccentBlue
import com.pureguard.mobile.ui.theme.PgAccentViolet
import com.pureguard.mobile.ui.theme.PgBgStart
import com.pureguard.mobile.ui.theme.PgMuted
import com.pureguard.mobile.ui.theme.PgSuccess
import com.pureguard.mobile.ui.theme.PgText

private data class OnboardingPage(
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val bullets: List<Pair<Int, Int>>
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Default.Shield,
        iconTint = PgAccentBlue,
        iconBg = Color(0xFF1E3A5F),
        titleRes = R.string.onboarding_page1_title,
        subtitleRes = R.string.onboarding_page1_subtitle,
        bullets = listOf(
            R.string.onboarding_page1_bullet1_title to R.string.onboarding_page1_bullet1_body,
            R.string.onboarding_page1_bullet2_title to R.string.onboarding_page1_bullet2_body,
            R.string.onboarding_page1_bullet3_title to R.string.onboarding_page1_bullet3_body
        )
    ),
    OnboardingPage(
        icon = Icons.Default.Wifi,
        iconTint = PgAccentViolet,
        iconBg = Color(0xFF2D1B69),
        titleRes = R.string.onboarding_page2_title,
        subtitleRes = R.string.onboarding_page2_subtitle,
        bullets = listOf(
            R.string.onboarding_page2_bullet1_title to R.string.onboarding_page2_bullet1_body,
            R.string.onboarding_page2_bullet2_title to R.string.onboarding_page2_bullet2_body,
            R.string.onboarding_page2_bullet3_title to R.string.onboarding_page2_bullet3_body
        )
    ),
    OnboardingPage(
        icon = Icons.Default.Analytics,
        iconTint = PgSuccess,
        iconBg = Color(0xFF0F3B2A),
        titleRes = R.string.onboarding_page3_title,
        subtitleRes = R.string.onboarding_page3_subtitle,
        bullets = listOf(
            R.string.onboarding_page3_bullet1_title to R.string.onboarding_page3_bullet1_body,
            R.string.onboarding_page3_bullet2_title to R.string.onboarding_page3_bullet2_body,
            R.string.onboarding_page3_bullet3_title to R.string.onboarding_page3_bullet3_body
        )
    )
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(0) }
    val page = pages[currentPage]

    val iconScale = remember { Animatable(0.5f) }
    val iconAlpha = remember { Animatable(0f) }

    LaunchedEffect(currentPage) {
        iconScale.snapTo(0.5f)
        iconAlpha.snapTo(0f)
        iconScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        iconAlpha.animateTo(1f, tween(300))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0F1E), PgBgStart, Color(0xFF0F172A))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onFinished) {
                    Text(
                        text = stringResource(R.string.onboarding_skip),
                        color = PgMuted,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(iconScale.value)
                    .alpha(iconAlpha.value)
                    .background(page.iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(page.iconTint.copy(alpha = 0.15f), Color.Transparent)
                            ),
                            CircleShape
                        )
                )
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = page.iconTint,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(400)) + slideInHorizontally(tween(400)) { it / 5 },
                exit = fadeOut() + slideOutHorizontally { -it / 5 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(page.titleRes),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = PgText,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(page.subtitleRes),
                        fontSize = 16.sp,
                        color = PgMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                page.bullets.forEach { (labelRes, detailRes) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(page.iconTint, CircleShape)
                        )
                        Column {
                            Text(
                                text = stringResource(labelRes),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PgText
                            )
                            Text(
                                text = stringResource(detailRes),
                                fontSize = 13.sp,
                                color = PgMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                pages.indices.forEach { index ->
                    val isSelected = index == currentPage
                    val width by animateFloatAsState(
                        targetValue = if (isSelected) 28f else 8f,
                        animationSpec = tween(300, easing = EaseOutCubic),
                        label = "indicator"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) PgAccentBlue else PgMuted.copy(alpha = 0.4f))
                    )
                }
            }

            Button(
                onClick = {
                    if (currentPage < pages.lastIndex) {
                        currentPage++
                    } else {
                        onFinished()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 0.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PgAccentBlue
                )
            ) {
                Text(
                    text = if (currentPage < pages.lastIndex) {
                        stringResource(R.string.onboarding_continue)
                    } else {
                        stringResource(R.string.onboarding_get_started)
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0A0F1E)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
