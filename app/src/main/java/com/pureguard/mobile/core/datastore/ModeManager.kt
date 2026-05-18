package com.pureguard.mobile.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pureguard.mobile.core.common.PureGuardMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface ModeManager {

    val currentMode: Flow<PureGuardMode>

    suspend fun setMode(mode: PureGuardMode)
}

class ModeManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ModeManager {

    private val MODE_KEY =
        stringPreferencesKey("selected_app_mode")

    override val currentMode: Flow<PureGuardMode> =
        dataStore.data.map { pref ->

            val modeName =
                pref[MODE_KEY]
                    ?: PureGuardMode.UNDEFINED.name

            PureGuardMode.valueOf(modeName)
        }

    override suspend fun setMode(mode: PureGuardMode) {
        dataStore.edit { pref ->
            pref[MODE_KEY] = mode.name
        }
    }
}