package com.csiq6823.knowb4

import com.csiq6823.knowb4.Data.ThreatRepository
import com.csiq6823.knowb4.Data.ThreatScan
import org.junit.Test
import com.csiq6823.knowb4.api.ThreatMatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue


class ThreatLogicTest {


    @Test fun extractsUrlFromMessyText() {
        assertEquals(
            "http://testsafebrowsing.appspot.com/s/phishing.html",
            ThreatRepository.extractFirstUrl("URGENT!! verify here: http://testsafebrowsing.appspot.com/s/phishing.html now")
        )
    }

    @Test fun stripsTrailingSentencePunctuation() {
        assertEquals("https://x.com/a/status/1", ThreatRepository.extractFirstUrl("look (https://x.com/a/status/1)."))
    }

    @Test fun keepsBalancedParenthesesInsideUrl() {
        assertEquals(
            "https://en.wikipedia.org/wiki/Java_(programming_language)",
            ThreatRepository.extractFirstUrl("see https://en.wikipedia.org/wiki/Java_(programming_language)!")
        )
    }

    @Test fun returnsFirstOfSeveralUrls() {
        assertEquals("https://a.com", ThreatRepository.extractFirstUrl("https://a.com and https://b.com"))
    }

    @Test fun findsWwwLinks() {
        assertEquals("www.example.com/login", ThreatRepository.extractFirstUrl("go to www.example.com/login"))
    }

    @Test fun returnsNullWhenNoUrl() {
        assertNull(ThreatRepository.extractFirstUrl("hey, are we still on for lunch?"))
    }

    //Sub-Task 1.3
    @Test fun upgradesHttpToHttps() {
        assertEquals("https://example.com/x", ThreatRepository.enforceHttps("http://example.com/x"))
        assertEquals("https://example.com", ThreatRepository.enforceHttps("HTTP://example.com"))
    }

    @Test fun leavesHttpsAlone() {
        assertEquals("https://example.com", ThreatRepository.enforceHttps("https://example.com"))
    }

    @Test fun addsSchemeWhenMissing() {
        assertEquals("https://www.example.com", ThreatRepository.enforceHttps("www.example.com"))
    }

    @Test fun mapsVerdicts() {
        assertEquals(ThreatScan.SAFE, ThreatRepository.mapMatchesToVerdict(null))
        assertEquals(ThreatScan.SAFE, ThreatRepository.mapMatchesToVerdict(emptyList()))
        assertEquals(ThreatScan.PHISHING, ThreatRepository.mapMatchesToVerdict(listOf(ThreatMatch(threatType = "SOCIAL_ENGINEERING"))))
        assertEquals(ThreatScan.MALWARE, ThreatRepository.mapMatchesToVerdict(listOf(ThreatMatch(threatType = "MALWARE"))))
        assertEquals(ThreatScan.SPAM, ThreatRepository.mapMatchesToVerdict(listOf(ThreatMatch(threatType = "UNWANTED_SOFTWARE"))))
    }

    @Test fun mapsKnownPackages() {
        assertEquals("X", MainActivity.mapPackageToSource("com.twitter.android"))
        assertEquals("Reddit", MainActivity.mapPackageToSource("com.reddit.frontpage"))
        assertEquals("SMS", MainActivity.mapPackageToSource("com.google.android.apps.messaging"))
        assertNull(MainActivity.mapPackageToSource("com.example.other"))
    }

    @Test fun detectsUrlOnlyPayload() {
        assertTrue(MainActivity.isUrlOnly("https://bit.ly/3xYz"))
        assertTrue(MainActivity.isUrlOnly("  https://bit.ly/3xYz  "))
        assertFalse(MainActivity.isUrlOnly("Your parcel is waiting https://bit.ly/3xYz"))
        assertFalse(MainActivity.isUrlOnly("no link here"))
    }
}
