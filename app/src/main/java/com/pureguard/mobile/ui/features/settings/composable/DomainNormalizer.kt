package com.pureguard.mobile.ui.features.settings.composable

import java.util.Locale

fun parseDomains(text: String): List<String> =
    text.split(Regex("[\\n,;]+"))
        .mapNotNull { normalizeDomainCandidate(it) }
        .distinct()

fun normalizeDomainCandidate(rawInput: String): String? {
    val cleaned = rawInput.trim().lowercase(Locale.US)
        .removePrefix("https://")
        .removePrefix("http://")
        .removePrefix("www.")
        .removePrefix("*.")
        .removePrefix(".")
        .substringBefore("/").substringBefore("?").substringBefore("#")
        .substringBefore(":")
        .trim('.')
    if (cleaned.isBlank() || cleaned.length < 3 || !cleaned.contains('.') || cleaned.any { it.isWhitespace() }) return null
    return cleaned
}
