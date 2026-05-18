package com.pureguard.mobile.features.blocking.data.mapper

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
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

    val BlockedCount = intPreferencesKey("blocked_count")
    val ScannedCount = intPreferencesKey("scanned_count")
    val SafeSearchRewriteCount = intPreferencesKey("safe_search_rewrite_count")
    val AllowOnceCount = intPreferencesKey("allow_once_count")
    val DnsBlockedCount = intPreferencesKey("dns_blocked_count")
    val KeywordBlockedCount = intPreferencesKey("keyword_blocked_count")
    val PrivateModeBlockedCount = intPreferencesKey("private_mode_blocked_count")
    val StrictModeBlockedCount = intPreferencesKey("strict_mode_blocked_count")

    val PasswordSalt = stringPreferencesKey("password_salt")
    val PasswordHash = stringPreferencesKey("password_hash")
    val PasswordIterations = intPreferencesKey("password_iterations")
    val LockEnabled = booleanPreferencesKey("lock_enabled")
}
