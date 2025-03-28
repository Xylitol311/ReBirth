package com.example.fe.ui.screens.myCard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.R
import kotlin.math.absoluteValue
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.fe.ui.components.backgrounds.StarryBackground
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import kotlinx.coroutines.delay
import androidx.compose.animation.core.EaseInOut
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.mutableStateListOf
// CardItem.kt의 클래스들을 가져옴
import com.example.fe.ui.screens.myCard.CardItem
import com.example.fe.ui.screens.myCard.CardItemWithVisibility
import com.example.fe.ui.screens.myCard.CardOrderManager

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MyCardScreen(
    modifier: Modifier = Modifier,
    onScrollOffsetChange: (Float) -> Unit = {},
    onCardClick: (CardItem) -> Unit = {},
    onManageCardsClick: () -> Unit = {}
) {
    // 현재 화면 밀도 가져오기
    val density = LocalDensity.current.density
    
    // 초기 카드 데이터 - 관리 화면에서 정렬된 데이터가 없을 경우에만 사용
    val initialCards = remember {
        listOf(
            CardItem(1, "토스 신한카드 Mr.Life", "•••• •••• •••• 3456"),
            CardItem(2, "현대카드", "•••• •••• •••• 4567"),
            CardItem(3, "삼성카드", "•••• •••• •••• 5678")
        )
    }
    
    // 카드 관리 매니저 초기화
    LaunchedEffect(Unit) {
        // 초기 데이터 설정 (비어있을 경우에만)
        CardOrderManager.initializeIfEmpty(initialCards.map { CardItemWithVisibility(it) })
    }
    
    // 카드 관리 매니저에서 카드 목록 가져오기
    val managedCards = remember { CardOrderManager.sortedCards }
    
    // 표시할 카드 목록
    val realCards = if (managedCards.isNotEmpty()) {
        // 카드 관리 화면에서 설정한 카드 목록 (표시 상태가 true인 것만)
        managedCards.filter { it.isVisible }.map { it.card }
    } else {
        // 초기 카드 목록
        initialCards
    }
    
    // 카드 관리 매니저 리스너 등록 (카드 순서 변경 감지)
    DisposableEffect(Unit) {
        val listener = {
            // 리스너는 비어있어도 됨 (상태 변경 감지만 필요)
            // remember 블록이 재실행되어 표시할 카드 목록이 업데이트됨
        }
        CardOrderManager.addListener(listener)
        
        // 정리 함수 (화면이 사라질 때 호출)
        onDispose {
            CardOrderManager.removeListener(listener)
        }
    }

    // 페이저 상태
    val pagerState = rememberPagerState(
        pageCount = { realCards.size },
        initialPage = 0
    )

    // 스크롤 오프셋 (별 배경 이동을 위해)
    var scrollOffset by remember { mutableStateOf(0f) }

    // 카드 슬라이드에 따른 배경 이동 계산
    LaunchedEffect(pagerState) {
        snapshotFlow {
            pagerState.currentPage * 800f + pagerState.currentPageOffsetFraction * 1200f
        }.distinctUntilChanged().collect { offset: Float ->
            scrollOffset = offset
            onScrollOffsetChange(offset)
        }
    }

    // 현재 실제 카드 인덱스
    val currentRealCardIndex by remember {
        derivedStateOf {
            pagerState.currentPage.coerceIn(0, realCards.size - 1)
        }
    }

    val coroutineScope = rememberCoroutineScope()

    // 네비게이션 상태 (카드 애니메이션용)
    var isNavigating by remember { mutableStateOf(false) }
    var navigatingCardPage by remember { mutableStateOf(-1) }
    
    // 카드 초기 등장 애니메이션 상태
    var cardsAppeared by remember { mutableStateOf(false) }
    
    // 카드 등장 애니메이션 시작
    LaunchedEffect(Unit) {
        delay(300) // 약간의 지연 후 시작
        cardsAppeared = true
    }
    
    // 카드 등장 애니메이션 값
    val cardsAppearTranslationY by animateFloatAsState(
        targetValue = if (cardsAppeared) 0f else 800f, // 아래에서 올라오는 효과
        animationSpec = tween(700, easing = EaseInOut),
        label = "cardsAppearTranslationY"
    )
    
    // 카드 등장 애니메이션 알파값
    val cardsAppearAlpha by animateFloatAsState(
        targetValue = if (cardsAppeared) 1f else 0f,
        animationSpec = tween(500, easing = EaseInOut),
        label = "cardsAppearAlpha"
    )
    
    // UI 페이드 아웃 애니메이션
    val uiAlpha by animateFloatAsState(
        targetValue = if (isNavigating) 0f else 1f,
        animationSpec = tween(300, easing = EaseInOut),
        label = "uiAlpha"
    )
    
    // UI 위치 애니메이션
    val uiTranslationY by animateFloatAsState(
        targetValue = if (isNavigating) -100f else 0f,
        animationSpec = tween(300, easing = EaseInOut),
        label = "uiTranslationY"
    )
    
    // 배경과 콘텐츠를 함께 배치
    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 (스크롤에 따라 움직임)
        StarryBackground(
            scrollOffset = 0f, // 세로 스크롤 오프셋은 0으로 고정
            starCount = 150,
            horizontalOffset = scrollOffset, // 가로 스크롤 오프셋 전달
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // 네비게이션 중일 때 배경을 어둡게
                    alpha = if (isNavigating) 0.7f else 1f
                }
        ) {
            // 빈 Box - 배경만 표시
        }

        // 나머지 UI 요소 (헤더와 카드 이름)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // 네비게이션 중일 때 UI 요소 사라짐 - 애니메이션 적용
                    alpha = uiAlpha
                    translationY = uiTranslationY
                }
        ) {
            // 헤더 텍스트
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 헤더 줄 (제목과 관리 버튼)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 왼쪽 여백용 투명 아이콘
                    Spacer(modifier = Modifier.width(36.dp))
                    
                    // 중앙 제목
                    Text(
                        text = "내 카드",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE0E0E0)
                    )
                    
                    // 오른쪽 관리 버튼
                    IconButton(
                        onClick = {
                            // 페이드 아웃 애니메이션 시작
                            isNavigating = true
                            
                            // 페이드 아웃 후 카드 관리 화면으로 이동
                            coroutineScope.launch {
                                // 페이드 아웃 애니메이션 완료 대기
                                delay(300)
                                onManageCardsClick()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .graphicsLayer {
                                alpha = uiAlpha
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "카드 관리",
                            tint = Color(0xFFE0E0E0)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "보유 중인 카드 목록",
                    fontSize = 16.sp,
                    color = Color(0xFFE0E0E0)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // 카드 이름을 별도로 배치 (카드 바로 위에)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 370.dp)
                .graphicsLayer {
                    alpha = if (isNavigating) uiAlpha else cardsAppearAlpha
                    translationY = if (isNavigating) uiTranslationY else cardsAppearTranslationY
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            if (realCards.isNotEmpty()) {
                Text(
                    text = realCards[currentRealCardIndex].name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE0E0E0),
                    textAlign = TextAlign.Center
                )
            }
        }

        // 카드 슬라이더 (화면 중앙 하단에 배치)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 30.dp)
                .graphicsLayer {
                    // 등장 애니메이션 적용
                    translationY = cardsAppearTranslationY
                    alpha = cardsAppearAlpha
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            HorizontalPager(
                state = pagerState,
                pageSpacing = 0.dp,
                contentPadding = PaddingValues(start = 85.dp, end = 75.dp),
                userScrollEnabled = !isNavigating // 네비게이션 중에는 스크롤 불가
            ) { page ->
                // 현재 페이지와의 거리 계산
                val pageOffset = (
                        (pagerState.currentPage - page) + pagerState
                            .currentPageOffsetFraction
                        ).absoluteValue

                // 현재 카드는 더 크게, 다른 카드는 작게
                val baseScale = if (page == pagerState.currentPage) 1.2f else 0.9f
                
                // 각 카드에 대한 애니메이션 결정
                val isSelected = isNavigating && page == navigatingCardPage
                val isNonSelected = isNavigating && page != navigatingCardPage
                
                // 모든 카드의 투명도 애니메이션 - 동일한 속도로
                val cardAlpha by animateFloatAsState(
                    targetValue = if (isNavigating) 0f else 1f,
                    animationSpec = tween(300, easing = EaseInOut),
                    label = "cardAlpha"
                )
                
                // 선택된 카드의 Z-Index
                val zIndex by animateFloatAsState(
                    targetValue = if (isSelected) 100f else 0f,
                    animationSpec = tween(100),
                    label = "zIndex"
                )
                
                // 선택된 카드 스케일 - 약간 확대
                val selectedScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.0f else baseScale,
                    animationSpec = tween(300),
                    label = "selectedScale"
                )
                
                // 카드 이미지 - res/drawable의 card.png 사용
                Box(
                    modifier = Modifier
                        .width(240.dp)
                        .height(340.dp)
                        .padding(8.dp)
                        .zIndex(zIndex)
                        .clickable(enabled = !isNavigating) {
                            // 현재 페이지가 아닌 경우 해당 카드로 스크롤
                            if (page != pagerState.currentPage) {
                                // 코루틴 스코프 필요
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(page)
                                }
                            } else {
                                // 현재 카드(중앙에 있는 카드)만 클릭 시 상세 화면으로 이동
                                isNavigating = true
                                navigatingCardPage = page

                                // 페이드 아웃 후 상세 화면으로 이동
                                coroutineScope.launch {
                                    // 페이드 아웃 애니메이션 완료 대기
                                    delay(300)
                                    onCardClick(realCards[page])
                                }
                            }
                        }
                        .graphicsLayer(
                            scaleX = selectedScale,
                            scaleY = selectedScale,
                            alpha = cardAlpha,
                            clip = false,
                            cameraDistance = 12f * density
                        )
                ) {
                    // 카드 이미지 (세로로 회전된 상태 유지)
                    Image(
                        painter = painterResource(id = R.drawable.card),
                        contentDescription = "카드 이미지",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                rotationZ = 90f // 세로로 회전
                            )
                    )
                }
            }
        }
    }
}