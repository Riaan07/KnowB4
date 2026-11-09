package com.csiq6823.knowb4.data

object UrlTools {
    private val URL_REGEX = Regex(
        "(?i)\\bhttps?://[^\\s<>\\\"'\\u201c\\u201d]+"
    )

    fun extractFirstUrl(text: String): String? = URL_REGEX.find(text)?.value
        ?.trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}')
        ?.takeIf { candidate ->
            val host = candidate.substringAfter("://").substringBefore('/').substringBefore('?')
                .substringBefore('#').substringBefore(':')
            host.contains('.') && !host.startsWith('.') && !host.endsWith('.')
        }

    fun upgradeToHttps(url: String): String =
        if (url.startsWith("http://", ignoreCase = true)) "https://" + url.substring(7) else url

    fun isUrlOnly(text: String): Boolean {
        val cleaned = text.trim()
        return extractFirstUrl(cleaned) == cleaned
    }
}


