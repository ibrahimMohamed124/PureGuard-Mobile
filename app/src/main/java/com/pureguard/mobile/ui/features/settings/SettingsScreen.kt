package com.pureguard.mobile.ui.features.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pureguard.mobile.features.blocking.domain.model.Sensitivity
import com.pureguard.mobile.features.blocking.domain.model.SettingsPatch
import com.pureguard.mobile.features.blocking.presentation.viewmodel.ProtectionUiState
import com.pureguard.mobile.ui.features.settings.composable.ChangeLockPasswordDialog
import com.pureguard.mobile.ui.features.settings.composable.DomainManagerScreen
import com.pureguard.mobile.ui.features.settings.composable.PasswordGateDialog
import com.pureguard.mobile.ui.features.settings.composable.SettingsSection
import com.pureguard.mobile.ui.features.settings.composable.SettingsToggleRow
import com.pureguard.mobile.ui.features.settings.composable.normalizeDomainCandidate
import com.pureguard.mobile.ui.features.settings.composable.parseDomains
import com.pureguard.mobile.ui.theme.PgAccentBlue
import com.pureguard.mobile.ui.theme.PgAccentViolet
import com.pureguard.mobile.ui.theme.PgDanger
import com.pureguard.mobile.ui.theme.PgMuted
import com.pureguard.mobile.ui.theme.PgSuccess
import com.pureguard.mobile.ui.theme.PgText
import com.pureguard.mobile.ui.theme.TbColor

public enum class SettingsDestination { MAIN, WHITELIST, BLACKLIST }

private data class PendingSettingChange(
    val title: String,
    val message: String,
    val patch: SettingsPatch,
    val confirmText: String = "Apply change",
    val onSuccess: () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: ProtectionUiState,
    onOpenDrawer: () -> Unit = {},
    onSavePatch: (SettingsPatch, String?, (Boolean) -> Unit) -> Unit,
    onSetPassword: (String, String, (Boolean) -> Unit) -> Unit
) {
    val settings = state.snapshot.settings
    val lockState = state.snapshot.lockState

    var destination by rememberSaveable { mutableStateOf(SettingsDestination.MAIN) }
    var domainInput by rememberSaveable { mutableStateOf("") }
    var whitelistText by rememberSaveable(settings.whitelist.joinToString("\n")) {
        mutableStateOf(settings.whitelist.joinToString("\n"))
    }
    var blacklistText by rememberSaveable(settings.blacklist.joinToString("\n")) {
        mutableStateOf(settings.blacklist.joinToString("\n"))
    }

    var pendingChange by remember { mutableStateOf<PendingSettingChange?>(null) }
    var showChangePasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showFastScanLimitDialog by rememberSaveable { mutableStateOf(false) }

    val appBarTitle = when (destination) {
        SettingsDestination.MAIN -> "Settings"
        SettingsDestination.WHITELIST -> "Whitelist"
        SettingsDestination.BLACKLIST -> "Blacklist"
    }
    val currentDomains = when (destination) {
        SettingsDestination.WHITELIST -> parseDomains(whitelistText)
        SettingsDestination.BLACKLIST -> parseDomains(blacklistText)
        SettingsDestination.MAIN -> emptyList()
    }

    fun requestChange(change: PendingSettingChange) {
        pendingChange = change
    }

    fun requestPatch(title: String, message: String, patch: SettingsPatch) {
        requestChange(PendingSettingChange(title = title, message = message, patch = patch))
    }

    fun requestDomainPatch(domain: String, isAdd: Boolean) {
        val whitelist = parseDomains(whitelistText)
        val blacklist = parseDomains(blacklistText)
        val isWhitelist = destination == SettingsDestination.WHITELIST
        val nextDomains = if (isAdd) {
            (currentDomains + domain).distinct()
        } else {
            currentDomains.filterNot { it == domain }
        }
        val nextWhitelist = if (isWhitelist) nextDomains else whitelist
        val nextBlacklist = if (isWhitelist) blacklist else nextDomains
        val action = if (isAdd) "Add domain" else "Remove domain"

        requestChange(
            PendingSettingChange(
                title = action,
                message = "Enter your lock password to update domain rules.",
                patch = SettingsPatch(whitelist = nextWhitelist, blacklist = nextBlacklist),
                confirmText = action,
                onSuccess = {
                    whitelistText = nextWhitelist.joinToString("\n")
                    blacklistText = nextBlacklist.joinToString("\n")
                    if (isAdd) domainInput = ""
                }
            )
        )
    }

    Scaffold(
        topBar = {
            SettingsTopBar(
                title = appBarTitle,
                subtitle = when (destination) {
                    SettingsDestination.MAIN -> "Manage protection and security"
                    SettingsDestination.WHITELIST -> "Always allowed domains"
                    SettingsDestination.BLACKLIST -> "Always blocked domains"
                },
                hasPassword = lockState.hasPassword,
                showBack = destination != SettingsDestination.MAIN,
                onBack = {
                    destination = SettingsDestination.MAIN
                    domainInput = ""
                },
                onOpenDrawer = onOpenDrawer
            )
        },
        floatingActionButton = {
            if (destination != SettingsDestination.MAIN) {
                FloatingActionButton(
                    onClick = {
                        normalizeDomainCandidate(domainInput)?.let { normalized ->
                            requestDomainPatch(normalized, isAdd = true)
                        }
                    },
                    containerColor = PgAccentBlue,
                    contentColor = Color(0xFF0A0F1E)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add domain")
                }
            }
        }
    ) { innerPadding ->
        if (destination == SettingsDestination.MAIN) {
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
                    SettingsSection(title = "Core protection") {
                        SettingsToggleRow(
                            icon = Icons.Default.Shield,
                            iconColor = PgAccentBlue,
                            title = "Enable PureGuard",
                            subtitle = "Master switch for all layers",
                            checked = settings.enabled,
                            onCheckedChange = {
                                requestPatch(
                                    title = "Enable PureGuard",
                                    message = "Enter your lock password to change the master switch.",
                                    patch = SettingsPatch(enabled = it)
                                )
                            }
                        )
                        SettingsToggleRow(
                            icon = Icons.Default.Search,
                            iconColor = PgAccentViolet,
                            title = "Force SafeSearch",
                            subtitle = "Rewrite search engines to strict mode",
                            checked = settings.enforceSafeSearch,
                            onCheckedChange = {
                                requestPatch(
                                    title = "Force SafeSearch",
                                    message = "Enter your lock password to change SafeSearch.",
                                    patch = SettingsPatch(enforceSafeSearch = it)
                                )
                            }
                        )
                        SettingsToggleRow(
                            icon = Icons.Default.Image,
                            iconColor = PgAccentBlue,
                            title = "On-device image scan",
                            subtitle = "Analyze page images locally",
                            checked = settings.enableImageScan,
                            onCheckedChange = {
                                requestPatch(
                                    title = "Image scan",
                                    message = "Enter your lock password to change image scanning.",
                                    patch = SettingsPatch(enableImageScan = it)
                                )
                            }
                        )
                        SettingsToggleRow(
                            icon = Icons.Default.Speed,
                            iconColor = PgSuccess,
                            title = "Fast scan",
                            subtitle = "Quick pass first, deep pass after",
                            checked = settings.fastScan,
                            onCheckedChange = {
                                requestPatch(
                                    title = "Fast scan",
                                    message = "Enter your lock password to change fast scan.",
                                    patch = SettingsPatch(fastScan = it)
                                )
                            },
                            showDivider = false
                        )
                    }
                }

                item {
                    SettingsSection(title = "Advanced Blocking") {
                        SettingsToggleRow(
                            icon = Icons.Default.Warning,
                            iconColor = PgDanger,
                            title = "Strict mode",
                            subtitle = "Block suspicious pages immediately",
                            checked = settings.strictMode,
                            onCheckedChange = {
                                requestPatch(
                                    title = "Strict mode",
                                    message = "Enter your lock password to change strict mode.",
                                    patch = SettingsPatch(strictMode = it)
                                )
                            }
                        )
                        SettingsToggleRow(
                            icon = Icons.Default.VisibilityOff,
                            iconColor = PgAccentViolet,
                            title = "Block private tabs",
                            subtitle = "Prevent Incognito / Private browsing",
                            checked = settings.incognitoEnabled,
                            onCheckedChange = {
                                requestPatch(
                                    title = "Private tabs",
                                    message = "Enter your lock password to change private tab blocking.",
                                    patch = SettingsPatch(incognitoEnabled = it)
                                )
                            },
                            showDivider = false
                        )
                    }
                }

                item {
                    SettingsSection(title = "Filter sensitivity") {
                        Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
                            ModernSensitivitySelector(
                                selected = settings.sensitivity,
                                onSelect = {
                                    requestPatch(
                                        title = "Filter sensitivity",
                                        message = "Enter your lock password to change sensitivity.",
                                        patch = SettingsPatch(sensitivity = it)
                                    )
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                            SettingsActionRow(
                                icon = Icons.Default.Speed,
                                iconColor = PgAccentBlue,
                                title = "Fast-scan image budget",
                                subtitle = "${settings.fastScanLimit} images per pass",
                                onClick = { showFastScanLimitDialog = true },
                                showDivider = false
                            )
                        }
                    }
                }

                item {
                    SettingsSection(title = "Domain rules") {
                        DomainNavRow(
                            icon = Icons.Default.CheckCircle,
                            iconColor = PgSuccess,
                            title = "Whitelist",
                            subtitle = "Domains that are always allowed",
                            count = parseDomains(whitelistText).size,
                            onClick = {
                                destination = SettingsDestination.WHITELIST
                                domainInput = ""
                            }
                        )
                        DomainNavRow(
                            icon = Icons.Default.Block,
                            iconColor = PgDanger,
                            title = "Blacklist",
                            subtitle = "Domains that are always blocked",
                            count = parseDomains(blacklistText).size,
                            showDivider = false,
                            onClick = {
                                destination = SettingsDestination.BLACKLIST
                                domainInput = ""
                            }
                        )
                    }
                }

                item {
                    SettingsSection(title = "Security") {
                        SettingsActionRow(
                            icon = Icons.Default.Lock,
                            iconColor = PgAccentBlue,
                            title = "Change lock password",
                            subtitle = "Update the password used for protected changes",
                            onClick = { showChangePasswordDialog = true },
                            showDivider = false
                        )
                    }
                }
            }
        } else {
            DomainManagerScreen(
                destination = destination,
                domains = currentDomains,
                locked = false,
                domainInput = domainInput,
                onDomainInputChange = { domainInput = it },
                onDeleteDomain = { requestDomainPatch(it, isAdd = false) },
                innerPadding = innerPadding
            )
        }
    }

    pendingChange?.let { change ->
        PasswordGateDialog(
            title = change.title,
            message = change.message,
            confirmText = change.confirmText,
            onDismiss = { pendingChange = null },
            onConfirm = { password ->
                onSavePatch(change.patch, password) { ok ->
                    if (ok) {
                        change.onSuccess()
                        pendingChange = null
                    }
                }
            }
        )
    }

    if (showChangePasswordDialog) {
        ChangeLockPasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { oldPassword, newPassword ->
                onSetPassword(oldPassword, newPassword) { ok ->
                    if (ok) showChangePasswordDialog = false
                }
            }
        )
    }

    if (showFastScanLimitDialog) {
        FastScanLimitDialog(
            currentValue = settings.fastScanLimit,
            onDismiss = { showFastScanLimitDialog = false },
            onConfirm = { newLimit, password ->
                onSavePatch(SettingsPatch(fastScanLimit = newLimit), password) { ok ->
                    if (ok) showFastScanLimitDialog = false
                }
            }
        )
    }
}

@Composable
private fun SettingsTopBar(
    title: String,
    subtitle: String,
    hasPassword: Boolean,
    showBack: Boolean,
    onBack: () -> Unit,
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
            if (showBack) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(0.05f))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PgText,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
            } else {
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
                        contentDescription = "Open menu",
                        tint = PgText,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PgText)
                Text(subtitle, fontSize = 12.sp, color = PgMuted)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (hasPassword) PgSuccess.copy(0.15f) else PgDanger.copy(0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (hasPassword) "Secure" else "Required",
                    color = if (hasPassword) PgSuccess else PgDanger,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DomainNavRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    count: Int,
    showDivider: Boolean = true,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 14.dp, horizontal = 4.dp),
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
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("$count", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = iconColor)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = PgMuted, modifier = Modifier.size(18.dp))
        }
        if (showDivider) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(0.05f)))
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    showDivider: Boolean = true,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 14.dp, horizontal = 4.dp),
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
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = PgMuted, modifier = Modifier.size(18.dp))
        }
        if (showDivider) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(0.05f)))
        }
    }
}

@Composable
private fun ModernSensitivitySelector(
    selected: Sensitivity,
    onSelect: (Sensitivity) -> Unit
) {
    val options = Sensitivity.entries
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(0.05f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val active = selected == option
            val label = option.name.lowercase().replaceFirstChar { it.titlecase() }
            val activeColor = when (option) {
                Sensitivity.LOW -> PgSuccess
                Sensitivity.MEDIUM -> PgAccentBlue
                Sensitivity.HIGH -> PgDanger
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) activeColor.copy(0.2f) else Color.Transparent)
                    .clickable { onSelect(option) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    color = if (active) activeColor else PgMuted
                )
            }
        }
    }
}

@Composable
private fun FastScanLimitDialog(
    currentValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var value by rememberSaveable(currentValue) { mutableStateOf(currentValue.toString()) }
    var password by rememberSaveable { mutableStateOf("") }
    val parsed = value.toIntOrNull()?.coerceIn(4, 40)

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(TbColor.copy(alpha = 0.98f))
                .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(24.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Fast-scan image budget", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = PgText)
            Text("Choose a value from 4 to 40 and confirm with your lock password.", fontSize = 12.sp, color = PgMuted)
            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PgAccentBlue,
                unfocusedBorderColor = Color.White.copy(0.12f),
                focusedTextColor = PgText,
                unfocusedTextColor = PgText,
                disabledTextColor = PgMuted,
                disabledBorderColor = Color.White.copy(0.06f)
            )
            OutlinedTextField(
                value = value,
                onValueChange = { value = it.filter { c -> c.isDigit() }.take(2) },
                label = { Text("Image budget", color = PgMuted, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = fieldColors
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = PgMuted, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                colors = fieldColors
            )
            Button(
                onClick = { parsed?.let { onConfirm(it, password) } },
                enabled = parsed != null && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PgAccentBlue,
                    disabledContainerColor = Color.White.copy(0.07f)
                )
            ) {
                Text("Apply change", color = Color(0xFF0A0F1E), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
