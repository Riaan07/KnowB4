package com.csiq6823.knowb4.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ThreatIntelligenceApi {

    @POST("v4/threatMatches:find")
    suspend fun findThreatMatches(
        @Query("key") apiKey: String,
        @Body request: SafeBrowsingRequest
    ): SafeBrowsingResponse
}