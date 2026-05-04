package com.pureguard.mobile

import android.app.Application
import com.pureguard.mobile.core.AppContainer

class PureGuardApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
