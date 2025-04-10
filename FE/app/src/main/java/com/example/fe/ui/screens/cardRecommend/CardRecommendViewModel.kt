package com.example.fe.ui.screens.cardRecommend

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fe.data.model.cardRecommend.*
import com.example.fe.data.network.NetworkClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * 카드 추천 화면의 ViewModel
 */
class CardRecommendViewModel : ViewModel() {
    private val TAG = "CardRecommendViewModel"
    private val apiService = NetworkClient.cardRecommendApiService

    // UI 상태 (데이터 클래스 방식)
    var uiState by mutableStateOf(CardRecommendUiState())
        private set

    // 선택된 탭 인덱스를 저장하는 변수
    var selectedTabIndex = 0

    // API 요청을 위한 파라미터를 저장
    private val _searchParameters = mutableStateOf(com.example.fe.data.model.cardRecommend.CardSearchParameters())
    val searchParameters: com.example.fe.data.model.cardRecommend.CardSearchParameters
        get() = _searchParameters.value

    private val _filterCounts = mutableStateMapOf<String, Int>()
    val filterCounts: Map<String, Int> = _filterCounts

    // 상태 갱신 여부를 추적하기 위한 플래그
    private var _isFilterApplied = false
    val isFilterApplied: Boolean
        get() = _isFilterApplied

    init {
        loadRecommendations()
        setupFilterTags()
        // 처음에는 전체 카드 검색 (빈 파라미터)
        loadInitialCardList()
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
                val response = apiService.getTop3ForAll()
                if (response.success) {
                    uiState = uiState.copy(
                        top3ForAll = response.data,
                        isLoading = false,
                        error = null
                    )
                    Log.d(TAG, "TOP 3 추천 카드 로드 성공: ${response.data}")
                } else {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = response.message
                    )
                    Log.e(TAG, "TOP 3 추천 카드 로드 실패: ${response.message}")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "알 수 없는 오류가 발생했습니다."
                )
                Log.e(TAG, "TOP 3 추천 카드 로드 중 예외 발생", e)
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
                val response = apiService.getTop3ForCategory()
                if (response.success) {
                    uiState = uiState.copy(
                        categoryRecommendations = response.data,
                        isLoadingCategories = false,
                        errorCategories = null
                    )
                    Log.d(TAG, "카테고리별 추천 카드 로드 성공: ${response.data}")
                } else {
                    uiState = uiState.copy(
                        isLoadingCategories = false,
                        errorCategories = response.message
                    )
                    Log.e(TAG, "카테고리별 추천 카드 로드 실패: ${response.message}")
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
        val emptyParameters = com.example.fe.data.model.cardRecommend.CardSearchParameters(
            benefitType = emptyList(),  // 빈 배열
            cardCompany = emptyList(),  // 빈 배열
            category = emptyList(),     // 카테고리도 빈 배열로 수정
            minPerformanceRange = 0,
            maxPerformanceRange = Int.MAX_VALUE,
            minAnnualFee = 0,
            maxAnnualFee = Int.MAX_VALUE
        )

        // 필터 초기화 시에도 UI 갱신 버전 증가
        Log.d(TAG, "초기 카드 목록 로드 시작 - 필터 초기화")
        
        // 검색 실행
        searchCards(emptyParameters)
    }

    /**
     * 검색 매개변수에 따른 카드를 검색합니다.
     */
    fun searchCards(parameters: com.example.fe.data.model.cardRecommend.CardSearchParameters) {
        // 먼저 상태를 로딩으로 설정하고 결과를 비웁니다
        uiState = uiState.copy(
            searchResults = emptyList(),  // 목록을 비우는 것이 중요합니다
            isLoadingSearch = true,
            errorSearch = null,
            searchResultVersion = uiState.searchResultVersion + 1  // 버전 증가시켜 UI 갱신 트리거
        )
        
        Log.d(TAG, "로딩 시작 - 결과 목록 비움, 버전: ${uiState.searchResultVersion}")
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "API 호출 직전 - 검색 파라미터: $parameters")
                val response = apiService.searchByParameter(parameters)
                
                // 전체 응답을 로그로 출력
                val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
                val jsonResponse = gson.toJson(response)
                Log.d(TAG, "API 전체 응답 데이터: \n$jsonResponse")
                
                Log.d(TAG, "API 응답 결과 - 성공 여부: ${response.success}, 메시지: ${response.message}, 응답 데이터 수: ${response.data?.size ?: 0}")
                
                if (response.success) {
                    // 약간의 지연 후 결과 설정 (UI 갱신을 확실히 하기 위함)
                    delay(50)
                    
                    // 결과 데이터로 상태 업데이트
                    uiState = uiState.copy(
                        searchResults = response.data,
                        isLoadingSearch = false,
                        errorSearch = null,
                        searchResultVersion = uiState.searchResultVersion + 1  // 다시 버전 증가
                    )
                    
                    Log.d(TAG, "카드 검색 성공: ${response.data?.size}개의 카드, 버전: ${uiState.searchResultVersion}")
                    Log.d(TAG, "UI 상태 업데이트 완료: ${uiState.searchResults?.size}개 카드가 UI에 반영됨")
                    response.data?.forEach { apiCard -> 
                        Log.d(TAG, "  - 카드: ${apiCard.cardId}, ${apiCard.cardName}")
                    }
                } else {
                    uiState = uiState.copy(
                        isLoadingSearch = false,
                        errorSearch = response.message,
                        searchResultVersion = uiState.searchResultVersion + 1  // 에러 상태에서도 버전 증가
                    )
                    Log.e(TAG, "카드 검색 실패: ${response.message}")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoadingSearch = false,
                    errorSearch = e.message ?: "알 수 없는 오류가 발생했습니다.",
                    searchResultVersion = uiState.searchResultVersion + 1  // 예외 상태에서도 버전 증가
                )
                Log.e(TAG, "카드 검색 중 예외 발생", e)
            }
        }
    }

    /**
     * 필터 태그를 업데이트하고 카드를 검색합니다.
     */
    fun updateFilterAndSearch(category: String, option: String) {
        // 필터 카운트 업데이트
        when (category) {
            "타입" -> {
                val newParams = searchParameters.copy(
                    benefitType = if (option.isEmpty()) emptyList() else listOf(option)
                )
                _searchParameters.value = newParams
                _filterCounts[category] = if (option.isEmpty()) 0 else 1
            }
            "카드사" -> {
                val newParams = searchParameters.copy(
                    cardCompany = if (option.isEmpty()) emptyList() else listOf(option)
                )
                _searchParameters.value = newParams
                _filterCounts[category] = if (option.isEmpty()) 0 else 1
            }
            "카테고리" -> {
                val currentCategories = uiState.filterTags.find { it.category == "카테고리" }?.selectedOptions ?: emptyList()
                val newParams = searchParameters.copy(
                    category = currentCategories
                )
                _searchParameters.value = newParams
                _filterCounts[category] = currentCategories.size
            }
            "실적/연회비" -> {
                // 실적/연회비 파라미터 업데이트
                try {
                    val parts = option.split(",")
                    if (parts.size >= 2) {
                        val minRange = parts[0].toIntOrNull() ?: 0
                        val maxRange = parts[1].toIntOrNull() ?: 2000000
                        val newParams = searchParameters.copy(
                            minPerformanceRange = minRange,
                            maxPerformanceRange = maxRange,
                            minAnnualFee = 0,
                            maxAnnualFee = 1000000
                        )
                        _searchParameters.value = newParams
                        _filterCounts[category] = 1
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "실적/연회비 파라미터 파싱 오류: ${e.message}")
                }
            }
        }
        
        // 검색 실행
        searchCards(searchParameters)
    }
    
    // 카테고리 필터 선택 업데이트 및 카운트 업데이트
    fun updateCategoryFilter(option: String, isSelected: Boolean) {
        val filterTag = uiState.filterTags.find { it.category == "카테고리" }
        filterTag?.let {
            val currentOptions = it.selectedOptions.toMutableList()
            if (isSelected) {
                if (!currentOptions.contains(option)) {
                    currentOptions.add(option)
                }
            } else {
                currentOptions.remove(option)
            }
            
            // 카테고리 필터태그 업데이트
            val updatedFilterTags = uiState.filterTags.map { tag ->
                if (tag.category == "카테고리") {
                    tag.copy(selectedOptions = currentOptions)
                } else {
                    tag
                }
            }
            
            uiState = uiState.copy(
                filterTags = updatedFilterTags
            )
            
            // 검색 파라미터 업데이트
            val newParams = searchParameters.copy(
                category = currentOptions
            )
            _searchParameters.value = newParams
            
            // 카운트 업데이트
            _filterCounts["카테고리"] = currentOptions.size
        }
    }
    
    // 모든 필터를 한 번에 설정하는 함수
    fun setFiltersAtOnce(
        benefitType: List<String>,
        cardCompany: List<String>,
        categories: List<String>,
        performanceMin: Int,
        performanceMax: Int,
        annualFeeMin: Int,
        annualFeeMax: Int
    ) {
        Log.d(TAG, "모든 필터 한 번에 설정")
        Log.d(TAG, "혜택 타입: $benefitType")
        Log.d(TAG, "카드사: $cardCompany")
        Log.d(TAG, "카테고리: $categories")
        Log.d(TAG, "전월실적 범위: $performanceMin ~ $performanceMax")
        Log.d(TAG, "연회비 범위: $annualFeeMin ~ $annualFeeMax")
        
        // 필터 초기화 방지를 위해 중간 API 호출 없이 한 번에 모든 필터 설정
        _searchParameters.value = com.example.fe.data.model.cardRecommend.CardSearchParameters(
            benefitType = benefitType,
            cardCompany = cardCompany,
            category = categories,
            minPerformanceRange = performanceMin,
            maxPerformanceRange = performanceMax,
            minAnnualFee = annualFeeMin,
            maxAnnualFee = annualFeeMax
        )
        
        // 필터 카운트 업데이트
        _filterCounts["타입"] = benefitType.size
        _filterCounts["카드사"] = cardCompany.size
        _filterCounts["카테고리"] = categories.size
        _filterCounts["실적/연회비"] = if (performanceMin != 0 || performanceMax != 1000000) 1 else 0
        
        // 카테고리 필터 태그 업데이트 (필터 화면에서 다시 열었을 때 선택 상태 표시)
        val updatedFilterTags = uiState.filterTags.map { tag ->
            if (tag.category == "카테고리") {
                tag.copy(selectedOptions = categories)
            } else {
                tag
            }
        }
        
        uiState = uiState.copy(filterTags = updatedFilterTags)
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

        // 혜택 정보 처리 - 쉼표(,)로 구분하여 리스트로 변환
        val benefits = apiCard.cardInfo
            .split(",")  // 쉼표로 분리
            .map { it.trim() }  // 각 항목의 앞뒤 공백 제거
            .filter { it.isNotBlank() }  // 빈 항목 제거

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
            benefits = benefits,
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

        // 혜택 정보 처리 - 쉼표(,)로 구분하여 리스트로 변환
        val benefits = apiCard.cardInfo
            .split(",")  // 쉼표로 분리
            .map { it.trim() }  // 각 항목의 앞뒤 공백 제거
            .filter { it.isNotBlank() }  // 빈 항목 제거

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

    private fun setupFilterTags() {
        // 필터 태그 초기화
        uiState = uiState.copy(
            filterTags = listOf(
                FilterTag("타입", listOf("할인", "적립"), "전체"),
                FilterTag("카드사", listOf("KB국민", "신한카드", "삼성카드", "현대카드", "롯데카드", "우리카드", "하나카드", "NH농협", "IBK기업", "기타 카드사"), "전체"),
                FilterTag("카테고리", listOf("교통", "식당", "카페", "영화", "쇼핑", "마트", "편의점", "병원", "약국"), "전체"),
                FilterTag("전월 실적", listOf("30만원 미만", "30~50만원"), "전체"),
                FilterTag("연회비", listOf("만원 미만", "1~2만원"), "전체")
            )
        )
    }

    // 필터 선택 화면에서 결과 보기 버튼을 눌렀을 때 호출
    fun applyFilters() {
        // 저장된 파라미터로 API 요청 수행
        Log.d(TAG, "최종 검색 파라미터로 API 요청: $searchParameters")
        _isFilterApplied = false
        
        // API 호출 전에 로딩 상태로 변경
        uiState = uiState.copy(isLoadingSearch = true)
        searchCards(searchParameters)
        
        // API 호출 완료 플래그 설정
        _isFilterApplied = true
        Log.d(TAG, "applyFilters 완료 - 필터 적용 플래그: $_isFilterApplied")
    }

    // 최신 검색 결과로 UI를 강제 갱신하는 함수
    fun forceRefreshUIWithLatestResults() {
        // 현재 searchResults가 없으면 무시
        if (uiState.searchResults == null) return
        
        Log.d(TAG, "UI 강제 갱신 시작 - 현재 버전: ${uiState.searchResultVersion}, 카드 수: ${uiState.searchResults?.size ?: 0}개")
        
        // 임시로 검색 결과를 비우고 다시 설정하여 UI가 강제로 갱신되도록 함
        val currentResults = uiState.searchResults
        
        try {
            // 강제로 버전만 증가시켜 UI 갱신 트리거 (즉시 갱신을 위해)
            uiState = uiState.copy(
                searchResultVersion = uiState.searchResultVersion + 1
            )
            
            Log.d(TAG, "UI 강제 갱신 - 버전 증가: ${uiState.searchResultVersion}")
            
            // 결과를 잠시 비움 (즉시 빈 결과로 화면 갱신)
            uiState = uiState.copy(
                searchResults = emptyList(),
                searchResultVersion = uiState.searchResultVersion + 1
            )
            
            Log.d(TAG, "UI 강제 갱신 - 결과 비움, 버전: ${uiState.searchResultVersion}")
            
            // 약간의 지연 후 결과 다시 설정 (더 짧은 지연)
            viewModelScope.launch {
                delay(2)
                
                // 원래 결과 다시 설정
                uiState = uiState.copy(
                    searchResults = currentResults,
                    searchResultVersion = uiState.searchResultVersion + 1
                )
                
                Log.d(TAG, "UI 강제 갱신 완료 - 새 버전: ${uiState.searchResultVersion}, 카드 수: ${uiState.searchResults?.size ?: 0}개")
            }
        } catch (e: Exception) {
            Log.e(TAG, "UI 강제 갱신 중 오류 발생: ${e.message}")
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
    val searchResults: List<CardInfoApi>? = null,
    val isLoadingSearch: Boolean = false,
    val errorSearch: String? = null,
    val searchResultVersion: Int = 0, // 검색 결과 업데이트 버전 관리자

    // 선택된 카드 상세 정보
    val selectedCardDetail: CardInfoApi? = null,
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

/**
 * 필터 태그 모델
 */
data class FilterTag(
    val category: String,
    val options: List<String>,
    val selectedOption: String = "",
    val selectedOptions: List<String> = emptyList()
)

// UI에서 사용할 카드 정보 데이터 클래스
data class CardInfo(
    val id: Int,
    val name: String,
    val company: String,
    val benefits: List<String>,
    val annualFee: String,
    val minSpending: String,
    val cardImage: String?,
    val icons: List<String>
)

// 카드 검색 파라미터 데이터 클래스
data class CardSearchParameters(
    val benefitType: List<String> = emptyList(),
    val cardCompany: List<String> = emptyList(),
    val category: List<String> = emptyList(),
    val performanceRangeMin: Int = 0,
    val performanceRangeMax: Int = 2000000,
    val annualFeeMin: Int = 0,
    val annualFeeMax: Int = 1000000
) {
    override fun toString(): String {
        return "CardSearchParameters(" +
                "benefitType=$benefitType, " +
                "cardCompany=$cardCompany, " +
                "category=$category, " +
                "performanceRange=[$performanceRangeMin, $performanceRangeMax], " +
                "annualFeeRange=[$annualFeeMin, $annualFeeMax]" +
                ")"
    }
}