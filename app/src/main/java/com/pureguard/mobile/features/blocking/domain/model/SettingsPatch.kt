package com.pureguard.mobile.features.blocking.domain.model

data class SettingsPatch(
    val enabled: Boolean? = null,
    val sensitivity: Sensitivity? = null,
    val enforceSafeSearch: Boolean? = null,
    val enableImageScan: Boolean? = null,
    val fastScan: Boolean? = null,
    val fastScanLimit: Int? = null,
    val strictMode: Boolean? = null,
    val whitelist: List<String>? = null,
    val blacklist: List<String>? = null,
    val incognitoEnabled: Boolean? = null
) {
    fun touchesProtectedFields(): Boolean {
        return listOf(
            enabled,
            sensitivity,
            enforceSafeSearch,
            enableImageScan,
            fastScan,
            fastScanLimit,
            strictMode,
            whitelist,
            blacklist,
            incognitoEnabled
        ).any { it != null }
    }
}
