package com.pureguard.mobile.features.blocking.data.remote

import com.pureguard.mobile.domain.engine.DnsSafetyChecker
import com.pureguard.mobile.domain.engine.MetadataAnalyzer
import com.pureguard.mobile.domain.engine.OnDeviceImageScanner
import com.pureguard.mobile.domain.engine.SafeSearchRewriter
import com.pureguard.mobile.domain.engine.UrlScoringEngine
import com.pureguard.mobile.features.blocking.domain.model.DecisionType
import com.pureguard.mobile.features.blocking.domain.model.DnsLayerVerdict
import com.pureguard.mobile.features.blocking.domain.model.LayerVerdict
import com.pureguard.mobile.features.blocking.domain.model.PageSignals
import com.pureguard.mobile.features.blocking.domain.model.ProtectionDecision
import com.pureguard.mobile.features.blocking.domain.model.ProtectionSettings
import java.net.URI
import java.util.Locale

class ProtectionCoordinator(
    private val urlScoringEngine: UrlScoringEngine = UrlScoringEngine(),
    private val dnsSafetyChecker: DnsSafetyChecker = DnsSafetyChecker(),
    private val safeSearchRewriter: SafeSearchRewriter = SafeSearchRewriter(),
    private val metadataAnalyzer: MetadataAnalyzer = MetadataAnalyzer(),
    private val imageScanner: OnDeviceImageScanner = OnDeviceImageScanner()
) {
    private val verdictCache = mutableMapOf<String, CacheEntry>()
    private val dnsCache = mutableMapOf<String, DnsCacheEntry>()
    private val ttlMs = 30 * 60 * 1000L

    suspend fun evaluateNavigation(
        url: String,
        settings: ProtectionSettings,
        allowOnce: Boolean
    ): NavigationEvaluation {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return NavigationEvaluation(ProtectionDecision(DecisionType.ALLOW, "Unsupported scheme", url))
        }

        val host = host(url) ?: return NavigationEvaluation(
            ProtectionDecision(DecisionType.ALLOW, "Invalid URL", url)
        )

        if (allowOnce) {
            return NavigationEvaluation(ProtectionDecision(DecisionType.ALLOW, "Allowed once", url))
        }

        val rewrittenUrl = if (settings.enforceSafeSearch) safeSearchRewriter.rewrite(url) else url
        val rewrittenHost = host(rewrittenUrl) ?: host

        if (urlScoringEngine.inList(rewrittenHost, settings.whitelist)) {
            return NavigationEvaluation(
                ProtectionDecision(
                    DecisionType.ALLOW,
                    "Whitelisted domain",
                    url,
                    rewrittenUrl = rewrittenUrl.takeIf { it != url }
                )
            )
        }
        if (urlScoringEngine.inList(rewrittenHost, settings.blacklist)) {
            return NavigationEvaluation(
                ProtectionDecision(
                    DecisionType.BLOCK,
                    "User blacklist",
                    url,
                    rewrittenUrl = rewrittenUrl.takeIf { it != url }
                )
            )
        }
        if (urlScoringEngine.isTrusted(rewrittenHost)) {
            return NavigationEvaluation(
                ProtectionDecision(
                    DecisionType.ALLOW,
                    "Trusted domain",
                    url,
                    rewrittenUrl = rewrittenUrl.takeIf { it != url }
                )
            )
        }

        getCachedHostVerdict(rewrittenHost)?.let { cached ->
            if (cached == DecisionType.BLOCK) {
                return NavigationEvaluation(
                    ProtectionDecision(
                        DecisionType.BLOCK,
                        "Cached flagged domain",
                        url,
                        rewrittenUrl = rewrittenUrl.takeIf { it != url }
                    )
                )
            }
            if (cached == DecisionType.ALLOW) {
                return NavigationEvaluation(
                    ProtectionDecision(
                        DecisionType.ALLOW,
                        "Cached clean domain",
                        url,
                        rewrittenUrl = rewrittenUrl.takeIf { it != url }
                    )
                )
            }
        }

        val score = urlScoringEngine.score(rewrittenUrl)
        val dns = dnsForHost(rewrittenHost)

        val dnsBlocked = dns.cloudflare.verdict == LayerVerdict.BLOCK || dns.adguard.verdict == LayerVerdict.BLOCK
        if (dnsBlocked) {
            setCachedHostVerdict(rewrittenHost, DecisionType.BLOCK)
            val reason = listOf(dns.cloudflare, dns.adguard)
                .filter { it.verdict == LayerVerdict.BLOCK }
                .joinToString(" + ") { it.reason }
            return NavigationEvaluation(
                decision = ProtectionDecision(
                    type = DecisionType.BLOCK,
                    reason = "DNS layer: $reason",
                    url = url,
                    rewrittenUrl = rewrittenUrl.takeIf { it != url }
                ),
                dnsLayers = dns
            )
        }

        if (score.score >= settings.sensitivity.urlThreshold) {
            setCachedHostVerdict(rewrittenHost, DecisionType.BLOCK)
            return NavigationEvaluation(
                decision = ProtectionDecision(
                    type = DecisionType.BLOCK,
                    reason = "URL/keyword: ${score.reasons.take(3).joinToString(", ")}",
                    url = url,
                    rewrittenUrl = rewrittenUrl.takeIf { it != url }
                ),
                dnsLayers = dns
            )
        }

        val suspect = score.score >= (settings.sensitivity.urlThreshold * 0.6f) &&
            (dns.cloudflare.verdict != LayerVerdict.ALLOW || dns.adguard.verdict != LayerVerdict.ALLOW)
        if (suspect) {
            return NavigationEvaluation(
                decision = ProtectionDecision(
                    type = DecisionType.SUSPECT,
                    reason = "Suspicious URL + uncertain DNS",
                    url = url,
                    rewrittenUrl = rewrittenUrl.takeIf { it != url }
                ),
                dnsLayers = dns
            )
        }

        setCachedHostVerdict(rewrittenHost, DecisionType.ALLOW)
        return NavigationEvaluation(
            decision = ProtectionDecision(
                type = DecisionType.ALLOW,
                reason = "Navigation passed all checks",
                url = url,
                rewrittenUrl = rewrittenUrl.takeIf { it != url }
            ),
            dnsLayers = dns
        )
    }

    suspend fun evaluatePageContent(
        url: String,
        settings: ProtectionSettings,
        signals: PageSignals,
        dnsLayers: DnsLayerVerdict?
    ): ProtectionDecision {
        val dns = dnsLayers ?: dnsForHost(host(url).orEmpty())
        val cfBlock = dns.cloudflare.verdict == LayerVerdict.BLOCK
        val adgBlock = dns.adguard.verdict == LayerVerdict.BLOCK
        if (cfBlock || adgBlock) {
            val reason = listOfNotNull(
                dns.cloudflare.reason.takeIf { cfBlock },
                dns.adguard.reason.takeIf { adgBlock }
            ).joinToString(" + ")
            return ProtectionDecision(DecisionType.BLOCK, "DNS layer: $reason", url)
        }

        val metadata = metadataAnalyzer.analyze(signals.metadataText)
        val cfSuspect = dns.cloudflare.verdict != LayerVerdict.ALLOW
        val adgSuspect = dns.adguard.verdict != LayerVerdict.ALLOW
        if ((cfSuspect || adgSuspect) && metadata.hits >= 1) {
            return ProtectionDecision(
                DecisionType.BLOCK,
                "DNS suspect + metadata: ${metadata.matched.take(4).joinToString(", ")}",
                url
            )
        }
        if (metadata.hits >= 2) {
            return ProtectionDecision(
                DecisionType.BLOCK,
                "Metadata: ${metadata.matched.take(6).joinToString(", ")}",
                url
            )
        }

        if (!settings.enableImageScan) {
            return if (metadata.hits >= 1) {
                ProtectionDecision(
                    DecisionType.BLOCK,
                    "Metadata suspicious while image scan disabled",
                    url
                )
            } else {
                ProtectionDecision(DecisionType.ALLOW, "Metadata clean", url)
            }
        }

        val scanInput = (signals.imageUrls + signals.videoPosterUrls).distinct()
        val imageResult = imageScanner.scan(
            imageUrls = scanInput,
            sensitivity = settings.sensitivity,
            fastScan = settings.fastScan,
            fastLimit = settings.fastScanLimit
        )
        if (imageResult.blocked) {
            return ProtectionDecision(DecisionType.BLOCK, imageResult.reason, url)
        }

        return ProtectionDecision(DecisionType.ALLOW, "Page content scan clear", url)
    }

    suspend fun dnsForHost(host: String): DnsLayerVerdict {
        if (host.isBlank()) return DnsLayerVerdict()
        val normalized = host.lowercase(Locale.US)
        val cached = dnsCache[normalized]
        if (cached != null && System.currentTimeMillis() - cached.timestamp <= ttlMs) {
            return cached.verdict
        }
        val verdict = dnsSafetyChecker.check(normalized)
        dnsCache[normalized] = DnsCacheEntry(verdict, System.currentTimeMillis())
        return verdict
    }

    fun clearExpiredCache() {
        val now = System.currentTimeMillis()
        verdictCache.entries.removeIf { now - it.value.timestamp > ttlMs }
        dnsCache.entries.removeIf { now - it.value.timestamp > ttlMs }
    }

    private fun getCachedHostVerdict(host: String): DecisionType? {
        clearExpiredCache()
        return verdictCache[host.lowercase(Locale.US)]?.decision
    }

    private fun setCachedHostVerdict(host: String, decisionType: DecisionType) {
        verdictCache[host.lowercase(Locale.US)] = CacheEntry(decision = decisionType, timestamp = System.currentTimeMillis())
    }

    private fun host(url: String): String? {
        return runCatching { URI(url).host?.lowercase(Locale.US) }.getOrNull()
    }
}

data class NavigationEvaluation(
    val decision: ProtectionDecision,
    val dnsLayers: DnsLayerVerdict? = null
)

private data class CacheEntry(val decision: DecisionType, val timestamp: Long)
private data class DnsCacheEntry(val verdict: DnsLayerVerdict, val timestamp: Long)
