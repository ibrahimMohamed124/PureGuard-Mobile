package com.pureguard.mobile.ui.features.settings

import android.annotation.SuppressLint
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private enum class SettingsDestination { MAIN, WHITELIST, BLACKLIST }

@OptIn(ExperimentalMaterial3Api::class)
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

    var destination by rememberSaveable { mutableStateOf(SettingsDestination.MAIN) }
    var domainInput by rememberSaveable { mutableStateOf("") }

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
    LaunchedEffect(locked) {
        if (locked && destination != SettingsDestination.MAIN) {
            destination = SettingsDestination.MAIN
            domainInput = ""
        }
    }

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

    fun addDomain(domain: String) {
        when (destination) {
            SettingsDestination.WHITELIST -> whitelistText = (parseDomains(whitelistText) + domain).distinct().joinToString("\n")
            SettingsDestination.BLACKLIST -> blacklistText = (parseDomains(blacklistText) + domain).distinct().joinToString("\n")
            SettingsDestination.MAIN -> Unit
        }
    }

    fun removeDomain(domain: String) {
        when (destination) {
            SettingsDestination.WHITELIST -> whitelistText = parseDomains(whitelistText).filterNot { it == domain }.joinToString("\n")
            SettingsDestination.BLACKLIST -> blacklistText = parseDomains(blacklistText).filterNot { it == domain }.joinToString("\n")
            SettingsDestination.MAIN -> Unit
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        TbColor.copy(alpha = 0.92f)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(0.06f)
                    )
                    .padding(
                        top = 8.dp
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Back Button
                    if (destination != SettingsDestination.MAIN) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(0.05f))
                                .clickable {
                                    destination = SettingsDestination.MAIN
                                    domainInput = ""
                                },
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
                    }

                    // Title + Subtitle
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = appBarTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PgText
                        )

                        Text(
                            text = when (destination) {
                                SettingsDestination.MAIN ->
                                    "Manage protection and security"

                                SettingsDestination.WHITELIST ->
                                    "Always allowed domains"

                                SettingsDestination.BLACKLIST ->
                                    "Always blocked domains"
                            },
                            fontSize = 12.sp,
                            color = PgMuted
                        )
                    }

                    // Right Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (locked)
                                    PgDanger.copy(0.15f)
                                else
                                    PgSuccess.copy(0.15f)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (locked) "Locked" else "Secure",
                            color = if (locked) PgDanger else PgSuccess,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (destination != SettingsDestination.MAIN && !locked) {
                FloatingActionButton(
                    onClick = {
                        normalizeDomainCandidate(domainInput)?.let { normalized ->
                            addDomain(normalized)
                            domainInput = ""
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
            val whitelistDomains = parseDomains(whitelistText)
            val blacklistDomains = parseDomains(blacklistText)
            val patch = SettingsPatch(
                enabled = enabled,
                sensitivity = sensitivity,
                enforceSafeSearch = safeSearch,
                enableImageScan = imageScan,
                fastScan = fastScan,
                fastScanLimit = fastScanLimit.toIntOrNull() ?: settings.fastScanLimit,
                strictMode = strictMode,
                whitelist = whitelistDomains,
                blacklist = blacklistDomains,
                incognitoEnabled = incognitoEnabled
            )

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
                if (locked) {
                    item { LockCard(unlockPassword, { unlockPassword = it }) { ok ->
                        onVerifyPassword(unlockPassword) { result ->
                            if (result) {
                                localUnlocked = true
                                sessionPassword = unlockPassword
                                unlockPassword = ""
                            }
                        }
                    } }
                }

                item {
                    SettingsSection(title = "Core protection") {
                        SettingsToggleRow(
                            icon = Icons.Default.Shield,
                            iconColor = PgAccentBlue,
                            title = "Enable PureGuard",
                            subtitle = "Master switch for all layers",
                            checked = enabled,
                            enabled = !locked,
                            onCheckedChange = { enabled = it }
                        )
                        SettingsToggleRow(
                            icon = Icons.Default.Search,
                            iconColor = PgAccentViolet,
                            title = "Force SafeSearch",
                            subtitle = "Rewrite search engines to strict mode",
                            checked = safeSearch,
                            enabled = !locked,
                            onCheckedChange = { safeSearch = it }
                        )
                        SettingsToggleRow(
                            icon = Icons.Default.Image,
                            iconColor = PgAccentBlue,
                            title = "On-device image scan",
                            subtitle = "Analyze page images locally",
                            checked = imageScan,
                            enabled = !locked,
                            onCheckedChange = { imageScan = it }
                        )
                        SettingsToggleRow(
                            icon = Icons.Default.Speed,
                            iconColor = PgSuccess,
                            title = "Fast scan",
                            subtitle = "Quick pass first, deep pass after",
                            checked = fastScan,
                            enabled = !locked,
                            onCheckedChange = { fastScan = it },
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
                            checked = strictMode,
                            enabled = !locked,
                            onCheckedChange = { strictMode = it }
                        )
                        SettingsToggleRow(
                            icon = Icons.Default.VisibilityOff,
                            iconColor = PgAccentViolet,
                            title = "Block private tabs",
                            subtitle = "Prevent Incognito / Private browsing",
                            checked = incognitoEnabled,
                            enabled = !locked,
                            onCheckedChange = { incognitoEnabled = it },
                            showDivider = false
                        )
                    }
                }

                item {
                    SettingsSection(title = "Filter sensitivity") {
                        Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
                            ModernSensitivitySelector(
                                selected = sensitivity,
                                enabled = !locked,
                                onSelect = { sensitivity = it }
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = fastScanLimit,
                                onValueChange = { fastScanLimit = it.filter { c -> c.isDigit() } },
                                label = { Text("Fast-scan image budget (4–40)", color = PgMuted, fontSize = 13.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !locked,
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PgAccentBlue,
                                    unfocusedBorderColor = Color.White.copy(0.12f),
                                    focusedTextColor = PgText,
                                    unfocusedTextColor = PgText,
                                    disabledTextColor = PgMuted,
                                    disabledBorderColor = Color.White.copy(0.06f)
                                )
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
                            count = whitelistDomains.size,
                            enabled = !locked,
                            onClick = { destination = SettingsDestination.WHITELIST; domainInput = "" }
                        )
                        DomainNavRow(
                            icon = Icons.Default.Block,
                            iconColor = PgDanger,
                            title = "Blacklist",
                            subtitle = "Domains that are always blocked",
                            count = blacklistDomains.size,
                            enabled = !locked,
                            showDivider = false,
                            onClick = { destination = SettingsDestination.BLACKLIST; domainInput = "" }
                        )
                    }
                }

                item {
                    TamperProtectionCard(
                        hasPassword = lockState.hasPassword,
                        locked = locked,
                        oldPassword = oldPassword,
                        newPassword = newPassword,
                        newPassword2 = newPassword2,
                        removePassword = removePassword,
                        onOldPasswordChange = { oldPassword = it },
                        onNewPasswordChange = { newPassword = it },
                        onNewPassword2Change = { newPassword2 = it },
                        onRemovePasswordChange = { removePassword = it },
                        onSetPassword = {
                            if (newPassword.isNotBlank() && newPassword == newPassword2) {
                                onSetPassword(oldPassword, newPassword)
                                oldPassword = ""; newPassword = ""; newPassword2 = ""
                            }
                        },
                        onRemovePassword = {
                            onRemovePassword(removePassword)
                            removePassword = ""; localUnlocked = false; sessionPassword = ""
                        }
                    )
                }

                item {
                    Box(modifier = Modifier.padding(top = 4.dp)) {
                        Button(
                            onClick = { onSavePatch(patch, sessionPassword.ifBlank { null }) },
                            enabled = !locked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PgAccentBlue,
                                disabledContainerColor = Color.White.copy(0.07f)
                            )
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Save settings",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0A0F1E)
                            )
                        }
                    }
                }
            }
        } else {
            DomainManagerScreen(
                destination = destination,
                domains = currentDomains,
                locked = locked,
                domainInput = domainInput,
                onDomainInputChange = { domainInput = it },
                onDeleteDomain = { removeDomain(it) },
                innerPadding = innerPadding
            )
        }
    }
}

@Composable
private fun LockCard(
    password: String,
    onPasswordChange: (String) -> Unit,
    onUnlock: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1A1030))
            .border(1.dp, PgAccentViolet.copy(0.3f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(PgAccentViolet.copy(0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, null, tint = PgAccentViolet, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text("Settings locked", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PgText)
                    Text("Enter your password to edit settings", fontSize = 13.sp, color = PgMuted)
                }
            }

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password", color = PgMuted, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PgAccentViolet,
                    unfocusedBorderColor = Color.White.copy(0.12f),
                    focusedTextColor = PgText,
                    unfocusedTextColor = PgText
                )
            )

            Button(
                onClick = { onUnlock(true) },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PgAccentViolet)
            ) {
                Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp), tint = Color(0xFF0A0F1E))
                Spacer(Modifier.width(8.dp))
                Text("Unlock", fontWeight = FontWeight.SemiBold, color = Color(0xFF0A0F1E))
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // عنوان القسم بره الـ Container عشان يفصل المجموعات بشكل نظيف جداً كـ Headers
        Text(
            text = title.uppercase(), // أو سيبها عادي حسب رغبتك
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = PgAccentBlue, // أو أي لون براند أساسي عندك زي PgAccentViolet
            modifier = Modifier
                .padding(start = 4.dp, bottom = 8.dp, top = 8.dp)
                .alpha(0.8f)
        )

        // الـ Container اللي جواه الـ Rows
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(0.04f))
                .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    showDivider: Boolean = true,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
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
            Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        }
        if (showDivider) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(0.05f)))
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
    enabled: Boolean,
    showDivider: Boolean = true,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.alpha(if (enabled) 1f else 0.5f)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
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
private fun ModernSensitivitySelector(
    selected: Sensitivity,
    enabled: Boolean,
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
                else -> PgAccentViolet
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) activeColor.copy(0.2f) else Color.Transparent)
                    .then(if (enabled) Modifier.clickable { onSelect(option) } else Modifier)
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
private fun TamperProtectionCard(
    hasPassword: Boolean,
    locked: Boolean,
    oldPassword: String,
    newPassword: String,
    newPassword2: String,
    removePassword: String,
    onOldPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onNewPassword2Change: (String) -> Unit,
    onRemovePasswordChange: (String) -> Unit,
    onSetPassword: () -> Unit,
    onRemovePassword: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(0.04f))
            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(PgAccentBlue.copy(0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, null, tint = PgAccentBlue, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Tamper protection", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PgText)
                Text("Password-protect your settings", fontSize = 12.sp, color = PgMuted)
            }
            if (hasPassword) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PgSuccess.copy(0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PgSuccess)
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(0.06f)))

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PgAccentBlue,
                unfocusedBorderColor = Color.White.copy(0.12f),
                focusedTextColor = PgText,
                unfocusedTextColor = PgText,
                disabledTextColor = PgMuted,
                disabledBorderColor = Color.White.copy(0.06f)
            )

            if (hasPassword) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = onOldPasswordChange,
                    label = { Text("Current password", color = PgMuted, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = fieldColors
                )
            }

            OutlinedTextField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                label = { Text("New password", color = PgMuted, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = fieldColors
            )

            OutlinedTextField(
                value = newPassword2,
                onValueChange = onNewPassword2Change,
                label = { Text("Confirm new password", color = PgMuted, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = fieldColors
            )

            val passwordsMatch = newPassword.isNotBlank() && newPassword == newPassword2
            Button(
                onClick = onSetPassword,
                enabled = passwordsMatch,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PgAccentBlue,
                    disabledContainerColor = Color.White.copy(0.07f)
                )
            ) {
                Text(
                    text = if (hasPassword) "Change password" else "Set password",
                    fontWeight = FontWeight.SemiBold,
                    color = if (passwordsMatch) Color(0xFF0A0F1E) else PgMuted
                )
            }

            if (hasPassword) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(0.06f)))
                OutlinedTextField(
                    value = removePassword,
                    onValueChange = onRemovePasswordChange,
                    label = { Text("Password to remove lock", color = PgMuted, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = fieldColors
                )
                TextButton(
                    onClick = onRemovePassword,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Remove password lock", color = PgDanger, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun DomainManagerScreen(
    destination: SettingsDestination,
    domains: List<String>,
    locked: Boolean,
    domainInput: String,
    onDomainInputChange: (String) -> Unit,
    onDeleteDomain: (String) -> Unit,
    innerPadding: PaddingValues
) {
    val isWhitelist = destination == SettingsDestination.WHITELIST
    val accentColor = if (isWhitelist) PgSuccess else PgDanger
    val title = if (isWhitelist) "Allowed domains" else "Blocked domains"
    val helper = if (isWhitelist) "These domains are always allowed through." else "These domains are always blocked."

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = innerPadding.calculateTopPadding() + 4.dp,
            bottom = innerPadding.calculateBottomPadding() + 88.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(accentColor.copy(0.06f))
                    .border(1.dp, accentColor.copy(0.2f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PgText)
                Text(helper, fontSize = 13.sp, color = PgMuted)
                OutlinedTextField(
                    value = domainInput,
                    onValueChange = onDomainInputChange,
                    label = { Text("Domain to add", color = PgMuted, fontSize = 13.sp) },
                    placeholder = { Text("example.com", color = PgMuted.copy(0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !locked,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color.White.copy(0.12f),
                        focusedTextColor = PgText,
                        unfocusedTextColor = PgText
                    )
                )
            }
        }

        if (domains.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(0.03f))
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(accentColor.copy(0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isWhitelist) Icons.Default.CheckCircle else Icons.Default.Block,
                                null, tint = accentColor, modifier = Modifier.size(24.dp)
                            )
                        }
                        Text("No domains added yet", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PgText)
                        Text("Use the + button to add your first domain", fontSize = 12.sp, color = PgMuted)
                    }
                }
            }
        } else {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(0.04f))
                        .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(16.dp))
                ) {
                    domains.forEachIndexed { index, domain ->
                        DomainRow(
                            domain = domain,
                            accentColor = accentColor,
                            enabled = !locked,
                            showDivider = index < domains.lastIndex,
                            onDelete = { onDeleteDomain(domain) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DomainRow(
    domain: String,
    accentColor: Color,
    enabled: Boolean,
    showDivider: Boolean,
    onDelete: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(accentColor, CircleShape)
            )
            Text(
                text = domain,
                style = MaterialTheme.typography.bodyMedium,
                color = PgText,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete, enabled = enabled, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Remove",
                    tint = if (enabled) PgDanger.copy(0.7f) else PgMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        if (showDivider) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(0.05f)))
        }
    }
}

private fun parseDomains(text: String): List<String> =
    text.lines().map { it.trim().lowercase() }.filter { it.isNotBlank() }.distinct()

private fun normalizeDomainCandidate(rawInput: String): String? {
    val cleaned = rawInput.trim().lowercase()
        .removePrefix("https://").removePrefix("http://").removePrefix("www.")
        .substringBefore("/").substringBefore("?").substringBefore("#")
    if (cleaned.isBlank() || cleaned.length < 3 || !cleaned.contains('.') || cleaned.any { it.isWhitespace() }) return null
    return cleaned
}
