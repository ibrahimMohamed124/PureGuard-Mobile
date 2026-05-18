package com.pureguard.mobile.domain.model.local

enum class LayerVerdict {
    ALLOW,
    BLOCK,
    UNKNOWN
}

data class LayerResult(
    val verdict: LayerVerdict = LayerVerdict.UNKNOWN,
    val reason: String = ""
)

data class DnsLayerVerdict(
    val cloudflare: LayerResult = LayerResult(),
    val adguard: LayerResult = LayerResult()
)
