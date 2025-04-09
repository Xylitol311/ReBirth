package com.example.fe.data.network.api

import com.example.fe.data.model.auth.ApiResponseDTO
import com.example.fe.data.model.auth.SendSmsRequest
import com.example.fe.data.model.payment.ApiResponse
import com.example.fe.data.model.payment.CardRegistrationRequest
import com.example.fe.data.model.payment.TokenInfo
import com.example.fe.data.model.payment.QRPaymentResponse
import com.example.fe.data.model.payment.PaymentResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Body
import okhttp3.RequestBody

interface PaymentApiService {
    
    @GET("api/payment/disposabletoken")
    suspend fun getPaymentToken(): Response<ApiResponse<List<TokenInfo>>>

    @POST("api/payment/onlinedisposabletoken")
    suspend fun sendQRToken(@Body tokenRequest: QRTokenRequest): Response<ApiResponse<QRPaymentResponse>>

    @POST("api/payment/onlineprogresspay")
    suspend fun completePayment(@Body requestBody: RequestBody): Response<ApiResponse<PaymentResult>>

    @POST("api/payment/registpaymentcard")
    suspend fun registerPaymentCard(@Body request: CardRegistrationRequest): Response<ApiResponse<Unit>>
}

// QR 토큰 요청 데이터 클래스
data class QRTokenRequest(
    val token: String,
    val userId: String
)