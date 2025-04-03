package com.example.fe.data.repository

import android.util.Log
import com.example.fe.config.AppConfig
import com.example.fe.data.model.payment.*
import com.example.fe.data.network.api.PaymentApiService
import com.example.fe.data.network.client.PaymentSseClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.random.Random

class PaymentRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.Server.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val paymentApiService = retrofit.create(PaymentApiService::class.java)
    private val paymentSseClient = PaymentSseClient()
    
    // 결제 토큰 요청
    suspend fun getPaymentTokens(userId: String): Result<List<TokenInfo>> {
        Log.e("PaymentRepository", "getPaymentTokens STARTED")
        return try {
            val response = paymentApiService.getPaymentToken(userId)
            Log.e("PaymentRepository", "getPaymentTokens response: ${response.body()}")

            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    // API 응답에서 data가 null일 때 빈 리스트 반환
                    if (apiResponse.data == null) {
                        Log.e("PaymentRepository", "API response data is null, returning empty list")
                        Result.success(emptyList())
                    } else {
                        Result.success(apiResponse.data)
                    }
                } else {
                    Log.e("PaymentRepository", "API error: ${apiResponse.message}")
                    Result.failure(Exception("API error: ${apiResponse.message}"))
                }
            } else {
                Log.e("PaymentRepository", "Failed to get payment token: ${response.code()}")
                Result.failure(Exception("Failed to get payment token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("PaymentRepository", "getPaymentTokens ERROR: ${e.message}", e)
            Result.failure(e)
        }
    }

    // SSE 연결 및 이벤트 수신
    fun connectToPaymentEvents(userId: String, useTestMode: Boolean = AppConfig.App.DEBUG_MODE): Flow<PaymentEvent> {
        Log.e("PaymentRepository", "Connecting to payment events. UserId: $userId, UseTestMode: $useTestMode")
        return if (useTestMode) {
            Log.e("PaymentRepository", "Using test mode for SSE")
            simulatePaymentEvents(userId)
        } else {
            Log.e("PaymentRepository", "Using real SSE connection with userId")
            paymentSseClient.connectToPaymentEvents(userId)
        }
    }
    
    // SSE 연결 종료
    fun disconnectFromPaymentEvents() {
        paymentSseClient.disconnect()
    }
    
    // 테스트용 SSE 이벤트 시뮬레이션
    fun simulatePaymentEvents(paymentToken: String): Flow<PaymentEvent> = flow {
        // 준비 상태 이벤트
        emit(PaymentEvent(PaymentStatus.READY, "결제 준비 완료"))
        delay(2000)
        
        // 처리 중 상태 이벤트
        emit(PaymentEvent(PaymentStatus.PROCESSING, "결제 처리 중"))
        delay(3000)
        
        // 완료 상태 이벤트 (50% 확률로 성공 또는 실패)
        if (Random.nextBoolean()) {
            emit(
                PaymentEvent(
                    status = PaymentStatus.COMPLETED,
                    message = "결제가 완료되었습니다",
                    transactionId = "tx_${System.currentTimeMillis()}",
                    amount = Random.nextDouble(1000.0, 50000.0),
                    merchantName = paymentToken
                )
            )
        } else {
            emit(PaymentEvent(PaymentStatus.FAILED, "결제 처리 중 오류가 발생했습니다"))
        }
    }
}