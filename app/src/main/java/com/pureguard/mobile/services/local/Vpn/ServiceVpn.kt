package com.pureguard.mobile.services.local.Vpn

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.pureguard.mobile.R
import com.pureguard.mobile.core.localization.AppLanguage
import com.pureguard.mobile.services.local.background.ProtectionAlertNotifier

@SuppressLint("VpnServicePolicy")
class ServiceVpn : VpnService() {

    private val blockedPackages = linkedSetOf<String>()
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return try {
            if (intent == null) {
                syncTunnel()
                return START_STICKY
            }

            when (intent.action) {
                ACTION_BLOCK_PACKAGE -> {
                    val pkg = intent.getStringExtra(EXTRA_PACKAGE_NAME).orEmpty().trim()
                    if (pkg.isNotBlank()) blockedPackages += pkg
                }

                ACTION_UNBLOCK_PACKAGE -> {
                    val pkg = intent.getStringExtra(EXTRA_PACKAGE_NAME).orEmpty().trim()
                    if (pkg.isNotBlank()) blockedPackages.remove(pkg)
                }

                ACTION_START -> Unit

                ACTION_STOP -> {
                    blockedPackages.clear()
                    tearDownTunnel()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    isTunnelActive = false
                    ProtectionAlertNotifier.showVpnDisabled(this)
                    stopSelf()
                    return START_NOT_STICKY
                }
            }

            syncTunnel()
            START_STICKY
        } catch (t: Throwable) {
            Log.e(TAG, "ServiceVpn onStartCommand failed", t)
            blockedPackages.clear()
            tearDownTunnel()
            stopForeground(STOP_FOREGROUND_REMOVE)
            isTunnelActive = false
            START_NOT_STICKY
        }
    }

    override fun onDestroy() {
        tearDownTunnel()
        isTunnelActive = false
        super.onDestroy()
    }

    override fun onRevoke() {
        blockedPackages.clear()
        tearDownTunnel()
        isTunnelActive = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        ProtectionAlertNotifier.showVpnDisabled(this)
        stopSelf()
        super.onRevoke()
    }

    override fun onBind(intent: Intent?) = super.onBind(intent)

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguage.wrap(newBase))
    }

    private fun syncTunnel() {
        if (blockedPackages.isEmpty()) {
            tearDownTunnel()
            stopForeground(STOP_FOREGROUND_REMOVE)
            isTunnelActive = false
            return
        }

        startAsForeground()
        establishBlockingTunnel()
    }

    private fun establishBlockingTunnel() {
        tearDownTunnel()

        if (prepare(this) != null) {
            isTunnelActive = false
            return
        }

        val builder = Builder()
            .setSession(VPN_SESSION_NAME)
            .setMtu(1500)
            .addAddress("10.9.0.1", 32)
            .addRoute("0.0.0.0", 0)
            .addRoute("::", 0)

        blockedPackages.forEach { packageName ->
            runCatching { builder.addAllowedApplication(packageName) }
        }

        vpnInterface = runCatching { builder.establish() }.getOrNull()
        isTunnelActive = vpnInterface != null
        if (!isTunnelActive) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            updateForegroundText()
        }
    }

    private fun tearDownTunnel() {
        runCatching { vpnInterface?.close() }
        vpnInterface = null
    }

    private fun startAsForeground() {
        ensureChannel()
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification_pureguard)
            .setContentTitle(getString(R.string.notification_vpn_foreground_title))
            .setContentText(getString(R.string.notification_vpn_foreground_start))
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateForegroundText() {
        ensureChannel()
        val text = if (blockedPackages.size == 1) {
            getString(R.string.notification_vpn_one_browser_blocked)
        } else {
            getString(R.string.notification_vpn_many_browsers_blocked, blockedPackages.size)
        }
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification_pureguard)
            .setContentTitle(getString(R.string.notification_vpn_foreground_title))
            .setContentText(text)
            .setOngoing(true)
            .build()
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(NOTIFICATION_CHANNEL)
        if (existing != null) {
            existing.setName(getString(R.string.notification_vpn_channel))
            manager.createNotificationChannel(existing)
            return
        }
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL,
            getString(R.string.notification_vpn_channel),
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "ServiceVpn"
        private const val ACTION_START = "com.pureguard.mobile.vpn.action.START"
        private const val ACTION_BLOCK_PACKAGE = "com.pureguard.mobile.vpn.action.BLOCK_PACKAGE"
        private const val ACTION_UNBLOCK_PACKAGE = "com.pureguard.mobile.vpn.action.UNBLOCK_PACKAGE"
        private const val ACTION_STOP = "com.pureguard.mobile.vpn.action.STOP"
        private const val EXTRA_PACKAGE_NAME = "package_name"
        private const val NOTIFICATION_CHANNEL = "pureguard_vpn_channel"
        private const val NOTIFICATION_ID = 7341
        private const val VPN_SESSION_NAME = "PureGuardBrowserShield"

        @Volatile
        var isTunnelActive: Boolean = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, ServiceVpn::class.java).apply {
                action = ACTION_START
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            if (!isTunnelActive) return
            val intent = Intent(context, ServiceVpn::class.java).apply {
                action = ACTION_STOP
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun blockBrowserPackage(context: Context, packageName: String) {
            val intent = Intent(context, ServiceVpn::class.java).apply {
                action = ACTION_BLOCK_PACKAGE
                putExtra(EXTRA_PACKAGE_NAME, packageName)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun unblockBrowserPackage(context: Context, packageName: String) {
            if (!isTunnelActive) return
            val intent = Intent(context, ServiceVpn::class.java).apply {
                action = ACTION_UNBLOCK_PACKAGE
                putExtra(EXTRA_PACKAGE_NAME, packageName)
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
