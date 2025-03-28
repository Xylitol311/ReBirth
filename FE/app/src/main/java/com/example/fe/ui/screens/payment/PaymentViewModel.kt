package com.example.fe.ui.screens.payment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fe.config.AppConfig
import com.example.fe.data.model.payment.PaymentEvent
import com.example.fe.data.model.payment.PaymentInfo
import com.example.fe.data.model.payment.PaymentStatus
import com.example.fe.data.model.payment.TokenInfo
import com.example.fe.data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {
    private val paymentRepository = PaymentRepository()
    
    // 결제 상태 Flow
    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState
    
    // 결제 정보
    private val _paymentInfo = MutableStateFlow<PaymentInfo?>(null)
    val paymentInfo: StateFlow<PaymentInfo?> = _paymentInfo
    
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
                        // 첫 번째 카드를 기본 선택
                        val firstCardId = tokens.first().cardId
                        selectCard(firstCardId)
                        
                        // SSE 연결 시작 (userId 사용)
                        // Log.e("PaymentViewModel", "Starting SSE connection with userId: $userId")
                        // connectToPaymentEvents(userId)
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
        Log.e("PaymentViewModel", "Selecting card: $cardId")
        
        // 기존 SSE 연결 종료
        paymentRepository.disconnectFromPaymentEvents()
        
        _selectedCardId.value = cardId
        
        // 선택된 카드에 대한 결제 정보 설정
        _paymentInfo.value = PaymentInfo(cardId)
        
        // 선택된 카드의 토큰 찾기
        val selectedToken = _cardTokens.value.find { it.cardId == cardId }?.token
        
//        if (selectedToken != null) {
//            // 카드 선택 시 바로 SSE 연결 시작
//            Log.e("PaymentViewModel", "Starting SSE connection for selected card with token: $selectedToken")
//            connectToPaymentEvents(selectedToken)
//        }
    }
    
    // 실제 결제 시도 시 호출 (바코드/QR 스캔 시) - 이미 SSE 연결이 되어 있으므로 추가 작업 불필요
    fun startPaymentForSelectedCard() {
        // SSE 연결은 이미 카드 선택 시 시작되었으므로 추가 작업 불필요
        // 필요한 경우 여기서 추가 로직 구현 가능
        Log.e("PaymentViewModel", "Payment started for selected card")
    }
    
    // SSE 연결 및 이벤트 수신
    private fun connectToPaymentEvents(userId: String) {
        viewModelScope.launch {
            paymentRepository.connectToPaymentEvents(userId, AppConfig.App.DEBUG_MODE)
                .catch { error ->
                    _paymentState.value = PaymentState.Error("이벤트 수신 오류: ${error.message}")
                }
                .collect { event ->
                    // 이벤트 상태에 따라 UI 상태 업데이트
                    when (event.status) {
                        PaymentStatus.READY -> {
                            _paymentState.value = PaymentState.Ready
                        }
                        PaymentStatus.PROCESSING -> {
                            _paymentState.value = PaymentState.Processing
                        }
                        PaymentStatus.COMPLETED -> {
                            // 결제 정보 업데이트
                            _paymentInfo.value = _paymentInfo.value?.copy(
                                amount = event.amount,
                                merchantName = event.message,
                                transactionId = event.transactionId
                            )
                            _paymentState.value = PaymentState.Completed
                        }
                        PaymentStatus.FAILED -> {
                            _paymentState.value = PaymentState.Failed(event.message ?: "결제 실패")
                        }
                        PaymentStatus.CANCELLED -> {
                            _paymentState.value = PaymentState.Cancelled(event.message ?: "결제 취소됨")
                        }
                        PaymentStatus.EXPIRED -> {
                            _paymentState.value = PaymentState.Expired
                        }
                    }
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
        
        Log.e("PaymentViewModel", "getTokenForCard: cardId=$cardId, serverCardId=$serverCardId, token=$token")
        
        return token
    }
} 