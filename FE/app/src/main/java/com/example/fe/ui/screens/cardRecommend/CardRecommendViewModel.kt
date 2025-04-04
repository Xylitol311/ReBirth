package com.example.fe.ui.screens.cardRecommend

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fe.data.model.cardRecommend.*
import com.example.fe.data.repository.CardRecommendRepository
import kotlinx.coroutines.launch

/**
 * 카드 추천 화면의 ViewModel
 */
class CardRecommendViewModel : ViewModel() {
    private val userId = 2
    private val TAG = "CardRecommendViewModel"

    // Repository 인스턴스
    private val repository = CardRecommendRepository()

    // UI 상태
    var uiState by mutableStateOf(CardRecommendUiState())
        private set

    // 초기화
    init {
        loadRecommendations()
    }

    /**
     * 모든 추천 데이터를 로드합니다.
     */
    fun loadRecommendations() {
        loadTop3ForAll()
        loadTop3ForCategory()
        loadInitialCardList()
    }

    /**
     * 전체 추천 TOP 3 카드를 로드합니다.
     */
    fun loadTop3ForAll(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            try {
                val result = repository.getTop3ForAll(userId, forceRefresh)
                result.onSuccess { response ->
                    uiState = uiState.copy(
                        top3ForAll = response,
                        isLoading = false,
                        error = null
                    )
                    Log.d(TAG, "TOP 3 카드 로드 성공: $response")
                }.onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        error = error.message ?: "알 수 없는 오류가 발생했습니다."
                    )
                    Log.e(TAG, "TOP 3 카드 로드 실패", error)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "알 수 없는 오류가 발생했습니다."
                )
                Log.e(TAG, "TOP 3 카드 로드 중 예외 발생", e)
            }
        }
    }

    /**
     * 카테고리별 추천 카드를 로드합니다.
     */
    fun loadTop3ForCategory(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoadingCategories = true)

            try {
                val result = repository.getTop3ForCategory(userId, forceRefresh)
                result.onSuccess { response ->
                    uiState = uiState.copy(
                        categoryRecommendations = response,
                        isLoadingCategories = false,
                        errorCategories = null
                    )
                    Log.d(TAG, "카테고리별 추천 카드 로드 성공: $response")
                }.onFailure { error ->
                    uiState = uiState.copy(
                        isLoadingCategories = false,
                        errorCategories = error.message ?: "알 수 없는 오류가 발생했습니다."
                    )
                    Log.e(TAG, "카테고리별 추천 카드 로드 실패", error)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoadingCategories = false,
                    errorCategories = e.message ?: "알 수 없는 오류가 발생했습니다."
                )
                Log.e(TAG, "카테고리별 추천 카드 로드 중 예외 발생", e)
            }
        }
    }

    fun loadInitialCardList() {
        // 수정된 검색 매개변수 생성
        val emptyParameters = CardSearchParameters(
            benefitType = emptyList(),  // 빈 배열
            cardCompany = emptyList(),  // 빈 배열
            category = emptyList(),     // 카테고리도 빈 배열로 수정
            minPerformanceRange = 0,
            maxPerformanceRange = Int.MAX_VALUE,
            minAnnualFee = 0,
            maxAnnualFee = Int.MAX_VALUE
        )

        // 검색 실행
        searchCards(emptyParameters)
    }

    /**
     * 검색 매개변수에 따른 카드를 검색합니다.
     */
    fun searchCards(parameters: CardSearchParameters) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoadingSearch = true)

            try {
                val result = repository.searchByParameter(parameters)
                result.onSuccess { response ->
                    uiState = uiState.copy(
                        searchResults = response,
                        isLoadingSearch = false,
                        errorSearch = null
                    )
                    Log.d(TAG, "카드 검색 성공: $response")
                }.onFailure { error ->
                    uiState = uiState.copy(
                        isLoadingSearch = false,
                        errorSearch = error.message ?: "알 수 없는 오류가 발생했습니다."
                    )
                    Log.e(TAG, "카드 검색 실패", error)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoadingSearch = false,
                    errorSearch = e.message ?: "알 수 없는 오류가 발생했습니다."
                )
                Log.e(TAG, "카드 검색 중 예외 발생", e)
            }
        }
    }

    /**
     * 필터 태그를 업데이트하고 카드를 검색합니다.
     */
    fun updateFilterAndSearch(category: String, option: String) {
        val updatedFilters = uiState.filterTags.map {
            if (it.category == category) it.copy(selectedOption = option) else it
        }

        uiState = uiState.copy(filterTags = updatedFilters)

        // 필터 옵션을 검색 매개변수로 변환
        val parameters = createSearchParametersFromFilters(updatedFilters)

        // 검색 실행
        searchCards(parameters)
    }

    /**
     * 필터 태그에서 검색 매개변수를 생성합니다.
     */
    private fun createSearchParametersFromFilters(filters: List<FilterTag>): CardSearchParameters {
        // 혜택 타입 필터
        val benefitTypeFilter = filters.find { it.category == "타입" }
        val benefitType = if (benefitTypeFilter?.selectedOption != "전체") {
            listOf(benefitTypeFilter?.selectedOption ?: "")
        } else emptyList()  // null 대신 emptyList()

        // 카드사 필터
        val cardCompanyFilter = filters.find { it.category == "카드사" }
        val cardCompany = if (cardCompanyFilter?.selectedOption != "전체") {
            listOf(cardCompanyFilter?.selectedOption ?: "")
        } else emptyList()  // null 대신 emptyList()

        // 카테고리 필터
        val categoryFilter = filters.find { it.category == "카테고리" }
        val category = if (categoryFilter?.selectedOption != "전체") {
            listOf(categoryFilter?.selectedOption ?: "")
        } else emptyList()

        // 전월 실적 필터
        val performanceFilter = filters.find { it.category == "전월 실적" }
        val (minPerformance, maxPerformance) = when (performanceFilter?.selectedOption) {
            "30만원 미만" -> Pair(0, 300000)
            "30~50만원" -> Pair(300000, 500000)
            else -> Pair(0, Int.MAX_VALUE)  // null 대신 0과 Int.MAX_VALUE
        }

        // 연회비 필터
        val annualFeeFilter = filters.find { it.category == "연회비" }
        val (minAnnualFee, maxAnnualFee) = when (annualFeeFilter?.selectedOption) {
            "만원 미만" -> Pair(0, 10000)
            "1~2만원" -> Pair(10000, 20000)
            else -> Pair(0, Int.MAX_VALUE)  // null 대신 0과 Int.MAX_VALUE
        }

        return CardSearchParameters(
            benefitType = benefitType,
            cardCompany = cardCompany,
            category = category,
            minPerformanceRange = minPerformance,
            maxPerformanceRange = maxPerformance,
            minAnnualFee = minAnnualFee,
            maxAnnualFee = maxAnnualFee
        )
    }

    /**
     * 카드 정보에서 아이콘을 추출합니다.
     */
    private fun extractIconsFromCardInfo(cardInfo: String): List<String> {
        // 카드 정보에서 카테고리 키워드를 추출하는 로직
        val keywords = listOf("교통", "식당", "카페", "영화", "쇼핑", "마트", "편의점", "병원", "약국")
        return keywords.filter { cardInfo.contains(it) }
    }

    /**
     * API 카드 정보를 UI 카드 정보로 변환합니다.
     */
    fun mapApiCardToUiCard(apiCard: CardInfoApi): CardInfo {
        // 전월 실적 처리
        val minSpending = if (apiCard.performanceRange != null && apiCard.performanceRange.isNotEmpty()) {
            "${apiCard.performanceRange[0]}원 이상"
        } else {
            "전월 실적 없음"
        }

        return CardInfo(
            id = apiCard.cardId,
            name = apiCard.cardName,
            company = getCardCompanyName(apiCard.cardCompanyId),
            benefits = listOf(apiCard.cardInfo),
            annualFee = if (apiCard.annualFee != null) "${apiCard.annualFee}원" else "0원",
            minSpending = minSpending,
            cardImage = apiCard.imageUrl,
            icons = extractIconsFromCardInfo(apiCard.cardInfo)
        )
    }

    /**
     * 카드 ID로 상세 정보를 로드합니다.
     */
    fun loadCardDetail(cardId: Int) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoadingCardDetail = true)

            try {
                // 먼저 검색 결과에서 카드 찾기
                val cardFromSearch = uiState.searchResults?.find { it.cardId == cardId }

                // 검색 결과에 없으면 TOP 3에서 찾기
                val cardFromTop3 = if (cardFromSearch == null) {
                    uiState.top3ForAll?.recommendCards?.find { it.cardId == cardId }
                } else null

                // TOP 3에도 없으면 카테고리별 추천에서 찾기
                val cardFromCategory = if (cardFromSearch == null && cardFromTop3 == null) {
                    uiState.categoryRecommendations?.flatMap { it.recommendCards }?.find { it.cardId == cardId }
                } else null

                // 찾은 카드 정보 사용
                val cardInfo = cardFromSearch ?: cardFromTop3 ?: cardFromCategory

                if (cardInfo != null) {
                    // 카드 정보를 UI 상태에 저장
                    uiState = uiState.copy(
                        selectedCardDetail = cardInfo,
                        isLoadingCardDetail = false,
                        errorCardDetail = null
                    )
                    Log.d(TAG, "카드 상세 정보 로드 성공: $cardInfo")
                } else {
                    uiState = uiState.copy(
                        isLoadingCardDetail = false,
                        errorCardDetail = "카드 정보를 찾을 수 없습니다."
                    )
                    Log.e(TAG, "카드 상세 정보를 찾을 수 없음: cardId=$cardId")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoadingCardDetail = false,
                    errorCardDetail = e.message ?: "알 수 없는 오류가 발생했습니다."
                )
                Log.e(TAG, "카드 상세 정보 로드 중 예외 발생", e)
            }
        }
    }

    /**
     * 선택된 카드의 상세 정보를 UI 카드 정보로 변환합니다.
     */
    fun getSelectedCardDetailForUI(): CardInfo? {
        val apiCard = uiState.selectedCardDetail ?: return null

        // 혜택 정보 처리
        val benefits = listOf(apiCard.cardInfo)

        // 전월 실적 처리
        val minSpending = if (apiCard.performanceRange != null && apiCard.performanceRange.isNotEmpty()) {
            "${apiCard.performanceRange[0]}원 이상"
        } else {
            "전월 실적 없음"
        }

        // 연회비 처리
        val annualFee = if (apiCard.annualFee != null) {
            "${apiCard.annualFee}원"
        } else {
            "0원"
        }

        return CardInfo(
            id = apiCard.cardId,
            name = apiCard.cardName,
            company = getCardCompanyName(apiCard.cardCompanyId),
            benefits = benefits,
            annualFee = annualFee,
            minSpending = minSpending,
            cardImage = apiCard.imageUrl,
            icons = extractIconsFromCardInfo(apiCard.cardInfo)
        )
    }
    /**
     * 카드사 ID를 이름으로 변환합니다.
     */
    private fun getCardCompanyName(cardCompanyId: Int?): String {
        return when (cardCompanyId) {
            1 -> "KB국민"
            2 -> "신한카드"
            3 -> "삼성카드"
            4 -> "현대카드"
            5 -> "롯데카드"
            6 -> "우리카드"
            7 -> "하나카드"
            8 -> "NH농협"
            9 -> "IBK기업"
            else -> "기타 카드사"
        }
    }

}

/**
 * 카드 추천 화면의 UI 상태
 */
data class CardRecommendUiState(
    // TOP 3 추천 카드
    val top3ForAll: Top3ForAllResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,

    // 카테고리별 추천 카드
    val categoryRecommendations: List<CategoryRecommendation>? = null,
    val isLoadingCategories: Boolean = false,
    val errorCategories: String? = null,

    // 검색 결과
    val searchResults: SearchByParameterResponse? = null,
    val isLoadingSearch: Boolean = false,
    val errorSearch: String? = null,

    // 선택된 카드 상세 정보
    val selectedCardDetail: com.example.fe.data.model.cardRecommend.CardInfoApi? = null,
    val isLoadingCardDetail: Boolean = false,
    val errorCardDetail: String? = null,

    // 필터 태그
    val filterTags: List<FilterTag> = listOf(
        FilterTag("타입", listOf("할인", "적립"), "전체"),
        FilterTag("카드사", listOf("KB국민", "신한카드", "삼성카드", "현대카드", "롯데카드", "우리카드", "하나카드", "NH농협", "IBK기업", "기타 카드사"), "전체"),
        FilterTag("카테고리", listOf("교통", "식당", "카페", "영화", "쇼핑", "마트", "편의점", "병원", "약국"), "전체"),
        FilterTag("전월 실적", listOf("30만원 미만", "30~50만원"), "전체"),
        FilterTag("연회비", listOf("만원 미만", "1~2만원"), "전체")
    )
)