package com.csiq6823.knowb4.api

data class SafeBrowsingRequest(
    val client: ClientInfo = ClientInfo(),
    val threatInfo: ThreatInfo
)

data class ClientInfo(
    val clientId: String = "csiq6823-knowb4",
    val clientVersion: String = "1.0"
)

data class ThreatInfo(
    val threatTypes: List<String> = listOf(
        "malware",
        "POTENTIALLY HARMFUL APPLICATION",
        "SOCIAL ENGINEERING",
        "UNWANTED SOFTWARE",
    ),
    val platformTypes: List<String> = listOf("ANY_PLATFORM"),
    val threatEntryTypes: List<String> = listOf("URL"),
    val threatEntries: List<ThreatEntry>
)

data class ThreatEntry(
    val url: String
)
data class SafeBrowsingResponse(
    val matches: List<ThreatMatch>? = null
)

data class ThreatMatch(
    val threatType: String? = null,
    val platformType: String? = null,
    val threatEntryType: String? = null,
    val threat: ThreatEntry? = null,
    val cacheDuration: String? = null
)

