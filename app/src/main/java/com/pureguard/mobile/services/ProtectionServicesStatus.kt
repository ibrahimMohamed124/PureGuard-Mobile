package com.pureguard.mobile.services

import android.content.ComponentName
import android.content.Context
import android.net.VpnService
import android.provider.Settings

object ProtectionServicesStatus {

    fun isAccessibilityEnabled(context: Context): Boolean {
        val expected = ComponentName(
            context,
            BrowserAccessibilityService::class.java
        )
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        if (enabled.isBlank()) return false
        val entries = enabled.split(':')
        return entries.any { entry ->
            entry.equals(expected.flattenToString(), ignoreCase = true) ||
                entry.equals(expected.flattenToShortString(), ignoreCase = true)
        }
    }

    fun needsVpnConsent(context: Context): Boolean {
        return VpnService.prepare(context) != null
    }
}
