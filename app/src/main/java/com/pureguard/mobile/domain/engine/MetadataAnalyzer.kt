package com.pureguard.mobile.domain.engine

import com.pureguard.mobile.features.blocking.data.mapper.ProtectionConstants

class MetadataAnalyzer {

    data class MetadataResult(
        val hits: Int,
        val matched: List<String>
    )

    fun analyze(text: String): MetadataResult {
        val normalized = text.lowercase().take(120_000)
        var hits = 0
        val matched = mutableListOf<String>()
        for (keyword in ProtectionConstants.nsfwKeywords) {
            val regex = Regex("(^|[^a-z])${Regex.escape(keyword)}([^a-z]|$)", RegexOption.IGNORE_CASE)
            if (regex.containsMatchIn(normalized)) {
                hits++
                matched += keyword
                if (hits >= 20) break
            }
        }
        return MetadataResult(hits = hits, matched = matched)
    }
}