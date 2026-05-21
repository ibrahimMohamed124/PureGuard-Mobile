package com.pureguard.mobile.ui.features.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pureguard.mobile.features.blocking.presentation.viewmodel.ProtectionUiState
import com.pureguard.mobile.ui.GlassCard
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                TbColor,
                                TbColor.copy(alpha = 0.94f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(0.05f)
                    )
                    .statusBarsPadding()
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {

                    // Top Row (Compact)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Icon (smaller)
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            PgAccentBlue.copy(0.22f),
                                            PgAccentViolet.copy(0.18f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = PgAccentBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Title
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = "Analytics",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PgText
                            )

                            Text(
                                text = if (hasTelemetry)
                                    "Real-time protection insights"
                                else
                                    "Protection activity will appear here",
                                fontSize = 11.sp,
                                color = PgMuted
                            )
                        }

                        // Status Badge (compact)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (vpnReady)
                                        PgSuccess.copy(0.14f)
                                    else
                                        PgDanger.copy(0.14f)
                                )
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .background(
                                            if (vpnReady) PgSuccess else PgDanger,
                                            CircleShape
                                        )
                                )

                                Text(
                                    text = if (vpnReady) "Protected" else "Limited",
                                    color = if (vpnReady) PgSuccess else PgDanger,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Stats strip (reduced spacing)
                    if (hasTelemetry) {

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(0.035f))
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {

                            MiniTopMetric(
                                value = "${stats.blockedCount}",
                                label = "Blocked",
                                color = PgDanger
                            )

                            MiniDivider()

                            MiniTopMetric(
                                value = "${stats.scannedCount}",
                                label = "Scanned",
                                color = PgSuccess
                            )

                            MiniDivider()

                            MiniTopMetric(
                                value = "$protectionRate%",
                                label = "Protection",
                                color = PgAccentBlue
                            )
                        }
                    }
                }
            }
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
                if (hasTelemetry) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(PgAccentBlue.copy(0.18f), PgAccentViolet.copy(0.08f))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                "Protection overview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PgText
                            )
                            Text(
                                "Your session at a glance",
                                style = MaterialTheme.typography.bodySmall,
                                color = PgMuted,
                                modifier = Modifier.padding(top = 2.dp, bottom = 20.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                BigMetric(value = "${stats.blockedCount}", label = "Blocked", color = PgDanger)
                                VerticalDivider()
                                BigMetric(value = "${stats.scannedCount}", label = "Allowed", color = PgSuccess)
                                VerticalDivider()
                                BigMetric(value = "$protectionRate%", label = "Block rate", color = PgAccentBlue)
                            }
                        }
                    }
                } else {
                    EmptyAnalyticsState()
                }
            }

            if (hasTelemetry) {
                item {
                    GlassCard {
                        Column(
                            Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            Text(
                                "Activity breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Detailed view of all protection actions",
                                style = MaterialTheme.typography.bodySmall,
                                color = PgMuted,
                                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                            )

                            ActivityRow("SafeSearch enforced", "${stats.safeSearchRewriteCount} times", PgAccentViolet)
                            ActivityRow("Allowed once (override)", "${stats.allowOnceCount} times", PgAccentBlue)
                            ActivityRow("DNS filter blocks", "${stats.dnsBlockedCount}", PgDanger)
                            ActivityRow("Keyword & content blocks", "${stats.keywordBlockedCount}", PgDanger)
                            ActivityRow("Private mode blocks", "${stats.privateModeBlockedCount}", PgDanger)
                            ActivityRow("Strict mode blocks", "${stats.strictModeBlockedCount}", PgAccentViolet)
                        }
                    }
                }
            }

            item {
                GlassCard {
                    Column(
                        Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            "Shield status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Real-time health of your protection layers",
                            style = MaterialTheme.typography.bodySmall,
                            color = PgMuted,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        StatusRow(
                            icon = Icons.Default.Accessibility,
                            iconBg = if (accessibilityEnabled) PgSuccess.copy(0.12f) else PgDanger.copy(0.12f),
                            iconTint = if (accessibilityEnabled) PgSuccess else PgDanger,
                            title = "Screen monitor",
                            message = if (accessibilityEnabled)
                                "Active — watching browsers in real time"
                            else
                                "Disabled — browser URLs cannot be monitored",
                            isActive = accessibilityEnabled
                        )

                        StatusRow(
                            icon = Icons.Default.Wifi,
                            iconBg = if (vpnReady) PgSuccess.copy(0.12f) else PgDanger.copy(0.12f),
                            iconTint = if (vpnReady) PgSuccess else PgDanger,
                            title = "VPN shield",
                            message = if (vpnReady)
                                "Running — filtering network traffic"
                            else
                                "Inactive — network-level blocking is off",
                            isActive = vpnReady
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PgAccentBlue.copy(0.08f))
                                .padding(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lightbulb, null, tint = PgAccentBlue, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (hasTelemetry)
                                        "Keep both layers active together for the strongest protection."
                                    else
                                        "Browse normally and PureGuard will build your insights automatically.",
                                    fontSize = 13.sp,
                                    color = PgMuted,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onResetStats,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.07f))
                ) {
                    Text("Reset all analytics", color = PgMuted, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(48.dp)
            .background(Color.White.copy(alpha = 0.1f))
    )
}

@Composable
private fun BigMetric(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 30.sp,
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
private fun ActivityRow(label: String, value: String, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(iconBg, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
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
                text = if (isActive) "Active" else "Off",
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
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(PgAccentBlue.copy(0.1f), Color.Transparent)))
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(PgAccentBlue.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = PgAccentBlue,
                    modifier = Modifier.size(44.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "No threats detected yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PgText
                )
                Text(
                    "Analytics will appear here as PureGuard\nobserves your browsing activity.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PgMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun MiniTopMetric(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )

        Text(
            text = label,
            fontSize = 11.sp,
            color = PgMuted
        )
    }
}

@Composable
private fun MiniDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(Color.White.copy(0.08f))
    )
}