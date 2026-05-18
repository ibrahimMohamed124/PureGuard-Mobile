package com.pureguard.mobile.domain.engine

import android.net.Uri
import java.util.Locale

class SafeSearchRewriter {

    fun rewrite(url: String): String {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return url
        val host = uri.host?.lowercase(Locale.US) ?: return url
        val path = uri.path.orEmpty().lowercase(Locale.US)

        val builder = uri.buildUpon().clearQuery()
        val params = uri.queryParameterNames.associateWith { name -> uri.getQueryParameter(name).orEmpty() }.toMutableMap()

        when {
            host.contains("google.") && path.startsWith("/search") -> {
                params["safe"] = "active"
            }

            host.endsWith("bing.com") && path.startsWith("/search") -> {
                params["adlt"] = "strict"
            }

            host.endsWith("duckduckgo.com") -> {
                params["safesearch"] = "strict"
                params["kp"] = "1"
            }

            host.endsWith("search.yahoo.com") || (host.endsWith("yahoo.com") && path.startsWith("/search")) -> {
                params["family"] = "yes"
                params["fr"] = "yfp-t"
                params["vm"] = "r"
            }

            host.endsWith("yandex.com") && path.startsWith("/search") -> {
                params["family"] = "yes"
                params["fyandex"] = "1"
            }

            host.endsWith("brave.com") && path.startsWith("/search") -> {
                params["safe_search"] = "1"
            }

            host.endsWith("startpage.com") && path.contains("/do/search") -> {
                params["safesearch"] = "1"
            }
        }

        params.forEach { (k, v) -> builder.appendQueryParameter(k, v) }
        return builder.build().toString()
    }
}