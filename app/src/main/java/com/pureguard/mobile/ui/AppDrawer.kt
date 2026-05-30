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
import com.pureguard.mobile.core.datastore.Prefs
import com.pureguard.mobile.core.navigation.NavRoutes
import com.pureguard.mobile.ui.theme.PgAccentBlue
import com.pureguard.mobile.ui.theme.PgAccentViolet
import com.pureguard.mobile.ui.theme.PgMuted
import com.pureguard.mobile.ui.theme.PgText
import com.pureguard.mobile.ui.theme.TbColor

private const val PREF_DRAWER_USERNAME = "drawer_username"
internal const val PREF_DRAWER_THEME = "drawer_theme"
internal const val PREF_DRAWER_LANGUAGE = "drawer_language"

private data class DrawerPalette(
    val background: Color,
    val surface: Color,
    val elevated: Color,
    val text: Color,
    val muted: Color,
    val border: Color
)

private data class FaqItem(
    val question: String,
    val answer: String
)

private val faqItems = listOf(
    FaqItem(
        question = "How does the local VPN work?",
        answer = "PureGuard creates a local VPN tunnel on your device that intercepts all DNS requests on port 53. It matches requested domains against a blocklist of 250,000+ known adult and malicious sites. All filtering happens on-device - your traffic is never routed to an external server, keeping your data completely private."
    ),
    FaqItem(
        question = "Why does the app need Accessibility permissions?",
        answer = "The Accessibility Service allows PureGuard to monitor browser URLs in real-time across all apps. When a user navigates to a blocked site, PureGuard instantly overlays a block screen. It also prevents unauthorized tampering with the app's settings or attempts to force-stop the service."
    ),
    FaqItem(
        question = "Does it drain my battery?",
        answer = "No. PureGuard's local filtering engine is highly optimized for minimal resource usage. The DNS interception layer processes requests in microseconds with near-zero CPU overhead, and the Accessibility Service runs only when a browser or system event is detected."
    ),
    FaqItem(
        question = "Can I add custom domains to block?",
        answer = "Yes. In Advanced Protection, you can add any domain to your custom blocklist. These domains are blocked in addition to the built-in filter list. You can remove them at any time."
    ),
    FaqItem(
        question = "Which DNS providers does PureGuard support?",
        answer = "PureGuard supports Cloudflare Families (1.1.1.3), CleanBrowsing Family Filter, and AdGuard Family DNS - all of which block adult content and malware. You can also enter a custom DNS over HTTPS (DoH) or DNS over TLS (DoT) endpoint."
    ),
    FaqItem(
        question = "How do I uninstall PureGuard?",
        answer = "If Device Administrator privileges are active, you must first deactivate them in Settings > Device Admin before uninstalling. If an uninstall PIN is configured, you'll be prompted to enter it. This protection mechanism ensures PureGuard cannot be bypassed accidentally."
    )
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
    val displayName = username.ifBlank { "PureGuard User" }

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
                    title = "Menu",
                    icon = Icons.Default.Menu
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DrawerMenuButton(
                        icon = Icons.Default.Palette,
                        title = "Preferences",
                        subtitle = "Theme and language",
                        selected = currentRoute == NavRoutes.Preferences.route,
                        palette = palette,
                        onClick = { onNavigate(NavRoutes.Preferences.route) }
                    )
                    DrawerMenuButton(
                        icon = Icons.Default.SupportAgent,
                        title = "Support",
                        subtitle = "Email, issue type and message",
                        selected = currentRoute == NavRoutes.Support.route,
                        palette = palette,
                        onClick = { onNavigate(NavRoutes.Support.route) }
                    )
                    DrawerMenuButton(
                        icon = Icons.Default.Info,
                        title = "FAQs",
                        subtitle = "Quick answers and guidance",
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
                title = "Preferences",
                subtitle = "Theme and language controls",
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
                    title = "Preferences",
                    icon = Icons.Default.Palette,
                    palette = palette
                ) {
                    DrawerChoiceGroup(
                        icon = Icons.Default.Palette,
                        title = "Theme",
                        selected = selectedTheme,
                        options = listOf("Dark", "Light"),
                        palette = palette,
                        onSelect = onThemeSelected
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    DrawerChoiceGroup(
                        icon = Icons.Default.Language,
                        title = "Language",
                        selected = selectedLanguage,
                        options = listOf("English", "العربية"),
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
    var issueType by rememberSaveable { mutableStateOf("Technical issue") }
    var supportMessage by rememberSaveable { mutableStateOf("") }
    val palette = remember(selectedTheme) { drawerPalette(selectedTheme) }

    Scaffold(
        topBar = {
            DrawerFeatureTopBar(
                title = "Support",
                subtitle = "Tell us what you need help with",
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
                    title = "Support",
                    icon = Icons.Default.SupportAgent,
                    palette = palette
                ) {
                    SupportForm(
                        email = supportEmail,
                        onEmailChange = { supportEmail = it },
                        issueType = issueType,
                        onIssueTypeChange = { issueType = it },
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
                title = "FAQs",
                subtitle = "Answers about PureGuard protection",
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
                    contentDescription = "Back",
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
            Text("Set username", color = PgAccentBlue, fontWeight = FontWeight.SemiBold)
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
    selected: String,
    options: List<String>,
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
                Text(selected, color = palette.muted, fontSize = 12.sp)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val selectedOption = selected == option
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
                        .clickable { onSelect(option) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
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
    issueType: String,
    onIssueTypeChange: (String) -> Unit,
    message: String,
    onMessageChange: (String) -> Unit,
    palette: DrawerPalette
) {
    var issueMenuExpanded by remember { mutableStateOf(false) }
    val issueTypes = listOf("Technical issue", "Bug Report", "Feature Request", "Other")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
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
                    Text("Issue type", color = palette.muted, fontSize = 12.sp)
                    Text(issueType, color = palette.text, fontWeight = FontWeight.SemiBold)
                }
                Text("v", color = PgAccentBlue, fontWeight = FontWeight.Bold)
            }

            DropdownMenu(
                expanded = issueMenuExpanded,
                onDismissRequest = { issueMenuExpanded = false }
            ) {
                issueTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            onIssueTypeChange(type)
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
            label = { Text("Message") },
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
                text = item.question,
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
                text = item.answer,
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
                    Text("Set username", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("This name appears in your drawer profile.", color = palette.muted, fontSize = 12.sp)
                }
            }

            OutlinedTextField(
                value = draftUsername,
                onValueChange = { draftUsername = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") },
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
                    Text("Cancel", color = palette.muted)
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
                    Text("Save", fontWeight = FontWeight.Bold)
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
