package com.example.fe.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fe.ui.components.backgrounds.StarryBackground
import com.example.fe.ui.components.navigation.BottomNavBar
import com.example.fe.ui.components.navigation.BottomNavItem
import com.example.fe.ui.components.navigation.TopBar
import com.example.fe.ui.screens.calendar.CalendarScreen
import com.example.fe.ui.screens.recCard.recCard
import com.example.fe.ui.screens.mycard.MyCardScreen
import com.example.fe.ui.screens.payment.PaymentScreen
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.rememberCoroutineScope
import com.example.fe.ui.screens.home.HomeScreenContent
import com.example.fe.ui.screens.home.HomeDetailScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLayoutDirection

// 네비게이션 경로 상수 추가
object NavRoutes {
    const val HOME_DETAIL = "home_detail"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    // 현재 경로 추적
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: BottomNavItem.Home.route
    
    // 공유 스크롤 오프셋 상태
    var scrollOffset by remember { mutableStateOf(0f) }
    
    // 누적 가로 오프셋 (별들의 전체 이동 거리)
    var cumulativeOffset by remember { mutableStateOf(0f) }
    
    // 화면 전환 방향 (-1: 왼쪽으로, 1: 오른쪽으로)
    var transitionDirection by remember { mutableStateOf(0) }
    
    // 애니메이션 트리거를 위한 카운터
    var animationCounter by remember { mutableStateOf(0) }
    
    // 현재 선택된 탭 인덱스 추적
    var currentTabIndex by remember { mutableStateOf(0) }
    var previousTabIndex by remember { mutableStateOf(0) }
    
    // 코루틴 스코프
    val coroutineScope = rememberCoroutineScope()
    
    // 화면 전환 애니메이션을 위한 상태
    val isNavigatingBack = remember { mutableStateOf(false) }
    
    // 탭 인덱스 맵
    val tabIndices = mapOf(
        BottomNavItem.Home.route to 0,
        BottomNavItem.MyCard.route to 1,
        BottomNavItem.Payment.route to 2,
        BottomNavItem.Calendar.route to 3,
        BottomNavItem.CardRecommend.route to 4
    )
    
    // 애니메이션 적용된 가로 오프셋
    val animatedHorizontalOffset by animateFloatAsState(
        // 누적 오프셋 + 현재 전환에 의한 오프셋
        targetValue = cumulativeOffset,
        animationSpec = tween(300, easing = EaseInOut),
        label = "horizontalOffset",
        finishedListener = {
            // 애니메이션이 끝나면 방향 초기화 (다음 애니메이션을 위해)
            transitionDirection = 0
        }
    )
    
    // 네비게이션 바 애니메이션을 위한 상태
    val bottomBarVisible = currentRoute != NavRoutes.HOME_DETAIL
    
    // 네비게이션 바 애니메이션 값
    val bottomBarAlpha by animateFloatAsState(
        targetValue = if (bottomBarVisible) 1f else 0f,
        animationSpec = tween(300, easing = EaseInOut),
        label = "bottomBarAlpha"
    )
    
    // 네비게이션 바 슬라이드 애니메이션 값
    val bottomBarOffset by animateFloatAsState(
        targetValue = if (bottomBarVisible) 0f else 100f,
        animationSpec = tween(300, easing = EaseInOut),
        label = "bottomBarOffset"
    )
    
    // 네비게이션 바 높이 (일반적으로 80dp)
    val bottomBarHeight = 80.dp
    
    // 패딩 애니메이션 값 (네비게이션 바가 사라질 때 패딩도 0으로)
    val bottomPadding by animateDpAsState(
        targetValue = if (bottomBarVisible) bottomBarHeight else 0.dp,
        animationSpec = tween(300, easing = EaseInOut),
        label = "bottomPadding"
    )
    
    Scaffold(
        topBar = { 
            // 현재 경로에 따라 TopBar 내용 변경
            when (currentRoute) {
                NavRoutes.HOME_DETAIL -> {
                    TopBar(
                        title = "이번 달 사용 내역",
                        showBackButton = true,
                        onBackClick = {
                            // 페이드아웃 시작
                            isNavigatingBack.value = true
                            // 약간의 지연 후 네비게이션
                            coroutineScope.launch {
                                delay(200)
                                // 뒤로가기 시 애니메이션 방향 설정
                                transitionDirection = -1
                                // 누적 오프셋 업데이트 (별들이 반대 방향으로 이동)
                                cumulativeOffset += transitionDirection * 800f
                                animationCounter++
                                navController.popBackStack()
                            }
                        }
                    )
                }
                else -> {
                    TopBar() // 기본 TopBar
                }
            }
        },
        bottomBar = { 
            // 상세 화면에서도 BottomBar 유지하되 투명도와 위치 애니메이션 적용
            Box(
                modifier = Modifier
                    .graphicsLayer(
                        alpha = bottomBarAlpha,
                        translationY = bottomBarOffset
                    )
            ) {
                BottomNavBar(
                    navController = navController,
                    onTabSelected = { item: BottomNavItem ->
                        // 현재 경로와 선택한 경로가 다를 때만 처리
                        if (currentRoute != item.route) {
                            // 탭 선택 시 전환 방향 계산
                            val newIndex = tabIndices[item.route] ?: 0
                            previousTabIndex = currentTabIndex
                            currentTabIndex = newIndex
                            
                            // 방향 계산 및 애니메이션 트리거
                            transitionDirection = if (newIndex > previousTabIndex) 1 else -1
                            
                            // 누적 오프셋 업데이트
                            cumulativeOffset += transitionDirection * 300f
                            
                            animationCounter++ // 애니메이션 트리거
                        }
                    }
                )
            }
        },
        // 동적 패딩 적용
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        // 패딩 값을 동적으로 조정
        val adjustedPaddingValues = PaddingValues(
            top = paddingValues.calculateTopPadding(),
            bottom = bottomPadding, // 동적 패딩 적용
            start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
            end = paddingValues.calculateEndPadding(LocalLayoutDirection.current)
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(adjustedPaddingValues) // 조정된 패딩 적용
        ) {
            // 공유 StarryBackground - 각 화면에서 별도로 StarryBackground를 사용하지 않도록 수정
            StarryBackground(
                scrollOffset = 0f,
                starCount = 150,
                horizontalOffset = animatedHorizontalOffset
            ) {
                // 내비게이션 호스트를 StarryBackground 내부에 배치
                NavHost(
                    navController = navController,
                    startDestination = BottomNavItem.Home.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(
                        route = BottomNavItem.Home.route,
                        enterTransition = {
                            // 왼쪽에서 들어오는 애니메이션 + 페이드인
                            slideIntoContainer(
                                towards = if (transitionDirection > 0) 
                                    AnimatedContentTransitionScope.SlideDirection.Left
                                else 
                                    AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(700, easing = EaseInOut)
                            ) + fadeIn(
                                animationSpec = tween(500, easing = EaseInOut)
                            )
                        },
                        exitTransition = {
                            // 나가는 애니메이션 + 페이드아웃
                            slideOutOfContainer(
                                towards = if (transitionDirection > 0) 
                                    AnimatedContentTransitionScope.SlideDirection.Left
                                else 
                                    AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(700, easing = EaseInOut)
                            ) + fadeOut(
                                animationSpec = tween(500, easing = EaseInOut)
                            )
                        }
                    ) {
                        // StarryBackground 없이 직접 콘텐츠만 표시
                        HomeScreenContent(
                            onScrollOffsetChange = { offset ->
                                scrollOffset = offset
                            },
                            onNavigateToDetail = {
                                // 홈 상세 화면으로 이동
                                navController.navigate(NavRoutes.HOME_DETAIL) {
                                    // 애니메이션 트리거를 위한 방향 설정
                                    transitionDirection = 1
                                    // 누적 오프셋 업데이트 (별들이 더 빠르게 이동)
                                    cumulativeOffset += transitionDirection * 800f
                                    animationCounter++
                                }
                            }
                        )
                    }
                    
                    // 다른 화면들도 동일하게 수정
                    composable(
                        route = BottomNavItem.MyCard.route,
                        enterTransition = {
                            slideIntoContainer(
                                towards = if (transitionDirection > 0) 
                                    AnimatedContentTransitionScope.SlideDirection.Left
                                else 
                                    AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(500, easing = EaseInOut)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = if (transitionDirection > 0) 
                                    AnimatedContentTransitionScope.SlideDirection.Left
                                else 
                                    AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(500, easing = EaseInOut)
                            )
                        }
                    ) {
                        MyCardScreen(
                            onScrollOffsetChange = { offset ->
                                scrollOffset = offset
                            }
                        )
                    }
                    
                    composable(
                        route = BottomNavItem.Payment.route,
                        enterTransition = {
                            slideIntoContainer(
                                towards = if (transitionDirection > 0) 
                                    AnimatedContentTransitionScope.SlideDirection.Left
                                else 
                                    AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(500, easing = EaseInOut)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = if (transitionDirection > 0) 
                                    AnimatedContentTransitionScope.SlideDirection.Left
                                else 
                                    AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(500, easing = EaseInOut)
                            )
                        }
                    ) {
                        PaymentScreen(
                            onScrollOffsetChange = { offset ->
                                scrollOffset = offset
                            }
                        )
                    }
                    
                    composable(
                        route = BottomNavItem.Calendar.route,
                        enterTransition = {
                            slideIntoContainer(
                                towards = if (transitionDirection > 0) 
                                    AnimatedContentTransitionScope.SlideDirection.Left
                                else 
                                    AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(500, easing = EaseInOut)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = if (transitionDirection > 0) 
                                    AnimatedContentTransitionScope.SlideDirection.Left
                                else 
                                    AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(500, easing = EaseInOut)
                            )
                        }
                    ) {
                        CalendarScreen(
                            onScrollOffsetChange = { offset ->
                                scrollOffset = offset
                            }
                        )
                    }
                    
                    composable(
                        route = BottomNavItem.CardRecommend.route,
                        enterTransition = {
                            slideIntoContainer(
                                towards = if (transitionDirection > 0) 
                                    AnimatedContentTransitionScope.SlideDirection.Left
                                else 
                                    AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(500, easing = EaseInOut)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = if (transitionDirection > 0) 
                                    AnimatedContentTransitionScope.SlideDirection.Left
                                else 
                                    AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(500, easing = EaseInOut)
                            )
                        }
                    ) {
                        recCard(
                            onScrollOffsetChange = { offset ->
                                scrollOffset = offset
                            }
                        )
                    }
                    
                    // 홈 상세 화면 추가
                    composable(
                        route = NavRoutes.HOME_DETAIL,
                        enterTransition = {
                            // 오른쪽에서 들어오는 애니메이션 + 페이드인
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(700, easing = EaseInOut)
                            ) + fadeIn(
                                animationSpec = tween(500, easing = EaseInOut)
                            )
                        },
                        exitTransition = {
                            // 오른쪽으로 나가는 애니메이션 + 페이드아웃
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(700, easing = EaseInOut)
                            ) + fadeOut(
                                animationSpec = tween(500, easing = EaseInOut)
                            )
                        }
                    ) {
                        HomeDetailScreen()
                    }
                }
            }
        }
    }
}