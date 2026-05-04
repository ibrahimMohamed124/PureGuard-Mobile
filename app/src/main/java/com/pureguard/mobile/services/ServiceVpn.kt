package com.pureguard.mobile.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.PureGuard.R

class ServiceVpn : VpnService() {

    private val blockedPackages = linkedSetOf<String>()
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_BLOCK_PACKAGE -> {
                val pkg = intent.getStringExtra(EXTRA_PACKAGE_NAME).orEmpty().trim()
                if (pkg.isNotBlank()) blockedPackages += pkg
            }

            ACTION_UNBLOCK_PACKAGE -> {
                val pkg = intent.getStringExtra(EXTRA_PACKAGE_NAME).orEmpty().trim()
                if (pkg.isNotBlank()) blockedPackages.remove(pkg)
            }

            ACTION_STOP -> {
                blockedPackages.clear()
                tearDownTunnel()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        syncTunnel()
        return START_STICKY
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
        stopSelf()
        super.onRevoke()
    }

    override fun onBind(intent: Intent?) = super.onBind(intent)

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

        if (VpnService.prepare(this) != null) {
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
            .setSmallIcon(R.drawable.ic_pureguard)
            .setContentTitle("PureGuard VPN shield")
            .setContentText("Blocking risky browser session")
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateForegroundText() {
        ensureChannel()
        val text = if (blockedPackages.size == 1) {
            "1 browser is currently blocked"
        } else {
            "${blockedPackages.size} browsers are currently blocked"
        }
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_pureguard)
            .setContentTitle("PureGuard VPN shield")
            .setContentText(text)
            .setOngoing(true)
            .build()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(NOTIFICATION_CHANNEL) != null) return
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL,
            "PureGuard VPN",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
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
            val intent = Intent(context, ServiceVpn::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
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
            val intent = Intent(context, ServiceVpn::class.java).apply {
                action = ACTION_UNBLOCK_PACKAGE
                putExtra(EXTRA_PACKAGE_NAME, packageName)
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
