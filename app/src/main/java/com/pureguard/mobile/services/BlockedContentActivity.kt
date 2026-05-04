package com.pureguard.mobile.services

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pureguard.mobile.PureGuardApp
import com.pureguard.mobile.ui.GlassCard
import com.pureguard.mobile.ui.GradientBackground
import com.pureguard.mobile.ui.theme.PureGuardTheme
import com.pureguard.mobile.ui.theme.PgDanger
import com.pureguard.mobile.ui.theme.PgMuted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.net.URI
import java.util.Locale

class BlockedContentActivity : ComponentActivity() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val blockedUrl = intent.getStringExtra(EXTRA_URL).orEmpty()
        val blockedReason = intent.getStringExtra(EXTRA_REASON).orEmpty()
        val browserPackage = intent.getStringExtra(EXTRA_BROWSER_PACKAGE).orEmpty()

        setContent {
            PureGuardTheme {
                var password by remember { mutableStateOf("") }
                var submitting by remember { mutableStateOf(false) }
                var status by remember { mutableStateOf("") }

                GradientBackground {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = PgDanger,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                Text(
                                    text = "Page Blocked",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                Text(
                                    text = "PureGuard stopped this page to keep browsing safer.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PgMuted
                                )
                                HorizontalDivider(color = Color.White.copy(alpha = 0.14f))
                                Text(
                                    text = "Reason",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PgDanger
                                )
                                Text(
                                    text = blockedReason.ifBlank { "Suspicious content detected." },
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "URL",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PgMuted
                                )
                                Text(
                                    text = blockedUrl,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Password (if lock is enabled)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    visualTransformation = PasswordVisualTransformation(),
                                    enabled = !submitting
                                )
                                if (status.isNotBlank()) {
                                    Text(
                                        text = status,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PgDanger
                                    )
                                }
                                Button(
                                    onClick = {
                                        if (submitting) return@Button
                                        submitting = true
                                        status = ""
                                        allowOnce(
                                            blockedUrl = blockedUrl,
                                            browserPackage = browserPackage,
                                            password = password
                                        ) { ok, message ->
                                            submitting = false
                                            if (!ok) {
                                                status = message
                                            } else {
                                                openInDefaultBrowser(blockedUrl)
                                                finish()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !submitting,
                                    colors = ButtonDefaults.buttonColors(containerColor = PgDanger)
                                ) {
                                    Text("Allow once")
                                }
                                TextButton(
                                    onClick = {
                                        BrowserBlockBridge.requestCloseBlockedTab(
                                            packageName = browserPackage,
                                            blockedUrl = blockedUrl
                                        )
                                        goHome()
                                        finish()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !submitting
                                ) {
                                    Text("Stay protected")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        isVisible = true
    }

    override fun onStop() {
        isVisible = false
        super.onStop()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun allowOnce(
        blockedUrl: String,
        browserPackage: String,
        password: String,
        onDone: (Boolean, String) -> Unit
    ) {
        val app = application as PureGuardApp
        scope.launch {
            val repository = app.container.repository
            val snapshot = repository.getSnapshot()
            if (snapshot.lockState.lockEnabled && !snapshot.lockState.unlocked) {
                val ok = repository.verifyPassword(password)
                if (!ok) {
                    onDone(false, "Wrong password")
                    return@launch
                }
            }

            val host = runCatching {
                URI(blockedUrl).host?.lowercase(Locale.US)
            }.getOrNull().orEmpty()

            if (host.isBlank()) {
                onDone(false, "Invalid URL")
                return@launch
            }

            repository.allowHostOnce(host)
            if (browserPackage.isNotBlank()) {
                ServiceVpn.unblockBrowserPackage(this@BlockedContentActivity, browserPackage)
            }
            Toast.makeText(
                this@BlockedContentActivity,
                "Allowed once for $host",
                Toast.LENGTH_SHORT
            ).show()
            onDone(true, "")
        }
    }

    private fun openInDefaultBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { startActivity(intent) }
    }

    private fun goHome() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { startActivity(intent) }
    }

    companion object {
        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_REASON = "extra_reason"
        private const val EXTRA_BROWSER_PACKAGE = "extra_browser_package"
        @Volatile
        var isVisible: Boolean = false
            private set

        fun launch(
            context: android.content.Context,
            blockedUrl: String,
            reason: String,
            browserPackage: String
        ) {
            val intent = Intent(context, BlockedContentActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(EXTRA_URL, blockedUrl)
                putExtra(EXTRA_REASON, reason)
                putExtra(EXTRA_BROWSER_PACKAGE, browserPackage)
            }
            context.startActivity(intent)
        }
    }
}
