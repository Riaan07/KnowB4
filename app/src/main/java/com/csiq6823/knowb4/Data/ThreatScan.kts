package com.csiq6823.knowb4.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// TODO: STUDENT GAP (Sub-Task 1.1)
// The Room entity is enabled so the starter database can compile.
@Entity(tableName = "threat_scans")
data class ThreatScan(
    // TODO: STUDENT GAP (Sub-Task 1.1)
    // Room requires a primary key; extend this as part of Sub-Task 1.1.
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val sharedMessage: String,
    val verdict: String,
    val capturedTimestamp: Long = System.currentTimeMillis(),
    val sourceApp: String
)

