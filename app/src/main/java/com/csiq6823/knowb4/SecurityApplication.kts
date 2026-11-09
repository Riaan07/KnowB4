package com.csiq6823.knowb4

import android.app.Application
import com.csiq6823.knowb4.api.ThreatIntelligenceApi
import com.csiq6823.knowb4.data.SecurityAppDatabase
import com.csiq6823.knowb4.data.ThreatRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class SecurityApplication : Application() {
    private val database by lazy { SecurityAppDatabase.getDatabase(this) }
    val repository by lazy {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://safebrowsing.googleapis.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .build()
        ThreatRepository(
            database.threatScanDao(),
            retrofit.create(ThreatIntelligenceApi::class.java),
            BuildConfig.SAFE_BROWSING_API_KEY
        )
    }
}


