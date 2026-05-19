package com.pureguard.mobile

import android.app.Application
import com.pureguard.mobile.core.AppContainer
import com.pureguard.mobile.core.datastore.Prefs

class PureGuardApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
        container = AppContainer(this)
    }
}
