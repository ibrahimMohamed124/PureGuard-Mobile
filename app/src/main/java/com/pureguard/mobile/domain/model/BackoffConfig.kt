package com.pureguard.mobile.domain.model

data class BackoffConfig(
    val sendMaxRetries: Int = 8,
    val sendBaseDelayMs: Long = 150,
    val sendMaxDelayMs: Long = 1500,
    val safetyRevealMs: Long = 8000,
    val failClosedGraceMs: Long = 1500
)
