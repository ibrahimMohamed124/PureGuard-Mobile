package com.pureguard.mobile.core.security

class UnlockSessionManager(
    private val unlockWindowMs: Long = 60_000L
) {
    private var unlockExpiresAt: Long = 0L

    fun grantUnlock() {
        unlockExpiresAt = System.currentTimeMillis() + unlockWindowMs
    }

    fun clearUnlock() {
        unlockExpiresAt = 0L
    }

    fun isUnlocked(): Boolean = System.currentTimeMillis() < unlockExpiresAt
}
