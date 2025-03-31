package com.example.fe.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.fe.ui.components.backgrounds.StarryBackground
import com.example.fe.ui.components.navigation.BottomNavBar
import com.example.fe.ui.components.navigation.BottomNavItem
import com.example.fe.ui.components.navigation.TopBar
import com.example.fe.ui.screens.calendar.CalendarScreen
import com.example.fe.ui.screens.cardRecommend.CardRecommendScreen
import com.example.fe.ui.screens.myCard.MyCardScreen
import com.example.fe.ui.screens.onboard.OnboardingViewModel
import com.example.fe.ui.screens.onboard.OnboardingViewModelFactory
import com.example.fe.ui.screens.payment.PaymentScreen
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fe.ui.screens.home.HomeScreen
import com.example.fe.ui.screens.home.HomeDetailScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.fe.ui.screens.myCard.CardItem
import com.example.fe.ui.screens.myCard.CardDetailScreen
import com.example.fe.ui.screens.myCard.CardManagementScreen
import com.example.fe.ui.screens.cardRecommend.CardDetailInfoScreen
import com.example.fe.ui.screens.cardRecommend.CardInfo

// 네비게이션 경로 상수 추가
object NavRoutes {
    const val HOME_DETAIL = "home_detail"
    const val CARD_DETAIL = "card_detail/{cardId}"
    const val CARD_MANAGEMENT = "card_management"
    const val CARD_DETAIL_INFO = "card_detail_info/{cardId}"
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()
    
    // 로그아웃을 위한 ViewModel
    val viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModelFactory(context)
    )

    // 현재 경로 추적
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: BottomNavItem.Home.route

    // 네비게이션 바 표시 여부 상태
    var bottomBarVisible by remember { mutableStateOf(true) }

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
    
    // 화면 전환 시 콘텐츠 페이드 효과
    val contentAlpha by animateFloatAsState(
        targetValue = if (isNavigatingBack.value) 0f else 1f,
        animationSpec = tween(300),
        label = "contentAlpha"
    )

    // 탭 인덱스 맵
    val tabIndices = mapOf(
        BottomNavItem.Home.route to 0,
        BottomNavItem.MyCard.route to 1,
        BottomNavItem.Payment.route to 2,
        BottomNavItem.Calendar.route to 3,
        BottomNavItem.CardRecommend.route to 4
    )
    
    // 배경 이동 거리 배율 (더 큰 값으로 변경)
    val backgroundMovementMultiplier = 800f  // 기존 300f에서 증가
    
    // 상세 화면 이동 시 배경 이동 거리 배율 (더 큰 값으로 변경)
    val detailBackgroundMovementMultiplier = 1500f  // 기존 800f에서 증가
    
    // 애니메이션 적용된 가로 오프셋
    val animatedHorizontalOffset by animateFloatAsState(
        // 누적 오프셋 + 현재 전환에 의한 오프셋
        targetValue = cumulativeOffset,
        // 애니메이션 속도 증가 (300ms에서 200ms로 감소)
        animationSpec = tween(200, easing = EaseInOut),
        label = "horizontalOffset",
        finishedListener = {
            // 애니메이션이 끝나면 방향 초기화 (다음 애니메이션을 위해)
            transitionDirection = 0
        }
    )

    // 네비게이션 바 애니메이션을 위한 상태
    val shouldShowBottomBar = bottomBarVisible && currentRoute != NavRoutes.HOME_DETAIL && 
                          currentRoute?.startsWith("card_detail") != true

    // 네비게이션 바 애니메이션 값
    val bottomBarAlpha by animateFloatAsState(
        targetValue = if (shouldShowBottomBar) 1f else 0f,
        animationSpec = tween(300, easing = EaseInOut),
        label = "bottomBarAlpha"
    )

    // 네비게이션 바 슬라이드 애니메이션 값
    val bottomBarOffset by animateFloatAsState(
        targetValue = if (shouldShowBottomBar) 0f else 100f,
        animationSpec = tween(300, easing = EaseInOut),
        label = "bottomBarOffset"
    )

    // 네비게이션 바 높이 (일반적으로 80dp)
    val bottomBarHeight = 80.dp

    // 패딩 애니메이션 값 (네비게이션 바가 사라질 때 패딩도 0으로)
    val bottomPadding by animateDpAsState(
        targetValue = if (shouldShowBottomBar) bottomBarHeight else 0.dp,
        animationSpec = tween(300, easing = EaseInOut),
        label = "bottomPadding"
    )

    Scaffold(
        topBar = {
            // 현재 경로에 따라 TopBar 내용 변경
            when {
                currentRoute == NavRoutes.HOME_DETAIL -> {
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
                                cumulativeOffset += transitionDirection * detailBackgroundMovementMultiplier
                                animationCounter++
                                navController.popBackStack()
                            }
                        },
                        onLogoutClick = {
                            // 로그아웃 처리
                            viewModel.logout()
                        }
                    )
                }
                // 카드 상세 화면일 때 TopBar 변경
                currentRoute?.startsWith("card_detail/") == true -> {
                    TopBar(
                        title = "카드 상세 정보",
                        showBackButton = true,
                        onBackClick = {
                            // 뒤로가기 클릭 시 애니메이션 방향 설정
                            transitionDirection = -1
                            // 누적 오프셋 업데이트 (별들이 반대 방향으로 이동)
                            cumulativeOffset += transitionDirection * 800f
                            animationCounter++
                            navController.popBackStack()
                        },
                        onLogoutClick = {
                            // 로그아웃 처리
                            viewModel.logout()
                        }
                    )
                }
                else -> {
                    TopBar(
                        onProfileClick = { /* 프로필 화면으로 이동 */ },
                        onLogoutClick = {
                            // 로그아웃 처리
                            viewModel.logout()
                        }
                    ) // 기본 TopBar
                }
            }
        },
        bottomBar = {
            // 네비게이션 바가 표시되지 않을 때는 빈 상자를 렌더링하여 클릭 이벤트도 처리하지 않음
            if (shouldShowBottomBar) {
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
                            if (currentRoute != item.route) {
                                val newIndex = tabIndices[item.route] ?: 0
                                previousTabIndex = currentTabIndex
                                currentTabIndex = newIndex
                                transitionDirection = if (newIndex > previousTabIndex) 1 else -1
                                cumulativeOffset += transitionDirection * backgroundMovementMultiplier
                                animationCounter++
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }
        },
        // 동적 패딩 적용
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            StarryBackground(
                scrollOffset = scrollOffset,
                horizontalOffset = animatedHorizontalOffset
            ) {
                NavHost(
                    navController = navController,
                    startDestination = BottomNavItem.Home.route
                ) {
                    composable(BottomNavItem.Home.route) {
                        HomeScreen(
                            navController = navController
                        )
                    }
                    composable(BottomNavItem.MyCard.route) {
                        MyCardScreen(
                            onCardClick = { cardItem ->
                                navController.navigate("card_detail/${cardItem.id}")
                            },
                            onManageCardsClick = {
                                navController.navigate(NavRoutes.CARD_MANAGEMENT)
                            }
                        )
                    }
                    composable(BottomNavItem.Payment.route) {
                        PaymentScreen(
                            onNavigateToHome = {
                                navController.navigate(BottomNavItem.Home.route) {
                                    popUpTo(BottomNavItem.Home.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(BottomNavItem.Calendar.route) {
                        CalendarScreen()
                    }
                    composable(BottomNavItem.CardRecommend.route) {
                        CardRecommendScreen(
                            onCardClick = { cardInfo ->
                                navController.navigate("card_detail_info/${cardInfo.id}")
                            }
                        )
                    }
                    composable(NavRoutes.HOME_DETAIL) {
                        HomeDetailScreen(
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(
                        route = NavRoutes.CARD_DETAIL,
                        arguments = listOf(
                            navArgument("cardId") { type = NavType.IntType }
                        )
                    ) {
                        val cardId = it.arguments?.getInt("cardId") ?: 1
                        CardDetailScreen(
                            cardItem = getCardById(cardId),
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(NavRoutes.CARD_MANAGEMENT) {
                        CardManagementScreen(
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(
                        route = NavRoutes.CARD_DETAIL_INFO,
                        arguments = listOf(
                            navArgument("cardId") { type = NavType.IntType }
                        )
                    ) {
                        val cardId = it.arguments?.getInt("cardId") ?: 1
                        CardDetailInfoScreen(
                            card = CardInfo(
                                id = cardId,
                                name = "추천 카드 ${cardId}",
                                company = "카드사",
                                annualFee = "연회비 3만원",
                                minSpending = "월 30만원 이상",
                                benefits = listOf(
                                    "커피 전문점 10% 할인",
                                    "영화관 20% 할인",
                                    "대중교통 10% 할인"
                                )
                            ),
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun getCardById(cardId: Int): CardItem {
    val cards = listOf(
        CardItem(1, "토스 신한카드 Mr.Life", "•••• •••• •••• 3456"),
        CardItem(2, "현대카드", "•••• •••• •••• 4567"),
        CardItem(3, "삼성카드", "•••• •••• •••• 5678")
    )
    return cards.find { it.id == cardId } ?: cards[0]
}