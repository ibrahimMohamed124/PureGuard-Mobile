package com.pureguard.mobile.features.blocking.domain.model

data class ProtectionSnapshot(
    val settings: ProtectionSettings = ProtectionSettings(),
    val stats: ProtectionStats = ProtectionStats(),
    val lockState: LockState = LockState()
)
