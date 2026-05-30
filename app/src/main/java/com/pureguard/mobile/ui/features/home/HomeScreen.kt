package com.pureguard.mobile.ui.features.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import com.pureguard.mobile.R
import com.pureguard.mobile.features.blocking.domain.model.Sensitivity
import com.pureguard.mobile.features.blocking.domain.model.SettingsPatch
import com.pureguard.mobile.features.blocking.presentation.viewmodel.ProtectionUiState
import com.pureguard.mobile.ui.theme.PgAccentBlue
import com.pureguard.mobile.ui.theme.PgAccentViolet
import com.pureguard.mobile.ui.theme.PgDanger
import com.pureguard.mobile.ui.theme.PgMuted
import com.pureguard.mobile.ui.theme.PgSuccess
import com.pureguard.mobile.ui.theme.PgText
import com.pureguard.mobile.ui.theme.TbColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: ProtectionUiState,
    accessibilityEnabled: Boolean,
    vpnActive: Boolean,
    vpnNeedsConsent: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onToggleVpn: () -> Unit,
    onOpenDrawer: () -> Unit = {},
    onPatch: (SettingsPatch) -> Unit,
    onResetStats: () -> Unit
) {
    val settings = state.snapshot.settings
    val stats = state.snapshot.stats
    val lock = state.snapshot.lockState
    val vpnReady = vpnActive || !vpnNeedsConsent

    val shieldScale = remember { Animatable(0.8f) }
    LaunchedEffect(settings.enabled) {
        shieldScale.animateTo(
            if (settings.enabled) 1f else 0.85f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
    }

    val isFullyActive = settings.enabled && accessibilityEnabled && vpnReady
    val isPartiallyActive = settings.enabled && (accessibilityEnabled || vpnReady)

    val statusColor = when {
        isFullyActive -> PgSuccess
        isPartiallyActive -> PgAccentBlue
        else -> PgDanger
    }
    val statusLabel = when {
        isFullyActive -> stringResource(R.string.home_status_fully_protected)
        isPartiallyActive -> stringResource(R.string.home_status_partially_active)
        else -> stringResource(R.string.home_status_off)
    }
    val statusSubtitle = when {
        isFullyActive -> stringResource(R.string.home_status_fully_subtitle)
        isPartiallyActive -> stringResource(R.string.home_status_partially_subtitle)
        else -> stringResource(R.string.home_status_off_subtitle)
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                statusLabel = statusLabel,
                statusColor = statusColor,
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
                HomeSection(title = stringResource(R.string.home_section_protection_status)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        statusColor.copy(alpha = 0.18f),
                                        statusColor.copy(alpha = 0.06f)
                                    )
                                )
                            )
                            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(18.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
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
                                        .size(76.dp)
                                        .background(statusColor.copy(0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = statusLabel,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                                Text(
                                    text = statusSubtitle,
                                    fontSize = 13.sp,
                                    color = PgMuted,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatPill(label = stringResource(R.string.home_stat_blocked), value = "${stats.blockedCount}", color = PgDanger)
                                StatPill(label = stringResource(R.string.home_stat_scanned), value = "${stats.scannedCount}", color = PgAccentBlue)
                                StatPill(label = stringResource(R.string.home_stat_dns), value = "${stats.dnsBlockedCount}", color = PgAccentViolet)
                            }
                        }
                    }
                }
            }

            item {
                HomeSection(title = stringResource(R.string.home_section_core_protection)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.home_enable_pureguard),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = PgText
                            )
                            Text(
                                stringResource(R.string.home_master_switch),
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
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(0.05f)))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .padding(top = 12.dp, bottom = 8.dp, start = 4.dp, end = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PgAccentBlue.copy(0.12f))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Lock, null, tint = PgAccentBlue, modifier = Modifier.size(14.dp))
                            Text(
                                stringResource(R.string.home_tamper_lock_enabled),
                                fontSize = 12.sp,
                                color = PgAccentBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item {
                HomeSection(title = stringResource(R.string.home_section_protection_layers)) {
                    FeatureToggleRow(
                        icon = Icons.Default.Search,
                        iconColor = PgAccentViolet,
                        title = stringResource(R.string.home_force_safesearch),
                        subtitle = stringResource(R.string.home_force_safesearch_subtitle),
                        checked = settings.enforceSafeSearch,
                        onCheckedChange = { onPatch(SettingsPatch(enforceSafeSearch = it)) },
                        enabled = settings.enabled
                    )
                    FeatureToggleRow(
                        icon = Icons.Default.Image,
                        iconColor = PgAccentBlue,
                        title = stringResource(R.string.home_image_scan),
                        subtitle = stringResource(R.string.home_image_scan_subtitle),
                        checked = settings.enableImageScan,
                        onCheckedChange = { onPatch(SettingsPatch(enableImageScan = it)) },
                        enabled = settings.enabled,
                        showDivider = false
                    )
                }
            }

            item {
                HomeSection(title = stringResource(R.string.home_section_filter_sensitivity)) {
                    Text(
                        text = stringResource(R.string.home_sensitivity_helper),
                        style = MaterialTheme.typography.bodySmall,
                        color = PgMuted,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                    SensitivitySelector(
                        selected = settings.sensitivity,
                        onSelect = { onPatch(SettingsPatch(sensitivity = it)) },
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }
            }

            item {
                HomeSection(title = stringResource(R.string.home_section_session_stats)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.home_current_session_counters),
                            style = MaterialTheme.typography.bodySmall,
                            color = PgMuted
                        )
                        TextButton(onClick = onResetStats) {
                            Text(stringResource(R.string.home_reset), fontSize = 13.sp, color = PgMuted)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LargeStatCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.home_pages_blocked),
                            value = "${stats.blockedCount}",
                            color = PgDanger
                        )
                        LargeStatCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.home_pages_scanned),
                            value = "${stats.scannedCount}",
                            color = PgAccentBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LargeStatCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.home_dns_blocked),
                            value = "${stats.dnsBlockedCount}",
                            color = PgAccentViolet
                        )
                        LargeStatCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.home_safesearch_enforced),
                            value = "${stats.safeSearchRewriteCount}",
                            color = PgSuccess
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    statusLabel: String,
    statusColor: Color,
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
                    text = stringResource(R.string.nav_home),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PgText
                )
                Text(
                    text = stringResource(R.string.home_top_subtitle),
                    fontSize = 12.sp,
                    color = PgMuted
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusColor.copy(0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = statusLabel,
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun HomeSection(
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
    showDivider: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.alpha(if (enabled) 1f else 0.5f)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconColor.copy(0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PgText)
                Text(subtitle, fontSize = 12.sp, color = PgMuted)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
        if (showDivider) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(0.05f)))
        }
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
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.09f))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column {
            Text(
                text = value,
                fontSize = 24.sp,
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
            val label = sensitivityLabel(option)
            val bgAlpha by animateFloatAsState(
                targetValue = if (active) 1f else 0f,
                animationSpec = tween(200),
                label = "bg"
            )

            val activeColors = when (option) {
                Sensitivity.LOW -> PgSuccess
                Sensitivity.MEDIUM -> PgAccentBlue
                Sensitivity.HIGH -> PgDanger
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

@Composable
private fun sensitivityLabel(option: Sensitivity): String {
    return when (option) {
        Sensitivity.LOW -> stringResource(R.string.sensitivity_low)
        Sensitivity.MEDIUM -> stringResource(R.string.sensitivity_medium)
        Sensitivity.HIGH -> stringResource(R.string.sensitivity_high)
    }
}
