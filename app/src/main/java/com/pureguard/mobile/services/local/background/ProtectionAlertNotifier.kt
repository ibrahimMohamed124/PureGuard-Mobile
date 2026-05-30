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
import com.pureguard.mobile.core.localization.AppLanguage

object ProtectionAlertNotifier {

    private const val CHANNEL_ID = "pureguard_protection_alerts"
    private const val ACCESSIBILITY_DISABLED_NOTIFICATION_ID = 9201
    private const val VPN_DISABLED_NOTIFICATION_ID = 9202

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showAccessibilityDisabled(context: Context) {
        if (!canPostNotifications(context)) return
        val localizedContext = AppLanguage.wrap(context)
        ensureChannel(localizedContext)

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
            .setSmallIcon(R.drawable.ic_notification_pureguard)
            .setContentTitle(localizedContext.getString(R.string.notification_accessibility_title))
            .setContentText(localizedContext.getString(R.string.notification_accessibility_text))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(localizedContext.getString(R.string.notification_accessibility_big))
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
        val localizedContext = AppLanguage.wrap(context)
        ensureChannel(localizedContext)

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
            .setSmallIcon(R.drawable.ic_notification_pureguard)
            .setContentTitle(localizedContext.getString(R.string.notification_vpn_title))
            .setContentText(localizedContext.getString(R.string.notification_vpn_text))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(localizedContext.getString(R.string.notification_vpn_big))
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
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) {
            existing.setName(context.getString(R.string.notification_protection_channel))
            existing.description = context.getString(R.string.notification_protection_channel_description)
            manager.createNotificationChannel(existing)
            return
        }
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_protection_channel),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_protection_channel_description)
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
