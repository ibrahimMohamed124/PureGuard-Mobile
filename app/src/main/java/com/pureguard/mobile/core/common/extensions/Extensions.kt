package com.pureguard.mobile.core.common

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.VpnService
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import java.util.Locale

/* -------------------------------------------------------------------------- */
/*                                  PERMISSION                                */
/* -------------------------------------------------------------------------- */

fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED

fun Context.isAccessibilityEnabled(
    service: Class<out AccessibilityService>
): Boolean {
    val expected = "$packageName/${service.name}"

    val enabledServices = Settings.Secure.getString(
        contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    return enabledServices.lowercase(Locale.getDefault())
        .contains(expected.lowercase(Locale.getDefault()))
}

fun Context.hasUsageStatsPermission(): Boolean {
    val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
    } else {
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
    }

    return mode == AppOpsManager.MODE_ALLOWED
}

fun Context.canDrawOverlays(): Boolean =
    Settings.canDrawOverlays(this)

fun Context.isIgnoringBatteryOptimizations(): Boolean {
    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

    return powerManager.isIgnoringBatteryOptimizations(packageName)
}

fun Context.isVpnPrepared(): Boolean =
    VpnService.prepare(this) == null

/* -------------------------------------------------------------------------- */
/*                                   SETTINGS                                 */
/* -------------------------------------------------------------------------- */

fun Context.openAccessibilitySettings() {
    startActivity(
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

fun Context.openOverlaySettings() {
    startActivity(
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

fun Context.openUsageAccessSettings() {
    startActivity(
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

fun Context.openBatteryOptimizationSettings() {
    startActivity(
        Intent(
            Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

fun Context.openVpnSettings() {
    startActivity(
        Intent(Settings.ACTION_VPN_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

/* -------------------------------------------------------------------------- */
/*                                     APPS                                   */
/* -------------------------------------------------------------------------- */

fun Context.isPackageInstalled(packageName: String): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (_: Exception) {
        false
    }
}

@SuppressLint("QueryPermissionsNeeded")
fun Context.getInstalledApps(): List<String> {
    return packageManager.getInstalledApplications(0)
        .map { it.packageName }
}

fun Context.getAppName(packageName: String): String {
    return try {
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        packageManager.getApplicationLabel(appInfo).toString()
    } catch (_: Exception) {
        packageName
    }
}

/* -------------------------------------------------------------------------- */
/*                                   NETWORK                                  */
/* -------------------------------------------------------------------------- */

@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
fun Context.isInternetAvailable(): Boolean {
    val connectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

    return connectivityManager.activeNetwork != null
}

/* -------------------------------------------------------------------------- */
/*                                     TOAST                                  */
/* -------------------------------------------------------------------------- */

fun Context.showToast(message: String) {
    Toast.makeText(
        this,
        message,
        Toast.LENGTH_SHORT
    ).show()
}

/* -------------------------------------------------------------------------- */
/*                                     FLOW                                   */
/* -------------------------------------------------------------------------- */

@Composable
fun <T> Flow<T>.observeAsState(initial: T): State<T> {
    val lifecycleOwner = LocalLifecycleOwner.current

    return produceState(
        initialValue = initial,
        key1 = lifecycleOwner
    ) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(
            Lifecycle.State.STARTED
        ) {
            this@observeAsState.collect {
                value = it
            }
        }
    }
}