package com.pureguard.mobile.domain.engine

import com.pureguard.mobile.domain.model.DnsLayerVerdict
import com.pureguard.mobile.domain.model.LayerResult
import com.pureguard.mobile.domain.model.LayerVerdict
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit

class DnsSafetyChecker(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(1500, TimeUnit.MILLISECONDS)
        .build()
) {
    private val adguardSinkholes = setOf("94.140.14.35", "94.140.14.36", "::ff", "0.0.0.0")
    private val cloudflareSinkholes = setOf("0.0.0.0", "::")

    suspend fun check(host: String): DnsLayerVerdict = coroutineScope {
        val normalized = host.lowercase(Locale.US)
        val cf = async { checkCloudflare(normalized) }
        val adg = async { checkAdGuard(normalized) }
        DnsLayerVerdict(
            cloudflare = cf.await(),
            adguard = adg.await()
        )
    }

    private fun checkCloudflare(host: String): LayerResult {
        val json = doh("https://family.cloudflare-dns.com/dns-query", host) ?: return LayerResult(
            verdict = LayerVerdict.UNKNOWN,
            reason = "Cloudflare timeout"
        )
        if (json.optInt("Status", -1) == 3) {
            return LayerResult(LayerVerdict.BLOCK, "Cloudflare Family: NXDOMAIN")
        }
        val answers = json.optJSONArray("Answer")
        if (answers != null) {
            for (i in 0 until answers.length()) {
                val item = answers.optJSONObject(i) ?: continue
                val data = item.optString("data").trim()
                if (cloudflareSinkholes.contains(data)) {
                    return LayerResult(LayerVerdict.BLOCK, "Cloudflare Family sinkhole")
                }
            }
        }
        return LayerResult(LayerVerdict.ALLOW, "Cloudflare Family: clean")
    }

    private fun checkAdGuard(host: String): LayerResult {
        val json = doh("https://family.adguard-dns.com/dns-query", host) ?: return LayerResult(
            verdict = LayerVerdict.UNKNOWN,
            reason = "AdGuard timeout"
        )
        if (json.optInt("Status", -1) == 3) {
            return LayerResult(LayerVerdict.BLOCK, "AdGuard Family: NXDOMAIN")
        }
        val answers = json.optJSONArray("Answer")
        if (answers != null) {
            for (i in 0 until answers.length()) {
                val item = answers.optJSONObject(i) ?: continue
                val data = item.optString("data").trim()
                if (adguardSinkholes.contains(data)) {
                    return LayerResult(LayerVerdict.BLOCK, "AdGuard Family sinkhole")
                }
            }
        }
        return LayerResult(LayerVerdict.ALLOW, "AdGuard Family: clean")
    }

    private fun doh(endpoint: String, host: String): JSONObject? {
        return runCatching {
            val encoded = URLEncoder.encode(host, "UTF-8")
            val url = "$endpoint?name=$encoded&type=A"
            val request = Request.Builder()
                .url(url)
                .header("accept", "application/dns-json")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                JSONObject(body)
            }
        }.getOrNull()
    }
}
