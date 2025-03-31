package com.example.fe.ui.screens.payment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fe.data.model.payment.PaymentStatus
import com.example.fe.data.model.payment.TokenInfo
import com.example.fe.data.repository.PaymentRepository
import com.example.fe.data.model.payment.PaymentEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {
    init {
        Log.d("PaymentViewModel", "PaymentViewModel 초기화됨")
    }
    
    private val paymentRepository = PaymentRepository()
    
    // 결제 상태 Flow
    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState
    
    // 모든 카드의 토큰 정보
    private val _cardTokens = MutableStateFlow<List<TokenInfo>>(emptyList())
    val cardTokens: StateFlow<List<TokenInfo>> = _cardTokens
    
    // 현재 선택된 카드 ID
    private val _selectedCardId = MutableStateFlow<String?>(null)
    val selectedCardId: StateFlow<String?> = _selectedCardId
    
    // 사용자 ID (실제 앱에서는 로그인 정보에서 가져와야 함)
    private val userId = "1" // 임시 사용자 ID
    
    // 결제 화면 진입 시 호출 - 모든 카드의 토큰 요청 및 SSE 연결 시작
    fun initializePaymentProcess() {
        Log.e("PaymentViewModel", "initializePaymentProcess STARTED")
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading

            Log.e("PaymentViewModel", "initializePaymentProcess getPaymentTokens")
            paymentRepository.getPaymentTokens(userId)
                .onSuccess { tokens ->
                    Log.e("PaymentViewModel", "initializePaymentProcess getPaymentTokens success")
                    _cardTokens.value = tokens
                    _paymentState.value = PaymentState.TokensReceived(tokens)
                    
                    // 토큰을 받은 후 바로 SSE 연결 시작
                    if (tokens.isNotEmpty()) {
                        // SSE 연결 시작 (userId 사용)
                         Log.e("PaymentViewModel", "Starting SSE connection with userId: $userId")
                         connectToPaymentEvents(userId)
                    }
                }
                .onFailure { error ->
                    Log.e("PaymentViewModel", "initializePaymentProcess getPaymentTokens failure: ${error.message}")
                    _paymentState.value = PaymentState.Error("토큰 요청 실패: ${error.message}")
                }
        }
    }
    
    // 카드 선택 시 호출 - 기존 SSE 연결 종료 후 새 카드로 SSE 연결 시작
    fun selectCard(cardId: String) {

        // 기존 SSE 연결 종료
//        paymentRepository.disconnectFromPaymentEvents()

        _selectedCardId.value = cardId

        // 선택된 카드의 토큰 찾기

//        if (selectedToken != null) {
//            // 카드 선택 시 바로 SSE 연결 시작
//            Log.e("PaymentViewModel", "Starting SSE connection for selected card with token: $selectedToken")
//            connectToPaymentEvents(selectedToken)
//        }
    }
    
    // SSE 연결 시작
    private fun connectToPaymentEvents(userId: String) {
        viewModelScope.launch {
            paymentRepository.connectToPaymentEvents(userId)
                .catch { e ->
                    Log.e("PaymentViewModel", "Error in SSE connection: ${e.message}")
                    _paymentState.value = PaymentState.Error("SSE 연결 오류: ${e.message}")
                }
                .collect { event ->
                    Log.e("PaymentViewModel", "Received payment event: $event")
                    
                    // 이벤트 타입과 메시지 내용에 따라 상태 업데이트
                    when (event.eventType) {
                        "결제이벤트" -> {
                            Log.d("PaymentViewModel", "처리: 결제 이벤트 - ${event.message}")
                            _paymentState.value = PaymentState.Ready
                        }
                        "결제중" -> {
                            Log.d("PaymentViewModel", "처리: 결제 진행 중 - ${event.message}")
                            when {
                                event.message?.contains("결제시작") == true -> {
                                    _paymentState.value = PaymentState.Processing
                                }
                                event.message?.startsWith("TXN") == true -> {
                                    // 트랜잭션 ID가 포함된 메시지는 결제 완료로 처리
                                    _paymentState.value = PaymentState.Completed
                                }
                                event.message?.contains("실패") == true -> {
                                    _paymentState.value = PaymentState.Failed(event.message)
                                }
                                event.message?.contains("취소") == true -> {
                                    _paymentState.value = PaymentState.Cancelled(event.message)
                                }
                            }
                        }
                        else -> {
                            // 이벤트 타입이 없는 경우 메시지 내용으로 판단
                            Log.d("PaymentViewModel", "처리: 기타 이벤트 - ${event.message}")
                            when {
                                event.message?.contains("연결") == true -> {
                                    _paymentState.value = PaymentState.Ready
                                }
                                event.message?.contains("처리 중") == true -> {
                                    _paymentState.value = PaymentState.Processing
                                }
                                event.message?.contains("완료") == true || 
                                event.message?.startsWith("TXN") == true -> {
                                    _paymentState.value = PaymentState.Completed
                                }
                                event.message?.contains("실패") == true -> {
                                    _paymentState.value = PaymentState.Failed(event.message)
                                }
                                event.message?.contains("취소") == true -> {
                                    _paymentState.value = PaymentState.Cancelled(event.message)
                                }
                                event.message?.contains("만료") == true -> {
                                    _paymentState.value = PaymentState.Expired
                                }
                                event.message?.contains("오류") == true -> {
                                    _paymentState.value = PaymentState.Error(event.message)
                                }
                            }
                        }
                    }
                    
                    // 상태 변경 로그
                    Log.d("PaymentViewModel", "Payment state updated to: ${_paymentState.value}")
                }
        }
    }
    
    // 토큰 갱신 요청
    fun refreshTokens() {
        initializePaymentProcess()
    }
    
    // 결제 프로세스 종료
    fun stopPaymentProcess() {
        paymentRepository.disconnectFromPaymentEvents()
        _paymentState.value = PaymentState.Idle
    }
    
    // 화면 이탈 시 리소스 정리
    override fun onCleared() {
        super.onCleared()
        paymentRepository.disconnectFromPaymentEvents()
    }
    
    // 결제 상태를 나타내는 sealed class
    sealed class PaymentState {
        object Idle : PaymentState()
        object Loading : PaymentState()
        data class TokensReceived(val tokens: List<TokenInfo>) : PaymentState()
        object Ready : PaymentState()
        object Processing : PaymentState()
        object Completed : PaymentState()
        data class Failed(val reason: String) : PaymentState()
        data class Cancelled(val reason: String) : PaymentState()
        object Expired : PaymentState()
        data class Error(val message: String) : PaymentState()
    }
    
    // 특정 카드의 토큰 가져오기
    fun getTokenForCard(cardId: String): String? {
        // 카드 ID 매핑 (앱 ID -> 서버 ID)
        val serverCardId = when(cardId) {
            "card1" -> "tes_card_unique_number"
            "card2" -> "000"
            else -> cardId
        }
        
        // 매핑된 ID로 토큰 찾기
        val token = _cardTokens.value.find { it.cardId == serverCardId }?.token
        
//        Log.e("PaymentViewModel", "getTokenForCard: cardId=$cardId, serverCardId=$serverCardId, token=$token")
        
        return token
    }
} 