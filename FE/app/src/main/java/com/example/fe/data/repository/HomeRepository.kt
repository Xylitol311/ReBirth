package com.example.fe.data.repository

import android.util.Log
import com.example.fe.data.model.PreBenefitFeedbackResponse
import com.example.fe.data.model.SummaryResponse
import com.example.fe.data.model.UserInfoResponse
import com.example.fe.data.model.cardRecommend.ApiResponse
import com.example.fe.data.model.cardRecommend.Top3ForAllResponse
import com.example.fe.data.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.fe.ui.screens.onboard.viewmodel.dataStore

class HomeRepository {
    private val TAG = "HomeRepository"
    private val apiService = NetworkClient.homeApiService
    private val recommendApiService = NetworkClient.cardRecommendApiService

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

    suspend fun getTop3ForAll(): Result<ApiResponse<Top3ForAllResponse>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "getTop3ForAll 호출")
            val response = recommendApiService.getTop3ForAll()
            Log.d(TAG, "getTop3ForAll 응답: $response")
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "getTop3ForAll 오류: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getUserInfo(): Result<UserInfoResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "getUserInfo 호출")
            val response = apiService.getUserInfo()
            Log.d(TAG, "getUserInfo 응답: $response")
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "getUserInfo 오류: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getUserName(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userInfo = apiService.getUserInfo()
            Result.success(userInfo.name)
        } catch (e: Exception) {
            Log.e(TAG, "사용자 이름 가져오기 실패", e)
            Result.failure(e)
        }
    }
} 