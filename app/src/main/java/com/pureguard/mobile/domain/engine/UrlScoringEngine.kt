package com.pureguard.mobile.domain.engine

import com.pureguard.mobile.features.blocking.data.mapper.ProtectionConstants
import java.net.URI
import java.util.Locale

data class UrlScoreResult(
    val score: Int,
    val reasons: List<String>
)

class UrlScoringEngine {

    fun score(rawUrl: String): UrlScoreResult {
        return runCatching {
            val uri = URI(rawUrl)
            val host = (uri.host ?: "").lowercase(Locale.US)
            val path = "${uri.path ?: ""}${uri.query?.let { "?$it" } ?: ""}${uri.fragment?.let { "#$it" } ?: ""}"
                .lowercase(Locale.US)
            val full = host + path
            var score = 0
            val reasons = mutableListOf<String>()

            ProtectionConstants.strictTlds.forEach { tld ->
                if (host.endsWith(tld)) {
                    score += 100
                    reasons += "Adult TLD $tld"
                }
            }

            var domainHits = 0
            for (keyword in ProtectionConstants.nsfwKeywords) {
                val regex = Regex("(^|[^a-z])${Regex.escape(keyword)}([^a-z]|$)", RegexOption.IGNORE_CASE)
                if (regex.containsMatchIn(host)) {
                    domainHits++
                    reasons += "Domain keyword: $keyword"
                    if (domainHits >= 3) break
                }
            }
            if (domainHits > 0) score += 60 * domainHits

            var pathHits = 0
            val matched = mutableListOf<String>()
            for (keyword in ProtectionConstants.nsfwKeywords) {
                if (full.contains(keyword)) {
                    pathHits++
                    matched += keyword
                }
            }
            if (pathHits > 0) {
                score += minOf(80, pathHits * 18)
                reasons += "$pathHits URL keyword(s): ${matched.take(4).joinToString(",")}"
            }

            if (Regex("[a-z]{3,}-(porn|sex|nude|xxx|fuck|hentai)-[a-z]{3,}", RegexOption.IGNORE_CASE).containsMatchIn(full)) {
                score += 50
                reasons += "Adult URL slug pattern"
            }

            UrlScoreResult(score = score, reasons = reasons)
        }.getOrElse {
            UrlScoreResult(score = 0, reasons = emptyList())
        }
    }

    fun hostFromUrl(rawUrl: String): String? {
        return runCatching { URI(rawUrl).host?.lowercase(Locale.US) }.getOrNull()
    }

    fun inList(host: String, list: List<String>): Boolean {
        val normalized = host.lowercase(Locale.US)
        return list.any { entry ->
            val e = entry.trim().lowercase(Locale.US)
            e.isNotBlank() && (normalized == e || normalized.endsWith(".$e"))
        }
    }

    fun isTrusted(host: String): Boolean {
        val normalized = host.lowercase(Locale.US)
        return ProtectionConstants.trustedDomains.any { t ->
            normalized == t || normalized.endsWith(".$t")
        }
    }
}
