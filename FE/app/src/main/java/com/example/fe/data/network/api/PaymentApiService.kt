package com.example.fe.data.network.api

import com.example.fe.data.model.payment.ApiResponse
import com.example.fe.data.model.payment.TokenInfo
import com.example.fe.data.model.payment.PaymentEvent
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PaymentApiService {
    
    @GET("api/payment/disposabletoken")
    suspend fun getPaymentToken(@Query("userId") userId: String): Response<ApiResponse<List<TokenInfo>>>
    
    @GET("api/payment/events")
    suspend fun getPaymentEvents(@Query("userId") userId: String): Response<ApiResponse<List<PaymentEvent>>>
}