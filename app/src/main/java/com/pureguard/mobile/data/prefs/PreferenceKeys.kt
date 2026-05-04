package com.pureguard.mobile.data.prefs

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val Enabled = booleanPreferencesKey("enabled")
    val Sensitivity = stringPreferencesKey("sensitivity")
    val EnforceSafeSearch = booleanPreferencesKey("enforce_safe_search")
    val EnableImageScan = booleanPreferencesKey("enable_image_scan")
    val FastScan = booleanPreferencesKey("fast_scan")
    val FastScanLimit = intPreferencesKey("fast_scan_limit")
    val StrictMode = booleanPreferencesKey("strict_mode")
    val Whitelist = stringPreferencesKey("whitelist")
    val Blacklist = stringPreferencesKey("blacklist")
    val IncognitoEnabled = booleanPreferencesKey("incognito_enabled")

    val BackoffRetries = intPreferencesKey("backoff_retries")
    val BackoffBaseDelay = longPreferencesKey("backoff_base_delay")
    val BackoffMaxDelay = longPreferencesKey("backoff_max_delay")
    val BackoffSafetyReveal = longPreferencesKey("backoff_safety_reveal")
    val BackoffFailClosedGrace = longPreferencesKey("backoff_fail_closed_grace")

    val BlockedCount = intPreferencesKey("blocked_count")
    val ScannedCount = intPreferencesKey("scanned_count")

    val PasswordSalt = stringPreferencesKey("password_salt")
    val PasswordHash = stringPreferencesKey("password_hash")
    val PasswordIterations = intPreferencesKey("password_iterations")
    val LockEnabled = booleanPreferencesKey("lock_enabled")
}
