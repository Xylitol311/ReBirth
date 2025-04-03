package com.example.fe.ui.screens.cardRecommend

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
    onCardClick: (CardInfo) -> Unit = {}
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

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
                onCardClick = onCardClick
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

        item {
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16f,
                blurRadius = 10f
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "카테고리별 추천 카드",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "이전 3개월 소비를 바탕으로 API가 추천해요",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CardCarousel(cards = cards, onCardClick = onCardClick)
                }
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
}


@Composable
fun CardFinder(
    filterTags: List<FilterTag>,
    onFilterChange: (String, String) -> Unit,
    cards: List<CardInfo>,
    onCardClick: (CardInfo) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    var maxLabelWidth by remember { mutableStateOf(0.dp) }

    LaunchedEffect(filterTags) {
        val maxText = filterTags.maxByOrNull { it.category.length }?.category ?: ""
        val textLayoutResult = textMeasurer.measure(text = maxText)
        maxLabelWidth = with(density) { textLayoutResult.size.width.toDp() }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                cornerRadius = 16f
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    filterTags.forEach { tag ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            FilterTagRow(
                                tag = tag,
                                labelPadding = maxLabelWidth,
                                onOptionSelected = { option ->
                                    onFilterChange(tag.category, option)
                                }
                            )

                            if (tag != filterTags.last()) {
                                Divider(
                                    color = Color.Gray.copy(alpha = 0.3f),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
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

        items(cards) { card ->
            CardListItem(card = card, onClick = { onCardClick(card) })
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}


@Composable
fun FilterTagRow(
    tag: FilterTag,
    labelPadding: Dp,
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = tag.category,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(end = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = labelPadding)
        ) {
            item {
                val isSelected = tag.selectedOption == "전체"
                Text(
                    text = "전체",
                    color = if (isSelected) Color(0xFFFFF176) else Color.White,
                    fontSize = 20.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .clickable { onOptionSelected("전체") }
                        .padding(vertical = 4.dp)
                )
            }

            items(tag.options) { option ->
                val isSelected = tag.selectedOption == option
                Text(
                    text = option,
                    color = if (isSelected) Color(0xFFFFF176) else Color.White,
                    fontSize = 20.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .clickable { onOptionSelected(option) }
                        .padding(vertical = 4.dp)
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
            .height(470.dp)
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
                    Box(
                        modifier = Modifier.height(420.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(380.dp)
                                .height(340.dp)
                                .padding(top = 0.dp, bottom = 0.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.card),
                                contentDescription = extendedCards[page].name,
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(rotationZ = 90f)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(top = 10.dp)
                                .padding(bottom = 15.dp)
                                .padding(horizontal = 24.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "카페 10% 할인",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )

                            Text(
                                text = extendedCards[page].name,
                                color = Color.White,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CardListItem(
    card: CardInfo,
    onClick: () -> Unit
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
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
