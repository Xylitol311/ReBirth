package com.example.fe.data.network.api

import com.example.fe.data.model.PreBenefitFeedbackResponse
import com.example.fe.data.model.SummaryResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface HomeApiService {
    @GET("api/main/summary")
    suspend fun getSummary(): SummaryResponse

    @GET("api/main/prebenefit")
    suspend fun getPreBenefitFeedback(): PreBenefitFeedbackResponse
} 