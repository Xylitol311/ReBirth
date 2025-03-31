package com.example.fe.data.repository

import android.util.Log
import com.example.fe.config.AppConfig
import com.example.fe.data.model.payment.*
import com.example.fe.data.network.api.PaymentApiService
import com.example.fe.data.network.client.PaymentSseClient
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
                    Result.success(apiResponse.data)
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
}