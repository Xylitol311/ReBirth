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
    val data: T? = null
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

// 카드 등록 요청 데이터 클래스
data class CardRegistrationRequest(
    val userCI: String? = null,  // 요청에 따라 null로 보내기
    val cardNumber: String,
    val password: String,
    val cvc: String
)