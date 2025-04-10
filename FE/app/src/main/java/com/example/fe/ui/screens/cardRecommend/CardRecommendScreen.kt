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
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import com.example.fe.ui.components.backgrounds.GlassSurface
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalConfiguration
import java.text.NumberFormat
import java.util.Locale
import com.example.fe.ui.components.navigation.BottomNavItem
import androidx.compose.material3.ExperimentalMaterial3Api

@Composable
fun CardRecommendScreen(
    viewModel: CardRecommendViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onCardClick: (Int) -> Unit,
    navController: NavHostController
) {
    val uiState = viewModel.uiState
    
    // 탭 인덱스를 ViewModel로 관리하여 화면이 재구성되어도 유지되도록 합니다
    var selectedTabIndex = remember { mutableStateOf(viewModel.selectedTabIndex) }
    
    // 선택된 탭이 변경될 때 ViewModel에 저장
    LaunchedEffect(selectedTabIndex.value) {
        viewModel.selectedTabIndex = selectedTabIndex.value
    }
    
    // 네비게이션 백스택 이벤트 감지
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    
    // 필터 선택 화면에서 돌아올 때 직접 찾기 탭이 선택되도록 합니다
    LaunchedEffect(navBackStackEntry) {
        val currentRoute = navBackStackEntry?.destination?.route
        if (currentRoute == BottomNavItem.CardRecommend.route) {
            val prevRoute = navController.previousBackStackEntry?.destination?.route
            if (prevRoute?.startsWith("filter_selection") == true) {
                selectedTabIndex.value = 1
            }
        }
    }

    // 카드 클릭 이벤트 핸들러 추가
    val handleCardClick: (Int) -> Unit = { cardId ->
        try {
            onCardClick(cardId)
        } catch (e: Exception) {
            Log.e("CardRecommendScreen", "Navigation error: ${e.message}")
        }
    }

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
                        .clickable { selectedTabIndex.value = 0 },
                    horizontalAlignment = CenterHorizontally
                ) {
                    Text(
                        text = "추천 카드",
                        color = if (selectedTabIndex.value == 0) Color.White else Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = if (selectedTabIndex.value == 0) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(2.dp)
                            .align(CenterHorizontally)
                            .background(
                                color = if (selectedTabIndex.value == 0) Color.White else Color.Transparent
                            )
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTabIndex.value = 1 },
                    horizontalAlignment = CenterHorizontally
                ) {
                    Text(
                        text = "직접 찾기",
                        color = if (selectedTabIndex.value == 1) Color.White else Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = if (selectedTabIndex.value == 1) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(2.dp)
                            .align(CenterHorizontally)
                            .background(
                                color = if (selectedTabIndex.value == 1) Color.White else Color.Transparent
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTabIndex.value) {
            0 -> PersonalizedRecommendations(
                uiState = uiState,
                onRefresh = { viewModel.loadRecommendations() },
                onCardClick = handleCardClick
            )
            1 -> CardFinder(
                filterTags = uiState.filterTags,
                onFilterChange = { category, option ->
                    viewModel.updateFilterAndSearch(category, option)
                },
                cards = uiState.searchResults?.mapNotNull { apiCard ->
                    try {
                        viewModel.mapApiCardToUiCard(apiCard)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList(),
                isLoading = uiState.isLoadingSearch,
                error = uiState.errorSearch,
                onCardClick = handleCardClick,
                navController = navController
            )
        }
    }
}

@Composable
fun PersonalizedRecommendations(
    uiState: CardRecommendUiState,
    onRefresh: () -> Unit,
    onCardClick: (Int) -> Unit
) {
    // 금액 포맷팅 함수
    val formatAmount = { amount: Int ->
        NumberFormat.getNumberInstance(Locale.KOREA).format(amount)
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 35.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "이번 달,",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "당신에게 가장 추천하는 카드",
                    color = Color.White,
                    fontSize = 20.sp,
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
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        text = "추천 TOP 3",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState.top3ForAll != null) {
                        Text(
                            text = "3개월 간 총 ${formatAmount(uiState.top3ForAll.amount)}원 썼어요",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                        )

                        val cards = uiState.top3ForAll.recommendCards?.map { cardInfo ->
                            cardInfo.cardId to ""  // 카드 ID만 전달
                        } ?: emptyList()
                        CardCarousel(cards = cards, onCardClick = onCardClick)
                    } else if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    } else if (uiState.error != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "데이터를 불러오는데 실패했습니다",
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = onRefresh,
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("다시 시도")
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // 카테고리별 추천 카드 섹션
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 카테고리 소개 텍스트 추가
                Text(
                    text = "카테고리별 추천 카드",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                Text(
                    text = "이전 3개월 소비를 바탕으로 AI가 추천해요",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // 카드가 있는 카테고리만 필터링
            val categoriesWithCards = uiState.categoryRecommendations?.filter { 
                it.recommendCards.isNotEmpty() 
            } ?: emptyList()
            
            if (categoriesWithCards.isNotEmpty()) {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16f,
                    blurRadius = 10f
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        categoriesWithCards.forEachIndexed { index, category ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "${category.categoryName} 혜택 추천",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${category.categoryName}에 ${formatAmount(category.amount)}원 썼어요",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                val cards = category.recommendCards.map { cardInfo ->
                                    cardInfo.cardId to ""
                                }

                                CardCarousel(cards = cards, onCardClick = onCardClick)
                                
                                // 마지막 카테고리가 아니면 구분선 추가
                                if (index < categoriesWithCards.size - 1) {
                                    Divider(
                                        color = Color(0xAA87CEEB),
                                        thickness = 1.dp,
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                            .fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 로딩 상태 표시
        if (uiState.isLoadingCategories) {
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
        } else if (uiState.errorCategories != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "카테고리 데이터를 불러오는데 실패했습니다",
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onRefresh,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("다시 시도")
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
    isLoading: Boolean,
    error: String?,
    onCardClick: (Int) -> Unit,
    navController: NavHostController
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 필터 카테고리 (고정 부분)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 35.dp, vertical = 8.dp)
        ) {
            // 필터 버튼들
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(end = 8.dp)
            ) {
                items(filterTags) { tag ->
                    // 카테고리 이름 변환
                    val displayName = when(tag.category) {
                        "타입" -> "혜택 타입"
                        else -> tag.category
                    }
                    
                    FilterCategoryButton(
                        displayName = displayName,
                        tag = tag,
                        onClick = {
                            // 필터 선택 페이지로 이동
                            navController.navigate("filter_selection/${tag.category}")
                        },
                        onOptionSelected = { option ->
                            onFilterChange(tag.category, option)
                        }
                    )
                }
            }
            
            // 정렬 및 필터 옵션
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "인기순",
                    color = Color.White,
                    fontSize = 18.sp
                )

                Text(
                    text = "이벤트 카드 제외",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
        
        // 카드 목록 (스크롤 부분)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 35.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
            } else if (error != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "카드를 불러오는데 실패했습니다",
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(cards) { card ->
                    CardListItem(card = card, onClick = { onCardClick(card.id) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun FilterCategoryButton(
    displayName: String,
    tag: FilterTag,
    onClick: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .wrapContentWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(Color(0x33FFFFFF))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(19.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayName,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            
            // 아래 방향 화살표 아이콘
            Text(
                text = "∨",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardCarousel(
    cards: List<Pair<Int, String>>,
    onCardClick: (Int) -> Unit
) {
    // 카드가 없는 경우 처리
    if (cards.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "추천 카드가 없습니다",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val pagerState = rememberPagerState(
        pageCount = { cards.size },
        initialPage = 0
    )

    val density = LocalDensity.current.density
    val coroutineScope = rememberCoroutineScope()

    // 화면 너비 가져오기
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    // 화면 높이 가져오기
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    
    // 카드 크기 계산 (화면 크기에 비례하도록)
    val cardWidth = screenWidth * 0.4f // 너비 비율 증가
    val cardHeight = screenHeight * 0.38f // 높이 비율 증가

    Column(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 카드 슬라이더
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .padding(vertical = 0.dp) // 패딩 유지
        ) {
            HorizontalPager(
                state = pagerState,
                pageSpacing = 4.dp, // 페이지 간격 줄임
                contentPadding = PaddingValues(horizontal = screenWidth * 0.15f), // 좌우 패딩 줄임
                modifier = Modifier.fillMaxWidth(),
                flingBehavior = PagerDefaults.flingBehavior(
                    state = pagerState,
                    snapPositionalThreshold = 0.1f
                ),
                key = { index -> cards[index].first }
            ) { page ->
                val cardId = cards[page].first
                
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val pageOffset = (
                            (pagerState.currentPage - page) + pagerState
                                .currentPageOffsetFraction
                            ).absoluteValue

                    // 카드 스케일 및 투명도 조정
                    val scale = 1f - (pageOffset * 0.15f).coerceIn(0f, 0.15f)  // 스케일 효과 감소
                    val alpha = 1f - (pageOffset * 0.3f).coerceIn(0f, 0.3f)  // 투명도 효과 감소
                    
                    Box(
                        modifier = Modifier
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                alpha = alpha,
                                clip = false,
                                cameraDistance = 8f * density
                            )
                            .clickable {
                                if (page != pagerState.currentPage) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(page)
                                    }
                                } else {
                                    onCardClick(cardId)
                                }
                            },
                    ) {
                        // 실제 API에서 받아온 카드 정보 가져오기 (cardId 기반)
                        val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<CardRecommendViewModel>()
                        val cardInfo = viewModel.uiState.categoryRecommendations?.flatMap { it.recommendCards }
                            ?.find { it.cardId == cardId }
                            ?: viewModel.uiState.top3ForAll?.recommendCards?.find { it.cardId == cardId }
                        
                        if (cardInfo != null && !cardInfo.imageUrl.isNullOrEmpty()) {
                            // API로부터 받은 이미지 URL 사용
                            Box(
                                modifier = Modifier
                                    .width(320.dp)  // 훨씬 더 큰 너비
                                    .height(480.dp)  // 훨씬 더 큰 높이
                                    .align(Alignment.Center)
                            ) {
                                AsyncImage(
                                    model = cardInfo.imageUrl,
                                    contentDescription = cardInfo.cardName,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer(
                                            rotationZ = 90f,
                                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                                        )
                                )
                            }
                        } else {
                            // 기본 카드 이미지 사용
                            Box(
                                modifier = Modifier
                                    .width(320.dp)  // 훨씬 더 큰 너비
                                    .height(480.dp)  // 훨씬 더 큰 높이
                                    .align(Alignment.Center)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.card),
                                    contentDescription = "카드 이미지",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer(
                                            rotationZ = 90f,
                                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 페이지 인디케이터
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 1.dp, bottom = 1.dp) // 패딩 더 줄임
        ) {
            repeat(cards.size) { index ->
                val isSelected = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)  // 간격 줄임
                        .size(if (isSelected) 7.dp else 5.dp)  // 크기 더 줄임
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
                        )
                )
            }
        }

        // 카드 정보 (현재 선택된 카드)
        if (cards.isNotEmpty() && pagerState.currentPage < cards.size) {
            // 선택된 카드의 ID
            val selectedCardId = cards[pagerState.currentPage].first
            
            // cardId를 사용하여 카드 정보 찾기
            val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<CardRecommendViewModel>()
            val cardInfo = viewModel.uiState.categoryRecommendations?.flatMap { it.recommendCards }
                ?.find { it.cardId == selectedCardId }
                ?: viewModel.uiState.top3ForAll?.recommendCards?.find { it.cardId == selectedCardId }
            
            if (cardInfo != null) {
                // 카드 혜택 정보 추출
                val benefits = cardInfo.cardInfo.split(",")
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp) // 패딩 더 줄임
                ) {
                    Text(
                        text = cardInfo.cardName,
                        fontSize = 18.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // 혜택 목록 표시 - 항상 최소 첫 번째 혜택은 표시
                    if (benefits.isNotEmpty()) {
                        Text(
                            text = benefits[0].trim(),
                            fontSize = 16.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 1.dp)  // 패딩 최소화
                        )
                    }
                    
                    // 두 번째 혜택이 있다면 간략하게 표시
                    if (benefits.size > 1) {
                        Text(
                            text = if (benefits.size > 2) 
                                "${benefits[1].trim()} 외 ${benefits.size - 2}개 혜택" 
                            else 
                                benefits[1].trim(),
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CardListItem(
    card: CardInfo,
    onClick: (Int) -> Unit
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        cornerRadius = 12f
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { onClick(card.id) })
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 카드 이미지: 비율 유지하며 조정
            Image(
                painter = painterResource(id = R.drawable.card), // ← 네 카드 이미지
                contentDescription = card.name,
                contentScale = ContentScale.Fit, // 비율 유지
                modifier = Modifier
                    .width(72.dp)
                    .height(48.dp) // 가로로 긴 카드 형태 반영
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
            )

            // 카드 정보
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = card.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    card.icons.forEach { icon ->
                        Text(
                            text = icon,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
