package com.pureguard.mobile.ui.features.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pureguard.mobile.features.blocking.domain.model.Sensitivity
import com.pureguard.mobile.features.blocking.domain.model.SettingsPatch
import com.pureguard.mobile.features.blocking.presentation.viewmodel.ProtectionUiState
import com.pureguard.mobile.ui.GlassCard
import com.pureguard.mobile.ui.theme.PgAccentBlue
import com.pureguard.mobile.ui.theme.PgAccentViolet
import com.pureguard.mobile.ui.theme.PgDanger
import com.pureguard.mobile.ui.theme.PgMuted
import com.pureguard.mobile.ui.theme.PgSuccess
import com.pureguard.mobile.ui.theme.PgText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: ProtectionUiState,
    accessibilityEnabled: Boolean,
    vpnActive: Boolean,
    vpnNeedsConsent: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onToggleVpn: () -> Unit,
    onPatch: (SettingsPatch) -> Unit,
    onResetStats: () -> Unit
) {
    val settings = state.snapshot.settings
    val stats = state.snapshot.stats
    val lock = state.snapshot.lockState

    val shieldScale = remember { Animatable(0.8f) }
    LaunchedEffect(settings.enabled) {
        shieldScale.animateTo(
            if (settings.enabled) 1f else 0.85f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
    }

    val isFullyActive = settings.enabled && accessibilityEnabled && vpnActive
    val isPartiallyActive = settings.enabled && (accessibilityEnabled || vpnActive)

    val statusColor = when {
        isFullyActive -> PgSuccess
        isPartiallyActive -> PgAccentBlue
        else -> PgDanger
    }
    val statusLabel = when {
        isFullyActive -> "Fully Protected"
        isPartiallyActive -> "Partially Active"
        else -> "Protection Off"
    }
    val statusSubtitle = when {
        isFullyActive -> "All layers active — you're safe"
        isPartiallyActive -> "Enable all layers for full coverage"
        else -> "Turn on PureGuard to protect your device"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = PgAccentBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PureGuard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PgText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    statusColor.copy(alpha = 0.18f),
                                    statusColor.copy(alpha = 0.06f)
                                )
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .scale(shieldScale.value),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            listOf(statusColor.copy(0.25f), Color.Transparent)
                                        ),
                                        CircleShape
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(statusColor.copy(0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = statusLabel,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                            Text(
                                text = statusSubtitle,
                                fontSize = 14.sp,
                                color = PgMuted,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatPill(label = "Blocked", value = "${stats.blockedCount}", color = PgDanger)
                            StatPill(label = "Scanned", value = "${stats.scannedCount}", color = PgAccentBlue)
                            StatPill(label = "DNS", value = "${stats.dnsBlockedCount}", color = PgAccentViolet)
                        }
                    }
                }
            }

            item {
                GlassCard {
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "PureGuard",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Master protection switch",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PgMuted
                                )
                            }
                            Switch(
                                checked = settings.enabled,
                                onCheckedChange = { onPatch(SettingsPatch(enabled = it)) }
                            )
                        }

                        if (lock.hasPassword) {
                            Spacer(Modifier.height(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PgAccentBlue.copy(0.12f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Lock, null, tint = PgAccentBlue, modifier = Modifier.size(13.dp))
                                Text("Tamper lock enabled", fontSize = 12.sp, color = PgAccentBlue, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            item {
                GlassCard {
                    Column(Modifier.padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)) {
                        Text(
                            "Protection layers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Fine-tune how PureGuard protects you",
                            style = MaterialTheme.typography.bodySmall,
                            color = PgMuted,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        FeatureToggleRow(
                            icon = Icons.Default.Search,
                            iconColor = PgAccentViolet,
                            title = "Force SafeSearch",
                            subtitle = "Enforce strict mode on Google, Bing & YouTube",
                            checked = settings.enforceSafeSearch,
                            onCheckedChange = { onPatch(SettingsPatch(enforceSafeSearch = it)) },
                            enabled = settings.enabled
                        )
                        FeatureToggleRow(
                            icon = Icons.Default.Image,
                            iconColor = PgAccentBlue,
                            title = "On-device image scan",
                            subtitle = "Scan page images locally before displaying",
                            checked = settings.enableImageScan,
                            onCheckedChange = { onPatch(SettingsPatch(enableImageScan = it)) },
                            enabled = settings.enabled
                        )
                    }
                }
            }

            item {
                GlassCard {
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Text(
                            "Filter sensitivity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Higher levels block content more aggressively",
                            style = MaterialTheme.typography.bodySmall,
                            color = PgMuted,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        SensitivitySelector(
                            selected = settings.sensitivity,
                            onSelect = { onPatch(SettingsPatch(sensitivity = it)) },
                            modifier = Modifier.padding(top = 14.dp)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Session stats",
                        style = MaterialTheme.typography.labelLarge,
                        color = PgMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = onResetStats) {
                        Text("Reset", fontSize = 13.sp, color = PgMuted)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LargeStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Pages blocked",
                        value = "${stats.blockedCount}",
                        color = PgDanger
                    )
                    LargeStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Pages scanned",
                        value = "${stats.scannedCount}",
                        color = PgAccentBlue
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LargeStatCard(
                        modifier = Modifier.weight(1f),
                        label = "DNS blocked",
                        value = "${stats.dnsBlockedCount}",
                        color = PgAccentViolet
                    )
                    LargeStatCard(
                        modifier = Modifier.weight(1f),
                        label = "SafeSearch enforced",
                        value = "${stats.safeSearchRewriteCount}",
                        color = PgSuccess
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
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
private fun FeatureToggleRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor.copy(0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = PgMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun LargeStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(color.copy(alpha = 0.09f))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column {
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = PgMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun SensitivitySelector(
    selected: Sensitivity,
    onSelect: (Sensitivity) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = Sensitivity.entries

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val active = selected == option
            val label = option.name.lowercase().replaceFirstChar { it.titlecase() }
            val bgAlpha by animateFloatAsState(
                targetValue = if (active) 1f else 0f,
                animationSpec = tween(200),
                label = "bg"
            )

            val activeColors = when (option) {
                Sensitivity.LOW -> PgSuccess
                Sensitivity.MEDIUM -> PgAccentBlue
                Sensitivity.HIGH -> PgDanger
                else -> PgAccentViolet
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(activeColors.copy(alpha = 0.18f * bgAlpha))
                    .clickable { onSelect(option) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (active) activeColors else PgMuted,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
