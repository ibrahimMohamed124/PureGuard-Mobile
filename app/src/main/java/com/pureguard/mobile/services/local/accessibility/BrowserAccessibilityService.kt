package com.pureguard.mobile.services.local

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.pureguard.mobile.PureGuardApp
import com.pureguard.mobile.domain.model.local.DecisionType
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.net.URI
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

@SuppressLint("AccessibilityPolicy")
class BrowserAccessibilityService : AccessibilityService() {

    private val crashHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Accessibility coroutine failure", throwable)
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + crashHandler)
    private val lastRunMs = AtomicLong(0L)

    private var lastSignature: String = ""
    private var lastBlockedPackage: String? = null
    private var lastBlockedUrl: String? = null
    private var lastBlockUiAt: Long = 0L

    override fun onServiceConnected() {
        runCatching {
            val info = serviceInfo ?: AccessibilityServiceInfo()
            info.apply {
                eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOWS_CHANGED
                feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
                notificationTimeout = 120
                flags = flags or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            }
            serviceInfo = info
        }.onFailure {
            Log.e(TAG, "Failed to configure accessibility service", it)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            if (event == null) return

            val packageName = event.packageName?.toString().orEmpty()
            if (packageName.isBlank()) return
            if (packageName == applicationContext.packageName) return
            if (!BrowserPackageCatalog.looksLikeBrowserPackage(packageName)) {
                releaseIfNeeded(packageName)
                return
            }

            val now = System.currentTimeMillis()
            if (now - lastRunMs.get() < 180L) return
            lastRunMs.set(now)

            val root = rootInActiveWindow ?: return
            val extracted = extractUrl(root, packageName) ?: return
            if (extracted.fromKnownAddressBar && extracted.isUserEditingAddressBar) return
            if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED && extracted.fromKnownAddressBar) return

            val normalizedUrl = normalizeUrl(extracted.url) ?: return
            if (handleCloseTabRequest(packageName, normalizedUrl)) return
            val signature = "$packageName|$normalizedUrl"
            if (signature == lastSignature && now - lastBlockUiAt < 1_000L) return
            lastSignature = signature

            scope.launch {
                runCatching {
                    evaluate(packageName, normalizedUrl)
                }.onFailure {
                    Log.e(TAG, "Evaluation failure for $normalizedUrl", it)
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "onAccessibilityEvent failure", t)
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private suspend fun evaluate(packageName: String, url: String) {
        val app = application as? PureGuardApp ?: return
        val repository = app.container.repository
        val coordinator = app.container.protectionCoordinator
        val settings = repository.getSettings()
        if (!settings.enabled) {
            ServiceVpn.unblockBrowserPackage(this, packageName)
            return
        }

        val host = hostOf(url).orEmpty()
        val allowOnce = repository.isHostAllowedOnce(host)
        val evaluation = coordinator.evaluateNavigation(
            url = url,
            settings = settings,
            allowOnce = allowOnce
        )
        val decision = evaluation.decision
        if (decision.type == DecisionType.BLOCK) {
            repository.bumpBlocked()
            ServiceVpn.blockBrowserPackage(this, packageName)
            scope.launch(Dispatchers.Main) {
                maybeShowBlockedUi(
                    packageName = packageName,
                    blockedUrl = url,
                    reason = decision.reason
                )
            }
            lastBlockedPackage = packageName
            lastBlockedUrl = url
        } else {
            repository.bumpScanned()
            ServiceVpn.unblockBrowserPackage(this, packageName)
            if (lastBlockedPackage == packageName && lastBlockedUrl == url) {
                lastBlockedPackage = null
                lastBlockedUrl = null
            }
        }
    }

    private fun maybeShowBlockedUi(packageName: String, blockedUrl: String, reason: String) {
        if (BrowserBlockBridge.shouldSuppressBlockUi(packageName, blockedUrl)) return
        if (BlockedContentActivity.isVisible) return
        val now = System.currentTimeMillis()
        val isSameBlock = lastBlockedPackage == packageName && lastBlockedUrl == blockedUrl
        if (isSameBlock && (now - lastBlockUiAt) < 3500L) return
        lastBlockUiAt = now
        runCatching {
            BlockedContentActivity.launch(
            context = this,
            blockedUrl = blockedUrl,
            reason = reason,
            browserPackage = packageName
            )
        }.onFailure {
            Log.e(TAG, "Failed to launch block page", it)
        }
    }

    private fun releaseIfNeeded(activePackage: String) {
        val blockedPkg = lastBlockedPackage ?: return
        if (blockedPkg == activePackage) return
        BrowserBlockBridge.clearCloseTabRequestFor(blockedPkg)
        ServiceVpn.unblockBrowserPackage(this, blockedPkg)
        lastBlockedPackage = null
        lastBlockedUrl = null
        lastSignature = ""
    }

    private fun extractUrl(root: AccessibilityNodeInfo, packageName: String): ExtractedUrl? {
        val knownAddressBars = BrowserPackageCatalog.knownAddressBars(packageName)
        if (knownAddressBars.isNotEmpty()) {
            return knownAddressBars
                .asSequence()
                .mapNotNull { id -> root.findAccessibilityNodeInfosByViewId(id).firstOrNull() }
                .mapNotNull { node ->
                    val raw = (node.text?.toString() ?: node.contentDescription?.toString()).orEmpty().trim()
                    if (!looksLikeUrl(raw)) return@mapNotNull null
                    ExtractedUrl(
                        url = raw,
                        fromKnownAddressBar = true,
                        isUserEditingAddressBar = node.isFocused || node.isAccessibilityFocused
                    )
                }
                .firstOrNull()
        }

        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var visited = 0
        while (queue.isNotEmpty() && visited < 220) {
            val node = queue.removeFirst()
            visited += 1
            val text = node.text?.toString()?.trim().orEmpty()
            if (looksLikeUrl(text)) {
                return ExtractedUrl(
                    url = text,
                    fromKnownAddressBar = false,
                    isUserEditingAddressBar = false
                )
            }
            val hint = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                node.hintText?.toString()?.trim().orEmpty()
            } else {
                ""
            }
            if (looksLikeUrl(hint)) {
                return ExtractedUrl(
                    url = hint,
                    fromKnownAddressBar = false,
                    isUserEditingAddressBar = false
                )
            }
            val desc = node.contentDescription?.toString()?.trim().orEmpty()
            if (looksLikeUrl(desc)) {
                return ExtractedUrl(
                    url = desc,
                    fromKnownAddressBar = false,
                    isUserEditingAddressBar = false
                )
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.addLast(it) }
            }
        }
        return null
    }

    private fun looksLikeUrl(text: String): Boolean {
        if (text.isBlank()) return false
        val candidate = text.lowercase(Locale.US)
        if (candidate.contains(" ")) return false
        if (candidate.startsWith("about:") || candidate.startsWith("chrome://")) return false
        if (candidate.startsWith("http://") || candidate.startsWith("https://")) return true
        return URL_PATTERN.matches(candidate)
    }

    private fun normalizeUrl(raw: String): String? {
        val value = raw.trim()
        if (value.isBlank()) return null
        val candidate = if (value.startsWith("http://") || value.startsWith("https://")) {
            value
        } else {
            "https://$value"
        }
        val parsed = runCatching { URI(candidate) }.getOrNull() ?: return null
        if (parsed.host.isNullOrBlank()) return null
        return candidate
    }

    private fun hostOf(url: String): String? {
        return runCatching { URI(url).host?.lowercase(Locale.US) }.getOrNull()
    }

    private fun handleCloseTabRequest(packageName: String, currentUrl: String): Boolean {
        if (!BrowserBlockBridge.consumeCloseTabRequestFor(packageName, currentUrl)) return false
        scope.launch(Dispatchers.Main) {
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
        ServiceVpn.unblockBrowserPackage(this, packageName)
        lastBlockedPackage = null
        lastBlockedUrl = null
        lastSignature = ""
        return true
    }

    companion object {
        private const val TAG = "BrowserA11yService"
        private val URL_PATTERN = Regex("""^([a-z0-9-]+\.)+[a-z]{2,}([/:?#].*)?$""")
    }
}

private data class ExtractedUrl(
    val url: String,
    val fromKnownAddressBar: Boolean,
    val isUserEditingAddressBar: Boolean
)
