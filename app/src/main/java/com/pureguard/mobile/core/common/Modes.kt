package com.pureguard.mobile.core.common

enum class PureGuardMode {
    UNDEFINED,
    RECOVERY,
    PARENTAL
}

@Suppress("unused")
fun PureGuardMode.isParental() = this == PureGuardMode.PARENTAL

@Suppress("unused")
fun PureGuardMode.isRecovery() = this == PureGuardMode.RECOVERY
