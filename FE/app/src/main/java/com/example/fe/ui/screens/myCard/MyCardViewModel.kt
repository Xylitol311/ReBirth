package com.example.fe.ui.screens.myCard

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fe.data.model.myCard.CardTransactionHistoryResponse
import com.example.fe.data.model.myCard.MyCardInfoResponse
import com.example.fe.data.model.myCard.MyCardsResponse
import com.example.fe.data.repository.MyCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class MyCardViewModel() : ViewModel() {
    private val TAG = "MyCardViewModel"
    private val myCardRepository = MyCardRepository()

    private val _uiState = MutableStateFlow<MyCardUiState>(MyCardUiState.Loading(null))
    val uiState: StateFlow<MyCardUiState> = _uiState.asStateFlow()

    private val _cards = MutableStateFlow<List<CardItem>>(emptyList())
    val cards: StateFlow<List<CardItem>> = _cards.asStateFlow()

    private val _cardInfoState = MutableStateFlow<CardInfoState>(CardInfoState.Initial)
    val cardInfoState: StateFlow<CardInfoState> = _cardInfoState.asStateFlow()

    private val _transactionHistoryState = MutableStateFlow<TransactionHistoryState>(TransactionHistoryState.Initial)
    val transactionHistoryState: StateFlow<TransactionHistoryState> = _transactionHistoryState.asStateFlow()

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var isDataLoaded = false
    private var loadedCardInfoIds = mutableSetOf<Int>()
    private var loadedTransactionHistoryIds = mutableMapOf<Int, MutableSet<Int>>()

    // 현재 선택된 카드 ID
    private var _selectedCardId = mutableStateOf<Int?>(null)
    val selectedCardId: State<Int?> = _selectedCardId

    // 현재 선택된 월
    private var _selectedMonth = mutableStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1)
    val selectedMonth: State<Int> = _selectedMonth

    // 현재 선택된 탭
    enum class CardDetailTab { BENEFIT, TRANSACTION }
    private val _selectedTab = mutableStateOf(CardDetailTab.BENEFIT)
    val selectedTab: State<CardDetailTab> = _selectedTab

    fun loadMyCards() {
        Log.d(TAG, "loadMyCards 호출")
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            // 이전 상태 유지하면서 로딩 표시
            _uiState.value = when (val currentState = _uiState.value) {
                is MyCardUiState.Success -> MyCardUiState.Loading(currentState.cards)
                else -> MyCardUiState.Loading()
            }

            Log.d(TAG, "상태 변경: Loading")

            try {

                Log.d(TAG, "API 요청 시작: GET /my-cards")

                val result = myCardRepository.getMyCards()
                Log.d(TAG, "Repository 호출: getMyCards 완료")

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    Log.d(TAG, "API 응답 성공: 상태 코드=200, 데이터=${response != null}")

                    if (response != null && response.success && response.data != null) {
                        val cardItems = mapResponseToCardItems(response)
                        Log.d(TAG, "카드 아이템 매핑 완료: ${cardItems.size}개")

                        _cards.value = cardItems
                        _uiState.value = MyCardUiState.Success(cardItems)
                        Log.d(TAG, "상태 변경: Success (${cardItems.size}개 카드)")

                        if (cardItems.isNotEmpty()) {
                            // 카드 순서 및 가시성 관리자 초기화
                            CardOrderManager.initializeIfEmpty(cardItems.map { CardItemWithVisibility(it) })
                        }

                        isDataLoaded = true

                    } else {
                        val message = response?.message ?: "카드 정보를 가져오는데 실패했습니다."
                        Log.e(TAG, "API 응답 오류: 메시지=$message")

                        errorMessage = message
                        _uiState.value = MyCardUiState.Error(message)
                        Log.d(TAG, "상태 변경: Error ($message)")
                    }
                } else {
                    val error = result.exceptionOrNull()
                    val statusCode = when (error) {
                        is retrofit2.HttpException -> error.code()
                        else -> "N/A"
                    }
                    val errorBody = when (error) {
                        is retrofit2.HttpException -> error.response()?.errorBody()?.string() ?: "없음"
                        else -> "없음"
                    }
                    val message = error?.message ?: "알 수 없는 오류가 발생했습니다."

                    Log.e(TAG, "Repository 오류: 상태 코드=$statusCode, 에러 메시지=$message, 에러 본문=$errorBody", error)

                    errorMessage = message
                    _uiState.value = MyCardUiState.Error("[$statusCode] $message")
                    Log.d(TAG, "상태 변경: Error ($message)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "예외 발생: 타입=${e.javaClass.simpleName}, 메시지=${e.message}", e)
                errorMessage = e.message ?: "카드 정보를 가져오는데 실패했습니다."
                _uiState.value = MyCardUiState.Error(errorMessage!!)
                Log.d(TAG, "상태 변경: Error (${e.message})")
            } finally {
                isLoading = false
                Log.d(TAG, "로딩 상태 종료")
            }
        }
    }

    /**
     * 특정 카드의 상세 정보를 가져옵니다.
     */
// getMyCardInfo 함수 수정
    fun getMyCardInfo(
        cardId: Int,
        year: Int = Calendar.getInstance().get(Calendar.YEAR),
        month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    ) {
        _selectedCardId.value = cardId

        Log.d(TAG, "getMyCardInfo 호출: cardId=$cardId, year=$year, month=$month")
        viewModelScope.launch {
            // 이전 상태 유지하면서 로딩 표시
            _cardInfoState.value = when (val currentState = _cardInfoState.value) {
                is CardInfoState.Success -> CardInfoState.Loading(currentState.cardInfo)
                else -> CardInfoState.Loading()
            }
            isLoading = true
            errorMessage = null

            try {
                Log.d(TAG, "API 요청 시작: GET /api/cards/detail/$cardId/$year/$month")

                val result = myCardRepository.getMyCardInfo(cardId, year, month)
                Log.d(TAG, "Repository 호출: getMyCardInfo 완료")

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    Log.d(TAG, "API 응답 성공: 상태 코드=200, 데이터=${response != null}")

                    if (response != null && response.success && response.data != null) {
                        val cardInfo = mapResponseToCardInfo(response)
                        _cardInfoState.value = CardInfoState.Success(cardInfo)
                        loadedCardInfoIds.add(cardId)

                        Log.d(TAG, "카드 상세 정보 로드 성공: $cardInfo")
                    } else {
                        val message = response?.message ?: "카드 상세 정보를 가져오는데 실패했습니다."
                        _cardInfoState.value = when (val currentState = _cardInfoState.value) {
                            is CardInfoState.Loading -> {
                                if (currentState.previousCardInfo != null) {
                                    CardInfoState.Error(message, currentState.previousCardInfo)
                                } else {
                                    CardInfoState.Error(message)
                                }
                            }
                            else -> CardInfoState.Error(message)
                        }
                        errorMessage = message
                        Log.e(TAG, "API 응답 오류: 메시지=$message")
                    }
                } else {
                    val error = result.exceptionOrNull()
                    val message = error?.message ?: "알 수 없는 오류가 발생했습니다."
                    _cardInfoState.value = when (val currentState = _cardInfoState.value) {
                        is CardInfoState.Loading -> {
                            if (currentState.previousCardInfo != null) {
                                CardInfoState.Error(message, currentState.previousCardInfo)
                            } else {
                                CardInfoState.Error(message)
                            }
                        }
                        else -> CardInfoState.Error(message)
                    }
                    errorMessage = message
                    Log.e(TAG, "Repository 오류: 메시지=$message", error)
                }
            } catch (e: Exception) {
                val message = e.message ?: "카드 상세 정보를 가져오는데 실패했습니다."
                _cardInfoState.value = when (val currentState = _cardInfoState.value) {
                    is CardInfoState.Loading -> {
                        if (currentState.previousCardInfo != null) {
                            CardInfoState.Error(message, currentState.previousCardInfo)
                        } else {
                            CardInfoState.Error(message)
                        }
                    }
                    else -> CardInfoState.Error(message)
                }
                errorMessage = message
                Log.e(TAG, "예외 발생: 타입=${e.javaClass.simpleName}, 메시지=${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    // getCardTransactionHistory 함수 수정
    fun getCardTransactionHistory(
        cardId: Int,
        month: Int = selectedMonth.value,
        page: Int = 0, // 0부터 시작하도록 변경
        pageSize: Int = 20
    ) {
        _selectedCardId.value = cardId
        _selectedMonth.value = month

        Log.d(TAG, "getCardTransactionHistory 호출: cardId=$cardId, month=$month, page=$page")
        viewModelScope.launch {
            // 이전 상태 유지하면서 로딩 표시
            _transactionHistoryState.value = when (val currentState = _transactionHistoryState.value) {
                is TransactionHistoryState.Success -> {
                    if (page > 0) {
                        // 페이지 추가 로딩 시 이전 데이터 유지
                        _isLoadingMoreTransactions.value = true
                        TransactionHistoryState.LoadingMore(
                            currentState.allTransactions,
                            currentState.currentPage,
                            currentState.pageSize,
                            currentState.hasMore
                        )
                    } else {
                        // 첫 페이지 로딩 시
                        TransactionHistoryState.Loading(currentState.allTransactions)
                    }
                }
                else -> TransactionHistoryState.Loading()
            }

            if (page == 0) {
                isLoading = true
            }
            errorMessage = null

            try {
                val token = "Bearer YOUR_TOKEN" // 실제 토큰으로 대체
                val year = Calendar.getInstance().get(Calendar.YEAR) // 현재 연도 사용
                Log.d(TAG, "API 요청 시작: POST /api/card/history (cardId=$cardId, month=$month, year=$year, page=$page)")

                val result = myCardRepository.getCardTransactionHistory(token, cardId, page, pageSize, month, year)
                Log.d(TAG, "Repository 호출: getCardTransactionHistory 완료")

                if (result.isSuccess) {
                    val response = result.getOrNull()
                    Log.d(TAG, "API 응답 성공: 상태 코드=200, 데이터=${response != null}")

                    if (response != null && response.success && response.data != null) {
                        val newTransactions = mapResponseToTransactions(response)
                        val pagination = response.data.pagination

                        // 이전 데이터와 새 데이터 병합
                        val allTransactions = if (page > 0 && _transactionHistoryState.value is TransactionHistoryState.LoadingMore) {
                            val previousTransactions = (_transactionHistoryState.value as TransactionHistoryState.LoadingMore).allTransactions
                            previousTransactions + newTransactions
                        } else {
                            newTransactions
                        }

                        // 더 로드할 수 있는지 확인
                        _canLoadMoreTransactions.value = pagination.hasMore

                        _transactionHistoryState.value = TransactionHistoryState.Success(
                            transactions = newTransactions,
                            allTransactions = allTransactions,
                            currentPage = pagination.currentPage,
                            pageSize = pagination.pageSize,
                            hasMore = pagination.hasMore
                        )

                        // 로드된 월 기록 (첫 페이지만)
                        if (page == 0) {
                            val monthsForCard = loadedTransactionHistoryIds.getOrPut(cardId) { mutableSetOf() }
                            monthsForCard.add(month)
                        }

                        Log.d(TAG, "카드 거래 내역 로드 성공: ${newTransactions.size}개 항목, 총 ${allTransactions.size}개, 더 로드 가능: ${pagination.hasMore}")
                    } else {
                        val message = response?.message ?: "카드 거래 내역을 가져오는데 실패했습니다."
                        _transactionHistoryState.value = when (val currentState = _transactionHistoryState.value) {
                            is TransactionHistoryState.Loading -> {
                                if (currentState.previousTransactions != null) {
                                    TransactionHistoryState.Error(message, currentState.previousTransactions)
                                } else {
                                    TransactionHistoryState.Error(message)
                                }
                            }
                            is TransactionHistoryState.LoadingMore -> {
                                TransactionHistoryState.Error(message, currentState.allTransactions)
                            }
                            else -> TransactionHistoryState.Error(message)
                        }
                        errorMessage = message
                        Log.e(TAG, "API 응답 오류: 메시지=$message")
                    }
                } else {
                    val error = result.exceptionOrNull()
                    val message = error?.message ?: "알 수 없는 오류가 발생했습니다."
                    _transactionHistoryState.value = when (val currentState = _transactionHistoryState.value) {
                        is TransactionHistoryState.Loading -> {
                            if (currentState.previousTransactions != null) {
                                TransactionHistoryState.Error(message, currentState.previousTransactions)
                            } else {
                                TransactionHistoryState.Error(message)
                            }
                        }
                        is TransactionHistoryState.LoadingMore -> {
                            TransactionHistoryState.Error(message, currentState.allTransactions)
                        }
                        else -> TransactionHistoryState.Error(message)
                    }
                    errorMessage = message
                    Log.e(TAG, "Repository 오류: 메시지=$message", error)
                }
            } catch (e: Exception) {
                val message = e.message ?: "카드 거래 내역을 가져오는데 실패했습니다."
                _transactionHistoryState.value = when (val currentState = _transactionHistoryState.value) {
                    is TransactionHistoryState.Loading -> {
                        if (currentState.previousTransactions != null) {
                            TransactionHistoryState.Error(message, currentState.previousTransactions)
                        } else {
                            TransactionHistoryState.Error(message)
                        }
                    }
                    is TransactionHistoryState.LoadingMore -> {
                        TransactionHistoryState.Error(message, currentState.allTransactions)
                    }
                    else -> TransactionHistoryState.Error(message)
                }
                errorMessage = message
                Log.e(TAG, "예외 발생: 타입=${e.javaClass.simpleName}, 메시지=${e.message}", e)
            } finally {
                isLoading = false
                _isLoadingMoreTransactions.value = false
            }
        }
    }

    // 페이지네이션 상태 관리
    private val _isLoadingMoreTransactions = MutableStateFlow(false)
    val isLoadingMoreTransactions = _isLoadingMoreTransactions.asStateFlow()

    private val _canLoadMoreTransactions = MutableStateFlow(true)
    val canLoadMoreTransactions = _canLoadMoreTransactions.asStateFlow()

    private var currentPage = 0
    private val pageSize = 10

    // 거래 내역 더 로드하기
    fun loadMoreTransactions(cardId: Int, month: Int = selectedMonth.value) {
        if (_isLoadingMoreTransactions.value || !_canLoadMoreTransactions.value) return

        viewModelScope.launch {
            _isLoadingMoreTransactions.value = true

            try {
                currentPage++
                getCardTransactionHistory(cardId, month, currentPage, pageSize)
            } catch (e: Exception) {
                Log.e(TAG, "추가 거래 내역 로드 중 오류 발생", e)
            } finally {
                _isLoadingMoreTransactions.value = false
            }
        }
    }

    // 거래 내역 초기화 (탭 변경이나 월 변경 시 호출)
    fun resetTransactionPagination() {
        currentPage = 0
        _canLoadMoreTransactions.value = true

        // 기존 거래 내역 상태 초기화
        _transactionHistoryState.value = when (val currentState = _transactionHistoryState.value) {
            is TransactionHistoryState.Success -> TransactionHistoryState.Loading(emptyList())
            else -> TransactionHistoryState.Loading(emptyList())
        }
    }

    /**
     * 선택된 월을 변경합니다.
     */
    fun setSelectedMonth(month: Int) {
        if (_selectedMonth.value != month) {
            _selectedMonth.value = month
            _selectedCardId.value?.let { cardId ->
                getCardTransactionHistory(cardId, month, 1) // 월이 변경되면 첫 페이지부터 다시 로드
            }
        }
    }

    /**
     * 선택된 카드를 변경합니다.
     */
    fun setSelectedCard(cardId: Int) {
        if (_selectedCardId.value != cardId) {
            _selectedCardId.value = cardId

            // 선택된 탭에 따라 필요한 데이터 로드
            when (_selectedTab.value) {
                CardDetailTab.BENEFIT -> {
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    getMyCardInfo(cardId, currentYear, _selectedMonth.value)
                }
                CardDetailTab.TRANSACTION -> getCardTransactionHistory(cardId, _selectedMonth.value, 1)
            }
        }
    }

    /**
     * 선택된 탭을 변경합니다.
     */
    fun setSelectedTab(tab: CardDetailTab) {
        if (_selectedTab.value != tab) {
            _selectedTab.value = tab

            // 필요한 데이터 로드
            _selectedCardId.value?.let { cardId ->
                when (tab) {
                    CardDetailTab.BENEFIT -> {
                        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                        getMyCardInfo(cardId, currentYear, _selectedMonth.value)
                    }
                    CardDetailTab.TRANSACTION -> {
                        val monthsForCard = loadedTransactionHistoryIds[cardId]
                        if (monthsForCard == null || !monthsForCard.contains(_selectedMonth.value)) {
                            getCardTransactionHistory(cardId, _selectedMonth.value, 1)
                        }
                    }
                }
            }
        }
    }

    // refreshAllData 함수 수정
    fun refreshAllData() {
        isDataLoaded = false
        loadedCardInfoIds.clear()
        loadedTransactionHistoryIds.clear()

        loadMyCards()

        _selectedCardId.value?.let { cardId ->
            when (_selectedTab.value) {
                CardDetailTab.BENEFIT -> {
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    getMyCardInfo(cardId, currentYear, _selectedMonth.value)
                }
                CardDetailTab.TRANSACTION -> getCardTransactionHistory(cardId, _selectedMonth.value, 1)
            }
        }
    }

    private fun mapResponseToCardItems(response: MyCardsResponse): List<CardItem> {
        Log.d(TAG, "mapResponseToCardItems 호출")
        val dataList = response.data ?: return emptyList()
        Log.d(TAG, "응답 데이터: ${dataList.size}개의 카드")

        return dataList.map { data ->
            CardItem(
                id = data.cardId,
                name = data.cardName,
                cardNumber = "•••• •••• •••• " + data.cardId.toString().takeLast(4),
                imageUrl = data.cardImgUrl,
                totalSpending = data.totalSpending,
                maxSpending = data.maxSpending,
                receivedBenefit = data.receivedBenefitAmount,
                maxBenefit = data.maxBenefitAmount
            )
        }.also {
            Log.d(TAG, "카드 매핑 결과: ${it.size}개의 카드")
        }
    }

    private fun mapResponseToCardInfo(response: MyCardInfoResponse): CardInfo {
        val data = response.data!!
        return CardInfo(
            id = data.cardId,
            name = data.cardName,
            imageUrl = data.cardImageUrl, // cardImgUrl에서 cardImageUrl로 변경
            currentPerformanceAmount = data.currentPerformanceAmount,
            maxPerformanceAmount = data.maxPerformanceAmount,
            spendingMaxTier = data.spendingMaxTier,
            currentSpendingTier = data.currentSpendingTier,
            amountRemainingNext = data.amountRemainingNext,
            performanceRange = data.performanceRange, // 추가된 필드
            benefits = data.cardBenefits.map { benefit ->
                BenefitInfo(
                    categories = benefit.benefitCategory, // category에서 benefitCategory로 변경, String에서 List<String>으로 변경
                    receivedBenefitAmount = benefit.receivedBenefitAmount,
                    remainingBenefitAmount = benefit.remainingBenefitAmount
                )
            }
        )
    }

    /**
     * 카드 거래 내역 응답을 UI 모델로 변환합니다.
     */
    private fun mapResponseToTransactions(response: CardTransactionHistoryResponse): List<TransactionInfo> {
        return response.data?.transactionHistory?.map { transaction ->
            TransactionInfo(
                date = transaction.transactionDate,
                category = transaction.transactionCategory ?: "기타", // null 처리
                amount = transaction.spendingAmount,
                merchantName = transaction.merchantName ?: "알 수 없음", // null 처리
                benefitAmount = transaction.receivedBenefitAmount ?: 0 // null 처리
            )
        } ?: emptyList()
    }

    // CardItem 클래스 확장 (추가 정보 포함)
    data class CardItem(
        val id: Int,
        val name: String,
        val cardNumber: String,
        val imageUrl: String = "",
        val totalSpending: Int = 0,
        val maxSpending: Int = 0,
        val receivedBenefit: Int = 0,
        val maxBenefit: Int = 0
    )

    // CardInfo 클래스 수정
    data class CardInfo(
        val id: Int,
        val name: String,
        val imageUrl: String,
        val currentPerformanceAmount: Int,
        val maxPerformanceAmount: Int,
        val spendingMaxTier: Int,
        val currentSpendingTier: Int,
        val amountRemainingNext: Int,
        val performanceRange: List<Int>, // 추가된 필드
        val benefits: List<BenefitInfo>
    )

    // BenefitInfo 클래스 수정
    data class BenefitInfo(
        val categories: List<String>, // String에서 List<String>으로 변경
        val receivedBenefitAmount: Int,
        val remainingBenefitAmount: Int
    )

    data class TransactionInfo(
        val date: String,
        val category: String,
        val amount: Int,
        val merchantName: String,
        val benefitAmount: Int
    )

    // 상태 클래스들
    sealed class MyCardUiState {
        object Initial : MyCardUiState()
        data class Loading(val previousCards: List<CardItem>? = null) : MyCardUiState()
        data class Success(val cards: List<CardItem>) : MyCardUiState()
        data class Error(val message: String, val previousCards: List<CardItem>? = null) : MyCardUiState()
    }

    sealed class CardInfoState {
        object Initial : CardInfoState()
        data class Loading(val previousCardInfo: CardInfo? = null) : CardInfoState()
        data class Success(val cardInfo: CardInfo) : CardInfoState()
        data class Error(val message: String, val previousCardInfo: CardInfo? = null) : CardInfoState()
    }

    sealed class TransactionHistoryState {
        object Initial : TransactionHistoryState()
        data class Loading(val previousTransactions: List<TransactionInfo>? = null) : TransactionHistoryState()
        data class LoadingMore(
            val allTransactions: List<TransactionInfo>,
            val currentPage: Int,
            val pageSize: Int,
            val hasMore: Boolean
        ) : TransactionHistoryState()
        data class Success(
            val transactions: List<TransactionInfo>,
            val allTransactions: List<TransactionInfo> = transactions,
            val currentPage: Int,
            val pageSize: Int,
            val hasMore: Boolean
        ) : TransactionHistoryState()
        data class Error(val message: String, val previousTransactions: List<TransactionInfo>? = null) : TransactionHistoryState()
    }

    // 카드 아이템과 표시 여부를 함께 관리하는 클래스
    data class CardItemWithVisibility(
        val card: CardItem,
        var isVisible: Boolean = true
    )

    // 카드 순서 관리자 (싱글톤)
    object CardOrderManager {

        private val _sortedCards = mutableStateListOf<CardItemWithVisibility>()
        val sortedCards: List<CardItemWithVisibility> get() = _sortedCards

        // 변경 리스너
        private val listeners = mutableListOf<() -> Unit>()

        // 카드 목록 초기화 (비어있을 경우에만)
        fun initializeIfEmpty(initialCards: List<CardItemWithVisibility>) {
            if (_sortedCards.isEmpty()) {
                _sortedCards.clear()
                _sortedCards.addAll(initialCards)
            }
        }

        // 카드 목록 업데이트
        fun updateCards(newCards: List<CardItemWithVisibility>) {
            _sortedCards.clear()
            _sortedCards.addAll(newCards)
            notifyListeners()
        }

        // 특정 ID의 카드 가져오기
        fun getCardById(id: Int): CardItemWithVisibility? {
            return _sortedCards.find { it.card.id == id }
        }

        // 카드 목록 설정
        fun setCards(cards: List<CardItemWithVisibility>) {
            _sortedCards.clear()
            _sortedCards.addAll(cards)
            notifyListeners()
        }

        // 카드 순서 변경
        fun moveCard(fromIndex: Int, toIndex: Int) {
            if (fromIndex in _sortedCards.indices && toIndex in _sortedCards.indices) {
                val card = _sortedCards.removeAt(fromIndex)
                _sortedCards.add(toIndex, card)
                notifyListeners()
            }
        }

        // 카드 표시 여부 변경
        fun setCardVisibility(cardId: Int, isVisible: Boolean) {
            val index = _sortedCards.indexOfFirst { it.card.id == cardId }
            if (index != -1) {
                _sortedCards[index] = _sortedCards[index].copy(isVisible = isVisible)
                notifyListeners()
            }
        }

        // 리스너 등록
        fun addListener(listener: () -> Unit) {
            listeners.add(listener)
        }

        // 리스너 제거
        fun removeListener(listener: () -> Unit) {
            listeners.remove(listener)
        }

        // 리스너에게 변경 알림
        private fun notifyListeners() {
            listeners.forEach { it.invoke() }
        }
    }

    // MyCardViewModel.kt에 추가
    fun updateSelectedCardIndex(index: Int) {
        viewModelScope.launch {
            if (_cards.value.isNotEmpty() && index < _cards.value.size) {
                // 선택된 카드 정보 업데이트
                val selectedCard = _cards.value[index]

                // 필요한 경우 추가 데이터 로드
                // 예: getMyCardInfo(selectedCard.id, currentYear, currentMonth)

                // 현재 선택된 카드 ID 업데이트
                _selectedCardId.value = selectedCard.id
            }
        }
    }
}





