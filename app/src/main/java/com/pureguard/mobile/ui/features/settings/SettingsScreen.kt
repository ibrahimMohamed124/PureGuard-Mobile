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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pureguard.mobile.R
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
    val confirmText: String? = null,
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
    val context = LocalContext.current

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
        SettingsDestination.MAIN -> stringResource(R.string.settings_title)
        SettingsDestination.WHITELIST -> stringResource(R.string.settings_whitelist_title)
        SettingsDestination.BLACKLIST -> stringResource(R.string.settings_blacklist_title)
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
        val action = if (isAdd) context.getString(R.string.settings_add_domain) else context.getString(R.string.settings_remove_domain)

        requestChange(
            PendingSettingChange(
                title = action,
                message = context.getString(R.string.settings_domain_password_message),
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
                    SettingsDestination.MAIN -> stringResource(R.string.settings_subtitle)
                    SettingsDestination.WHITELIST -> stringResource(R.string.settings_whitelist_subtitle)
                    SettingsDestination.BLACKLIST -> stringResource(R.string.settings_blacklist_subtitle)
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
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.settings_add_domain))
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
                    SettingsSection(title = stringResource(R.string.settings_section_core)) {
                        SettingsToggleRow(
                            icon = Icons.Default.Shield,
                            iconColor = PgAccentBlue,
                            title = stringResource(R.string.home_enable_pureguard),
                            subtitle = stringResource(R.string.home_master_switch),
                            checked = settings.enabled,
                            onCheckedChange = {
                                requestPatch(
                                    title = context.getString(R.string.home_enable_pureguard),
                                    message = context.getString(R.string.settings_enable_message),
                                    patch = SettingsPatch(enabled = it)
                                )
                            }
                        )
                        SettingsToggleRow(
                            icon = Icons.Default.Search,
                            iconColor = PgAccentViolet,
                            title = stringResource(R.string.home_force_safesearch),
                            subtitle = stringResource(R.string.settings_safesearch_subtitle),
                            checked = settings.enforceSafeSearch,
                            onCheckedChange = {
                                requestPatch(
                                    title = context.getString(R.string.home_force_safesearch),
                                    message = context.getString(R.string.settings_safesearch_message),
                                    patch = SettingsPatch(enforceSafeSearch = it)
                                )
                            }
                        )
                        SettingsToggleRow(
                            icon = Icons.Default.Image,
                            iconColor = PgAccentBlue,
                            title = stringResource(R.string.home_image_scan),
                            subtitle = stringResource(R.string.home_image_scan_subtitle),
                            checked = settings.enableImageScan,
                            onCheckedChange = {
                                requestPatch(
                                    title = context.getString(R.string.home_image_scan),
                                    message = context.getString(R.string.settings_image_scan_message),
                                    patch = SettingsPatch(enableImageScan = it)
                                )
                            }
                        )
                        SettingsToggleRow(
                            icon = Icons.Default.Speed,
                            iconColor = PgSuccess,
                            title = stringResource(R.string.settings_fast_scan),
                            subtitle = stringResource(R.string.settings_fast_scan_subtitle),
                            checked = settings.fastScan,
                            onCheckedChange = {
                                requestPatch(
                                    title = context.getString(R.string.settings_fast_scan),
                                    message = context.getString(R.string.settings_fast_scan_message),
                                    patch = SettingsPatch(fastScan = it)
                                )
                            },
                            showDivider = false
                        )
                    }
                }

                item {
                    SettingsSection(title = stringResource(R.string.settings_section_advanced)) {
                        SettingsToggleRow(
                            icon = Icons.Default.Warning,
                            iconColor = PgDanger,
                            title = stringResource(R.string.settings_strict_mode),
                            subtitle = stringResource(R.string.settings_strict_subtitle),
                            checked = settings.strictMode,
                            onCheckedChange = {
                                requestPatch(
                                    title = context.getString(R.string.settings_strict_mode),
                                    message = context.getString(R.string.settings_strict_message),
                                    patch = SettingsPatch(strictMode = it)
                                )
                            }
                        )
                        SettingsToggleRow(
                            icon = Icons.Default.VisibilityOff,
                            iconColor = PgAccentViolet,
                            title = stringResource(R.string.settings_block_private_tabs),
                            subtitle = stringResource(R.string.settings_private_tabs_subtitle),
                            checked = settings.incognitoEnabled,
                            onCheckedChange = {
                                requestPatch(
                                    title = context.getString(R.string.settings_private_tabs_title),
                                    message = context.getString(R.string.settings_private_tabs_message),
                                    patch = SettingsPatch(incognitoEnabled = it)
                                )
                            },
                            showDivider = false
                        )
                    }
                }

                item {
                    SettingsSection(title = stringResource(R.string.home_section_filter_sensitivity)) {
                        Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
                            ModernSensitivitySelector(
                                selected = settings.sensitivity,
                                onSelect = {
                                    requestPatch(
                                        title = context.getString(R.string.home_section_filter_sensitivity),
                                        message = context.getString(R.string.settings_sensitivity_message),
                                        patch = SettingsPatch(sensitivity = it)
                                    )
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                            SettingsActionRow(
                                icon = Icons.Default.Speed,
                                iconColor = PgAccentBlue,
                                title = stringResource(R.string.settings_fast_scan_budget),
                                subtitle = stringResource(R.string.settings_images_per_pass, settings.fastScanLimit),
                                onClick = { showFastScanLimitDialog = true },
                                showDivider = false
                            )
                        }
                    }
                }

                item {
                    SettingsSection(title = stringResource(R.string.settings_section_domain_rules)) {
                        DomainNavRow(
                            icon = Icons.Default.CheckCircle,
                            iconColor = PgSuccess,
                            title = stringResource(R.string.settings_whitelist_title),
                            subtitle = stringResource(R.string.settings_whitelist_subtitle_row),
                            count = parseDomains(whitelistText).size,
                            onClick = {
                                destination = SettingsDestination.WHITELIST
                                domainInput = ""
                            }
                        )
                        DomainNavRow(
                            icon = Icons.Default.Block,
                            iconColor = PgDanger,
                            title = stringResource(R.string.settings_blacklist_title),
                            subtitle = stringResource(R.string.settings_blacklist_subtitle_row),
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
                    SettingsSection(title = stringResource(R.string.settings_section_security)) {
                        SettingsActionRow(
                            icon = Icons.Default.Lock,
                            iconColor = PgAccentBlue,
                            title = stringResource(R.string.settings_change_lock_password),
                            subtitle = stringResource(R.string.settings_change_lock_password_subtitle),
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
            confirmText = change.confirmText ?: stringResource(R.string.common_apply_change),
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
                        contentDescription = stringResource(R.string.common_back),
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
                        contentDescription = stringResource(R.string.common_open_menu),
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
                    text = if (hasPassword) stringResource(R.string.common_secure) else stringResource(R.string.common_required),
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
            val label = sensitivityLabel(option)
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
            Text(stringResource(R.string.settings_fast_scan_budget), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = PgText)
            Text(stringResource(R.string.settings_choose_budget_message), fontSize = 12.sp, color = PgMuted)
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
                label = { Text(stringResource(R.string.settings_image_budget), color = PgMuted, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = fieldColors
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.common_password), color = PgMuted, fontSize = 13.sp) },
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
                Text(stringResource(R.string.common_apply_change), color = Color(0xFF0A0F1E), fontWeight = FontWeight.SemiBold)
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
