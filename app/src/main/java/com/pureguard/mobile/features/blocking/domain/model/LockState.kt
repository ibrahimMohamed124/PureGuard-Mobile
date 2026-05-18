package com.pureguard.mobile.domain.model.local

data class LockState(
    val hasPassword: Boolean = false,
    val lockEnabled: Boolean = false,
    val unlocked: Boolean = false
)
