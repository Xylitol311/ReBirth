package com.example.fe.data.repository

import com.example.fe.data.model.auth.ApiResponseDTO
import com.example.fe.data.model.auth.PatternNumbersRequest
import com.example.fe.data.model.auth.SignupRequest
import com.example.fe.data.model.auth.userLoginRequest
import com.example.fe.data.network.NetworkClient
import retrofit2.Response

class AuthRepository {
    private val authApiService = NetworkClient.authApiService

    suspend fun signUp(signupRequest: SignupRequest): Result<Unit> {
        return try {
            val response = authApiService.signup(signupRequest)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // token 제거 - Interceptor가 자동으로 처리함
    suspend fun registPattern(patternNumbers: String): Result<Unit> {
        return try {
            val response = authApiService.registPattern(PatternNumbersRequest(patternNumbers))
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadAllMyData(): Result<Unit> {
        return try {
            val response = authApiService.loadAllMyData()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateReportFromMyData(userId: Int): Result<Unit> {
        return try {
            val response = authApiService.generateReportFromMyData(userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(request: userLoginRequest): Result<Unit> {
        return try {
            val response = authApiService.login(request)
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 공통 응답 처리 함수
    private fun <T> handleResponse(response: Response<ApiResponseDTO<T>>): Result<Unit> {
        return if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(apiResponse?.message ?: "Unknown error"))
            }
        } else {
            Result.failure(Exception("Network request failed: ${response.code()}"))
        }
    }
}