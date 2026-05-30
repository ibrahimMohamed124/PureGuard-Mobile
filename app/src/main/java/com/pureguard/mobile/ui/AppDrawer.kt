package com.pureguard.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import com.pureguard.mobile.R
import com.pureguard.mobile.core.datastore.Prefs
import com.pureguard.mobile.core.localization.AppLanguage
import com.pureguard.mobile.core.navigation.NavRoutes
import com.pureguard.mobile.ui.theme.PgAccentBlue
import com.pureguard.mobile.ui.theme.PgAccentViolet
import com.pureguard.mobile.ui.theme.PgMuted
import com.pureguard.mobile.ui.theme.PgText
import com.pureguard.mobile.ui.theme.TbColor

private const val PREF_DRAWER_USERNAME = "drawer_username"
internal const val PREF_DRAWER_THEME = "drawer_theme"

private data class DrawerPalette(
    val background: Color,
    val surface: Color,
    val elevated: Color,
    val text: Color,
    val muted: Color,
    val border: Color
)

private data class FaqItem(
    @StringRes val questionRes: Int,
    @StringRes val answerRes: Int
)

private data class ChoiceOption(
    val value: String,
    val label: String
)

private val faqItems = listOf(
    FaqItem(R.string.faq_q_local_vpn, R.string.faq_a_local_vpn),
    FaqItem(R.string.faq_q_accessibility, R.string.faq_a_accessibility),
    FaqItem(R.string.faq_q_battery, R.string.faq_a_battery),
    FaqItem(R.string.faq_q_custom_domains, R.string.faq_a_custom_domains),
    FaqItem(R.string.faq_q_dns, R.string.faq_a_dns),
    FaqItem(R.string.faq_q_uninstall, R.string.faq_a_uninstall)
)

@Composable
fun AppDrawer(
    selectedTheme: String,
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    var username by rememberSaveable {
        mutableStateOf(Prefs.getString(PREF_DRAWER_USERNAME, "").orEmpty())
    }
    var showUsernameDialog by rememberSaveable { mutableStateOf(false) }

    val palette = remember(selectedTheme) { drawerPalette(selectedTheme) }
    val displayName = username.ifBlank { stringResource(R.string.common_pureguard_user) }

    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(max = 360.dp),
        drawerContainerColor = Color.Transparent,
        drawerContentColor = palette.text,
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            palette.background,
                            palette.background.copy(alpha = 0.96f)
                        )
                    )
                )
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 28.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                DrawerProfileHeader(
                    username = displayName,
                    palette = palette,
                    onSetUsername = { showUsernameDialog = true }
                )
            }

            item {
                DrawerSectionTitle(
                    title = stringResource(R.string.drawer_menu),
                    icon = Icons.Default.Menu
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DrawerMenuButton(
                        icon = Icons.Default.Palette,
                        title = stringResource(R.string.drawer_preferences),
                        subtitle = stringResource(R.string.drawer_preferences_subtitle),
                        selected = currentRoute == NavRoutes.Preferences.route,
                        palette = palette,
                        onClick = { onNavigate(NavRoutes.Preferences.route) }
                    )
                    DrawerMenuButton(
                        icon = Icons.Default.SupportAgent,
                        title = stringResource(R.string.drawer_support),
                        subtitle = stringResource(R.string.drawer_support_subtitle),
                        selected = currentRoute == NavRoutes.Support.route,
                        palette = palette,
                        onClick = { onNavigate(NavRoutes.Support.route) }
                    )
                    DrawerMenuButton(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.drawer_faqs),
                        subtitle = stringResource(R.string.drawer_faqs_subtitle),
                        selected = currentRoute == NavRoutes.Faqs.route,
                        palette = palette,
                        onClick = { onNavigate(NavRoutes.Faqs.route) }
                    )
                }
            }
        }
    }

    if (showUsernameDialog) {
        UsernameDialog(
            currentUsername = username,
            palette = palette,
            onDismiss = { showUsernameDialog = false },
            onSave = {
                username = it.trim()
                Prefs.putString(PREF_DRAWER_USERNAME, username)
                showUsernameDialog = false
            }
        )
    }
}

@Composable
fun PreferencesDrawerPage(
    selectedTheme: String,
    selectedLanguage: String,
    onThemeSelected: (String) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val palette = remember(selectedTheme) { drawerPalette(selectedTheme) }

    Scaffold(
        topBar = {
            DrawerFeatureTopBar(
                title = stringResource(R.string.drawer_preferences),
                subtitle = stringResource(R.string.preferences_subtitle),
                icon = Icons.Default.Palette,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DrawerSection(
                    title = stringResource(R.string.drawer_preferences),
                    icon = Icons.Default.Palette,
                    palette = palette
                ) {
                    DrawerChoiceGroup(
                        icon = Icons.Default.Palette,
                        title = stringResource(R.string.preferences_theme),
                        selectedValue = selectedTheme,
                        options = listOf(
                            ChoiceOption("Dark", stringResource(R.string.theme_dark)),
                            ChoiceOption("Light", stringResource(R.string.theme_light))
                        ),
                        palette = palette,
                        onSelect = onThemeSelected
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    DrawerChoiceGroup(
                        icon = Icons.Default.Language,
                        title = stringResource(R.string.preferences_language),
                        selectedValue = selectedLanguage,
                        options = listOf(
                            ChoiceOption(AppLanguage.SYSTEM, stringResource(R.string.language_system)),
                            ChoiceOption(AppLanguage.ENGLISH, stringResource(R.string.language_english)),
                            ChoiceOption(AppLanguage.ARABIC, stringResource(R.string.language_arabic))
                        ),
                        palette = palette,
                        onSelect = onLanguageSelected
                    )
                }
            }
        }
    }
}

@Composable
fun SupportDrawerPage(
    selectedTheme: String,
    onBack: () -> Unit
) {
    var supportEmail by rememberSaveable { mutableStateOf("") }
    var issueTypeIndex by rememberSaveable { mutableStateOf(0) }
    var supportMessage by rememberSaveable { mutableStateOf("") }
    val palette = remember(selectedTheme) { drawerPalette(selectedTheme) }

    Scaffold(
        topBar = {
            DrawerFeatureTopBar(
                title = stringResource(R.string.drawer_support),
                subtitle = stringResource(R.string.support_subtitle),
                icon = Icons.Default.SupportAgent,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DrawerSection(
                    title = stringResource(R.string.drawer_support),
                    icon = Icons.Default.SupportAgent,
                    palette = palette
                ) {
                    SupportForm(
                        email = supportEmail,
                        onEmailChange = { supportEmail = it },
                        issueTypeIndex = issueTypeIndex,
                        onIssueTypeChange = { issueTypeIndex = it },
                        message = supportMessage,
                        onMessageChange = { supportMessage = it },
                        palette = palette
                    )
                }
            }
        }
    }
}

@Composable
fun FaqsDrawerPage(
    selectedTheme: String,
    onBack: () -> Unit
) {
    var expandedFaq by rememberSaveable { mutableStateOf(-1) }
    val palette = remember(selectedTheme) { drawerPalette(selectedTheme) }

    Scaffold(
        topBar = {
            DrawerFeatureTopBar(
                title = stringResource(R.string.drawer_faqs),
                subtitle = stringResource(R.string.faqs_subtitle),
                icon = Icons.Default.Info,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(faqItems) { index, item ->
                FaqRow(
                    item = item,
                    expanded = expandedFaq == index,
                    palette = palette,
                    onClick = {
                        expandedFaq = if (expandedFaq == index) -1 else index
                    }
                )
            }
        }
    }
}

@Composable
private fun DrawerFeatureTopBar(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onBack: () -> Unit
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PgText
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = PgMuted
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PgAccentBlue.copy(0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PgAccentBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun DrawerProfileHeader(
    username: String,
    palette: DrawerPalette,
    onSetUsername: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        PgAccentBlue.copy(alpha = 0.18f),
                        PgAccentViolet.copy(alpha = 0.12f),
                        palette.surface
                    )
                )
            )
            .border(1.dp, palette.border, RoundedCornerShape(28.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(78.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(PgAccentBlue.copy(0.95f), PgAccentViolet.copy(0.9f))
                    )
                )
                .border(2.dp, Color.White.copy(0.22f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initialsFor(username),
                color = Color(0xFF07111F),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = username,
            color = palette.text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onSetUsername) {
            Text(stringResource(R.string.drawer_set_username), color = PgAccentBlue, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DrawerSectionTitle(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = PgAccentBlue, modifier = Modifier.size(17.dp))
        Text(
            text = title.uppercase(),
            color = PgAccentBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun DrawerMenuButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    palette: DrawerPalette,
    onClick: () -> Unit
) {
    val borderColor = if (selected) PgAccentBlue.copy(alpha = 0.5f) else palette.border
    val backgroundColor = if (selected) PgAccentBlue.copy(alpha = 0.14f) else palette.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(PgAccentBlue.copy(alpha = if (selected) 0.2f else 0.11f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PgAccentBlue, modifier = Modifier.size(19.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = palette.text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, color = palette.muted, fontSize = 12.sp)
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = if (selected) PgAccentBlue else palette.muted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun DrawerSection(
    title: String,
    icon: ImageVector,
    palette: DrawerPalette,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        DrawerSectionTitle(title = title, icon = icon)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(palette.surface)
                .border(1.dp, palette.border, RoundedCornerShape(24.dp))
                .padding(14.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun DrawerChoiceGroup(
    icon: ImageVector,
    title: String,
    selectedValue: String,
    options: List<ChoiceOption>,
    palette: DrawerPalette,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PgAccentBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PgAccentBlue, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, color = palette.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(options.firstOrNull { it.value == selectedValue }?.label.orEmpty(), color = palette.muted, fontSize = 12.sp)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val selectedOption = selectedValue == option.value
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selectedOption) PgAccentBlue.copy(alpha = 0.18f) else palette.elevated)
                        .border(
                            width = 1.dp,
                            color = if (selectedOption) PgAccentBlue.copy(alpha = 0.52f) else palette.border,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelect(option.value) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.label,
                        color = if (selectedOption) PgAccentBlue else palette.muted,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SupportForm(
    email: String,
    onEmailChange: (String) -> Unit,
    issueTypeIndex: Int,
    onIssueTypeChange: (Int) -> Unit,
    message: String,
    onMessageChange: (String) -> Unit,
    palette: DrawerPalette
) {
    var issueMenuExpanded by remember { mutableStateOf(false) }
    val issueTypes = listOf(
        R.string.support_issue_technical,
        R.string.support_issue_bug,
        R.string.support_issue_feature,
        R.string.support_issue_other
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.support_email)) },
            leadingIcon = {
                Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = PgAccentBlue)
            },
            singleLine = true,
            colors = drawerTextFieldColors(palette)
        )

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.elevated)
                    .border(1.dp, palette.border, RoundedCornerShape(16.dp))
                    .clickable { issueMenuExpanded = true }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.support_issue_type), color = palette.muted, fontSize = 12.sp)
                    Text(stringResource(issueTypes[issueTypeIndex]), color = palette.text, fontWeight = FontWeight.SemiBold)
                }
                Text("v", color = PgAccentBlue, fontWeight = FontWeight.Bold)
            }

            DropdownMenu(
                expanded = issueMenuExpanded,
                onDismissRequest = { issueMenuExpanded = false }
            ) {
                issueTypes.forEachIndexed { index, type ->
                    DropdownMenuItem(
                        text = { Text(stringResource(type)) },
                        onClick = {
                            onIssueTypeChange(index)
                            issueMenuExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp),
            label = { Text(stringResource(R.string.support_message)) },
            minLines = 6,
            colors = drawerTextFieldColors(palette)
        )
    }
}

@Composable
private fun FaqRow(
    item: FaqItem,
    expanded: Boolean,
    palette: DrawerPalette,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.surface)
            .border(1.dp, if (expanded) PgAccentBlue.copy(0.32f) else palette.border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(item.questionRes),
                color = palette.text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (expanded) "-" else "+",
                color = PgAccentBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(item.answerRes),
                color = palette.muted,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun UsernameDialog(
    currentUsername: String,
    palette: DrawerPalette,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var draftUsername by rememberSaveable { mutableStateOf(currentUsername) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(palette.background)
                .border(1.dp, palette.border, RoundedCornerShape(28.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PgAccentBlue.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = PgAccentBlue)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.drawer_set_username), color = palette.text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(stringResource(R.string.drawer_username_hint), color = palette.muted, fontSize = 12.sp)
                }
            }

            OutlinedTextField(
                value = draftUsername,
                onValueChange = { draftUsername = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.drawer_username_label)) },
                singleLine = true,
                colors = drawerTextFieldColors(palette)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.common_cancel), color = palette.muted)
                }
                Button(
                    onClick = { onSave(draftUsername) },
                    enabled = draftUsername.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PgAccentBlue,
                        contentColor = Color(0xFF07111F)
                    )
                ) {
                    Text(stringResource(R.string.common_save), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun drawerTextFieldColors(palette: DrawerPalette) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = palette.text,
    unfocusedTextColor = palette.text,
    focusedContainerColor = palette.elevated,
    unfocusedContainerColor = palette.elevated,
    focusedBorderColor = PgAccentBlue.copy(alpha = 0.72f),
    unfocusedBorderColor = palette.border,
    focusedLabelColor = PgAccentBlue,
    unfocusedLabelColor = palette.muted,
    cursorColor = PgAccentBlue
)

private fun drawerPalette(theme: String): DrawerPalette {
    return if (theme == "Light") {
        DrawerPalette(
            background = Color(0xFFF3F8FF),
            surface = Color(0xE6FFFFFF),
            elevated = Color(0xFFFFFFFF),
            text = Color(0xFF102033),
            muted = Color(0xFF637083),
            border = Color(0x220B1020)
        )
    } else {
        DrawerPalette(
            background = TbColor,
            surface = Color.White.copy(alpha = 0.055f),
            elevated = Color.White.copy(alpha = 0.055f),
            text = PgText,
            muted = PgMuted,
            border = Color.White.copy(alpha = 0.08f)
        )
    }
}

private fun initialsFor(username: String): String {
    val cleanParts = username
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() && it != "PureGuard" && it != "User" }

    return cleanParts
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "PG" }
}
