package com.example.fe.data.repository
import com.example.fe.config.NetworkClient
import com.example.fe.data.model.auth.ApiResponseDTO
import com.example.fe.data.model.auth.SignupRequest
import com.example.fe.data.model.auth.registPatternRequest
import com.example.fe.data.model.auth.userLoginRequest
import com.example.fe.data.network.api.AuthApiService
import retrofit2.Response
import javax.inject.Inject

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

    suspend fun registPattern(token: String, request: registPatternRequest): Result<Unit> {
        return try {
            val response = authApiService.registPattern(token, request)
            handleResponse(response)
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