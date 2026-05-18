package com.pureguard.mobile.services.local.background

import java.net.URI
import java.util.Locale

internal object BrowserBlockBridge {

    private const val UI_SUPPRESS_MS = 8000L
    private const val REQUEST_TTL_MS = 10_000L

    private data class CloseTabRequest(
        val packageName: String,
        val blockedUrl: String,
        val blockedHost: String,
        val createdAt: Long
    )

    @Volatile
    private var pendingCloseTabRequest: CloseTabRequest? = null
    private val suppressedUiBySignature = mutableMapOf<String, Long>()

    @Synchronized
    fun requestCloseBlockedTab(packageName: String, blockedUrl: String) {
        val pkg = packageName.trim()
        if (pkg.isBlank()) return
        val url = normalizeUrl(blockedUrl) ?: return
        val host = hostOf(url).orEmpty()
        val now = System.currentTimeMillis()
        pendingCloseTabRequest = CloseTabRequest(
            packageName = pkg,
            blockedUrl = url,
            blockedHost = host,
            createdAt = now
        )
        suppressUi(pkg, url, now + UI_SUPPRESS_MS)
    }

    @Synchronized
    fun consumeCloseTabRequestFor(packageName: String, currentUrl: String): Boolean {
        val now = System.currentTimeMillis()
        val request = pendingCloseTabRequest ?: return false
        if (now - request.createdAt > REQUEST_TTL_MS) {
            pendingCloseTabRequest = null
            return false
        }
        if (request.packageName != packageName) return false
        val normalizedCurrent = normalizeUrl(currentUrl) ?: return false
        val sameUrl = normalizedCurrent.equals(request.blockedUrl, ignoreCase = true)
        val sameHost = hostOf(normalizedCurrent) == request.blockedHost
        if (!sameUrl && !sameHost) return false
        pendingCloseTabRequest = null
        return true
    }

    @Synchronized
    fun clearCloseTabRequestFor(packageName: String) {
        val request = pendingCloseTabRequest ?: return
        if (request.packageName == packageName) {
            pendingCloseTabRequest = null
        }
    }

    @Synchronized
    fun shouldSuppressBlockUi(packageName: String, blockedUrl: String): Boolean {
        val now = System.currentTimeMillis()
        clearExpired(now)
        val key = signature(packageName, blockedUrl)
        val until = suppressedUiBySignature[key] ?: return false
        return now <= until
    }

    private fun suppressUi(packageName: String, blockedUrl: String, until: Long) {
        clearExpired(System.currentTimeMillis())
        suppressedUiBySignature[signature(packageName, blockedUrl)] = until
    }

    private fun clearExpired(now: Long) {
        suppressedUiBySignature.entries.removeIf { now > it.value }
    }

    private fun signature(packageName: String, blockedUrl: String): String {
        return packageName.trim() + "|" + blockedUrl.trim().lowercase()
    }

    private fun normalizeUrl(url: String): String? {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return null
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    private fun hostOf(url: String): String? {
        return runCatching { URI(url).host?.lowercase(Locale.US) }.getOrNull()
    }
}