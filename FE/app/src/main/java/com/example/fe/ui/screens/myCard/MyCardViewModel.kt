package com.example.fe.ui.screens.myCard

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fe.data.repository.MyCardRepository
import com.example.fe.data.model.myCard.MyCardsResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// UI 상태 정의
sealed class MyCardUiState {
    object Loading : MyCardUiState()
    data class Success(val cards: List<CardItem>) : MyCardUiState()
    data class Error(val message: String) : MyCardUiState()
}

class MyCardViewModel() : ViewModel() {
    private val TAG = "MyCardViewModel"
    private val myCardRepository = MyCardRepository()

    private val _uiState = MutableStateFlow<MyCardUiState>(MyCardUiState.Loading)
    val uiState: StateFlow<MyCardUiState> = _uiState.asStateFlow()

    private val _cards = MutableStateFlow<List<CardItem>>(emptyList())
    val cards: StateFlow<List<CardItem>> = _cards.asStateFlow()

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadMyCards()
    }

    fun loadMyCards(forceRefresh: Boolean = false) {
        Log.d(TAG, "loadMyCards 호출: forceRefresh=$forceRefresh")
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            _uiState.value = MyCardUiState.Loading
            Log.d(TAG, "상태 변경: Loading")

            try {
                val token = "Bearer YOUR_TOKEN"
                Log.d(TAG, "API 요청 시작: GET /my-cards, 토큰=${token.take(15)}... forceRefresh=$forceRefresh")

                val result = myCardRepository.getMyCards(token, forceRefresh)
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

                        CardOrderManager.initializeIfEmpty(cardItems.map { CardItemWithVisibility(it) })
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

    private fun mapResponseToCardItems(response: MyCardsResponse): List<CardItem> {
        Log.d(TAG, "mapResponseToCardItems 호출")
        val data = response.data ?: return emptyList()
        Log.d(TAG, "응답 데이터: cardId=${data.cardId}, cardName=${data.cardName}, totalSpending=${data.totalSpending}")
        return listOf(
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
        ).also{
            Log.d(TAG, "카드 매핑 결과: $it")
        }
    }

    fun refreshCards() {
        Log.d(TAG, "refreshCards 호출")
        loadMyCards(forceRefresh = true)
    }

    fun clearCardCache(cardId: Int) {
        Log.d(TAG, "clearCardCache 호출: cardId=$cardId")
        myCardRepository.clearCardCache(cardId)
        Log.d(TAG, "카드 캐시 삭제 완료: cardId=$cardId")
    }

    fun clearAllCardCache() {
        Log.d(TAG, "clearAllCardCache 호출")
        myCardRepository.clearAllCache()
        Log.d(TAG, "모든 카드 캐시 삭제 완료")
    }
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