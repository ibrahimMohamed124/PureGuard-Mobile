package com.pureguard.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.pureguard.mobile.ui.AppRoot
import com.pureguard.mobile.ui.ProtectionViewModel
import com.pureguard.mobile.ui.ProtectionViewModelFactory
import com.pureguard.mobile.ui.theme.PureGuardTheme

class MainActivity : ComponentActivity() {

    private val protectionViewModel: ProtectionViewModel by viewModels {
        val app = application as PureGuardApp
        ProtectionViewModelFactory(app.container.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PureGuardTheme {
                AppRoot(
                    protectionViewModel = protectionViewModel
                )
            }
        }
    }
}
