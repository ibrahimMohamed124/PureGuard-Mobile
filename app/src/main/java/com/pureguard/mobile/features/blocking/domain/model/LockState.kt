package com.pureguard.mobile.features.blocking.domain.model

data class LockState(
    val hasPassword: Boolean = false,
    val lockEnabled: Boolean = false,
    val unlocked: Boolean = false
)
