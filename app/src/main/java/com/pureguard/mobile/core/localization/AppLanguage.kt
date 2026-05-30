package com.pureguard.mobile.core.localization

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

object AppLanguage {
    const val PREF_LANGUAGE = "app_language"
    const val SYSTEM = "system"
    const val ENGLISH = "en"
    const val ARABIC = "ar"

    private const val PREF_NAME = "app_prefs"

    val supportedCodes = listOf(SYSTEM, ENGLISH, ARABIC)

    fun get(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(PREF_LANGUAGE, SYSTEM).orEmpty()
        return stored.takeIf { it in supportedCodes } ?: SYSTEM
    }

    fun set(context: Context, code: String) {
        val safeCode = code.takeIf { it in supportedCodes } ?: SYSTEM
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(PREF_LANGUAGE, safeCode)
            .apply()
    }

    fun wrap(context: Context): Context {
        val code = get(context)
        if (code == SYSTEM) return context

        val locale = Locale(code)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        return ContextWrapper(context.createConfigurationContext(config))
    }
}
