package com.pureguard.mobile.ui

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.provider.Settings
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
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
import com.pureguard.mobile.domain.model.SettingsPatch
import com.pureguard.mobile.services.ProtectionServicesStatus
import com.pureguard.mobile.services.ServiceVpn
import com.pureguard.mobile.ui.home.HomeScreen
import com.pureguard.mobile.ui.settings.SettingsScreen

private data class NavItem(val route: String, val label: String, val icon: @Composable () -> Unit)

@Composable
fun AppRoot(
    protectionViewModel: ProtectionViewModel
) {
    val navController = rememberNavController()
    val protectionState by protectionViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHost = remember { SnackbarHostState() }
    var accessibilityEnabled by remember { mutableStateOf(ProtectionServicesStatus.isAccessibilityEnabled(context)) }
    var vpnNeedsConsent by remember { mutableStateOf(ProtectionServicesStatus.needsVpnConsent(context)) }
    var vpnActive by remember { mutableStateOf(ServiceVpn.isTunnelActive) }

    val refreshStatus = rememberUpdatedState({
        accessibilityEnabled = ProtectionServicesStatus.isAccessibilityEnabled(context)
        vpnNeedsConsent = ProtectionServicesStatus.needsVpnConsent(context)
        vpnActive = ServiceVpn.isTunnelActive
    })

    val vpnConsentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            ServiceVpn.start(context)
        }
        refreshStatus.value()
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

    val navItems = listOf(
        NavItem("home", "Home", { Icon(Icons.Default.Home, contentDescription = "Home") }),
        NavItem("settings", "Settings", { Icon(Icons.Default.Settings, contentDescription = "Settings") })
    )

    LaunchedEffect(protectionState.toastMessage) {
        protectionState.toastMessage?.let {
            snackbarHost.showSnackbar(it)
            protectionViewModel.consumeToast()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        bottomBar = {
            val entry by navController.currentBackStackEntryAsState()
            val current = entry?.destination?.route
            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = current == item.route,
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
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                HomeScreen(
                    state = protectionState,
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
                    },
                    onPatch = { patch: SettingsPatch -> protectionViewModel.updateSettings(patch) },
                    onResetStats = protectionViewModel::resetStats
                )
            }
            composable("settings") {
                SettingsScreen(
                    state = protectionState,
                    onSavePatch = { patch, password -> protectionViewModel.updateSettings(patch, password) },
                    onVerifyPassword = { password, cb -> protectionViewModel.verifyPassword(password, cb) },
                    onSetPassword = { oldPassword, newPassword ->
                        protectionViewModel.setPassword(oldPassword, newPassword)
                    },
                    onRemovePassword = { password ->
                        protectionViewModel.removePassword(password)
                    }
                )
            }
        }
    }
}
