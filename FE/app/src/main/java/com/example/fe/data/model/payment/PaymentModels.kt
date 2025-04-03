package com.example.fe.data.model.payment

import java.time.LocalDateTime

// 결제 상태 열거형
enum class PaymentStatus {
    READY,          // 결제 준비 상태
    PROCESSING,     // 결제 처리 중
    COMPLETED,      // 결제 완료
    FAILED,         // 결제 실패
    CANCELLED,      // 결제 취소
    EXPIRED         // 토큰 만료
}

// 결제 이벤트 데이터 클래스
data class PaymentEvent(
    val status: PaymentStatus,
    val message: String? = null,
    val transactionId: String? = null,
    val amount: Double? = null,
    val merchantName: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)


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
    val data: T? = null
)

// 토큰 정보 클래스
data class TokenInfo(
    val token: String,
    val cardId: String
)

// 결제 정보 클래스 (UI 표시용)
data class PaymentInfo(
    val cardId: String,
    val amount: Double? = null,
    val merchantName: String? = null,
    val transactionId: String? = null
)