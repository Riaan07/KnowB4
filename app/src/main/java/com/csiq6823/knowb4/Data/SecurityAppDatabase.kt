package com.csiq6823.knowb4.Data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ThreatScan::class], version = 1, exportSchema = false)
abstract class SecurityAppDatabase : RoomDatabase() {

    abstract fun threatScanDao(): ThreatScanDao

    companion object {
        @Volatile
        private var INSTANCE: SecurityAppDatabase? = null

        fun getDatabase(context: Context): SecurityAppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SecurityAppDatabase::class.java,
                    "know_b4_security.db"
                ).build().also { INSTANCE = it }
            }
    }
}