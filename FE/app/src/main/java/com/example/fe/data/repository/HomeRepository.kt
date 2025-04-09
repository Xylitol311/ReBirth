package com.example.fe.data.repository

import android.util.Log
import com.example.fe.data.model.PreBenefitFeedbackResponse
import com.example.fe.data.model.SummaryResponse
import com.example.fe.data.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeRepository {
    private val TAG = "HomeRepository"
    private val apiService = NetworkClient.homeApiService

    suspend fun getSummary(): Result<SummaryResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "getSummary 호출")
            val response = apiService.getSummary()
            Log.d(TAG, "getSummary 응답: $response")
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "getSummary 오류: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getPreBenefitFeedback(): Result<PreBenefitFeedbackResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "getPreBenefitFeedback 호출")
            val response = apiService.getPreBenefitFeedback()
            Log.d(TAG, "getPreBenefitFeedback 응답: $response")
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "getPreBenefitFeedback 오류: ${e.message}")
            Result.failure(e)
        }
    }
} 