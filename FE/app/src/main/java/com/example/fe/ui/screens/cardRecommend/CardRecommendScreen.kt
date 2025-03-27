package com.example.fe.ui.screens.cardRecommend

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabPosition
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.foundation.lazy.itemsIndexed
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
)

// 필터 태그 데이터 클래스
data class FilterTag(
    val category: String,
    val options: List<String>,
    val selectedOption: String
)

@Composable
fun CardRecommendScreen(
    modifier: Modifier = Modifier,
    onScrollOffsetChange: (Float) -> Unit = {},
    onCardClick: (CardInfo) -> Unit = {}
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    // 샘플 카드 데이터
    val sampleCards = remember {
        listOf(
            CardInfo(
                id = 1,
                name = "토스 신한카드 Mr.Life",
                company = "신한카드",
                benefits = listOf(
                    "카페 10% 할인",
                    "편의점, 외식 10% 할인",
                    "병원, 약국, 전기, 가스요금, 해외 10% 할인"
                ),
                annualFee = "30,000원",
                minSpending = "월 30만원",
                icons = listOf("식당", "교통")
            )
        )
    }

    // 필터 태그 상태
    var filterTags by remember {
        mutableStateOf(
            listOf(
                FilterTag("타입", listOf("할인", "적립"), "전체"),
                FilterTag("카드사", listOf("KB국민", "IBK", "농협"), "전체"),
                FilterTag("카테고리", listOf("교통", "음식", "교육", "여가"), "전체"),
                FilterTag("전월 실적", listOf("30만원 미만", "30~50만원"), "전체"),
                FilterTag("연회비", listOf("만원 미만", "1~2만원"), "전체")
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 상단 영역
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            // 탭 바
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 왼쪽 탭 (추천 카드)
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
                    
                    // 인디케이터
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
                
                // 오른쪽 탭 (직접 찾기)
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
                    
                    // 인디케이터
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

        when (selectedTabIndex) {
            0 -> PersonalizedRecommendations(
                cards = sampleCards,
                onCardClick = onCardClick
            )
            1 -> CardFinder(
                filterTags = filterTags,
                onFilterChange = { category, option ->
                    filterTags = filterTags.map {
                        if (it.category == category) it.copy(selectedOption = option)
                        else it
                    }
                },
                cards = sampleCards,
                onCardClick = onCardClick
            )
        }
    }
}

@Composable
fun PersonalizedRecommendations(
    cards: List<CardInfo>,
    onCardClick: (CardInfo) -> Unit
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
                    color = Color.White,
                    fontSize = 14.sp
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
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFDDDDEE)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "추천 TOP 3",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "3개월 간 총 500,000원 썼어요",
                        color = Color.DarkGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 카드 캐러셀
                    CardCarousel(cards = cards, onCardClick = onCardClick)
                }
            }
        }

        item {
            Text(
                text = "카테고리별 추천 카드",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "이전 3개월 소비를 바탕으로 API가 추천해요",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        item {
            Text(
                text = "병원/약국 혜택 추천",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "병원/약국에 1,240,000원 썼어요",
                color = Color.Gray,
                fontSize = 12.sp
            )
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 필터 태그들
        items(filterTags) { tag ->
            FilterTagRow(
                tag = tag,
                onOptionSelected = { option ->
                    onFilterChange(tag.category, option)
                }
            )
        }

        // 구분선
        item {
            Divider(
                color = Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // 카드 목록
        items(cards) { card ->
            SimpleCardItem(card = card, onClick = { onCardClick(card) })
        }
    }
}

@Composable
fun FilterTagRow(
    tag: FilterTag,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tag.category,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.width(80.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tag.options.forEach { option ->
                val isSelected = tag.selectedOption == option
                Text(
                    text = option,
                    color = if (isSelected) Color.Yellow else Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onOptionSelected(option) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardCarousel(
    cards: List<CardInfo>,
    onCardClick: (CardInfo) -> Unit
) {
    // 여러 카드를 표시하기 위한 데이터 확장 (최소 3개)
    val extendedCards = if (cards.size < 3) {
        cards + cards + cards // 카드가 적으면 반복
    } else {
        cards
    }.take(3)
    
    // 페이저 상태
    val pagerState = rememberPagerState(
        pageCount = { extendedCards.size },
        initialPage = 0
    )

    // 현재 화면 밀도 가져오기
    val density = LocalDensity.current.density
    
    // 코루틴 스코프
    val coroutineScope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(460.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = (-50).dp,
            contentPadding = PaddingValues(horizontal = 40.dp)
        ) { page ->
            // 현재 페이지와의 거리 계산
            val pageOffset = (
                    (pagerState.currentPage - page) + pagerState
                        .currentPageOffsetFraction
                    ).absoluteValue

            // 현재 카드는 더 크게, 다른 카드는 작게
            val scale = if (page == pagerState.currentPage) 1.2f else 0.8f
            
            Box(
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        alpha = if (page == pagerState.currentPage) 1f else 0.6f,
                        clip = false,
                        cameraDistance = 12f * density
                    )
            ) {
                // 카드와 텍스트를 포함하는 전체 Box
                Box(
                    modifier = Modifier.height(430.dp)
                ) {
                    // 카드 이미지
                    Box(
                        modifier = Modifier
                            .width(300.dp)
                            .height(400.dp)
                            .padding(8.dp)
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
                        Image(
                            painter = painterResource(id = R.drawable.card),
                            contentDescription = extendedCards[page].name,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    rotationZ = 90f // 세로로 회전
                                )
                        )
                    }
                    
                    // 텍스트 컬럼
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 0.dp, start = 8.dp, end = 8.dp)
                    ) {
                        // 대표 혜택
                        Text(
                            text = "카페 10% 할인",
                            color = Color.Black,
                            fontSize = if (page == pagerState.currentPage) 18.sp else 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // 카드 이름
                        Text(
                            text = extendedCards[page].name,
                            color = Color.Black,
                            fontSize = if (page == pagerState.currentPage) 14.sp else 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CardItem(
    card: CardInfo,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(300.dp) // 너비 증가
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 카드 이미지
        Box(
            modifier = Modifier
                .width(300.dp) // 너비 증가
                .height(420.dp) // 높이 증가 (비율 유지)
                .clip(RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.card),
                contentDescription = card.name,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        rotationZ = 90f // 세로로 회전
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 카드 혜택 및 이름
        Text(
            text = "카페 10% 할인",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = card.name,
            color = Color.DarkGray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun RecommendedCardItem(
    card: CardInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3A3A50)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 카드 이미지 (실제로는 Image 컴포넌트로 대체)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFF4A4A60), RoundedCornerShape(8.dp))
            )
            
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
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    card.icons.forEach { icon ->
                        Text(
                            text = icon,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleCardItem(
    card: CardInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A40)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 카드 이미지 (실제로는 Image 컴포넌트로 대체)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF4A4A60), RoundedCornerShape(8.dp))
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = card.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    card.icons.forEach { icon ->
                        Text(
                            text = icon,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
