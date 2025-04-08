package com.example.fe.ui.screens.cardRecommend

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.fe.R
import kotlin.math.absoluteValue
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyRow
import com.example.fe.ui.components.backgrounds.GlassSurface
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import com.example.fe.data.model.cardRecommend.CategoryRecommendation
import com.example.fe.data.model.cardRecommend.RecommendCard
import com.example.fe.data.model.cardRecommend.Top3ForAllData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.fe.ui.components.images.NetworkImage
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.layout.offset

// 카드 데이터 클래스
data class CardInfo(
    val id: Int,
    val name: String,
    val company: String,
    val benefits: List<String>,
    val annualFee: String,
    val minSpending: String,
    val cardImage: String? = null,
    val icons: List<String> = listOf() // 교통, 식당 등의 아이콘
) {
    // 이미지 URL 유효성 검사
    fun hasValidImageUrl(): Boolean = cardImage != null && cardImage.isNotEmpty() && cardImage.startsWith("http")
    
    // 이미지 URL 정규화
    fun normalizedImageUrl(): String {
        val imgUrl = cardImage ?: ""
        // URL이 비어있거나 이미 http로 시작하면 그대로 반환
        if (imgUrl.isEmpty() || imgUrl.startsWith("http")) return imgUrl
        
        // 상대 경로인 경우 기본 URL 추가
        return "https://a602rebirth.s3.ap-northeast-2.amazonaws.com/card_img/$imgUrl"
    }
    
    // imgUrl getter 추가
    val imgUrl: String get() = cardImage ?: ""
}

// 필터 태그 데이터 클래스
data class FilterTag(
    val category: String,
    val options: List<String>,
    val selectedOption: String
)

@Composable
fun CardRecommendScreen(
    onCardClick: (CardInfo) -> Unit = {}
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    // ViewModel 생성
    val viewModel = viewModel<CardRecommendViewModel>()
    
    // 컴포즈에서 관찰 가능한 상태
    val top3Data by viewModel.top3Data
    val categoryData by viewModel.categoryData
    val top3Loading by viewModel.top3Loading
    val categoryLoading by viewModel.categoryLoading
    val errorMessage by viewModel.errorMessage
    
    // 첫 로드 시 API 호출
    LaunchedEffect(Unit) {
        viewModel.fetchTop3ForAll()
        viewModel.fetchTop3ForCategory()
    }
    
    // 로그 출력
    LaunchedEffect(top3Data, categoryData, errorMessage) {
        top3Data?.let {
            Log.d("CardRecommendScreen", "TOP 3 Data: $it")
        }
        
        if (categoryData.isNotEmpty()) {
            Log.d("CardRecommendScreen", "Category Data: $categoryData")
        }
        
        errorMessage?.let {
            Log.e("CardRecommendScreen", "Error: $it")
            viewModel.clearErrorMessage()
        }
    }

    val allCards = listOf(
        CardInfo(1, "토스 신한카드 Mr.Life", "신한카드", listOf("카페 10% 할인"), "30,000원", "월 30만원", icons = listOf("식당", "교통")),
        CardInfo(2, "삼성 iD카드", "삼성카드", listOf("영화 50% 할인"), "15,000원", "월 20만원", icons = listOf("여가")),
        CardInfo(3, "국민 톡톡카드", "KB국민", listOf("대중교통 10% 적립"), "12,000원", "월 30만원", icons = listOf("교통")),
        CardInfo(4, "농협 NH20해봄카드", "농협", listOf("편의점 5% 할인"), "10,000원", "월 10만원", icons = listOf("음식")),
        CardInfo(5, "IBK 참!좋은카드", "IBK", listOf("학원비 10% 적립"), "8,000원", "월 25만원", icons = listOf("교육"))
    )

    var filterTags by remember {
        mutableStateOf(
            listOf(
                FilterTag("타입", listOf("할인", "적립"), "전체"),
                FilterTag("카드사", listOf("KB국민", "IBK", "농협", "신한카드", "삼성카드"), "전체"),
                FilterTag("카테고리", listOf("교통", "음식", "교육", "여가", "식당"), "전체"),
                FilterTag("전월 실적", listOf("30만원 미만", "30~50만원"), "전체"),
                FilterTag("연회비", listOf("만원 미만", "1~2만원"), "전체")
            )
        )
    }

    val filteredCards = allCards
        .filter {
            val companyFilter = filterTags.find { it.category == "카드사" }?.selectedOption ?: "전체"
            companyFilter == "전체" || it.company == companyFilter
        }
        .filter {
            val categoryFilter = filterTags.find { it.category == "카테고리" }?.selectedOption ?: "전체"
            categoryFilter == "전체" || it.icons.contains(categoryFilter)
        }
        .sortedBy { it.name }

    Column(modifier = Modifier.fillMaxSize()) {
        // 상단 탭 바
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTabIndex = 0 },
                    horizontalAlignment = CenterHorizontally
                ) {
                    Text(
                        text = "추천 카드",
                        color = if (selectedTabIndex == 0) Color.White else Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(2.dp)
                            .align(CenterHorizontally)
                            .background(
                                color = if (selectedTabIndex == 0) Color.White else Color.Transparent
                            )
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTabIndex = 1 },
                    horizontalAlignment = CenterHorizontally
                ) {
                    Text(
                        text = "직접 찾기",
                        color = if (selectedTabIndex == 1) Color.White else Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(2.dp)
                            .align(CenterHorizontally)
                            .background(
                                color = if (selectedTabIndex == 1) Color.White else Color.Transparent
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTabIndex) {
            0 -> PersonalizedRecommendations(
                cards = filteredCards,
                onCardClick = onCardClick,
                top3Data = top3Data,
                categoryData = categoryData,
                isLoading = top3Loading || categoryLoading
            )
            1 -> CardFinder(
                filterTags = filterTags,
                onFilterChange = { category, option ->
                    filterTags = filterTags.map {
                        if (it.category == category) it.copy(selectedOption = option) else it
                    }
                },
                cards = filteredCards,
                onCardClick = onCardClick
            )
        }
    }
}

@Composable
fun PersonalizedRecommendations(
    cards: List<CardInfo>,
    onCardClick: (CardInfo) -> Unit,
    top3Data: Top3ForAllData?,
    categoryData: List<CategoryRecommendation>,
    isLoading: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column {
                Text(
                    text = "이번 달",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "당신에게 가장 추천하는 카드",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "이전 3개월 소비 내역을 바탕으로 추천해드려요",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        item {
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16f,
                blurRadius = 10f
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "추천 TOP 3",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // API 응답 데이터가 있는 경우 표시
                    if (top3Data != null) {
                        Text(
                            text = "3개월 간 총 ${top3Data.amount}원 썼어요",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // API에서 cards가 null인 경우를 방어적으로 처리
                        if (top3Data.cards != null && top3Data.cards.isNotEmpty()) {
                            Log.d("CardRecommendScreen", "Top3 카드 데이터가 있습니다: ${top3Data.cards.size}개")
                            val recommendedCards = top3Data.cards.map { recommendCard ->
                                CardInfo(
                                    id = recommendCard.cardId,
                                    name = recommendCard.cardName,
                                    company = "", // API에서 제공하지 않음
                                    benefits = recommendCard.cardInfo.split(", "),
                                    annualFee = "", // API에서 제공하지 않음
                                    minSpending = "", // API에서 제공하지 않음
                                    cardImage = recommendCard.imgUrl
                                )
                            }
                            CardCarousel(cards = recommendedCards, onCardClick = onCardClick)
                        } else {
                            // API에서 반환된 카드 데이터가 없는 경우 기본 카드 데이터 사용
                            Log.d("CardRecommendScreen", "Top3 카드 데이터가 없습니다. 기본 카드를 표시합니다.")
                            CardCarousel(cards = cards, onCardClick = onCardClick)
                        }
                    } else {
                        // API 응답 자체가 없는 경우
                    Text(
                        text = "3개월 간 총 500,000원 썼어요",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CardCarousel(cards = cards, onCardClick = onCardClick)
                    }
                }
            }
        }

        item {
            Text(
                text = "카테고리별 추천 카드",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        item {
            if (categoryData.isNotEmpty()) {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16f,
                    blurRadius = 10f
                ) {
                    Column(modifier = Modifier
                        .padding(16.dp)
                        .padding(bottom = 16.dp)) {
                        // 각 카테고리별 추천 카드 슬라이드를 보여줌
                        categoryData.forEachIndexed { index, category ->
                            CategoryCardSlide(
                                category = category,
                                onCardClick = { cardId ->
                                    // 해당 카드 ID로 CardInfo를 만들어 전달
                                    val recommendCard = category.recommendCards.find { it.cardId == cardId }
                                    if (recommendCard != null) {
                                        val cardInfo = CardInfo(
                                            id = recommendCard.cardId,
                                            name = recommendCard.cardName,
                                            company = "", // API에서 제공하지 않음
                                            benefits = listOf(recommendCard.cardInfo), // 콤마로 분리된 문자열을 그대로 사용
                                            annualFee = "", // API에서 제공하지 않음
                                            minSpending = "", // API에서 제공하지 않음
                                            cardImage = recommendCard.imgUrl
                                        )
                                        onCardClick(cardInfo)
                                    }
                                }
                            )
                            
                            // 마지막 카테고리가 아니면 구분선 추가
                            if (index < categoryData.size - 1) {
                                Divider(
                                    color = Color.Gray.copy(alpha = 0.3f),
                                    thickness = 1.dp,
                                    modifier = Modifier
                                        .padding(vertical = 16.dp)
                                        .padding(top = 16.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // 카테고리 데이터가 없는 경우 기존 UI 유지
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16f,
                blurRadius = 10f
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                            text = "병원/약국 혜택 추천",
                        color = Color.White,
                            fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                            text = "병원/약국에 1,240,000원 썼어요",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CardCarousel(cards = cards, onCardClick = onCardClick)
                    }
                }
            }
        }

        // 로딩 중인 경우 표시
        if (isLoading) {
        item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryCardSlide(
    category: CategoryRecommendation,
    onCardClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // 카테고리 이름과 소비 금액
                    Text(
            text = "${category.categoryName} 혜택 추천",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
            text = "${category.categoryName}에 ${category.amount}원 썼어요",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

        // 추천 카드 캐러셀
        if (category.recommendCards.isNotEmpty()) {
            CategoryCardCarousel(
                recommendCards = category.recommendCards,
                onCardClick = onCardClick
            )
        } else {
            // 추천 카드가 없는 경우
            Text(
                text = "추천 카드가 없습니다",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryCardCarousel(
    recommendCards: List<RecommendCard>,
    onCardClick: (Int) -> Unit
) {
    // 3개 미만이면 반복해서 3개로 만듦
    val extendedCards = if (recommendCards.size < 3) {
        val repeatedList = mutableListOf<RecommendCard>()
        repeat(3) { i ->
            repeatedList.add(recommendCards[i % recommendCards.size])
        }
        repeatedList
    } else {
        recommendCards.take(3)
    }

    val pagerState = rememberPagerState(
        pageCount = { extendedCards.size },
        initialPage = 0
    )

    val density = LocalDensity.current.density
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                // 혜택 개수에 따라 박스 높이도 동적으로 조절
                when {
                    extendedCards.getOrNull(0)?.cardInfo?.split(", ")?.size ?: 0 <= 1 -> 430.dp
                    extendedCards.getOrNull(0)?.cardInfo?.split(", ")?.size == 2 -> 450.dp
                    else -> 480.dp
                }
            )
    ) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = (-20).dp,
            contentPadding = PaddingValues(horizontal = 70.dp),
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val pageOffset = (
                        (pagerState.currentPage - page) + pagerState
                            .currentPageOffsetFraction
                        ).absoluteValue

                val scale = 1.2f - (pageOffset * 0.3f).coerceIn(0f, 0.3f)
                val alpha = 1f - (pageOffset * 0.4f).coerceIn(0f, 0.4f)

                Box(
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            alpha = alpha,
                            clip = false,
                            cameraDistance = 12f * density
                        )
                        .clickable {
                            if (page != pagerState.currentPage) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(page)
                                }
                            } else {
                                onCardClick(extendedCards[page].cardId)
                            }
                        }
                ) {
                    // 전체 카드 컨테이너
                    // 혜택 개수에 따라 동적으로 높이 조절
                    val benefits = extendedCards[page].cardInfo.split(", ")
                    val containerHeight = when {
                        benefits.size <= 1 -> 430.dp
                        benefits.size == 2 -> 450.dp
                        else -> 480.dp
                    }
                    
                    Column(
                        modifier = Modifier.height(containerHeight),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 카드 이미지 부분
                        Box(
                            modifier = Modifier
                                .width(380.dp)
                                .height(340.dp) // 원래 값으로 복원 (300dp에서 340dp로)
                                .padding(horizontal = 8.dp)
                        ) {
                            val card = extendedCards[page]
                            
                            // API에서 받은 이미지 URL 사용
                            if (card.imgUrl.isNotEmpty() && card.imgUrl.startsWith("http")) {
                                // Coil 라이브러리를 사용한 비동기 이미지 로딩
                                val imgUrl = card.imgUrl
                                Log.d("CardRecommendScreen", "카테고리 카드 이미지 로드 시도: $imgUrl")
                                
                                // NetworkImage 컴포넌트 사용
                                NetworkImage(
                                    url = imgUrl,
                                    contentDescription = card.cardName,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp), // 원래 값으로 복원 (6dp에서 12dp로)
                                    rotationZ = 90f,
                                    fallbackResId = R.drawable.card,
                                    onError = {
                                        Log.e("CardRecommendScreen", "⚠️ 카테고리 카드 이미지 로드 실패: $imgUrl")
                                    },
                                    onSuccess = {
                                        Log.d("CardRecommendScreen", "✅ 카테고리 카드 이미지 로드 성공: $imgUrl")
                                    }
                                )
                            } else {
                                // 이미지 URL이 없는 경우 기본 이미지 표시
                            Image(
                                painter = painterResource(id = R.drawable.card),
                                    contentDescription = card.cardName,
                                    contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                        .padding(12.dp) // 원래 값으로 복원
                                    .graphicsLayer(rotationZ = 90f)
                            )
                                Log.d("CardRecommendScreen", "⚠️ 카드 URL이 비어있어 기본 이미지 사용: ${card.cardName}")
                            }
                        }

                        // 카드 정보 (혜택 및 이름) - 이미지와 분리된 별도 영역
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp) // 원래 값으로 복원 (24dp에서 16dp로)
                                .padding(top = 0.dp, bottom = 16.dp) // 원래 값으로 복원 (8dp에서 16dp로)
                                .background(Color.Transparent) // 배경색 제거하여 카드와 분리감 강화
                        ) {
                            // 카드 혜택 정보 - 여러 줄로 표시
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                benefits.take(2).forEach { benefit ->
                            Text(
                                        text = benefit,
                                color = Color.White,
                                        fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                            .padding(bottom = 4.dp)
                                    )
                                }
                                
                                // 더 많은 혜택이 있는 경우 "... 외 N개" 형식으로 표시
                                if (benefits.size > 2) {
                                    Text(
                                        text = "... 외 ${benefits.size - 2}개",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 4.dp)
                                    )
                                }
                            }

                            // 혜택 개수에 따라 공간 조절 - 공간 더 줄이기
                            val spacerHeight = when {
                                benefits.size <= 1 -> {
                                    Log.d("CardRecommendScreen", "Card has ${benefits.size} benefits - using 4.dp spacing")
                                    4.dp // 8dp에서 4dp로 축소
                                }
                                benefits.size == 2 -> {
                                    Log.d("CardRecommendScreen", "Card has ${benefits.size} benefits - using 6.dp spacing")
                                    6.dp // 12dp에서 6dp로 축소
                                }
                                else -> {
                                    Log.d("CardRecommendScreen", "Card has ${benefits.size} benefits - using 8.dp spacing")
                                    8.dp // 16dp에서 8dp로 축소
                                }
                            }
                            Spacer(
                                modifier = Modifier
                                    .height(spacerHeight)
                                    .fillMaxWidth()
                            )

                            // 카드 이름 - 글자 크기 키우고 강조
                            Text(
                                text = extendedCards[page].cardName,
                                color = Color.White,
                                fontSize = 18.sp, // 16sp에서 18sp로 증가
                                fontWeight = FontWeight.Bold, // 볼드 추가
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .padding(bottom = 16.dp) // 카드 이름 아래 여백 추가
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardFinder(
    filterTags: List<FilterTag>,
    onFilterChange: (String, String) -> Unit,
    cards: List<CardInfo>,
    onCardClick: (CardInfo) -> Unit
) {
    // 필터 페이지 표시 여부
    var showFilterPage by remember { mutableStateOf(false) }
    // 현재 선택된 필터 카테고리 (필터 페이지에 표시할 카테고리)
    var selectedFilterCategory by remember { mutableStateOf("") }
    
    // ViewModel 가져오기
    val viewModel = viewModel<CardRecommendViewModel>()
    val searchResults by viewModel.searchResults
    val searchLoading by viewModel.searchLoading
    
    // 앱 첫 실행 시 초기 검색 실행
    LaunchedEffect(Unit) {
        viewModel.initialSearch()
    }
    
    // 필터 페이지 표시 중인 경우
    if (showFilterPage) {
        FilterPage(
            category = selectedFilterCategory,
            onClose = { showFilterPage = false },
            onApplyFilter = {
                showFilterPage = false
                // 필터 적용 후 API 검색 실행 (실제 구현 시 파라미터 추가)
                viewModel.searchByParams(viewModel.currentSearchParams.value)
            }
        )
        return
    }

    // 필터 버튼 데이터
    val filterButtons = listOf(
        "혜택 타입", "카드사", "카테고리", "전월실적", "연회비"
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 1. 상단 필터 버튼 영역
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterButtons) { filterName ->
                FilterButton(
                    text = filterName,
                    onClick = {
                        selectedFilterCategory = filterName
                        showFilterPage = true
                    }
                )
            }
        }
        
        // 구분선
        Divider(
            color = Color.Gray.copy(alpha = 0.3f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        // 2. "인기순" 라벨
        Text(
            text = "인기순",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )
        
        // 3. 카드 그리드 레이아웃을 포함한 GlassSurface
        if (searchLoading) {
            // 로딩 중 표시
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (searchResults.isEmpty()) {
            // 검색 결과가 없는 경우
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                cornerRadius = 16f,
                blurRadius = 10f
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "검색 결과가 없습니다",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        } else {
            // 검색 결과 카드 그리드 표시
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                cornerRadius = 16f,
                blurRadius = 10f
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // 카드 결과를 2열로 표시하기 위해 2개씩 묶어서 처리
                    val cardPairs = viewModel.searchResultsToCardInfo().chunked(2)
                    
                    items(cardPairs) { rowCards ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowCards.forEachIndexed { index, card ->
                                CardGridItem(
                                    card = card, 
                                    modifier = Modifier.weight(1f),
                                    onClick = { onCardClick(card) }
                                )
                            }
                            
                            // 행에 카드가 1개만 있는 경우 빈 공간 추가
                            if (rowCards.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    
                    // 하단 여백
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FilterButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
fun CardGridItem(
    card: CardInfo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 카드 이미지
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp))
                // 배경색 제거
        ) {
            if (card.hasValidImageUrl()) {
                NetworkImage(
                    url = card.normalizedImageUrl(),
                    contentDescription = card.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    rotationZ = 90f
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.card),
                    contentDescription = card.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .graphicsLayer(rotationZ = 90f)
                )
            }
        }
        
        // 카드 정보
        Text(
            text = card.name,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        // 혜택 요약 (첫 번째 혜택만 표시)
        if (card.benefits.isNotEmpty()) {
            Text(
                text = card.benefits.first(),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPage(
    category: String,
    onClose: () -> Unit,
    onApplyFilter: () -> Unit
) {
    // 필터 페이지 상태 관리
    val viewModel = viewModel<CardRecommendViewModel>()
    val currentParams by viewModel.currentSearchParams
    
    // 선택된 필터 값 (임시 상태)
    var selectedBenefitTypes by remember { mutableStateOf(currentParams.benefitType) }
    var selectedCardCompanies by remember { mutableStateOf(currentParams.cardCompany) }
    var selectedCategories by remember { mutableStateOf(currentParams.category) }
    var minPerformance by remember { mutableStateOf(currentParams.minPerformanceRange) }
    var maxPerformance by remember { mutableStateOf(currentParams.maxPerformanceRange) }
    var minAnnualFee by remember { mutableStateOf(currentParams.minAnnualFee) }
    var maxAnnualFee by remember { mutableStateOf(currentParams.maxAnnualFee) }
    
    // 슬라이더 위치 (0.0f ~ 1.0f)
    var performanceMinSliderPosition by remember { mutableStateOf(minPerformance / 1000000f) }
    var performanceMaxSliderPosition by remember { mutableStateOf(maxPerformance / 1000000f) }
    var annualFeeMinSliderPosition by remember { mutableStateOf(minAnnualFee / 20000f) }
    var annualFeeMaxSliderPosition by remember { mutableStateOf(maxAnnualFee / 20000f) }
    
    // 필터 옵션 데이터
    val benefitTypeOptions = listOf("DISCOUNT", "MILEAGE", "COUPON")
    val cardCompanyOptions = listOf("KB국민", "신한", "삼성", "현대", "롯데", "농협", "IBK", "우리")
    val categoryOptions = listOf("쇼핑", "교통", "통신", "식음료", "교육", "의료", "여행", "레저")
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "필터",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(onClick = onClose)
                )
            }
            
            // 필터 내용
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // 필터 카테고리에 따른 내용 표시
                when (category) {
                    "혜택 타입" -> {
                        item {
                Text(
                                text = "카드 종류",
                    color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                benefitTypeOptions.forEach { option ->
                                    FilterChip(
                                        option = option,
                                        isSelected = selectedBenefitTypes.contains(option),
                                        onToggle = {
                                            selectedBenefitTypes = if (selectedBenefitTypes.contains(option)) {
                                                selectedBenefitTypes - option
                                            } else {
                                                selectedBenefitTypes + option
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    "카드사" -> {
                        item {
                            Text(
                                text = "카드사",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            // 카드사 옵션을 2열 그리드로 표시
                            val rows = cardCompanyOptions.chunked(2)
                            rows.forEach { rowOptions ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowOptions.forEach { option ->
                                        FilterChip(
                                            option = option,
                                            isSelected = selectedCardCompanies.contains(option),
                                            onToggle = {
                                                selectedCardCompanies = if (selectedCardCompanies.contains(option)) {
                                                    selectedCardCompanies - option
                                                } else {
                                                    selectedCardCompanies + option
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    
                                    // 행에 하나만 있는 경우 빈 공간 추가
                                    if (rowOptions.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                    "카테고리" -> {
        item {
                            Text(
                                text = "카테고리",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            // 카테고리 옵션을 2열 그리드로 표시
                            val rows = categoryOptions.chunked(2)
                            rows.forEach { rowOptions ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowOptions.forEach { option ->
                                        FilterChip(
                                            option = option,
                                            isSelected = selectedCategories.contains(option),
                                            onToggle = {
                                                selectedCategories = if (selectedCategories.contains(option)) {
                                                    selectedCategories - option
                                                } else {
                                                    selectedCategories + option
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    
                                    // 행에 하나만 있는 경우 빈 공간 추가
                                    if (rowOptions.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                    "전월실적" -> {
                        item {
                            Text(
                                text = "전월실적",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            // 현재 선택된 값 표시
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${minPerformance}원",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "${maxPerformance}원",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                            
                            // 슬라이더
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            ) {
                                // 슬라이더 트랙
                                Box(
        modifier = Modifier
            .fillMaxWidth()
                                        .height(4.dp)
                                        .background(Color.Gray.copy(alpha = 0.5f))
                                        .align(Alignment.Center)
                                ) {
                                    // 선택된 범위 하이라이트
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(performanceMaxSliderPosition - performanceMinSliderPosition)
                                            .fillMaxHeight()
                                            .offset(x = (performanceMinSliderPosition * 100).dp)
                                            .background(Color.Cyan)
                                    )
                                }
                                
                                // 최소값 슬라이더 핸들
                                Slider(
                                    value = performanceMinSliderPosition,
                                    onValueChange = {
                                        // 최소값은 최대값보다 클 수 없음
                                        performanceMinSliderPosition = it.coerceAtMost(performanceMaxSliderPosition - 0.1f)
                                        minPerformance = (performanceMinSliderPosition * 1000000).toInt()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color.Transparent,
                                        inactiveTrackColor = Color.Transparent
                                    )
                                )
                                
                                // 최대값 슬라이더 핸들
                                Slider(
                                    value = performanceMaxSliderPosition,
                                    onValueChange = {
                                        // 최대값은 최소값보다 작을 수 없음
                                        performanceMaxSliderPosition = it.coerceAtLeast(performanceMinSliderPosition + 0.1f)
                                        maxPerformance = (performanceMaxSliderPosition * 1000000).toInt()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color.Transparent,
                                        inactiveTrackColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                    "연회비" -> {
                        item {
        Text(
                                text = "연회비",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
        )

                            // 현재 선택된 값 표시
                            Row(
            modifier = Modifier
                .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
        ) {
                Text(
                                    text = "${minAnnualFee}원",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "${maxAnnualFee}원",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                            
                            // 슬라이더
                            Box(
                    modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            ) {
                                // 슬라이더 트랙
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .background(Color.Gray.copy(alpha = 0.5f))
                                        .align(Alignment.Center)
                                ) {
                                    // 선택된 범위 하이라이트
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(annualFeeMaxSliderPosition - annualFeeMinSliderPosition)
                                            .fillMaxHeight()
                                            .offset(x = (annualFeeMinSliderPosition * 100).dp)
                                            .background(Color.Cyan)
                                    )
                                }
                                
                                // 최소값 슬라이더 핸들
                                Slider(
                                    value = annualFeeMinSliderPosition,
                                    onValueChange = {
                                        // 최소값은 최대값보다 클 수 없음
                                        annualFeeMinSliderPosition = it.coerceAtMost(annualFeeMaxSliderPosition - 0.1f)
                                        minAnnualFee = (annualFeeMinSliderPosition * 20000).toInt()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color.Transparent,
                                        inactiveTrackColor = Color.Transparent
                                    )
                                )
                                
                                // 최대값 슬라이더 핸들
                                Slider(
                                    value = annualFeeMaxSliderPosition,
                                    onValueChange = {
                                        // 최대값은 최소값보다 작을 수 없음
                                        annualFeeMaxSliderPosition = it.coerceAtLeast(annualFeeMinSliderPosition + 0.1f)
                                        maxAnnualFee = (annualFeeMaxSliderPosition * 20000).toInt()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color.Transparent,
                                        inactiveTrackColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                    else -> {
                        // 기본 필터 내용 (모든 필터 옵션 표시)
                        item {
                Text(
                                text = "모든 필터 옵션",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // 하단 버튼 영역 (고정)
            Row(
                    modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 초기화 버튼
                Button(
                    onClick = {
                        // 모든 필터 초기화
                        selectedBenefitTypes = listOf()
                        selectedCardCompanies = listOf()
                        selectedCategories = listOf()
                        minPerformance = 0
                        maxPerformance = 10000000
                        minAnnualFee = 0
                        maxAnnualFee = 20000
                        performanceMinSliderPosition = 0f
                        performanceMaxSliderPosition = 1f
                        annualFeeMinSliderPosition = 0f
                        annualFeeMaxSliderPosition = 1f
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Text("초기화")
                }
                
                // 결과보기 버튼
                Button(
                    onClick = {
                        // 선택된 필터를 ViewModel에 적용
                        viewModel.currentSearchParams.value = CardRecommendViewModel.SearchParams(
                            benefitType = selectedBenefitTypes,
                            cardCompany = selectedCardCompanies,
                            category = selectedCategories,
                            minPerformanceRange = minPerformance,
                            maxPerformanceRange = maxPerformance,
                            minAnnualFee = minAnnualFee,
                            maxAnnualFee = maxAnnualFee
                        )
                        onApplyFilter()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00BCD4),
                        contentColor = Color.White
                    )
                ) {
                    Text("결과보기")
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    option: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF00BCD4) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF00BCD4) else Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = option,
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CardCarousel(
    cards: List<CardInfo>,
    onCardClick: (CardInfo) -> Unit
) {
    val extendedCards = if (cards.size < 3) {
        cards + cards + cards
    } else {
        cards
    }.take(3)

    val pagerState = rememberPagerState(
        pageCount = { extendedCards.size },
        initialPage = 0
    )

    val density = LocalDensity.current.density
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                // 혜택 개수에 따라 박스 높이도 동적으로 조절
                when {
                    extendedCards.getOrNull(0)?.benefits?.size ?: 0 <= 1 -> 430.dp
                    extendedCards.getOrNull(0)?.benefits?.size == 2 -> 450.dp
                    else -> 480.dp
                }
            )
    ) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = (-20).dp,
            contentPadding = PaddingValues(horizontal = 70.dp),
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val pageOffset = (
                        (pagerState.currentPage - page) + pagerState
                            .currentPageOffsetFraction
                        ).absoluteValue

                val scale = 1.2f - (pageOffset * 0.3f).coerceIn(0f, 0.3f)
                val alpha = 1f - (pageOffset * 0.4f).coerceIn(0f, 0.4f)

                Box(
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            alpha = alpha,
                            clip = false,
                            cameraDistance = 12f * density
                        )
                        .clickable {
                            if (page != pagerState.currentPage) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(page)
                                }
                            } else {
                                onCardClick(extendedCards[page])
                            }
                        }
                ) {
                    // 전체 카드 컨테이너
                    // 혜택 개수에 따라 동적으로 높이 조절
                    val benefits = extendedCards[page].benefits
                    val containerHeight = when {
                        benefits.size <= 1 -> 430.dp
                        benefits.size == 2 -> 450.dp
                        else -> 480.dp
                    }
                    
                    Column(
                        modifier = Modifier.height(containerHeight),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 카드 이미지 부분
                        Box(
                            modifier = Modifier
                                .width(380.dp)
                                .height(340.dp) // 원래 값으로 복원 (300dp에서 340dp로)
                                .padding(horizontal = 8.dp)
                        ) {
                            val card = extendedCards[page]
                            
                            // API에서 받은 이미지 URL 사용
                            if (card.imgUrl.isNotEmpty() && card.imgUrl.startsWith("http")) {
                                // Coil 라이브러리를 사용한 비동기 이미지 로딩
                                val imgUrl = card.imgUrl
                                Log.d("CardRecommendScreen", "카테고리 카드 이미지 로드 시도: $imgUrl")
                                
                                // NetworkImage 컴포넌트 사용
                                NetworkImage(
                                    url = imgUrl,
                                    contentDescription = card.name,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp), // 원래 값으로 복원 (6dp에서 12dp로)
                                    rotationZ = 90f,
                                    fallbackResId = R.drawable.card,
                                    onError = {
                                        Log.e("CardRecommendScreen", "⚠️ 카테고리 카드 이미지 로드 실패: $imgUrl")
                                    },
                                    onSuccess = {
                                        Log.d("CardRecommendScreen", "✅ 카테고리 카드 이미지 로드 성공: $imgUrl")
                                    }
                                )
                            } else {
                                // 이미지 URL이 없는 경우 기본 이미지 표시
                            Image(
                                painter = painterResource(id = R.drawable.card),
                                    contentDescription = card.name,
                                    contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                        .padding(12.dp) // 원래 값으로 복원
                                    .graphicsLayer(rotationZ = 90f)
                            )
                                Log.d("CardRecommendScreen", "⚠️ 카드 URL이 비어있어 기본 이미지 사용: ${card.name}")
                            }
                        }

                        // 카드 정보 (혜택 및 이름) - 이미지와 분리된 별도 영역
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp) // 원래 값으로 복원 (24dp에서 16dp로)
                                .padding(top = 0.dp, bottom = 16.dp) // 원래 값으로 복원 (8dp에서 16dp로)
                                .background(Color.Transparent) // 배경색 제거하여 카드와 분리감 강화
                        ) {
                            // 카드 혜택 정보 - 여러 줄로 표시
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                benefits.take(2).forEach { benefit ->
                            Text(
                                        text = benefit,
                                color = Color.White,
                                        fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                            .padding(bottom = 4.dp)
                            )
                                }

                                // 더 많은 혜택이 있는 경우 "... 외 N개" 형식으로 표시
                                if (benefits.size > 2) {
                            Text(
                                        text = "... 외 ${benefits.size - 2}개",
                                        color = Color.White.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                            .padding(bottom = 4.dp)
                                    )
                                }
                            }

                            // 혜택 개수에 따라 공간 조절 - 공간 더 줄이기
                            val spacerHeight = when {
                                benefits.size <= 1 -> {
                                    Log.d("CardRecommendScreen", "Card has ${benefits.size} benefits - using 4.dp spacing")
                                    4.dp // 8dp에서 4dp로 축소
                                }
                                benefits.size == 2 -> {
                                    Log.d("CardRecommendScreen", "Card has ${benefits.size} benefits - using 6.dp spacing")
                                    6.dp // 12dp에서 6dp로 축소
                                }
                                else -> {
                                    Log.d("CardRecommendScreen", "Card has ${benefits.size} benefits - using 8.dp spacing")
                                    8.dp // 16dp에서 8dp로 축소
                                }
                            }
                            Spacer(
        modifier = Modifier
                                    .height(spacerHeight)
            .fillMaxWidth()
                            )

                            // 카드 이름 - 글자 크기 키우고 강조
                Text(
                                text = extendedCards[page].name,
                    color = Color.White,
                                fontSize = 18.sp, // 16sp에서 18sp로 증가
                                fontWeight = FontWeight.Bold, // 볼드 추가
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .padding(bottom = 16.dp) // 카드 이름 아래 여백 추가
                            )
                        }
                    }
                }
            }
        }
    }
}
