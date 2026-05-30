package com.pureguard.mobile.services.local.background

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.pureguard.mobile.R
import com.pureguard.mobile.activities.AccessibilitySettingsActivity
import com.pureguard.mobile.activities.VpnPermissionActivity

object ProtectionAlertNotifier {

    private const val CHANNEL_ID = "pureguard_protection_alerts"
    private const val ACCESSIBILITY_DISABLED_NOTIFICATION_ID = 9201
    private const val VPN_DISABLED_NOTIFICATION_ID = 9202

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showAccessibilityDisabled(context: Context) {
        if (!canPostNotifications(context)) return
        ensureChannel(context)

        val intent = Intent(context, AccessibilitySettingsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            ACCESSIBILITY_DISABLED_NOTIFICATION_ID,
            intent,
            pendingIntentFlags()
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pureguard)
            .setContentTitle("PureGuard protection disabled")
            .setContentText("Accessibility monitor was turned off. Tap to re-enable protection.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Accessibility monitor was turned off, so PureGuard cannot watch browser URLs. Tap to open Accessibility settings and turn it back on.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(
            ACCESSIBILITY_DISABLED_NOTIFICATION_ID,
            notification
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showVpnDisabled(context: Context) {
        if (!canPostNotifications(context)) return
        ensureChannel(context)

        val intent = Intent(context, VpnPermissionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            VPN_DISABLED_NOTIFICATION_ID,
            intent,
            pendingIntentFlags()
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pureguard)
            .setContentTitle("PureGuard VPN disabled")
            .setContentText("VPN shield was turned off. Tap to enable it again.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("VPN shield was turned off, so network-level protection is disabled. Tap to show the Android VPN permission prompt and enable PureGuard again.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(
            VPN_DISABLED_NOTIFICATION_ID,
            notification
        )
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "PureGuard protection alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when VPN or Accessibility protection is turned off."
        }
        manager.createNotificationChannel(channel)
    }

    private fun canPostNotifications(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun pendingIntentFlags(): Int {
        return PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    }
}
