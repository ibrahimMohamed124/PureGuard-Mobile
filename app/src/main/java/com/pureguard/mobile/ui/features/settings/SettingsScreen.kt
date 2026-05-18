package com.pureguard.mobile.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.pureguard.mobile.domain.model.BackoffConfig
import com.pureguard.mobile.domain.model.Sensitivity
import com.pureguard.mobile.domain.model.SettingsPatch
import com.pureguard.mobile.ui.GlassCard
import com.pureguard.mobile.ui.ProtectionUiState
import com.pureguard.mobile.ui.ToggleSettingRow
import com.pureguard.mobile.ui.theme.PgMuted

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    state: ProtectionUiState,
    onSavePatch: (SettingsPatch, String?) -> Unit,
    onVerifyPassword: (String, (Boolean) -> Unit) -> Unit,
    onSetPassword: (String, String) -> Unit,
    onRemovePassword: (String) -> Unit
) {
    val settings = state.snapshot.settings
    val lockState = state.snapshot.lockState

    var enabled by rememberSaveable(settings.enabled) { mutableStateOf(settings.enabled) }
    var safeSearch by rememberSaveable(settings.enforceSafeSearch) { mutableStateOf(settings.enforceSafeSearch) }
    var imageScan by rememberSaveable(settings.enableImageScan) { mutableStateOf(settings.enableImageScan) }
    var fastScan by rememberSaveable(settings.fastScan) { mutableStateOf(settings.fastScan) }
    var fastScanLimit by rememberSaveable(settings.fastScanLimit) { mutableStateOf(settings.fastScanLimit.toString()) }
    var strictMode by rememberSaveable(settings.strictMode) { mutableStateOf(settings.strictMode) }
    var incognitoEnabled by rememberSaveable(settings.incognitoEnabled) { mutableStateOf(settings.incognitoEnabled) }
    var sensitivity by rememberSaveable(settings.sensitivity.name) { mutableStateOf(settings.sensitivity) }
    var whitelistText by rememberSaveable(settings.whitelist.joinToString("\n")) { mutableStateOf(settings.whitelist.joinToString("\n")) }
    var blacklistText by rememberSaveable(settings.blacklist.joinToString("\n")) { mutableStateOf(settings.blacklist.joinToString("\n")) }

    var retries by rememberSaveable(settings.backoff.sendMaxRetries) { mutableStateOf(settings.backoff.sendMaxRetries.toString()) }
    var baseDelay by rememberSaveable(settings.backoff.sendBaseDelayMs) { mutableStateOf(settings.backoff.sendBaseDelayMs.toString()) }
    var maxDelay by rememberSaveable(settings.backoff.sendMaxDelayMs) { mutableStateOf(settings.backoff.sendMaxDelayMs.toString()) }
    var safetyVeil by rememberSaveable(settings.backoff.safetyRevealMs) { mutableStateOf(settings.backoff.safetyRevealMs.toString()) }
    var failClosedGrace by rememberSaveable(settings.backoff.failClosedGraceMs) { mutableStateOf(settings.backoff.failClosedGraceMs.toString()) }

    var unlockPassword by rememberSaveable { mutableStateOf("") }
    var sessionPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var newPassword2 by rememberSaveable { mutableStateOf("") }
    var oldPassword by rememberSaveable { mutableStateOf("") }
    var removePassword by rememberSaveable { mutableStateOf("") }
    var localUnlocked by rememberSaveable { mutableStateOf(false) }

    val locked = lockState.hasPassword && !lockState.unlocked && !localUnlocked

    LaunchedEffect(lockState.unlocked) {
        if (lockState.unlocked) localUnlocked = true
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (locked) {
            item {
                GlassCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Settings are locked", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Enter password to edit protected settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PgMuted
                        )
                        OutlinedTextField(
                            value = unlockPassword,
                            onValueChange = { unlockPassword = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation()
                        )
                        Button(
                            onClick = {
                                onVerifyPassword(unlockPassword) { ok ->
                                    if (ok) {
                                        localUnlocked = true
                                        sessionPassword = unlockPassword
                                        unlockPassword = ""
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Unlock") }
                    }
                }
            }
        }

        item {
            GlassCard {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Protection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    ToggleSettingRow(
                        title = "Enable PureGuard",
                        subtitle = "Master switch for all layers",
                        checked = enabled,
                        onCheckedChange = { enabled = it }
                    )
                    ToggleSettingRow(
                        title = "Force SafeSearch",
                        subtitle = "Rewrite search engines to strict mode",
                        checked = safeSearch,
                        onCheckedChange = { safeSearch = it }
                    )
                    ToggleSettingRow(
                        title = "On-device image scan",
                        subtitle = "Analyze page images locally",
                        checked = imageScan,
                        onCheckedChange = { imageScan = it }
                    )
                    ToggleSettingRow(
                        title = "Fast scan",
                        subtitle = "Quick pass first, deep pass after",
                        checked = fastScan,
                        onCheckedChange = { fastScan = it }
                    )
                    ToggleSettingRow(
                        title = "Strict mode",
                        subtitle = "Aggressive fail-closed behavior",
                        checked = strictMode,
                        onCheckedChange = { strictMode = it }
                    )
                    ToggleSettingRow(
                        title = "Incognito support flag",
                        subtitle = "Mirrors extension setting semantics",
                        checked = incognitoEnabled,
                        onCheckedChange = { incognitoEnabled = it }
                    )
                }
            }
        }

        item {
            GlassCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sensitivity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Sensitivity.entries.forEach { option ->
                            FilterChip(
                                selected = sensitivity == option,
                                onClick = { sensitivity = option },
                                label = { Text(option.name.lowercase().replaceFirstChar { it.titlecase() }) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = fastScanLimit,
                        onValueChange = { fastScanLimit = it.filter { c -> c.isDigit() } },
                        label = { Text("Fast-scan image budget (4..40)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            GlassCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Whitelist", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("One domain per line", style = MaterialTheme.typography.bodySmall, color = PgMuted)
                    OutlinedTextField(
                        value = whitelistText,
                        onValueChange = { whitelistText = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Text("Blacklist", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = blacklistText,
                        onValueChange = { blacklistText = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }
        }

        item {
            GlassCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Backoff & timing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = retries,
                        onValueChange = { retries = it.filter { c -> c.isDigit() } },
                        label = { Text("Send retries") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = baseDelay,
                        onValueChange = { baseDelay = it.filter { c -> c.isDigit() } },
                        label = { Text("Base delay ms") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = maxDelay,
                        onValueChange = { maxDelay = it.filter { c -> c.isDigit() } },
                        label = { Text("Max delay ms") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = safetyVeil,
                        onValueChange = { safetyVeil = it.filter { c -> c.isDigit() } },
                        label = { Text("Safety veil ceiling ms") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = failClosedGrace,
                        onValueChange = { failClosedGrace = it.filter { c -> c.isDigit() } },
                        label = { Text("Fail-closed grace ms") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            GlassCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tamper protection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Current password (if changing)") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = newPassword2,
                        onValueChange = { newPassword2 = it },
                        label = { Text("Confirm new password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Button(
                        onClick = {
                            if (newPassword.isNotBlank() && newPassword == newPassword2) {
                                onSetPassword(oldPassword, newPassword)
                                oldPassword = ""
                                newPassword = ""
                                newPassword2 = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set / change password")
                    }

                    if (lockState.hasPassword) {
                        OutlinedTextField(
                            value = removePassword,
                            onValueChange = { removePassword = it },
                            label = { Text("Password to remove lock") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation()
                        )
                        TextButton(
                            onClick = {
                                onRemovePassword(removePassword)
                                removePassword = ""
                                localUnlocked = false
                                sessionPassword = ""
                            }
                        ) {
                            Text("Remove password")
                        }
                    }
                }
            }
        }

        item {
            val backoff = BackoffConfig(
                sendMaxRetries = retries.toIntOrNull() ?: settings.backoff.sendMaxRetries,
                sendBaseDelayMs = baseDelay.toLongOrNull() ?: settings.backoff.sendBaseDelayMs,
                sendMaxDelayMs = maxDelay.toLongOrNull() ?: settings.backoff.sendMaxDelayMs,
                safetyRevealMs = safetyVeil.toLongOrNull() ?: settings.backoff.safetyRevealMs,
                failClosedGraceMs = failClosedGrace.toLongOrNull() ?: settings.backoff.failClosedGraceMs
            )
            val patch = SettingsPatch(
                enabled = enabled,
                sensitivity = sensitivity,
                enforceSafeSearch = safeSearch,
                enableImageScan = imageScan,
                fastScan = fastScan,
                fastScanLimit = fastScanLimit.toIntOrNull() ?: settings.fastScanLimit,
                strictMode = strictMode,
                whitelist = whitelistText.lines().map { it.trim().lowercase() }.filter { it.isNotBlank() },
                blacklist = blacklistText.lines().map { it.trim().lowercase() }.filter { it.isNotBlank() },
                incognitoEnabled = incognitoEnabled,
                backoff = backoff
            )
            Button(
                onClick = {
                    onSavePatch(patch, sessionPassword.ifBlank { null })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("Save settings")
            }
        }
    }
}
