//package com.example.fe.ui.screens.myCard
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.fe.data.repository.MyCardRepository
//import com.example.fe.util.AuthManager
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
///**
// * 내 카드 화면을 위한 ViewModel
// */
//@HiltViewModel
//class MyCardViewModel @Inject constructor(
//    private val repository: MyCardRepository,
//    private val authManager: AuthManager
//) : ViewModel() {
//
//    // UI 상태
//    sealed class UiState {
//        object Loading : UiState()
//        data class Success(val cards: List<CardItem>) : UiState()
//        data class Error(val message: String) : UiState()
//    }
//
//    // 카드 목록 상태
//    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
//    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
//
//    // 네비게이션 상태
//    private val _isNavigating = MutableStateFlow(false)
//    val isNavigating: StateFlow<Boolean> = _isNavigating.asStateFlow()
//
//    // 선택된 카드 페이지
//    private val _selectedCardPage = MutableStateFlow(-1)
//    val selectedCardPage: StateFlow<Int> = _selectedCardPage.asStateFlow()
//
//    // 카드 등장 애니메이션 상태
//    private val _cardsAppeared = MutableStateFlow(false)
//    val cardsAppeared: StateFlow<Boolean> = _cardsAppeared.asStateFlow()
//
//    // 초기화
//    init {
//        loadMyCards()
//    }
//
//    /**
//     * 내 카드 목록을 로드합니다.
//     * @param forceRefresh 강제 새로고침 여부
//     */
//    fun loadMyCards(forceRefresh: Boolean = false) {
//        _uiState.value = UiState.Loading
//
//        viewModelScope.launch {
//            try {
//                // 토큰 가져오기
//                val token = authManager.getAuthToken()
//
//                if (token != null) {
//                    // 리포지토리에서 카드 목록 가져오기
//                    repository.getMyCards(token, forceRefresh)
//                        .onSuccess { response ->
//                            if (response.success && response.data != null) {
//                                // 서버 응답 데이터를 UI 모델로 변환
//                                val cardItems = response.data.map { cardData ->
//                                    CardItem(
//                                        id = cardData.cardId,
//                                        name = cardData.cardName,
//                                        cardNumber = "•••• •••• •••• " + cardData.cardId.toString().takeLast(4)
//                                    )
//                                }
//
//                                // 카드 관리 매니저 초기화 (비어있을 경우에만)
//                                if (CardOrderManager.sortedCards.isEmpty()) {
//                                    CardOrderManager.initializeIfEmpty(cardItems.map { CardItemWithVisibility(it) })
//                                }
//
//                                _uiState.value = UiState.Success(cardItems)
//                            } else {
//                                _uiState.value = UiState.Error(response.message)
//                            }
//                        }
//                        .onFailure { exception ->
//                            _uiState.value = UiState.Error(exception.message ?: "카드 목록을 가져오는데 실패했습니다.")
//                        }
//                } else {
//                    _uiState.value = UiState.Error("로그인이 필요합니다.")
//                }
//            } catch (e: Exception) {
//                _uiState.value = UiState.Error("카드 목록을 가져오는데 실패했습니다: ${e.message}")
//            }
//        }
//    }
//
//    /**
//     * 카드 등장 애니메이션을 시작합니다.
//     */
//    fun startCardAppearAnimation() {
//        viewModelScope.launch {
//            kotlinx.coroutines.delay(300) // 약간의 지연 후 시작
//            _cardsAppeared.value = true
//        }
//    }
//
//    /**
//     * 네비게이션 상태를 설정합니다.
//     * @param navigating 네비게이션 중인지 여부
//     * @param cardPage 선택된 카드 페이지 (선택 사항)
//     */
//    fun setNavigating(navigating: Boolean, cardPage: Int = -1) {
//        _isNavigating.value = navigating
//        if (cardPage >= 0) {
//            _selectedCardPage.value = cardPage
//        }
//    }
//
//    /**
//     * 카드 관리 화면으로 이동합니다.
//     * @param onNavigate 네비게이션 콜백
//     */
//    fun navigateToManageCards(onNavigate: () -> Unit) {
//        setNavigating(true)
//
//        viewModelScope.launch {
//            kotlinx.coroutines.delay(300) // 애니메이션 완료 대기
//            onNavigate()
//            setNavigating(false)
//        }
//    }
//
//    /**
//     * 카드 상세 화면으로 이동합니다.
//     * @param cardItem 선택된 카드
//     * @param page 카드 페이지
//     * @param onNavigate 네비게이션 콜백
//     */
//    fun navigateToCardDetail(cardItem: CardItem, page: Int, onNavigate: (CardItem) -> Unit) {
//        setNavigating(true, page)
//
//        viewModelScope.launch {
//            kotlinx.coroutines.delay(300) // 애니메이션 완료 대기
//            onNavigate(cardItem)
//            setNavigating(false)
//        }
//    }
//}