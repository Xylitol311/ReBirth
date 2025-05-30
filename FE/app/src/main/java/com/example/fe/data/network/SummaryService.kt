package com.example.fe.data.network

import com.example.fe.data.model.PreBenefitFeedbackResponse
import com.example.fe.data.model.SummaryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SummaryService {
    @GET("api/main/summary")
    suspend fun getSummary(): SummaryResponse

    @GET("api/main/summary/card")
    suspend fun getSummaryCard(): SummaryCardResponse

    @GET("api/main/summary/category")
    suspend fun getSummaryCategory(): SummaryCategoryResponse
    
    @GET("api/main/prebenefit")
    suspend fun getPreBenefitFeedback(): PreBenefitFeedbackResponse
}

data class SummaryCardResponse(
    val success: Boolean,
    val message: String,
    val data: List<CardSummary>
)

data class CardSummary(
    val cardName: String,
    val cardImgUrl: String,
    val spendingAmount: Int,
    val benefitAmount: Int,
    val annualFee: Int
)

data class SummaryCategoryResponse(
    val success: Boolean,
    val message: String,
    val data: List<CategorySummary>
)

data class CategorySummary(
    val category: String,
    val amount: Int,
    val benefit: Int
) 