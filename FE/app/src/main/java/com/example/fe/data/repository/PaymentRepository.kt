package com.example.fe.data.repository

import android.util.Log
import com.example.fe.data.model.payment.ApiResponse
import com.example.fe.data.model.payment.CardRegistrationRequest
import com.example.fe.data.model.payment.PaymentEvent
import com.example.fe.data.model.payment.PaymentResult
import com.example.fe.data.model.payment.QRPaymentResponse
import com.example.fe.data.model.payment.TokenInfo
import com.example.fe.data.network.NetworkClient
import com.example.fe.data.network.api.QRTokenRequest
import com.example.fe.data.network.client.PaymentSseClient
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class PaymentRepository {

    private val paymentApiService = NetworkClient.paymentApiService
    private val paymentSseClient = PaymentSseClient()
    
    // 결제 토큰 요청
    suspend fun getPaymentTokens(): Result<List<TokenInfo>> {
        Log.d("PaymentRepository", "getPaymentTokens 시작")
        
        return try {
            val response = paymentApiService.getPaymentToken()
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
    fun connectToPaymentEvents(): Flow<PaymentEvent> {
        Log.e("PaymentRepository", "Connecting to payment events.")
        return paymentSseClient.connectToPaymentEvents()
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

    suspend fun registerPaymentCard(cardNumber: String, password: String, cvc: String): Result<Unit> {
        return try {
            val request = CardRegistrationRequest(
                userCI = null, // 요청에 따라 null로 보내기
                cardNumber = cardNumber,
                password = password,
                cvc = cvc
            )

            val response = paymentApiService.registerPaymentCard(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "카드 등록에 실패했습니다"))
            }
        } catch (e: Exception) {
            Log.e("PaymentRepository", "카드 등록 중 오류 발생", e)
            Result.failure(e)
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