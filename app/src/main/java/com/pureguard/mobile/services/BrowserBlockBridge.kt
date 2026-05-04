package com.pureguard.mobile.services

internal object BrowserBlockBridge {

    private const val UI_SUPPRESS_MS = 8000L
    private const val REQUEST_TTL_MS = 15000L

    private data class CloseTabRequest(
        val packageName: String,
        val blockedUrl: String,
        val createdAt: Long
    )

    @Volatile
    private var pendingCloseTabRequest: CloseTabRequest? = null
    private val suppressedUiBySignature = mutableMapOf<String, Long>()

    fun requestCloseBlockedTab(packageName: String, blockedUrl: String) {
        val pkg = packageName.trim()
        if (pkg.isBlank()) return
        val url = blockedUrl.trim()
        val now = System.currentTimeMillis()
        pendingCloseTabRequest = CloseTabRequest(
            packageName = pkg,
            blockedUrl = url,
            createdAt = now
        )
        suppressUi(pkg, url, now + UI_SUPPRESS_MS)
    }

    fun consumeCloseTabRequestFor(packageName: String): Boolean {
        val now = System.currentTimeMillis()
        val request = pendingCloseTabRequest ?: return false
        if (now - request.createdAt > REQUEST_TTL_MS) {
            pendingCloseTabRequest = null
            return false
        }
        if (request.packageName != packageName) return false
        pendingCloseTabRequest = null
        return true
    }

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
}
