package com.pureguard.mobile.domain.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.pureguard.mobile.features.blocking.data.mapper.ProtectionConstants
import com.pureguard.mobile.features.blocking.domain.model.Sensitivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import java.util.concurrent.TimeUnit

data class ImageScanResult(
    val blocked: Boolean,
    val maxScore: Float,
    val reason: String
)

class OnDeviceImageScanner(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(5, TimeUnit.SECONDS)
        .build()
) {
    suspend fun scan(
        imageUrls: List<String>,
        sensitivity: Sensitivity,
        fastScan: Boolean,
        fastLimit: Int
    ): ImageScanResult = coroutineScope {
        if (imageUrls.isEmpty()) {
            return@coroutineScope ImageScanResult(
                blocked = false,
                maxScore = 0f,
                reason = "No image signals"
            )
        }

        val budget = if (fastScan) fastLimit.coerceIn(4, 40) else 80
        val threshold = sensitivity.imageThreshold
        val candidates = imageUrls.distinct().take(budget)

        val scores = candidates.map { url ->
            async(Dispatchers.IO) { url to classify(url) }
        }.awaitAll()

        val best = scores.maxByOrNull { it.second } ?: ("" to 0f)
        val blocked = best.second >= threshold
        val reason = if (blocked) {
            "On-device image scan score ${(best.second * 100).toInt()}%"
        } else {
            "Image scan clear"
        }
        ImageScanResult(blocked = blocked, maxScore = best.second, reason = reason)
    }

    private suspend fun classify(url: String): Float = withContext(Dispatchers.IO) {
        val keywordScore = keywordScore(url)
        val bitmapScore = downloadAndScoreBitmap(url)
        (keywordScore * 0.45f + bitmapScore * 0.55f).coerceIn(0f, 1f)
    }

    private fun keywordScore(url: String): Float {
        val hostAndPath = runCatching {
            val uri = URI(url)
            "${uri.host.orEmpty()}${uri.path.orEmpty()}".lowercase()
        }.getOrDefault(url.lowercase())
        val hits = ProtectionConstants.nsfwKeywords.count { hostAndPath.contains(it) }
        return (hits / 6f).coerceIn(0f, 1f)
    }

    private fun downloadAndScoreBitmap(url: String): Float {
        return runCatching {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return 0f
                val bytes = response.body?.bytes() ?: return 0f
                val opts = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts) ?: return 0f
                val scaled = Bitmap.createScaledBitmap(bitmap, 96, 96, true)
                val score = skinHeuristicScore(scaled)
                scaled.recycle()
                bitmap.recycle()
                score
            }
        }.getOrDefault(0f)
    }

    private fun skinHeuristicScore(bitmap: Bitmap): Float {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        var skinCount = 0
        var warmCount = 0
        pixels.forEach { color ->
            val r = (color shr 16) and 0xff
            val g = (color shr 8) and 0xff
            val b = color and 0xff
            if (isSkin(r, g, b)) skinCount++
            if (r > g && g > b && r > 90) warmCount++
        }
        val total = pixels.size.coerceAtLeast(1)
        val skinRatio = skinCount / total.toFloat()
        val warmRatio = warmCount / total.toFloat()
        return ((skinRatio * 0.8f) + (warmRatio * 0.2f)).coerceIn(0f, 1f)
    }

    private fun isSkin(r: Int, g: Int, b: Int): Boolean {
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        return r > 95 && g > 40 && b > 20 && delta > 15 &&
            kotlin.math.abs(r - g) > 15 && r > g && r > b
    }
}
