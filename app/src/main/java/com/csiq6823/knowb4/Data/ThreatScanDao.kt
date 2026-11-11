package com.csiq6823.knowb4.Data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreatScanDao {

    @Query("SELECT * FROM threat_scans ORDER BY capturedTimestamp DESC")
    fun getAllScans(): Flow<List<ThreatScan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ThreatScan)

    @Update
    suspend fun updateScan(scan: ThreatScan)

    @Query("SELECT * FROM threat_scans WHERE verdict = :verdict")
    suspend fun getScansByVerdict(verdict: String): List<ThreatScan>

    @Query("DELETE FROM threat_scans")
    suspend fun deleteAll()
}
