package com.csiq6823.knowb4.Data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// TODO: STUDENT GAP (Sub-Task 1.1)
// Annotate this class as a Room Entity named "threat_scans"
@Entity(tableName = "threat_scans")
data class ThreatScan(
    // TODO: STUDENT GAP (Sub-Task 1.1)
    // Add the PrimaryKey annotation here
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val sharedMessage: String,
    val verdict: String,
    val capturedTimestamp: Long = System.currentTimeMillis(),
    val sourceApp: String
) {

    companion object {
        const val SAFE = "SAFE"
        const val PHISHING = "PHISHING"
        const val SPAM = "SPAM"
        const val MALWARE = "MALWARE"
        const val UNKNOWN = "UNKNOWN"

        private val THREATS = setOf(PHISHING, SPAM, MALWARE)

        fun isThreat(verdict: String): Boolean = verdict in THREATS
    }
}