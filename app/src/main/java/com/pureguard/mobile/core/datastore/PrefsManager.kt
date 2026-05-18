package com.pureguard.mobile.core.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object Prefs {

    private const val PREF_NAME = "app_prefs"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
    }

    // =========================
    // String
    // =========================

    fun putString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }

    fun getString(
        key: String,
        defaultValue: String? = null
    ): String? {
        return prefs.getString(key, defaultValue)
    }

    // =========================
    // Int
    // =========================

    fun putInt(key: String, value: Int) {
        prefs.edit { putInt(key, value) }
    }

    fun getInt(
        key: String,
        defaultValue: Int = 0
    ): Int {
        return prefs.getInt(key, defaultValue)
    }

    // =========================
    // Boolean
    // =========================

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    fun getBoolean(
        key: String,
        defaultValue: Boolean = false
    ): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    // =========================
    // Long
    // =========================

    fun putLong(key: String, value: Long) {
        prefs.edit { putLong(key, value) }
    }

    fun getLong(
        key: String,
        defaultValue: Long = 0L
    ): Long {
        return prefs.getLong(key, defaultValue)
    }

    // =========================
    // Float
    // =========================

    fun putFloat(key: String, value: Float) {
        prefs.edit { putFloat(key, value) }
    }

    fun getFloat(
        key: String,
        defaultValue: Float = 0f
    ): Float {
        return prefs.getFloat(key, defaultValue)
    }

    // =========================
    // Remove
    // =========================

    fun remove(key: String) {
        prefs.edit { remove(key) }
    }

    // =========================
    // Clear All
    // =========================

    fun clear() {
        prefs.edit { clear() }
    }

    // =========================
    // Check if exists
    // =========================

    fun contains(key: String): Boolean {
        return prefs.contains(key)
    }
}