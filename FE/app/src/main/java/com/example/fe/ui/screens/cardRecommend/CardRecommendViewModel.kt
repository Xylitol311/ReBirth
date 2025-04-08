package com.example.fe.ui.screens.cardRecommend

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.fe.config.AppConfig
import com.example.fe.data.model.cardRecommend.CategoryRecommendation
import com.example.fe.data.model.cardRecommend.RecommendCard
import com.example.fe.data.model.cardRecommend.Top3ForAllData
import com.example.fe.data.model.cardRecommend.Top3ForAllResponse
import com.example.fe.data.model.cardRecommend.Top3ForCategoryResponse
import com.example.fe.data.network.api.CardRecommendApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.net.URL

class CardRecommendViewModel : ViewModel() {
    private val TAG = "CardRecommendViewModel"
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.Server.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val cardRecommendApiService = retrofit.create(CardRecommendApiService::class.java)

    // 상태 변수들 (Compose MutableState 사용)
    val top3Loading = mutableStateOf(false)
    val categoryLoading = mutableStateOf(false)
    val top3Data = mutableStateOf<Top3ForAllData?>(null)
    val categoryData = mutableStateOf<List<CategoryRecommendation>>(emptyList())
    val errorMessage = mutableStateOf<String?>(null)

    // 검색 파라미터를 위한 데이터 클래스
    data class SearchParams(
        val benefitType: List<String> = listOf(),
        val cardCompany: List<String> = listOf(),
        val category: List<String> = listOf(),
        val minPerformanceRange: Int = 0,
        val maxPerformanceRange: Int = 10000000,
        val minAnnualFee: Int = 0,
        val maxAnnualFee: Int = 20000
    )

    // 검색 결과 데이터 클래스
    data class SearchResult(
        val cardTemplateId: Int,
        val cardCompanyId: Int,
        val cardName: String,
        val cardImgUrl: String,
        val annualFee: Int,
        val cardDetailInfo: String,
        val cardType: String,
        val cardConstellationInfo: String?,
        val performanceRange: List<Int>?
    )

    data class SearchResponse(
        val success: Boolean,
        val message: String,
        val data: List<SearchResult>
    )

    // 검색 결과 상태
    val searchResults = mutableStateOf<List<SearchResult>>(emptyList())
    val searchLoading = mutableStateOf(false)
    val searchError = mutableStateOf<String?>(null)

    // 현재 검색 필터 상태
    val currentSearchParams = mutableStateOf(SearchParams())

    fun fetchTop3ForAll() {
        top3Loading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = cardRecommendApiService.getTop3ForAll()
                Log.d(TAG, "Top3ForAll Response: $response")
                
                if (response.success) {
                    top3Data.value = response.data
                    // 로그 추가: 카드 데이터 확인 (상세 정보로 표시)
                    Log.d(TAG, "***** TOP 3 RESPONSE SUCCESS *****")
                    Log.d(TAG, "TOP 3 cards fetched: ${response.data.cards?.size ?: 0} cards")
                    
                    // response.data는 null이 아니지만 response.data.cards가 null인 경우 처리
                    if (response.data.cards == null) {
                        Log.w(TAG, "API 응답에 cards 필드가 null입니다. 서버에서 카드 데이터를 받지 못했습니다.")
                        
                        // 토스트 메시지 또는 UI 표시를 위해 에러 메시지 설정
                        // errorMessage.value = "카드 데이터를 불러오지 못했습니다."
                    } else if (response.data.cards.isEmpty()) {
                        Log.w(TAG, "API 응답에 cards 배열이 비어 있습니다. 추천 카드가 없습니다.")
                    } else {
                        response.data.cards.forEachIndexed { index, card ->
                            Log.d(TAG, "Card $index: ${card.cardName}")
                            Log.d(TAG, "  - imgUrl: '${card.imgUrl}'")
                            Log.d(TAG, "  - cardInfo: '${card.cardInfo}'")
                            Log.d(TAG, "  - cardId: ${card.cardId}")
                            
                            // 이미지 URL 테스트
                            if (card.imgUrl.isNotEmpty()) {
                                testImageUrl(card.imgUrl)
                            } else {
                                Log.w(TAG, "Card ${card.cardId} (${card.cardName})의 imgUrl이 비어 있습니다.")
                            }
                        }
                    }
                } else {
                    errorMessage.value = response.message
                    Log.e(TAG, "API Error: ${response.message}")
                }
            } catch (e: Exception) {
                errorMessage.value = "네트워크 오류: ${e.message}"
                Log.e(TAG, "Network Error: ${e.message}")
                e.printStackTrace()
            } finally {
                top3Loading.value = false
            }
        }
    }

    fun fetchTop3ForCategory() {
        categoryLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = cardRecommendApiService.getTop3ForCategory()
                Log.d(TAG, "Top3ForCategory Response: $response")
                
                if (response.success) {
                    // 디버깅: 이미지 URL 형식 확인
                    Log.d(TAG, "***** 카테고리별 API 응답 확인 *****")
                    if (response.data.isEmpty()) {
                        Log.w(TAG, "카테고리 데이터가 비어있습니다.")
                    } else {
                        response.data.forEachIndexed { categoryIndex, category ->
                            Log.d(TAG, "카테고리 ${categoryIndex+1}: ${category.categoryName}")
                            
                            if (category.recommendCards.isEmpty()) {
                                Log.w(TAG, "  카테고리 ${category.categoryName}에 추천 카드가 없습니다.")
                            } else {
                                category.recommendCards.forEachIndexed { cardIndex, card ->
                                    Log.d(TAG, "  카드 ${cardIndex+1}: ${card.cardName}")
                                    Log.d(TAG, "  이미지 URL: ${card.imgUrl}")
                                    
                                    // 이미지 URL이 유효한지 체크
                                    if (card.imgUrl.isEmpty()) {
                                        Log.w(TAG, "  ⚠️ 이미지 URL이 비어있습니다: 카드 ID ${card.cardId}, 이름 ${card.cardName}")
                                    } else if (!card.imgUrl.startsWith("http")) {
                                        Log.e(TAG, "  ⚠️ 유효하지 않은 이미지 URL 형식: ${card.imgUrl}")
                                    } else {
                                        // 이미지 URL 테스트
                                        testImageUrl(card.imgUrl)
                                    }
                                }
                            }
                        }
                    }
                    
                    categoryData.value = response.data
                } else {
                    errorMessage.value = response.message
                    Log.e(TAG, "API Error: ${response.message}")
                }
            } catch (e: Exception) {
                errorMessage.value = "네트워크 오류: ${e.message}"
                Log.e(TAG, "Network Error: ${e.message}")
                e.printStackTrace()
            } finally {
                categoryLoading.value = false
            }
        }
    }
    
    /**
     * 이미지 URL에 실제로 접근하여 상태 코드 확인
     * 백그라운드 스레드에서만 호출해야 함
     */
    private fun testImageUrl(imageUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "🔍 이미지 URL 테스트 시작: $imageUrl")
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"  // 헤더만 요청
                connection.connectTimeout = 5000   // 5초 타임아웃
                connection.connect()
                
                val responseCode = connection.responseCode
                val contentType = connection.contentType
                val contentLength = connection.contentLength
                
                when (responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        Log.d(TAG, "✅ 이미지 URL 테스트 성공: $imageUrl")
                        Log.d(TAG, "   상태 코드: $responseCode, 컨텐츠 타입: $contentType, 크기: ${contentLength}bytes")
                    }
                    HttpURLConnection.HTTP_NOT_FOUND -> {
                        Log.e(TAG, "❌ 이미지 URL 404 에러: $imageUrl")
                    }
                    else -> {
                        Log.e(TAG, "⚠️ 이미지 URL 응답 코드: $responseCode - $imageUrl")
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "⚠️ 이미지 URL 테스트 실패: $imageUrl", e)
                Log.e(TAG, "   오류 메시지: ${e.message}")
            }
        }
    }

    // 에러 메시지 초기화
    fun clearErrorMessage() {
        errorMessage.value = null
    }

    /**
     * 검색 파라미터로 카드 검색
     */
    fun searchByParams(params: SearchParams) {
        searchLoading.value = true
        currentSearchParams.value = params
        
        Log.d(TAG, "카드 검색 시작: $params")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 실제 API 호출을 위한 코드
                // val response = cardRecommendApiService.searchCards(params)
                
                // 임시 데이터 (API 연동 전 테스트용)
                // 실제 구현 시 아래 부분은 API 호출로 대체해야 함
                val dummyResults = listOf(
                    SearchResult(
                        cardTemplateId = 1,
                        cardCompanyId = 4,
                        cardName = "올바른 FLEX 카드",
                        cardImgUrl = "https://d1c5n4ri2guedi.cloudfront.net/card/666/card_img/21431/666card.png",
                        annualFee = 10000,
                        cardDetailInfo = "커피50%할인, 스트리밍20%할인, 영화30%할인",
                        cardType = "CREDIT",
                        cardConstellationInfo = null,
                        performanceRange = null
                    ),
                    SearchResult(
                        cardTemplateId = 2,
                        cardCompanyId = 8,
                        cardName = "신한카드 Mr.Life",
                        cardImgUrl = "https://d1c5n4ri2guedi.cloudfront.net/card/13/card_img/28201/13card.png",
                        annualFee = 15000,
                        cardDetailInfo = "공과금 10%할인, 마트,편의점 10%할인, 식음료 10%할인",
                        cardType = "CREDIT",
                        cardConstellationInfo = null,
                        performanceRange = null
                    ),
                    SearchResult(
                        cardTemplateId = 3,
                        cardCompanyId = 2,
                        cardName = "삼성카드 taptap O",
                        cardImgUrl = "https://d1c5n4ri2guedi.cloudfront.net/card/1/card_img/9081/1card.png",
                        annualFee = 15000,
                        cardDetailInfo = "쇼핑 10% 할인, 통신비 10% 할인",
                        cardType = "CREDIT",
                        cardConstellationInfo = null,
                        performanceRange = listOf(30000, 50000, 1000000)
                    ),
                    SearchResult(
                        cardTemplateId = 4,
                        cardCompanyId = 3,
                        cardName = "국민 톡톡 카드",
                        cardImgUrl = "https://d1c5n4ri2guedi.cloudfront.net/card/3/card_img/18881/3card.png",
                        annualFee = 12000,
                        cardDetailInfo = "대중교통 10% 적립, 편의점 5% 할인",
                        cardType = "CREDIT",
                        cardConstellationInfo = null,
                        performanceRange = listOf(30000, 50000, 1000000)
                    )
                )
                
                searchResults.value = dummyResults
                searchError.value = null
                Log.d(TAG, "카드 검색 결과: ${dummyResults.size}개 카드 찾음")
                
            } catch (e: Exception) {
                searchError.value = "검색 중 오류 발생: ${e.message}"
                Log.e(TAG, "카드 검색 오류", e)
            } finally {
                searchLoading.value = false
            }
        }
    }
    
    /**
     * 검색 결과를 CardInfo 모델로 변환
     */
    fun searchResultsToCardInfo(): List<CardInfo> {
        return searchResults.value.map { result ->
            CardInfo(
                id = result.cardTemplateId,
                name = result.cardName,
                company = "카드사 ${result.cardCompanyId}",
                benefits = result.cardDetailInfo.split(", "),
                annualFee = "${result.annualFee}원",
                minSpending = if (result.performanceRange != null && result.performanceRange.isNotEmpty()) 
                    "${result.performanceRange[0]} 이상" else "전월 실적 없음",
                cardImage = result.cardImgUrl
            )
        }
    }

    // 초기 검색 파라미터로 검색 실행 (앱 시작 시 호출)
    fun initialSearch() {
        searchByParams(currentSearchParams.value)
    }
} 