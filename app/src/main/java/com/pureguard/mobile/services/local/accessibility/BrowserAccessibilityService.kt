package com.pureguard.mobile.services.local.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.pureguard.mobile.PureGuardApp
import com.pureguard.mobile.features.blocking.domain.model.DecisionType
import com.pureguard.mobile.features.blocking.domain.model.PageSignals
import com.pureguard.mobile.features.blocking.presentation.ui.BlockedContentActivity
import com.pureguard.mobile.services.local.background.BrowserBlockBridge
import com.pureguard.mobile.services.local.BrowserPackageCatalog
import com.pureguard.mobile.services.local.Vpn.ServiceVpn
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
    private var lastSafeSearchRewriteSignature: String = ""
    private var lastSafeSearchRewriteAt: Long = 0L
    private var lastPrivateModeBlockedPackage: String? = null
    private var lastPrivateModeBlockAt: Long = 0L

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
            val inPrivateMode = looksLikePrivateMode(root)
            val extracted = extractUrl(root, packageName)
            if (extracted == null) {
                if (inPrivateMode) {
                    scope.launch {
                        runCatching {
                            maybeBlockPrivateMode(packageName)
                        }.onFailure {
                            Log.e(TAG, "Private mode evaluation failure", it)
                        }
                    }
                }
                return
            }
            if (extracted.fromKnownAddressBar && extracted.isUserEditingAddressBar) return
            if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED && extracted.fromKnownAddressBar) return

            val normalizedUrl = normalizeUrl(extracted.url) ?: return
            if (handleCloseTabRequest(packageName, normalizedUrl)) return
            val pageSignals = extractPageSignals(root)
            val signature = "$packageName|$normalizedUrl"
            if (signature == lastSignature && now - lastBlockUiAt < 1_000L) return
            lastSignature = signature

            scope.launch {
                runCatching {
                    evaluate(packageName, normalizedUrl, inPrivateMode, pageSignals)
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

    private suspend fun evaluate(
        packageName: String,
        url: String,
        inPrivateMode: Boolean,
        pageSignals: PageSignals
    ) {
        val app = application as? PureGuardApp ?: return
        val repository = app.container.repository
        val coordinator = app.container.protectionCoordinator
        val settings = repository.getSettings()
        if (!settings.enabled) {
            ServiceVpn.unblockBrowserPackage(this, packageName)
            return
        }
        if (settings.incognitoEnabled && inPrivateMode) {
            val now = System.currentTimeMillis()
            if (lastPrivateModeBlockedPackage == packageName && (now - lastPrivateModeBlockAt) < PRIVATE_MODE_BLOCK_COOLDOWN_MS) {
                return
            }
            blockAndShow(
                packageName = packageName,
                blockedUrl = url,
                reason = "Private browsing is disabled in settings"
            )
            repository.bumpBlocked()
            repository.bumpPrivateModeBlocked()
            lastPrivateModeBlockedPackage = packageName
            lastPrivateModeBlockAt = now
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

        val strictModeBlock = settings.strictMode && decision.type == DecisionType.SUSPECT
        val shouldBlock = decision.type == DecisionType.BLOCK || strictModeBlock
        if (shouldBlock) {
            val reason = if (strictModeBlock) {
                "Strict mode: ${decision.reason}"
            } else {
                decision.reason
            }

            repository.bumpBlocked()
            if (strictModeBlock) {
                repository.bumpStrictModeBlocked()
            } else if (decision.reason.startsWith("DNS layer:", ignoreCase = true)) {
                repository.bumpDnsBlocked()
            } else {
                repository.bumpKeywordBlocked()
            }
            blockAndShow(
                packageName = packageName,
                blockedUrl = url,
                reason = reason
            )
        } else {
            val rewrittenUrl = decision.rewrittenUrl
            if (!rewrittenUrl.isNullOrBlank()) {
                val rewritten = maybeApplySafeSearchRewrite(packageName, url, rewrittenUrl)
                if (rewritten) {
                    repository.bumpSafeSearchRewrite()
                    repository.bumpScanned()
                    ServiceVpn.unblockBrowserPackage(this, packageName)
                    if (lastBlockedPackage == packageName && lastBlockedUrl == url) {
                        lastBlockedPackage = null
                        lastBlockedUrl = null
                    }
                    return
                }
            }

            val pageDecision = coordinator.evaluatePageContent(
                url = url,
                settings = settings,
                signals = pageSignals,
                dnsLayers = evaluation.dnsLayers
            )
            if (pageDecision.type == DecisionType.BLOCK) {
                repository.bumpBlocked()
                repository.bumpKeywordBlocked()
                blockAndShow(
                    packageName = packageName,
                    blockedUrl = url,
                    reason = pageDecision.reason
                )
                return
            }
            repository.bumpScanned()
            ServiceVpn.unblockBrowserPackage(this, packageName)
            if (lastBlockedPackage == packageName && lastBlockedUrl == url) {
                lastBlockedPackage = null
                lastBlockedUrl = null
            }
        }
    }

    private fun blockAndShow(packageName: String, blockedUrl: String, reason: String) {
        ServiceVpn.blockBrowserPackage(this, packageName)
        scope.launch(Dispatchers.Main) {
            maybeShowBlockedUi(
                packageName = packageName,
                blockedUrl = blockedUrl,
                reason = reason
            )
        }
        lastBlockedPackage = packageName
        lastBlockedUrl = blockedUrl
    }

    private fun maybeShowBlockedUi(packageName: String, blockedUrl: String, reason: String) {
        if (BrowserBlockBridge.shouldSuppressBlockUi(packageName, blockedUrl)) return
        if (BlockedContentActivity.Companion.isVisible) return
        val now = System.currentTimeMillis()
        val isSameBlock = lastBlockedPackage == packageName && lastBlockedUrl == blockedUrl
        if (isSameBlock && (now - lastBlockUiAt) < 3500L) return
        lastBlockUiAt = now
        runCatching {
            BlockedContentActivity.Companion.launch(
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
        lastSafeSearchRewriteSignature = ""
    }

    private suspend fun maybeBlockPrivateMode(packageName: String) {
        val app = application as? PureGuardApp ?: return
        val repository = app.container.repository
        val settings = repository.getSettings()
        if (!settings.enabled || !settings.incognitoEnabled) return
        val now = System.currentTimeMillis()
        if (lastPrivateModeBlockedPackage == packageName && (now - lastPrivateModeBlockAt) < PRIVATE_MODE_BLOCK_COOLDOWN_MS) {
            return
        }

        val blockedUrl = rootInActiveWindow?.let { extractUrl(it, packageName)?.url }?.let { normalizeUrl(it) }
            ?: "https://private-tab.local"
        repository.bumpBlocked()
        repository.bumpPrivateModeBlocked()
        blockAndShow(
            packageName = packageName,
            blockedUrl = blockedUrl,
            reason = "Private browsing is disabled in settings"
        )
        lastPrivateModeBlockedPackage = packageName
        lastPrivateModeBlockAt = now
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

    private fun maybeApplySafeSearchRewrite(packageName: String, currentUrl: String, targetUrl: String): Boolean {
        val current = normalizeUrl(currentUrl) ?: return false
        val target = normalizeUrl(targetUrl) ?: return false
        if (current.equals(target, ignoreCase = true)) return false

        val now = System.currentTimeMillis()
        val signature = "$packageName|$current|$target"
        if (signature == lastSafeSearchRewriteSignature &&
            (now - lastSafeSearchRewriteAt) < SAFE_SEARCH_REWRITE_COOLDOWN_MS
        ) {
            return false
        }

        val root = rootInActiveWindow ?: return false
        val addressNode = findAddressBarNode(root, packageName) ?: return false
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, target)
        }

        addressNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        val textSet = addressNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        if (!textSet) return false

        val imeEnterSent = runCatching {
            addressNode.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER.id)
        }.getOrDefault(false)
        val clickSent = if (!imeEnterSent) {
            addressNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            true
        }
        if (!clickSent) return false

        lastSafeSearchRewriteSignature = signature
        lastSafeSearchRewriteAt = now
        return true
    }

    private fun findAddressBarNode(root: AccessibilityNodeInfo, packageName: String): AccessibilityNodeInfo? {
        val ids = BrowserPackageCatalog.knownAddressBars(packageName)
        if (ids.isEmpty()) return null

        for (id in ids) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id)
            val best = nodes.firstOrNull { it.isEditable || it.isFocusable }
            if (best != null) return best
            val first = nodes.firstOrNull()
            if (first != null) return first
        }
        return null
    }

    private fun looksLikePrivateMode(root: AccessibilityNodeInfo): Boolean {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var visited = 0

        while (queue.isNotEmpty() && visited < 250) {
            val node = queue.removeFirst()
            visited += 1

            val candidateText = buildList {
                add(node.text?.toString().orEmpty())
                add(node.contentDescription?.toString().orEmpty())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    add(node.hintText?.toString().orEmpty())
                }
            }.joinToString(" ").lowercase(Locale.US)

            if (candidateText.isNotBlank() && PRIVATE_MODE_PATTERN.containsMatchIn(candidateText)) {
                return true
            }

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.addLast(it) }
            }
        }
        return false
    }

    private fun extractPageSignals(root: AccessibilityNodeInfo): PageSignals {
        val metadata = StringBuilder()
        val imageUrls = linkedSetOf<String>()
        val posterUrls = linkedSetOf<String>()

        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var visited = 0

        while (queue.isNotEmpty() && visited < 260) {
            val node = queue.removeFirst()
            visited += 1

            val text = node.text?.toString().orEmpty()
            val desc = node.contentDescription?.toString().orEmpty()
            val hint = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                node.hintText?.toString().orEmpty()
            } else {
                ""
            }

            appendMetadata(metadata, text)
            appendMetadata(metadata, desc)
            appendMetadata(metadata, hint)

            collectMediaUrls(text, imageUrls, posterUrls)
            collectMediaUrls(desc, imageUrls, posterUrls)
            collectMediaUrls(hint, imageUrls, posterUrls)

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.addLast(it) }
            }
        }

        return PageSignals(
            metadataText = metadata.toString(),
            imageUrls = imageUrls.take(40),
            videoPosterUrls = posterUrls.take(20)
        )
    }

    private fun appendMetadata(buffer: StringBuilder, text: String) {
        val cleaned = text.trim()
        if (cleaned.isBlank()) return
        if (buffer.length >= MAX_METADATA_CHARS) return
        if (buffer.isNotEmpty()) buffer.append(' ')
        val remaining = MAX_METADATA_CHARS - buffer.length
        buffer.append(cleaned.take(remaining))
    }

    private fun collectMediaUrls(source: String, imageUrls: MutableSet<String>, posterUrls: MutableSet<String>) {
        if (source.isBlank()) return
        URL_EXTRACT_PATTERN.findAll(source).forEach { match ->
            val candidate = match.value.trimEnd('.', ',', ';', ')', ']', '"', '\'')
            if (!candidate.startsWith("http://") && !candidate.startsWith("https://")) return@forEach

            val normalized = candidate.lowercase(Locale.US)
            when {
                IMAGE_URL_PATTERN.containsMatchIn(normalized) -> imageUrls.add(candidate)
                normalized.contains("poster") || normalized.endsWith(".mp4") || normalized.endsWith(".webm") -> {
                    posterUrls.add(candidate)
                }
            }
        }
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
        lastSafeSearchRewriteSignature = ""
        return true
    }

    companion object {
        private const val TAG = "BrowserA11yService"
        private const val SAFE_SEARCH_REWRITE_COOLDOWN_MS = 4500L
        private const val PRIVATE_MODE_BLOCK_COOLDOWN_MS = 2500L
        private const val MAX_METADATA_CHARS = 120_000
        private val URL_PATTERN = Regex("""^([a-z0-9-]+\.)+[a-z]{2,}([/:?#].*)?$""")
        private val URL_EXTRACT_PATTERN = Regex("""https?://[^\s"'<>()]+""", RegexOption.IGNORE_CASE)
        private val IMAGE_URL_PATTERN = Regex("""\.(jpg|jpeg|png|webp|gif|bmp|avif)(\?.*)?$""", RegexOption.IGNORE_CASE)
        private val PRIVATE_MODE_PATTERN = Regex(
            "(incognito|private tab|private browsing|inprivate|secret mode|privacy tab)",
            RegexOption.IGNORE_CASE
        )
    }
}

private data class ExtractedUrl(
    val url: String,
    val fromKnownAddressBar: Boolean,
    val isUserEditingAddressBar: Boolean
)
