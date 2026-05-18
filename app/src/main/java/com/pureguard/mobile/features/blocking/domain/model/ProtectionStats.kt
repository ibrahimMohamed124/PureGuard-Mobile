package com.pureguard.mobile.features.blocking.domain.model

data class ProtectionStats(
    val blockedCount: Int = 0,
    val scannedCount: Int = 0,
    val safeSearchRewriteCount: Int = 0,
    val allowOnceCount: Int = 0,
    val dnsBlockedCount: Int = 0,
    val keywordBlockedCount: Int = 0,
    val privateModeBlockedCount: Int = 0,
    val strictModeBlockedCount: Int = 0
)
