package com.pureguard.mobile.ui

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pureguard.mobile.core.datastore.Prefs
import com.pureguard.mobile.core.navigation.NavRoutes
import com.pureguard.mobile.features.blocking.domain.model.SettingsPatch
import com.pureguard.mobile.features.blocking.presentation.state.RepoResult
import com.pureguard.mobile.features.blocking.presentation.viewmodel.ProtectionUiState
import com.pureguard.mobile.features.blocking.presentation.viewmodel.ProtectionViewModel
import com.pureguard.mobile.services.local.background.ProtectionServicesStatus
import com.pureguard.mobile.services.local.Vpn.ServiceVpn
import com.pureguard.mobile.ui.features.analytics.AnalyticsScreen
import com.pureguard.mobile.ui.features.home.HomeScreen
import com.pureguard.mobile.ui.features.onboarding.OnboardingScreen
import com.pureguard.mobile.ui.features.onboarding.PermissionSetupScreen
import com.pureguard.mobile.ui.features.settings.SettingsScreen
import com.pureguard.mobile.ui.features.settings.composable.MandatoryLockPasswordScreen
import com.pureguard.mobile.ui.features.settings.composable.PasswordGateDialog
import com.pureguard.mobile.ui.theme.PgAccentBlue
import com.pureguard.mobile.ui.theme.PgMuted
import com.pureguard.mobile.ui.theme.TbColor

private const val PREF_ONBOARDING_DONE = "onboarding_complete"
private const val PREF_PERMISSIONS_SEEN = "permissions_seen"

private data class NavItem(val route: String, val label: String, val icon: @Composable () -> Unit)

private enum class AppFlow { ONBOARDING, PERMISSIONS, LOCK_SETUP, MAIN }

@Composable
fun AppRoot(
    protectionViewModel: ProtectionViewModel
) {
    val navController = rememberNavController()
    val protectionState by protectionViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHost = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var accessibilityEnabled by remember { mutableStateOf(ProtectionServicesStatus.isAccessibilityEnabled(context)) }
    var vpnNeedsConsent by remember { mutableStateOf(ProtectionServicesStatus.needsVpnConsent(context)) }
    var vpnActive by remember { mutableStateOf(ServiceVpn.isTunnelActive) }

    var currentFlow by remember {
        val onboardingDone = Prefs.getBoolean(PREF_ONBOARDING_DONE, false)
        val permissionsSeen = Prefs.getBoolean(PREF_PERMISSIONS_SEEN, false)
        mutableStateOf(
            when {
                !onboardingDone -> AppFlow.ONBOARDING
                !permissionsSeen -> AppFlow.PERMISSIONS
                else -> AppFlow.MAIN
            }
        )
    }

    val refreshStatus = rememberUpdatedState {
        accessibilityEnabled = ProtectionServicesStatus.isAccessibilityEnabled(context)
        vpnNeedsConsent = ProtectionServicesStatus.needsVpnConsent(context)
        vpnActive = ServiceVpn.isTunnelActive
    }

    val vpnConsentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            ServiceVpn.start(context)
        }
        refreshStatus.value()
        coroutineScope.launch {
            delay(600)
            refreshStatus.value()
            delay(1200)
            refreshStatus.value()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshStatus.value()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(protectionState.toastMessage) {
        protectionState.toastMessage?.let {
            snackbarHost.showSnackbar(it)
            protectionViewModel.consumeToast()
        }
    }

    LaunchedEffect(
        protectionState.loading,
        protectionState.snapshot.lockState.hasPassword,
        currentFlow
    ) {
        val onboardingDone = Prefs.getBoolean(PREF_ONBOARDING_DONE, false)
        val permissionsSeen = Prefs.getBoolean(PREF_PERMISSIONS_SEEN, false)
        if (
            !protectionState.loading &&
            onboardingDone &&
            permissionsSeen &&
            !protectionState.snapshot.lockState.hasPassword &&
            currentFlow == AppFlow.MAIN
        ) {
            currentFlow = AppFlow.LOCK_SETUP
        }
    }

    AnimatedContent(
        targetState = currentFlow,
        transitionSpec = { fadeIn(tween(450)) togetherWith fadeOut(tween(300)) },
        label = "app_flow"
    ) { flow ->
        when (flow) {
            AppFlow.ONBOARDING -> {
                OnboardingScreen(
                    onFinished = {
                        Prefs.putBoolean(PREF_ONBOARDING_DONE, true)
                        currentFlow = AppFlow.PERMISSIONS
                    }
                )
            }

            AppFlow.PERMISSIONS -> {
                PermissionSetupScreen(
                    accessibilityEnabled = accessibilityEnabled,
                    vpnActive = vpnActive,
                    vpnNeedsConsent = vpnNeedsConsent,
                    onEnableAccessibility = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    },
                    onEnableVpn = {
                        val prepareIntent = VpnService.prepare(context)
                        if (prepareIntent != null) {
                            vpnConsentLauncher.launch(prepareIntent)
                        } else {
                            ServiceVpn.start(context)
                            refreshStatus.value()
                        }
                    },
                    onContinue = {
                        Prefs.putBoolean(PREF_PERMISSIONS_SEEN, true)
                        currentFlow = if (protectionState.snapshot.lockState.hasPassword) {
                            AppFlow.MAIN
                        } else {
                            AppFlow.LOCK_SETUP
                        }
                    }
                )
            }

            AppFlow.LOCK_SETUP -> {
                MandatoryLockPasswordScreen(
                    onCreatePassword = { password, onDone ->
                        protectionViewModel.setPassword("", password) { result ->
                            val ok = result is RepoResult.Success
                            if (ok) currentFlow = AppFlow.MAIN
                            onDone(ok)
                        }
                    }
                )
            }

            AppFlow.MAIN -> {
                MainAppScaffold(
                    protectionViewModel = protectionViewModel,
                    protectionState = protectionState,
                    navController = navController,
                    snackbarHost = snackbarHost,
                    accessibilityEnabled = accessibilityEnabled,
                    vpnActive = vpnActive,
                    vpnNeedsConsent = vpnNeedsConsent,
                    onOpenAccessibilitySettings = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    },
                    onToggleVpn = {
                        if (ServiceVpn.isTunnelActive) {
                            ServiceVpn.stop(context)
                        } else {
                            val prepareIntent = VpnService.prepare(context)
                            if (prepareIntent != null) {
                                vpnConsentLauncher.launch(prepareIntent)
                            } else {
                                ServiceVpn.start(context)
                            }
                        }
                        refreshStatus.value()
                    }
                )
            }
        }
    }
}

@Composable
private fun MainAppScaffold(
    protectionViewModel: ProtectionViewModel,
    protectionState: ProtectionUiState,
    navController: androidx.navigation.NavHostController,
    snackbarHost: SnackbarHostState,
    accessibilityEnabled: Boolean,
    vpnActive: Boolean,
    vpnNeedsConsent: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onToggleVpn: () -> Unit
) {
    var pendingProtectedPatch by remember { mutableStateOf<SettingsPatch?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()
    var drawerTheme by rememberSaveable {
        mutableStateOf(Prefs.getString(PREF_DRAWER_THEME, "Dark").orEmpty().ifBlank { "Dark" })
    }
    var drawerLanguage by rememberSaveable {
        mutableStateOf(Prefs.getString(PREF_DRAWER_LANGUAGE, "English").orEmpty().ifBlank { "English" })
    }
    val navItems = listOf(
        NavItem("home", "Home", { Icon(Icons.Default.Home, contentDescription = "Home") }),
        NavItem("analytics", "Analytics", { Icon(Icons.Default.Analytics, contentDescription = "Analytics") }),
        NavItem("settings", "Settings", { Icon(Icons.Default.Settings, contentDescription = "Settings") })
    )
    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                selectedTheme = drawerTheme,
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                    drawerScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHost) },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TbColor.copy(alpha = 0.92f))
                        .border(1.dp, Color.White.copy(0.06f))
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        navItems.forEach { item ->
                            NavigationBarItem(
                                selected = currentRoute == item.route,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        restoreState = true
                                        launchSingleTop = true
                                    }
                                },
                                icon = item.icon,
                                label = { Text(item.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PgAccentBlue,
                                    selectedTextColor = PgAccentBlue,
                                    unselectedIconColor = PgMuted,
                                    unselectedTextColor = PgMuted,
                                    indicatorColor = PgAccentBlue.copy(alpha = 0.14f)
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = NavRoutes.Home.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(NavRoutes.Home.route) {
                    HomeScreen(
                        state = protectionState,
                        accessibilityEnabled = accessibilityEnabled,
                        vpnActive = vpnActive,
                        vpnNeedsConsent = vpnNeedsConsent,
                        onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                        onToggleVpn = onToggleVpn,
                        onOpenDrawer = { drawerScope.launch { drawerState.open() } },
                        onPatch = { patch: SettingsPatch -> pendingProtectedPatch = patch },
                        onResetStats = protectionViewModel::resetStats
                    )
                }
                composable(NavRoutes.Settings.route) {
                    SettingsScreen(
                        state = protectionState,
                        onOpenDrawer = { drawerScope.launch { drawerState.open() } },
                        onSavePatch = { patch, password, onDone ->
                            protectionViewModel.updateSettings(patch, password) { result ->
                                onDone(result is RepoResult.Success)
                            }
                        },
                        onSetPassword = { oldPassword, newPassword, onDone ->
                            protectionViewModel.setPassword(oldPassword, newPassword) { result ->
                                onDone(result is RepoResult.Success)
                            }
                        }
                    )
                }
                composable(NavRoutes.Analytics.route) {
                    AnalyticsScreen(
                        state = protectionState,
                        accessibilityEnabled = accessibilityEnabled,
                        vpnNeedsConsent = vpnNeedsConsent,
                        vpnActive = vpnActive,
                        onOpenDrawer = { drawerScope.launch { drawerState.open() } },
                        onResetStats = protectionViewModel::resetStats
                    )
                }
                composable(NavRoutes.Preferences.route) {
                    PreferencesDrawerPage(
                        selectedTheme = drawerTheme,
                        selectedLanguage = drawerLanguage,
                        onThemeSelected = {
                            drawerTheme = it
                            Prefs.putString(PREF_DRAWER_THEME, it)
                        },
                        onLanguageSelected = {
                            drawerLanguage = it
                            Prefs.putString(PREF_DRAWER_LANGUAGE, it)
                        },
                        onBack = {
                            if (!navController.navigateUp()) {
                                navController.navigate(NavRoutes.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    restoreState = true
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
                composable(NavRoutes.Support.route) {
                    SupportDrawerPage(
                        selectedTheme = drawerTheme,
                        onBack = {
                            if (!navController.navigateUp()) {
                                navController.navigate(NavRoutes.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    restoreState = true
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
                composable(NavRoutes.Faqs.route) {
                    FaqsDrawerPage(
                        selectedTheme = drawerTheme,
                        onBack = {
                            if (!navController.navigateUp()) {
                                navController.navigate(NavRoutes.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    restoreState = true
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }

            pendingProtectedPatch?.let { patch ->
                PasswordGateDialog(
                    title = "Confirm setting change",
                    message = "Enter your lock password to apply this change.",
                    onDismiss = { pendingProtectedPatch = null },
                    onConfirm = { password ->
                        protectionViewModel.updateSettings(patch, password) { result ->
                            if (result is RepoResult.Success) pendingProtectedPatch = null
                        }
                    }
                )
            }
        }
    }
}
