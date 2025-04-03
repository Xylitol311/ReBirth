package com.example.fe.data.repository

import android.util.Log
import com.example.fe.config.AppConfig
import com.example.fe.data.model.payment.*
import com.example.fe.data.network.api.PaymentApiService
import com.example.fe.data.network.api.QRTokenRequest
import com.example.fe.data.network.client.PaymentSseClient
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class PaymentRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.Server.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val paymentApiService = retrofit.create(PaymentApiService::class.java)
    private val paymentSseClient = PaymentSseClient()
    
    // 결제 토큰 요청
    suspend fun getPaymentTokens(userId: String): Result<List<TokenInfo>> {
        Log.d("PaymentRepository", "getPaymentTokens 시작: userId=$userId")
        
        return try {
            val response = paymentApiService.getPaymentToken(userId)
            Log.d("PaymentRepository", "API 응답: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Log.d("PaymentRepository", "API 응답 성공: $apiResponse")
                
                if (apiResponse.success) {
                    // API 응답에서 data가 null일 때 빈 리스트 반환
                    if (apiResponse.data == null) {
                        Log.e("PaymentRepository", "API response data is null, returning empty list")
                        Result.success(emptyList())
                    } else {
                        Result.success(apiResponse.data)
                    }
                } else {
                    Log.e("PaymentRepository", "API 오류: ${apiResponse.message}")
                    Result.failure(Exception("API error: ${apiResponse.message}"))
                }
            } else {
                Log.e("PaymentRepository", "API 요청 실패: ${response.code()}, ${response.errorBody()?.string()}")
                Result.failure(Exception("Failed to get payment token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PaymentRepository", "getPaymentTokens 예외 발생", e)
            Result.failure(e)
        }
    }

    // SSE 연결 및 이벤트 수신
    fun connectToPaymentEvents(userId: String): Flow<PaymentEvent> {
        Log.e("PaymentRepository", "Connecting to payment events. UserId: $userId")
        return paymentSseClient.connectToPaymentEvents(userId)
    }
    
    // SSE 연결 종료
    fun disconnectFromPaymentEvents() {
        paymentSseClient.disconnect()
    }

    // QR 토큰 전송
    suspend fun sendQRToken(tokenRequest: QRTokenRequest): Response<ApiResponse<QRPaymentResponse>> {
        Log.d("PaymentRepository", "QR 토큰 전송 시작: $tokenRequest")
        return try {
            val response = paymentApiService.sendQRToken(tokenRequest)
            Log.d("PaymentRepository", "QR 토큰 전송 응답: ${response.code()}")
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Log.d("PaymentRepository", "QR 토큰 전송 성공: $apiResponse")
            }
            response // 응답 반환 (중복 호출 제거)
        } catch (e: Exception) {
            Log.e("PaymentRepository", "QR 토큰 전송 실패", e)
            throw Exception("Failed to send QR token", e)
        }
    }

    // 결제 완료 요청
    suspend fun completePayment(token: String): Response<ApiResponse<PaymentResult>> {
        Log.d("PaymentRepository", "결제 완료 요청 시작: $token")
        return try {
            // 토큰을 raw 문자열로 변환
            val requestBody = token.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val response = paymentApiService.completePayment(requestBody)
            Log.d("PaymentRepository", "결제 완료 응답: ${response.code()}")
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Log.d("PaymentRepository", "결제 완료 성공: $apiResponse")
            }
            response
        } catch (e: Exception) {
            Log.e("PaymentRepository", "결제 완료 실패", e)
            throw Exception("Failed to complete payment", e)
        }
    }
}