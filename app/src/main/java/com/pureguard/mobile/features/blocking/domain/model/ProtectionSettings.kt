package com.pureguard.mobile.domain.model.local

data class ProtectionSettings(
    val enabled: Boolean = true,
    val sensitivity: Sensitivity = Sensitivity.HIGH,
    val enforceSafeSearch: Boolean = true,
    val enableImageScan: Boolean = true,
    val fastScan: Boolean = true,
    val fastScanLimit: Int = 16,
    val strictMode: Boolean = true,
    val whitelist: List<String> = emptyList(),
    val blacklist: List<String> = emptyList(),
    val incognitoEnabled: Boolean = false,
    val backoff: BackoffConfig = BackoffConfig()
)
