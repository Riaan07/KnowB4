package com.csiq6823.knowb4.Data

import com.csiq6823.knowb4.api.ThreatMatch
import kotlinx.coroutines.flow.Flow
import java.util.regex.Pattern

class ThreatRepository(private val dao: ThreatScanDao) {
    val allScans: Flow<List<ThreatScan>> = dao.getAllScans()

    suspend fun processAndInsertScan(rawText: String, sourceApp: String) {
        val extractedUrl = extractUrlFromText(rawText)

        if (extractedUrl == null) {
            dao.insertScan(ThreatScan(sharedMessage = rawText, verdict = ThreatScan.SAFE, sourceApp = sourceApp))
            return
        }

        val finalizedUrl = upgradeToHttps(extractedUrl)
        val verdict = evaluateVerdict(finalizedUrl)

        dao.insertScan(ThreatScan(sharedMessage = rawText, verdict = verdict, sourceApp = sourceApp))
    }

    suspend fun rescanUnknownScans(): Int {
        var stillUnknown = 0
        for (scan in dao.getScansByVerdict(ThreatScan.UNKNOWN)) {
            val url = extractUrlFromText(scan.sharedMessage)
            if (url == null) {
                dao.updateScan(scan.copy(verdict = ThreatScan.SAFE))
                continue
            }
            val verdict = evaluateVerdict(upgradeToHttps(url))
            dao.updateScan(scan.copy(verdict = verdict))
        }
        return stillUnknown
    }

    suspend fun clearHistory() = dao.deleteAll()

    private fun evaluateVerdict(url: String): String {
        return if (url.contains("malware", ignoreCase = true) || url.contains("phish", ignoreCase = true) || url.contains("bad", ignoreCase = true)) {
            ThreatScan.PHISHING
        } else {
            ThreatScan.SAFE
        }
    }

    private fun extractUrlFromText(text: String): String? {
        return extractFirstUrl(text)
    }

    private fun upgradeToHttps(url: String): String {
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
            val first = matches.first()
            return when (first.threatType) {
                "SOCIAL_ENGINEERING" -> ThreatScan.PHISHING
                "MALWARE" -> ThreatScan.MALWARE
                "UNWANTED_SOFTWARE" -> ThreatScan.SPAM
                else -> ThreatScan.SAFE
            }
        }
    }
}
