package com.pureguard.mobile.core

import android.content.Context
import com.pureguard.mobile.data.prefs.PreferencesProtectionRepository
import com.pureguard.mobile.domain.ProtectionRepository
import com.pureguard.mobile.domain.engine.ProtectionCoordinator

class AppContainer(context: Context) {
    val repository: ProtectionRepository = PreferencesProtectionRepository(context)
    val protectionCoordinator: ProtectionCoordinator = ProtectionCoordinator()
}
