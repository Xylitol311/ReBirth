package com.example.fe.data.model.payment

import java.util.Date

// SSE 이벤트 데이터 클래스
data class PaymentEvent(
    val message: String? = null,
    val eventType: String? = null,
    val eventId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun toString(): String {
        return "PaymentEvent(type=$eventType, id=$eventId, message=$message)"
    }
}

// API 응답 래퍼 클래스
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T
)

data class QRPaymentResponse(
    val merchantName: String,
    val amount: Int,
    val paymentTokenResponseDTO: List<TokenInfo>
)

data class PaymentResult(
    val amount: Int,
    val createdAt: String,
    val approvalCode: String
) 