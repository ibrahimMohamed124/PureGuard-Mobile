package com.pureguard.mobile.domain.model.local

enum class DecisionType {
    ALLOW,
    BLOCK,
    SUSPECT
}

data class ProtectionDecision(
    val type: DecisionType,
    val reason: String,
    val url: String,
    val rewrittenUrl: String? = null
)
