package com.pureguard.mobile.ui.features.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.pureguard.mobile.R
import com.pureguard.mobile.features.blocking.presentation.viewmodel.ProtectionUiState
import com.pureguard.mobile.ui.theme.PgAccentBlue
import com.pureguard.mobile.ui.theme.PgAccentViolet
import com.pureguard.mobile.ui.theme.PgDanger
import com.pureguard.mobile.ui.theme.PgMuted
import com.pureguard.mobile.ui.theme.PgSuccess
import com.pureguard.mobile.ui.theme.PgText
import com.pureguard.mobile.ui.theme.TbColor
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    state: ProtectionUiState,
    vpnNeedsConsent: Boolean,
    accessibilityEnabled: Boolean,
    vpnActive: Boolean,
    onOpenDrawer: () -> Unit = {},
    onResetStats: () -> Unit
) {
    val stats = state.snapshot.stats
    val inspectedCount = stats.blockedCount + stats.scannedCount
    val protectionRate = if (inspectedCount == 0) 0 else ((stats.blockedCount * 100f) / inspectedCount).roundToInt()
    val vpnReady = vpnActive || !vpnNeedsConsent
    val hasTelemetry = listOf(
        stats.blockedCount,
        stats.scannedCount,
        stats.safeSearchRewriteCount,
        stats.allowOnceCount,
        stats.dnsBlockedCount,
        stats.keywordBlockedCount,
        stats.privateModeBlockedCount,
        stats.strictModeBlockedCount
    ).sum() > 0

    Scaffold(
        topBar = {
            AnalyticsTopBar(
                hasTelemetry = hasTelemetry,
                vpnReady = vpnReady,
                onOpenDrawer = onOpenDrawer
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 4.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AnalyticsSection(title = stringResource(R.string.analytics_section_overview)) {
                    if (hasTelemetry) {
                        Text(
                            text = stringResource(R.string.analytics_session_glance),
                            style = MaterialTheme.typography.bodySmall,
                            color = PgMuted,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.verticalGradient(listOf(PgAccentBlue.copy(0.12f), PgAccentViolet.copy(0.06f))))
                                .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(14.dp))
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            BigMetric(value = "${stats.blockedCount}", label = stringResource(R.string.home_stat_blocked), color = PgDanger)
                            VerticalDivider()
                            BigMetric(value = "${stats.scannedCount}", label = stringResource(R.string.analytics_allowed), color = PgSuccess)
                            VerticalDivider()
                            BigMetric(value = "$protectionRate%", label = stringResource(R.string.analytics_block_rate), color = PgAccentBlue)
                        }
                    } else {
                        EmptyAnalyticsState()
                    }
                }
            }

            if (hasTelemetry) {
                item {
                    AnalyticsSection(title = stringResource(R.string.analytics_section_activity)) {
                        Text(
                            text = stringResource(R.string.analytics_activity_helper),
                            style = MaterialTheme.typography.bodySmall,
                            color = PgMuted,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                        )

                        ActivityRow(stringResource(R.string.analytics_activity_safesearch), stringResource(R.string.analytics_times, stats.safeSearchRewriteCount), PgAccentViolet)
                        ActivityRow(stringResource(R.string.analytics_activity_allow_once), stringResource(R.string.analytics_times, stats.allowOnceCount), PgAccentBlue)
                        ActivityRow(stringResource(R.string.analytics_activity_dns), "${stats.dnsBlockedCount}", PgDanger)
                        ActivityRow(stringResource(R.string.analytics_activity_keyword), "${stats.keywordBlockedCount}", PgDanger)
                        ActivityRow(stringResource(R.string.analytics_activity_private), "${stats.privateModeBlockedCount}", PgDanger)
                        ActivityRow(
                            label = stringResource(R.string.analytics_activity_strict),
                            value = "${stats.strictModeBlockedCount}",
                            accent = PgAccentViolet,
                            showDivider = false
                        )
                    }
                }
            }

            item {
                AnalyticsSection(title = stringResource(R.string.analytics_section_shield_status)) {
                    Text(
                        text = stringResource(R.string.analytics_shield_helper),
                        style = MaterialTheme.typography.bodySmall,
                        color = PgMuted,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                    )

                    StatusRow(
                        icon = Icons.Default.Accessibility,
                        iconBg = if (accessibilityEnabled) PgSuccess.copy(0.12f) else PgDanger.copy(0.12f),
                        iconTint = if (accessibilityEnabled) PgSuccess else PgDanger,
                        title = stringResource(R.string.analytics_screen_monitor),
                        message = if (accessibilityEnabled)
                            stringResource(R.string.analytics_screen_active)
                        else
                            stringResource(R.string.analytics_screen_disabled),
                        isActive = accessibilityEnabled
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(0.05f)))

                    StatusRow(
                        icon = Icons.Default.Wifi,
                        iconBg = if (vpnReady) PgSuccess.copy(0.12f) else PgDanger.copy(0.12f),
                        iconTint = if (vpnReady) PgSuccess else PgDanger,
                        title = stringResource(R.string.analytics_vpn_shield),
                        message = if (vpnReady)
                            stringResource(R.string.analytics_vpn_running)
                        else
                            stringResource(R.string.analytics_vpn_inactive),
                        isActive = vpnReady
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PgAccentBlue.copy(0.08f))
                            .border(1.dp, PgAccentBlue.copy(0.2f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lightbulb, null, tint = PgAccentBlue, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (hasTelemetry)
                                    stringResource(R.string.analytics_tip_with_data)
                                else
                                    stringResource(R.string.analytics_tip_empty),
                                fontSize = 13.sp,
                                color = PgMuted,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onResetStats,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.07f))
                ) {
                    Text(stringResource(R.string.analytics_reset_all), color = PgMuted, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AnalyticsTopBar(
    hasTelemetry: Boolean,
    vpnReady: Boolean,
    onOpenDrawer: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TbColor.copy(alpha = 0.92f))
            .border(width = 1.dp, color = Color.White.copy(0.06f))
            .padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(0.05f))
                    .clickable(onClick = onOpenDrawer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = stringResource(R.string.common_open_menu),
                    tint = PgText,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.analytics_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PgText
                )
                Text(
                    text = if (hasTelemetry)
                        stringResource(R.string.analytics_subtitle_with_data)
                    else
                        stringResource(R.string.analytics_subtitle_empty),
                    fontSize = 12.sp,
                    color = PgMuted
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (vpnReady) PgSuccess.copy(0.15f) else PgDanger.copy(0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (vpnReady) stringResource(R.string.analytics_protected) else stringResource(R.string.analytics_limited),
                    color = if (vpnReady) PgSuccess else PgDanger,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AnalyticsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = PgAccentBlue,
            modifier = Modifier
                .padding(start = 4.dp, bottom = 8.dp, top = 8.dp)
                .alpha(0.8f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(0.04f))
                .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(44.dp)
            .background(Color.White.copy(alpha = 0.1f))
    )
}

@Composable
private fun BigMetric(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = PgMuted,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ActivityRow(
    label: String,
    value: String,
    accent: Color,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(accent, CircleShape)
                )
                Text(label, style = MaterialTheme.typography.bodyMedium, color = PgText)
            }
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
        if (showDivider) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(0.05f)))
        }
    }
}

@Composable
private fun StatusRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    message: String,
    isActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(message, style = MaterialTheme.typography.bodySmall, color = PgMuted)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isActive) PgSuccess.copy(0.12f) else PgDanger.copy(0.12f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isActive) stringResource(R.string.common_active) else stringResource(R.string.common_off),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) PgSuccess else PgDanger
            )
        }
    }
}

@Composable
private fun EmptyAnalyticsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(PgAccentBlue.copy(0.1f), Color.Transparent)))
            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(16.dp))
            .padding(vertical = 34.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .background(PgAccentBlue.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = PgAccentBlue,
                    modifier = Modifier.size(40.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    stringResource(R.string.analytics_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PgText
                )
                Text(
                    stringResource(R.string.analytics_empty_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = PgMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
