package com.example.fe.ui.screens.myCard

import android.annotation.SuppressLint
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fe.ui.components.cards.VerticalCardLayout
import com.example.fe.ui.screens.myCard.MyCardViewModel.CardItem
import com.example.fe.ui.screens.myCard.MyCardViewModel.CardItemWithVisibility
import com.example.fe.ui.screens.myCard.MyCardViewModel.CardOrderManager
import com.example.fe.ui.screens.myCard.MyCardViewModel.MyCardUiState
import java.text.NumberFormat
import java.util.Locale

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MyCardScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onScrollOffsetChange: (Float) -> Unit = {},  // 세로 스크롤 오프셋 콜백
    onHorizontalOffsetChange: (Float) -> Unit = {},  // 가로 스크롤 오프셋 콜백 추가
    onCardClick: (CardItem) -> Unit = {},
    onManageCardsClick: () -> Unit = {},
    viewModel: MyCardViewModel = viewModel()
) {
    // 현재 화면 밀도 가져오기
    val density = LocalDensity.current.density

    // 화면 너비 가져오기
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    // ViewModel에서 상태 가져오기
    val uiState by viewModel.uiState.collectAsState()
    val isLoading = viewModel.isLoading

    // 카드 관리 매니저 초기화
    LaunchedEffect(Unit) {
        viewModel.loadMyCards()
    }

    // 표시할 카드 목록
    val realCards = when (uiState) {
        is MyCardUiState.Success -> {
            val cards = (uiState as MyCardUiState.Success).cards
            // 카드 관리 매니저에서 카드 목록 가져오기
            val managedCards = CardOrderManager.sortedCards

            if (managedCards.isNotEmpty()) {
                // 카드 관리 화면에서 설정한 카드 목록 (표시 상태가 true인 것만)
                managedCards.filter { it.isVisible }.map { it.card }
            } else {
                // API에서 가져온 카드 목록
                cards
            }
        }
        else -> emptyList()
    }

    // 카드 관리 매니저 초기화 (API에서 데이터를 가져왔을 때만)
    LaunchedEffect(uiState) {
        if (uiState is MyCardUiState.Success) {
            val cards = (uiState as MyCardUiState.Success).cards
            if (cards.isNotEmpty()) {
                CardOrderManager.initializeIfEmpty(cards.map { CardItemWithVisibility(it) })
            }
        }
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
    // 페이저 스냅 동작 개선을 위한 설정
    val flingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapPositionalThreshold = 0.1f // 더 낮은 값으로 설정하여 더 빠르게 스냅되도록 함
    )

    // 스크롤 오프셋 (별 배경 이동을 위해)
    var scrollOffset by remember { mutableStateOf(0f) }
    var horizontalOffset by remember { mutableStateOf(0f) }

    // 카드 슬라이드에 따른 배경 이동 계산
    LaunchedEffect(pagerState) {
        snapshotFlow {
            // 여기서는 카드 슬라이드 위치만 추적하고 배경 이동은 계산하지 않음
            pagerState.currentPage to pagerState.currentPageOffsetFraction
        }.distinctUntilChanged().collect { (page, offset) ->
            // 카드 슬라이드 위치 업데이트
            horizontalOffset = page * 1000f + offset * 1600f
        }
    }


    // 현재 실제 카드 인덱스
    // 1. currentRealCardIndex를 mutableStateOf로 명확하게 정의
    var currentRealCardIndex by remember { mutableStateOf(0) }

    // 3. 단일 LaunchedEffect로 페이지 변경 감지 (중복 제거)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged() // 중복 이벤트 방지
            .collect { page ->
                if (realCards.isNotEmpty() && page < realCards.size) {
                    currentRealCardIndex = page
                    viewModel.updateSelectedCardIndex(page)
                }
            }
    }

    // 4. 카드 데이터 로드 완료 시 초기 카드 선택 강제 설정
    LaunchedEffect(realCards) {
        if (realCards.isNotEmpty()) {
            // 현재 페이지 강제 설정 (이미 0이더라도 상태 업데이트 트리거)
            currentRealCardIndex = pagerState.currentPage
            viewModel.updateSelectedCardIndex(currentRealCardIndex)
        }
    }

    val coroutineScope = rememberCoroutineScope()

    // 네비게이션 상태 (카드 애니메이션용)0
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
        targetValue = if (cardsAppeared) 0f else 800f, // 더 아래에서 올라오는 효과
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

    //콘텐츠를 배치
    Box(modifier = Modifier.fillMaxSize()) {
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
                    .padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 헤더 줄 (제목과 관리 버튼)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 왼쪽 여백용 투명 아이콘
                    Spacer(modifier = Modifier.width(30.dp))
                    
                    // 중앙 제목
                    Text(
                        text = "내 카드",
                        fontSize = 20.sp,
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
                            .size(30.dp)
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


                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "보유 중인 카드 목록",
                    fontSize = 14.sp,
                    color = Color(0xFFE0E0E0)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }


        // 로딩 인디케이터 추가
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFE0E0E0)
                )
            }
        }

        // 에러 메시지 표시
        if (uiState is MyCardUiState.Error) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (uiState as MyCardUiState.Error).message,
                    color = Color.Red,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // 등장 애니메이션 적용
                    translationY = cardsAppearTranslationY
                    alpha = cardsAppearAlpha
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // 중앙 정렬

        ) {
            Spacer(modifier = Modifier.height(40.dp))
            // 카드 슬라이더 (화면 중앙에 배치)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp), // 카드 높이 고정
                contentAlignment = Alignment.Center // 중앙 정렬
            ) {
                // 중앙 정렬을 위한 Box
                if (realCards.isNotEmpty()) {
                    // 카드 너비와 화면 너비 계산
                    val cardWidth = 230.dp
                    val horizontalPadding = (screenWidth - cardWidth) / 2

                    HorizontalPager(
                        state = pagerState,
                        pageSpacing = 0.dp,
                        flingBehavior = flingBehavior,
                        contentPadding = PaddingValues(
                            start = horizontalPadding,
                            end = horizontalPadding
                        ),
                        userScrollEnabled = !isNavigating,
                        pageSize = PageSize.Fixed(cardWidth),
                        key = { it }
                    ) { page ->
                        // 현재 페이지와의 거리 계산
                        val pageOffset = (
                                (pagerState.currentPage - page) + pagerState
                                    .currentPageOffsetFraction
                                ).absoluteValue

                        // 현재 카드는 더 크게, 다른 카드는 작게
                        val baseScale = if (page == pagerState.currentPage) 1.3f else 0.85f

                        // 각 카드에 대한 애니메이션 결정
                        val isSelected = isNavigating && page == navigatingCardPage
                        val isNonSelected = isNavigating && page != navigatingCardPage

                        // 모든 카드의 투명도 애니메이션
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

                        // 선택된 카드 스케일
                        val selectedScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.0f else baseScale,
                            animationSpec = tween(300),
                            label = "selectedScale"
                        )

                        // 카드 이미지
                        Box(
                            modifier = Modifier
                                .width(230.dp)
                                .height(300.dp)
                                .zIndex(zIndex)
                                .graphicsLayer(
                                    scaleX = selectedScale,
                                    scaleY = selectedScale,
                                    alpha = cardAlpha,
                                    clip = false,
                                    cameraDistance = 12f * density
                                )
                        ) {
                            if (realCards[page].imageUrl.isNotEmpty()) {
                                // URL이 있는 경우 URL 버전의 VerticalCardLayout 사용
                                VerticalCardLayout(
                                    cardImageUrl = realCards[page].imageUrl,
                                    width = 230.dp,
                                    height = 300.dp,
                                    cornerRadius = 13.dp,
                                    onClick = {
                                        if (page == pagerState.currentPage) {
                                            isNavigating = true
                                            navigatingCardPage = page

                                            coroutineScope.launch {
                                                delay(300)
                                                onCardClick(realCards[page])
                                            }
                                        } else {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(page)
                                            }
                                        }
                                    }
                                )
                            } else {

                            }
                        }
                    }
                } else if (!isLoading && uiState !is MyCardUiState.Error) {
                    // 카드가 없는 경우 메시지 표시
                    Text(
                        text = "등록된 카드가 없습니다",
                        color = Color(0xFFE0E0E0),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // 카드 이름을 별도로 배치 (카드 바로 아래)
            if (realCards.isNotEmpty() && pagerState.currentPage < realCards.size) {
                val currentCardIndex = pagerState.currentPage

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = realCards[currentCardIndex].name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00BCD4),
                        textAlign = TextAlign.Center,
                        maxLines = 1,                      // 한 줄로 제한
                        overflow = TextOverflow.Ellipsis,  // 넘치면 말줄임표로 표시
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)   // 양쪽 여백 추가
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 사용 금액 진행 상태
                    Column(
                        modifier = Modifier
                            .width(260.dp)
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row {
                                Text(
                                    text = "사용 금액",
                                    fontSize = 14.sp,
                                    color = Color(0xFF00BCD4)
                                )
                                Text(
                                    text = " / 실적",
                                    fontSize = 14.sp,
                                    color = Color(0xFFE0E0E0)
                                )
                            }

                            // 현재 사용 금액에 따른 실적 구간 계산
                            val currentPerformanceAmount = realCards[currentCardIndex].totalSpending
                            val performanceRanges = realCards[currentCardIndex].performanceRange

                            // 현재 사용 금액에 해당하는 실적 구간 찾기
                            val currentPerformanceTarget = performanceRanges.lastOrNull { range ->
                                currentPerformanceAmount >= range
                            } ?: performanceRanges.firstOrNull() ?: 0

                            // 다음 실적 구간 찾기
                            val nextPerformanceTarget = performanceRanges.find { range ->
                                range > currentPerformanceAmount
                            } ?: performanceRanges.lastOrNull() ?: 0

                            Row {
                                Text(
                                    text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(currentPerformanceAmount)}원",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00BCD4)
                                )
                                Text(
                                    text = " / ${NumberFormat.getNumberInstance(Locale.KOREA).format(nextPerformanceTarget)}원",
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 프로그레스 바 컨테이너
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                        ) {
                            // 배경 바
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x33FFFFFF))
                            )

                            // 현재 진행률 계산 (0.0 ~ 1.0)
                            val currentAmount = realCards[currentCardIndex].totalSpending.toFloat()
                            val maxAmount = realCards[currentCardIndex].performanceRange.lastOrNull()?.toFloat() ?:
                            realCards[currentCardIndex].maxSpending.toFloat()
                            val currentProgress = (currentAmount / maxAmount).coerceIn(0f, 1f)

                            // 현재 실적 바
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(currentProgress)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF00BCD4))
                            )

                            // 구간 마커 배치
                            val performanceRanges = realCards[currentCardIndex].performanceRange

                            // 구간이 1개 이상일 때만 마커 표시 (마지막 인덱스는 제외)
                            if (performanceRanges.size > 1) {
                                // 레이아웃 내에서 마커 위치 계산을 위한 BoxWithConstraints 사용
                                BoxWithConstraints(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    val barWidth = maxWidth

                                    // 마커 개수 (마지막 인덱스 제외)
                                    val markerCount = performanceRanges.size - 1

                                    // 각 구간별 마커 표시 (마지막 인덱스 제외)
                                    for (i in 0 until markerCount) {
                                        // 마커 위치는 균등 분할 (예: 마커가 2개면 33%, 66% 위치)
                                        val position = (i + 1).toFloat() / (markerCount + 1).toFloat()
                                        val xOffset = barWidth * position - 3.dp // 마커 중앙이 위치에 오도록 조정

                                        // 현재 마커가 나타내는 실적 값
                                        val markerValue = performanceRanges[i]

                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .offset(x = xOffset)
                                                .background(
                                                    if (realCards[currentCardIndex].totalSpending >= markerValue) Color(0xFF006064) else Color.Gray.copy(alpha = 0.5f),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = (i + 1).toString(),
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 혜택 금액 진행 상태
                    Column(
                        modifier = Modifier
                            .width(260.dp)
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row {
                                Text(
                                    text = "받은 혜택",
                                    fontSize = 14.sp,
                                    color = Color(0xFF00BCD4)
                                )
                                Text(
                                    text = " / 총 혜택",
                                    fontSize = 14.sp,
                                    color = Color(0xFFE0E0E0)
                                )
                            }
                            Row {
                                Text(
                                    text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(realCards[currentCardIndex].receivedBenefit)}원",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00BCD4)  // 금액 부분 청록색으로 변경
                                )
                                Text(
                                    text = if (realCards[currentCardIndex].maxBenefit == 0) {
                                        " / 무제한"
                                    } else {
                                        " / ${NumberFormat.getNumberInstance(Locale.KOREA).format(realCards[currentCardIndex].maxBenefit)}원"
                                    },
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = {
                                val received = realCards[currentCardIndex].receivedBenefit.toFloat()
                                val max = realCards[currentCardIndex].maxBenefit.toFloat()
                                if (max == 0f) {
                                    1f  // maxBenefit이 0이면 무제한이므로 프로그레스 바를 100% 채움
                                } else if (max > 0) {
                                    (received / max).coerceIn(0f, 1f)  // 1.0을 넘지 않도록 제한
                                } else {
                                    0f
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(2.dp)),  // 둥근 모서리 추가
                            color = Color(0xFF00BCD4),
                            trackColor = Color(0x33FFFFFF)
                        )
                    }
                }
            }
        }
    }
}
