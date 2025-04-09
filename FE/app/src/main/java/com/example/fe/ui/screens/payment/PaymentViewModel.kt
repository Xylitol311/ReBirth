package com.example.fe.ui.screens.payment

import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fe.data.model.payment.PaymentResult
import com.example.fe.data.model.payment.TokenInfo
import com.example.fe.data.network.api.QRTokenRequest
import com.example.fe.data.repository.PaymentRepository
import com.example.fe.ui.screens.onboard.viewmodel.AppTokenProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch


class PaymentViewModel(private val context: Context) : ViewModel() {
    init {
        Log.d("PaymentViewModel", "PaymentViewModel 초기화됨")
    }

    // 팩토리 클래스 추가
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PaymentViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private val paymentRepository = PaymentRepository()
    
    // 결제 상태 Flow
    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState
    
    // 모든 카드의 토큰 정보
    private val _cardTokens = MutableStateFlow<List<TokenInfo>>(emptyList())
    val cardTokens: StateFlow<List<TokenInfo>> = _cardTokens

    // API에서 가져온 카드 정보
    private val _cards = MutableStateFlow<List<PaymentCardInfo>>(emptyList())
    val cards: StateFlow<List<PaymentCardInfo>> = _cards
    
    // 현재 선택된 카드 이름 (cardId 대신 cardName 사용)
    private val _selectedCardName = MutableStateFlow<String?>(null)
    val selectedCardName: StateFlow<String?> = _selectedCardName

    // 사용자 ID
    private val userId= AppTokenProvider(context).getToken()

    // QR 토큰 전송 및 결제 정보 가져오기
    private val _paymentInfo = MutableStateFlow<PaymentInfo?>(null)
    val paymentInfo: StateFlow<PaymentInfo?> = _paymentInfo

    // 결제 결과 저장
    private val _paymentResult = MutableStateFlow<PaymentResult?>(null)
    val paymentResult: StateFlow<PaymentResult?> = _paymentResult

    private var isReconnecting = false
    private val reconnectDelay = 1000L // 1초 후 재연결

    // 결제 정보 데이터 클래스
    data class PaymentInfo(
        val merchantName: String = "가맹점",
        val amount: Int = 0,
        val cards: List<PaymentCardInfo> = emptyList()
    )
    
    // 결제 화면 진입 시 호출 - 모든 카드의 토큰 요청 및 SSE 연결 시작
    fun initializePaymentProcess() {
        Log.e("PaymentViewModel", "initializePaymentProcess STARTED")
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading

            Log.e("PaymentViewModel", "initializePaymentProcess getPaymentTokens")
            paymentRepository.getPaymentTokens()
                .onSuccess { tokens ->
                    Log.e("PaymentViewModel", "initializePaymentProcess getPaymentTokens success")
                    // API 응답에서 토큰 값이 null일 경우 대비
                    if (tokens == null) {
                        Log.e("PaymentViewModel", "Tokens from API is null")
                        _paymentState.value = PaymentState.Error("결제 토큰을 가져올 수 없습니다")
                        return@onSuccess
                    }
                    
                    _cardTokens.value = tokens
                    
                    // API 응답에서 카드 정보 추출 (추천 카드 제외)
                    val apiCards = tokens
                        .filter { it.cardName != "추천카드" }  // 추천 카드 제외
                        .map { tokenInfo ->
                            PaymentCardInfo(
                                cardName = tokenInfo.cardName ?: "",  // cardName을 주요 식별자로 사용
                                cardImageUrl = tokenInfo.cardImgUrl ?: "",
                                constellationInfo = tokenInfo.cardConstellationInfo ?: "{}"
                            )
                        }
                    _cards.value = apiCards
                    
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
    
    // 카드 선택 시 호출 - cardName 기반으로 변경
    fun selectCard(cardName: String) {
        _selectedCardName.value = cardName
        Log.d("PaymentViewModel", "카드 선택됨: $cardName")
    }
    
    // SSE 연결 시작
    private fun connectToPaymentEvents(userId: String) {
        viewModelScope.launch {
            paymentRepository.connectToPaymentEvents(userId)
                .catch { e ->
                    Log.e("PaymentViewModel", "Error in SSE connection: ${e.message}")
                    _paymentState.value = PaymentState.Error("SSE 연결 오류: ${e.message}")

                    // 타임아웃 오류인 경우 재연결 시도
                    if (e.message?.contains("timeout") == true && !isReconnecting) {
                        isReconnecting = true
                        Log.d("PaymentViewModel", "타임아웃으로 인한 SSE 재연결 시도 ($reconnectDelay ms 후)")
                        delay(reconnectDelay)
                        isReconnecting = false
                        connectToPaymentEvents(userId)
                    }

                }
                .collect { event ->
                    Log.e("PaymentViewModel", "Received payment event: $event")

                    // 타임아웃 오류 메시지 처리
                    if (event.message?.contains("timeout") == true && !isReconnecting) {
                        isReconnecting = true
                        Log.d("PaymentViewModel", "타임아웃 이벤트 감지, SSE 재연결 시도 ($reconnectDelay ms 후)")
                        delay(reconnectDelay)
                        isReconnecting = false
                        connectToPaymentEvents(userId)
                        return@collect
                    }

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
                                    // 최소 처리 시간 보장을 위한 지연 추가
                                    viewModelScope.launch {
                                        // 최소 3초 대기 후 결제 결과 확인
                                        delay(3000)
                                    }
                                }

                                event.message?.startsWith("TXN") == true -> {
                                    // TXN으로 시작하는 메시지는 결제 성공으로 처리
                                    try {
                                        // 서버에서 받은 결제 결과 데이터 파싱
                                        val paymentData = event.message.split(",")
                                        val amount = paymentData[1].toIntOrNull() ?: 0
                                        val createdAt = formatDateTime(paymentData[2])
                                        val approvalCode = paymentData[0]

                                        _paymentResult.value = PaymentResult(
                                            amount = amount,
                                            createdAt = createdAt,
                                            approvalCode = approvalCode
                                        )

                                        viewModelScope.launch {
                                            // 현재 상태가 Processing인 경우에만 지연 처리
                                            if (_paymentState.value is PaymentState.Processing) {
                                                // 최소 3초 대기 후 상태 변경
                                                delay(3000)
                                                _paymentState.value = PaymentState.Completed
                                                Log.d("PaymentViewModel", "결제 성공 상태로 변경 완료 (지연 후)")
                                            } else {
                                                // 이미 다른 상태인 경우 즉시 변경
                                                _paymentState.value = PaymentState.Completed
                                                Log.d("PaymentViewModel", "결제 성공 상태로 즉시 변경")
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("PaymentViewModel", "결제 데이터 파싱 오류: ${e.message}")
                                        // 파싱 오류 시 기본값 사용
                                        _paymentResult.value = PaymentResult(
                                            amount = 0,
                                            createdAt = getCurrentDateTime(),
                                            approvalCode = event.message ?: ""
                                        )
                                    }

                                    viewModelScope.launch {
                                        if (_paymentState.value is PaymentState.Processing) {
                                            delay(3000)
                                        }
                                        _paymentState.value = PaymentState.Completed
                                    }
                                }

                                event.message?.startsWith("REJ") == true -> {
                                    viewModelScope.launch {
                                        if (_paymentState.value is PaymentState.Processing) {
                                            delay(3000)
                                        }
                                        _paymentState.value = PaymentState.Failed("결제가 거절되었습니다: ${event.message}")
                                        Log.d("PaymentViewModel", "결제 실패: ${event.message}")
                                    }
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
                                    // TXN으로 시작하는 메시지는 결제 성공으로 처리
                                    try {
                                        // 서버에서 받은 결제 결과 데이터 파싱
                                        val paymentData = event.message.split(",")
                                        val amount = paymentData[1].toIntOrNull() ?: 0
                                        val createdAt = formatDateTime(paymentData[2])
                                        val approvalCode = paymentData[0]

                                        _paymentResult.value = PaymentResult(
                                            amount = amount,
                                            createdAt = createdAt,
                                            approvalCode = approvalCode
                                        )

                                        _paymentState.value = PaymentState.Completed
                                        Log.d("PaymentViewModel", "결제 성공 완료: ${event.message}")
                                    } catch (e: Exception) {
                                        Log.e("PaymentViewModel", "결제 데이터 파싱 오류: ${e.message}")
                                        // 파싱 오류 시 기본값 사용
                                        _paymentResult.value = PaymentResult(
                                            amount = 0,
                                            createdAt = getCurrentDateTime(),
                                            approvalCode = event.message ?: ""
                                        )
                                        _paymentState.value = PaymentState.Completed
                                    }
                                }

                                event.message?.startsWith("REJ") == true -> {
                                    // REJ로 시작하는 메시지는 결제 실패로 처리
                                    _paymentState.value = PaymentState.Failed("결제가 거절되었습니다: ${event.message}")
                                    Log.d("PaymentViewModel", "결제 실패: ${event.message}")
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
    
    // 특정 카드의 토큰 가져오기 (cardName 기반으로 변경)
    fun getTokenForCard(cardName: String): String? {
        // 카드 이름으로 토큰 찾기
        val token = _cardTokens.value.find { it.cardName == cardName }?.token
        
        // 추천 카드 토큰 가져오기 (cardName이 "추천카드"인 항목)
        if (cardName == "AUTO") {
            return _cardTokens.value.find { it.cardName == "추천카드" }?.token
        }
        
        return token
    }

    // 추천 카드 토큰 가져오기
    fun getAutoCardToken(): String? {
        return _cardTokens.value.find { it.cardName == "추천카드" }?.token
    }

    // 사용자 ID 가져오기 (현재는 하드코딩된 값 반환)
    private fun getUserId(): Int {
        return 2  // 임시로 고정된 사용자 ID 반환
    }

    // QR 토큰 전송
    fun sendQRToken(qrToken: String) {
        viewModelScope.launch {
            try {
                val userId = getUserId() // 사용자 ID 가져오기
                val response = paymentRepository.sendQRToken(QRTokenRequest(qrToken, userId))

                if (response.isSuccessful && response.body()?.success == true) {
                    val paymentResponse = response.body()?.data

                    if (paymentResponse != null) {
                        // 카드 정보 변환
                        val cardInfoList = paymentResponse.paymentTokenResponseDTO.map { tokenInfo ->
                            PaymentCardInfo(
                                cardName = tokenInfo.cardName ?: "",
                                cardImageUrl = tokenInfo.cardImgUrl ?: "",
                                constellationInfo = tokenInfo.cardConstellationInfo ?: "{}",
                                token = tokenInfo.token ?: ""
                            )
                        }

                        // 결제 정보 업데이트
                        _paymentInfo.value = PaymentInfo(
                            merchantName = paymentResponse.merchantName,
                            amount = paymentResponse.amount,
                            cards = cardInfoList
                        )

                        // 카드 목록 업데이트
                        _cards.value = cardInfoList

                        // 결제 상태 업데이트
                        _paymentState.value = PaymentState.Ready
                    } else {
                        _paymentState.value = PaymentState.Error("결제 정보가 없습니다")
                    }
                } else {
                    // 오류 처리
                    _paymentState.value = PaymentState.Error("QR 토큰 처리 실패: ${response.message()}")
                }
            } catch (e: Exception) {
                // 예외 처리
                _paymentState.value = PaymentState.Error("QR 토큰 처리 중 오류 발생: ${e.message}")
            }
        }
    }

    // 결제 완료 요청
    fun completePayment(token: String) {
        viewModelScope.launch {
            try {
                _paymentState.value = PaymentState.Processing

                val startTime = System.currentTimeMillis()

                val response = paymentRepository.completePayment(token)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val result = response.body()?.data
                    
                    if (result != null) {
                        // 결제 결과 저장 (API 응답에서 데이터 가져옴)
                        _paymentResult.value = PaymentResult(
                            amount = result.amount,
                            createdAt = formatDateTime(result.createdAt),
                            approvalCode = result.approvalCode
                        )

                        // 경과 시간 계산
                        val elapsedTime = System.currentTimeMillis() - startTime
                        val remainingTime = 3000 - elapsedTime

                        // 최소 3초 보장
                        if (remainingTime > 0) {
                            delay(remainingTime)
                        }

                        // 결제 상태 업데이트
                        _paymentState.value = PaymentState.Completed
                        
                        // 로그 출력
                        Log.d("PaymentViewModel", "결제 완료: ${result.approvalCode}, 금액: ${result.amount}, 시간: ${result.createdAt}")
                    } else {
                        val elapsedTime = System.currentTimeMillis() - startTime
                        val remainingTime = 3000 - elapsedTime
                        if (remainingTime > 0) {
                            delay(remainingTime)
                        }

                        _paymentState.value = PaymentState.Error("결제 결과가 없습니다")
                    }
                } else {
                    // 최소 3초 보장
                    val elapsedTime = System.currentTimeMillis() - startTime
                    val remainingTime = 3000 - elapsedTime
                    if (remainingTime > 0) {
                        delay(remainingTime)
                    }

                    // 오류 처리
                    _paymentState.value = PaymentState.Error("결제 처리 실패: ${response.message()}")
                }
            } catch (e: Exception) {
                // 예외 처리
                delay(3000)

                _paymentState.value = PaymentState.Error("결제 처리 중 오류 발생: ${e.message}")
            }
        }
    }

    // 날짜 포맷팅 함수 추가
    private fun formatDateTime(dateTimeStr: String): String {
        return try {
            // ISO 8601 형식의 날짜 문자열을 파싱
            val inputFormat = if (dateTimeStr.contains("T")) {
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", java.util.Locale.getDefault())
            } else {
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            }

            // 출력 형식 지정 (예: 2025-03-31 15:30)
            val outputFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())

            val date = inputFormat.parse(dateTimeStr)
            date?.let { outputFormat.format(it) } ?: dateTimeStr
        } catch (e: Exception) {
            Log.e("PaymentViewModel", "날짜 포맷팅 오류: ${e.message}")
            dateTimeStr // 오류 시 원본 문자열 반환
        }
    }

    // 현재 날짜/시간 가져오기
    private fun getCurrentDateTime(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }

    // 카드 추가 함수s
    fun addCard(cardName: String, cardNumber: String, expiryDate: String, cvv: String) {
        viewModelScope.launch {
            try {
                _paymentState.value = PaymentState.Loading
                
                // 카드 추가 API 호출 (실제 구현 필요)
                // 예시: paymentRepository.addCard(userId, cardName, cardNumber, expiryDate, cvv)
                
                // 임시 구현: 로컬에서만 카드 추가 (실제로는 API 호출 후 응답 처리 필요)
                val newCard = PaymentCardInfo(
                    cardName = cardName,
                    cardImageUrl = "", // 기본 이미지 사용
                    constellationInfo = "{}" // 기본 별자리 정보
                )
                
                // 카드 목록에 추가 (임시 구현)
                val currentCards = _cards.value.toMutableList()
                currentCards.add(newCard)
                _cards.value = currentCards
                
                // 상태 업데이트
                _paymentState.value = PaymentState.Ready
                
                // 토스트 메시지 표시 (실제 앱에서는 Context 필요)
                // Toast.makeText(context, "카드가 추가되었습니다", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                _paymentState.value = PaymentState.Error("카드 추가 실패: ${e.message}")
            }
        }
    }
} 