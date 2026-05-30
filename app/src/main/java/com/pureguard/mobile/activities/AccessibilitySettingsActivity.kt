package com.pureguard.mobile.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import com.pureguard.mobile.R
import com.pureguard.mobile.core.localization.AppLanguage

class AccessibilitySettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openAccessibilitySettings()
        finish()
    }

    private fun openAccessibilitySettings() {
        val accessibilityIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        runCatching {
            startActivity(accessibilityIntent)
        }.onFailure {
            runCatching {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }.onFailure {
                Toast.makeText(
                    this,
                    getString(R.string.open_accessibility_settings_to_reenable),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguage.wrap(newBase))
    }
}
