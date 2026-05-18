package com.pureguard.mobile.services

internal object BrowserPackageCatalog {
    private val addressBarsByPackage: Map<String, List<String>> = mapOf(
        "com.android.chrome" to listOf("com.android.chrome:id/url_bar"),
        "com.chrome.beta" to listOf("com.chrome.beta:id/url_bar"),
        "com.chrome.dev" to listOf("com.chrome.dev:id/url_bar"),
        "com.brave.browser" to listOf("com.brave.browser:id/url_bar"),
        "com.microsoft.emmx" to listOf("com.microsoft.emmx:id/url_bar"),
        "org.mozilla.firefox" to listOf(
            "org.mozilla.firefox:id/mozac_browser_toolbar_url_view",
            "org.mozilla.firefox:id/search_selector"
        ),
        "org.mozilla.firefox_beta" to listOf(
            "org.mozilla.firefox_beta:id/mozac_browser_toolbar_url_view",
            "org.mozilla.firefox_beta:id/search_selector"
        ),
        "com.sec.android.app.sbrowser" to listOf("com.sec.android.app.sbrowser:id/location_bar_edit_text"),
        "com.opera.browser" to listOf(
            "com.opera.browser:id/url_field",
            "com.opera.browser:id/url_field_custom"
        ),
        "com.duckduckgo.mobile.android" to listOf("com.duckduckgo.mobile.android:id/omnibarTextInput")
    )

    fun looksLikeBrowserPackage(packageName: String): Boolean {
        if (addressBarsByPackage.containsKey(packageName)) return true
        val normalized = packageName.lowercase()
        return normalized.contains("browser") ||
            normalized.contains("chrome") ||
            normalized.contains("firefox") ||
            normalized.contains("opera") ||
            normalized.contains("duckduckgo") ||
            normalized.contains("edge")
    }

    fun knownAddressBars(packageName: String): List<String> = addressBarsByPackage[packageName].orEmpty()
}
