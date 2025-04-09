package com.example.fe.data.network.api

import com.example.fe.data.model.ApiResponse
import com.example.fe.data.model.CardSummary
import com.example.fe.data.model.CategorySummary
import retrofit2.http.GET

interface SummaryService {
    @GET("api/main/summary/card")
    suspend fun getSummaryCard(): ApiResponse<List<CardSummary>>

    @GET("api/main/summary/category")
    suspend fun getSummaryCategory(): ApiResponse<List<CategorySummary>>
} 