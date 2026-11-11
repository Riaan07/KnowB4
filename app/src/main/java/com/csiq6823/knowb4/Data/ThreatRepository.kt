package com.csiq6823.knowb4.Data

import com.csiq6823.knowb4.BuildConfig
import com.csiq6823.knowb4.api.SafeBrowsingRequest
import com.csiq6823.knowb4.api.ThreatEntry
import com.csiq6823.knowb4.api.ThreatInfo
import com.csiq6823.knowb4.api.ThreatIntelligenceApi
import com.csiq6823.knowb4.api.ThreatMatch
import kotlinx.coroutines.flow.Flow
import java.util.regex.Pattern

class ThreatRepository(
    private val dao: ThreatScanDao,
    private val api: ThreatIntelligenceApi
) {
    val allScans: Flow<List<ThreatScan>> = dao.getAllScans()
    suspend fun processAndInsertScan(rawText: String, sourceApp: String) {
        val extractedUrl = extractUrlFromText(rawText)

        if (extractedUrl == null) {
            dao.insertScan(ThreatScan(sharedMessage = rawText, verdict = "SAFE", sourceApp = sourceApp))
            return
        }

        val finalizedUrl = upgradeToHttps(extractedUrl)

        // Network call to verify threat intelligence
        var finalVerdict = "SAFE"
        try {
            // Placeholder for network invocation
            // Students will wire the Retrofit lookup parameter checks here

            finalVerdict = lookUpVerdict(finalizedUrl)
        } catch (e: Exception) {
            finalVerdict = "UNKNOWN"
        }

        dao.insertScan(ThreatScan(sharedMessage = rawText, verdict = finalVerdict, sourceApp = sourceApp))
    }

    suspend fun rescanUnknownScans(): Int {
        var stillUnknown = 0
        for (scan in dao.getScansByVerdict(ThreatScan.UNKNOWN)) {
            val url = extractUrlFromText(scan.sharedMessage)
            if (url == null) {
                dao.updateScan(scan.copy(verdict = ThreatScan.SAFE))
                continue
            }
            try {
                dao.updateScan(scan.copy(verdict = lookUpVerdict(upgradeToHttps(url))))
            } catch (e: Exception) {
                stillUnknown++
            }
        }
        return stillUnknown
    }

    suspend fun clearHistory() = dao.deleteAll()

    private suspend fun lookUpVerdict(url: String): String {
        val apiKey = BuildConfig.SAFE_BROWSING_API_KEY
        check(apiKey.isNotBlank()) { "Safe Browsing API key is missing" }
        val request = SafeBrowsingRequest(
            threatInfo = ThreatInfo(threatEntries = listOf(ThreatEntry(url)))
        )
        val response = api.findThreatMatches(apiKey, request)
        return mapMatchesToVerdict(response.matches)
    }

    private fun extractUrlFromText(text: String): String? {
        // TODO: STUDENT GAP (Sub-Task 1.2)
        // Write the matching logic to extract a web link out of a messy message block using Regex
        return extractFirstUrl(text)
    }

    private fun upgradeToHttps(url: String): String {
        // TODO: STUDENT GAP (Sub-Task 1.3)
        // Implement the protocol check. If the string starts with http://, change it to https://
        return enforceHttps(url)
    }

    companion object {
        private val URL_PATTERN: Pattern = Pattern.compile(
            "(?i)\\b((?:https?://|www\\.)[^\\s<>\"'`{}|\\\\^]+)"
        )

        private const val TRAILING_PUNCTUATION = ".,;:!?)]}'\"*"

        fun extractFirstUrl(text: String): String? {
            val matcher = URL_PATTERN.matcher(text)
            if (!matcher.find()) return null
            var url = matcher.group(1) ?: return null
            while (url.isNotEmpty() && url.last() in TRAILING_PUNCTUATION) {
                if (url.last() == ')' && url.count { it == '(' } >= url.count { it == ')' }) break
                url = url.dropLast(1)
            }
            return url.ifEmpty { null }
        }
        fun enforceHttps(url: String): String {
            val trimmed = url.trim()
            return when {
                trimmed.startsWith("http://", ignoreCase = true) -> "https://" + trimmed.substring("http://".length)
                trimmed.startsWith("https://", ignoreCase = true) -> "https://" + trimmed.substring("https://".length)
                else -> "https://$trimmed"
            }
        }
        fun mapMatchesToVerdict(matches: List<ThreatMatch>?): String {
            if (matches.isNullOrEmpty()) return ThreatScan.SAFE
            val types = matches.mapNotNull { it.threatType?.uppercase() }.toSet()
            return when {
                "MALWARE" in types || "POTENTIALLY_HARMFUL_APPLICATION" in types -> ThreatScan.MALWARE
                "SOCIAL_ENGINEERING" in types -> ThreatScan.PHISHING
                "UNWANTED_SOFTWARE" in types -> ThreatScan.SPAM
                else -> ThreatScan.PHISHING
            }
        }
    }
}