package com.pureguard.mobile.ui.features.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import com.pureguard.mobile.features.blocking.presentation.viewmodel.ProtectionUiState
import com.pureguard.mobile.ui.GlassCard
import com.pureguard.mobile.ui.theme.PgMuted
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(
    state: ProtectionUiState,
    accessibilityEnabled: Boolean,
    vpnActive: Boolean,
    onResetStats: () -> Unit
) {
    val stats = state.snapshot.stats
    val inspectedCount = stats.blockedCount + stats.scannedCount
    val protectionRate = if (inspectedCount == 0) 0 else ((stats.blockedCount * 100f) / inspectedCount).roundToInt()
    val topBlockReason = listOf(
        "DNS safety filters" to stats.dnsBlockedCount,
        "Adult keywords in URL/content" to stats.keywordBlockedCount,
        "Private browsing blocked" to stats.privateModeBlockedCount,
        "Strict mode suspicious blocks" to stats.strictModeBlockedCount
    ).maxByOrNull { it.second }?.takeIf { it.second > 0 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Your protection snapshot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricColumn(label = "Blocked", value = "${stats.blockedCount}")
                        MetricColumn(label = "Allowed", value = "${stats.scannedCount}")
                        MetricColumn(label = "Protection rate", value = "$protectionRate%")
                    }
                }
            }
        }

        item {
            GlassCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("What PureGuard already did", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    DetailRow("SafeSearch enforced", "${stats.safeSearchRewriteCount} times")
                    DetailRow("Allowed once", "${stats.allowOnceCount} times")
                    DetailRow("Strict-mode preventive blocks", "${stats.strictModeBlockedCount} pages")
                }
            }
        }

        item {
            GlassCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Main block reasons", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    DetailRow("DNS family filters", "${stats.dnsBlockedCount}")
                    DetailRow("Keyword/content detection", "${stats.keywordBlockedCount}")
                    DetailRow("Private mode policy", "${stats.privateModeBlockedCount}")
                    val top = topBlockReason
                    if (top != null) {
                        Text(
                            "Most common reason: ${top.first}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PgMuted
                        )
                    }
                }
            }
        }

        item {
            GlassCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Protection health", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (accessibilityEnabled) "Accessibility monitor is active" else "Accessibility monitor is disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = PgMuted
                    )
                    Text(
                        if (vpnActive) "VPN shield is active" else "VPN shield is not active",
                        style = MaterialTheme.typography.bodySmall,
                        color = PgMuted
                    )
                    Text(
                        if (inspectedCount == 0) {
                            "Tip: browse normally for a few minutes to build useful insights here."
                        } else {
                            "Tip: keep both Accessibility and VPN ON for best protection coverage."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = PgMuted
                    )
                }
            }
        }

        item {
            Button(
                onClick = onResetStats,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("Reset analytics")
            }
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String) {
    Column {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = PgMuted)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
