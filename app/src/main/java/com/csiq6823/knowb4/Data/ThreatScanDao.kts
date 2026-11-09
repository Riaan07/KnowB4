package com.csiq6823.knowb4.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreatScanDao {
    @Query("SELECT * FROM threat_scans ORDER BY capturedTimestamp DESC")
    fun getAllScans(): Flow<List<ThreatScan>>

    @Insert
    suspend fun insertScan(scan: ThreatScan)
}


