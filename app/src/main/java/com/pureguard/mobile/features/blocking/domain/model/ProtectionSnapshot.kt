package com.pureguard.mobile.domain.model.local

data class ProtectionSnapshot(
    val settings: ProtectionSettings = ProtectionSettings(),
    val stats: ProtectionStats = ProtectionStats(),
    val lockState: LockState = LockState()
)
