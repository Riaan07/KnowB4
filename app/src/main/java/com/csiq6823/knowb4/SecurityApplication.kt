package com.csiq6823.knowb4

import android.app.Application
import com.csiq6823.knowb4.Data.SecurityAppDatabase
import com.csiq6823.knowb4.Data.ThreatRepository
import com.csiq6823.knowb4.Workers.RescanWorker

class SecurityApplication : Application() {

    private val database by lazy { SecurityAppDatabase.getDatabase(this) }

    val repository by lazy {
        ThreatRepository(database.threatScanDao())
    }

    override fun onCreate() {
        super.onCreate()
        RescanWorker.schedule(this)
    }
}
