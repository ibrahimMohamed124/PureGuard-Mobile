package com.pureguard.mobile.ui.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pureguard.mobile.features.blocking.domain.model.Sensitivity
import com.pureguard.mobile.features.blocking.domain.model.SettingsPatch
import com.pureguard.mobile.ui.GlassCard
import com.pureguard.mobile.features.blocking.presentation.viewmodel.ProtectionUiState
import com.pureguard.mobile.ui.ToggleSettingRow
import com.pureguard.mobile.ui.theme.PgAccentBlue
import com.pureguard.mobile.ui.theme.PgMuted

@OptIn(ExperimentalLayoutApi::class)
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "PureGuard Mobile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PgAccentBlue
                    )
                    Text(
                        text = "Privacy-first NSFW shield inspired by your extension",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PgMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (lock.hasPassword) {
                        Text(
                            text = "Lock enabled",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            }
        }

        item {
            GlassCard {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    ToggleSettingRow(
                        title = "Enable PureGuard",
                        subtitle = "Master switch for all protection layers",
                        checked = settings.enabled,
                        onCheckedChange = { onPatch(SettingsPatch(enabled = it)) }
                    )
                    ToggleSettingRow(
                        title = "Force SafeSearch",
                        subtitle = "Google, Bing, DuckDuckGo, Yahoo, Brave, Startpage",
                        checked = settings.enforceSafeSearch,
                        onCheckedChange = { onPatch(SettingsPatch(enforceSafeSearch = it)) }
                    )
                    ToggleSettingRow(
                        title = "On-device image scan",
                        subtitle = "Scan page images locally before allowing content",
                        checked = settings.enableImageScan,
                        onCheckedChange = { onPatch(SettingsPatch(enableImageScan = it)) }
                    )
                }
            }
        }

        item {
            GlassCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("System-wide blocking", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (accessibilityEnabled) {
                            "Accessibility: connected"
                        } else {
                            "Accessibility: disabled"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = PgMuted
                    )
                    Text(
                        if (vpnActive) {
                            "VPN shield: active"
                        } else if (vpnNeedsConsent) {
                            "VPN shield: needs approval"
                        } else {
                            "VPN shield: ready"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = PgMuted
                    )
                    TextButton(onClick = onOpenAccessibilitySettings) {
                        Text("Open accessibility settings")
                    }
                    TextButton(onClick = onToggleVpn) {
                        Text(if (vpnActive) "Stop VPN shield" else "Start VPN shield")
                    }
                }
            }
        }

        item {
            GlassCard {
                Column(Modifier.padding(16.dp)) {
                    Text("Sensitivity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Higher means more aggressive blocking",
                        style = MaterialTheme.typography.bodySmall,
                        color = PgMuted
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Sensitivity.entries.forEach { option ->
                            FilterChip(
                                selected = settings.sensitivity == option,
                                onClick = { onPatch(SettingsPatch(sensitivity = option)) },
                                label = { Text(option.name.lowercase().replaceFirstChar { it.titlecase() }) }
                            )
                        }
                    }
                }
            }
        }

        item {
            GlassCard {
                Column(Modifier.padding(16.dp)) {
                    Text("Protection stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("${stats.blockedCount}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("Pages blocked", style = MaterialTheme.typography.bodySmall, color = PgMuted)
                        }
                        Column {
                            Text("${stats.scannedCount}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("Pages scanned", style = MaterialTheme.typography.bodySmall, color = PgMuted)
                        }
                    }
                    TextButton(
                        onClick = onResetStats,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Reset stats")
                    }
                }
            }
        }
    }
}
