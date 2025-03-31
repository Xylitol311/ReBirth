package com.example.fe.data.model.payment

// SSE 상태 열거형

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


// 얼마결제됐는지
// 가맹점
// 결제 번호
// 결제 승인 내역 : status
// 결제 시간
// 한번 결제 했을 때 혜택 얼마나 봤는지?

// API 응답 래퍼 클래스
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T
)
