package com.pureguard.mobile.core

import android.content.Context
import com.pureguard.mobile.features.blocking.data.repository.PreferencesProtectionRepository
import com.pureguard.mobile.features.blocking.domain.repository.ProtectionRepository
import com.pureguard.mobile.features.blocking.data.remote.ProtectionCoordinator

class AppContainer(context: Context) {
    val repository: ProtectionRepository = PreferencesProtectionRepository(context)
    val protectionCoordinator: ProtectionCoordinator = ProtectionCoordinator()
}
