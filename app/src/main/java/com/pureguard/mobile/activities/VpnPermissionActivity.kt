package com.pureguard.mobile.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import com.pureguard.mobile.R
import com.pureguard.mobile.core.localization.AppLanguage
import com.pureguard.mobile.services.local.Vpn.ServiceVpn

class VpnPermissionActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestVpnPermission()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VPN_PERMISSION && resultCode == RESULT_OK) {
            ServiceVpn.start(this)
            Toast.makeText(this, getString(R.string.vpn_protection_ready), Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun requestVpnPermission() {
        val prepareIntent = VpnService.prepare(this)
        if (prepareIntent != null) {
            startActivityForResult(prepareIntent, REQUEST_VPN_PERMISSION)
        } else {
            ServiceVpn.start(this)
            Toast.makeText(this, getString(R.string.vpn_protection_ready), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguage.wrap(newBase))
    }

    companion object {
        private const val REQUEST_VPN_PERMISSION = 4812
    }
}
