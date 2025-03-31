package com.example.fe.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.example.fe.ui.screens.home.HomeScreenContent
import com.example.fe.ui.screens.home.HomeDetailScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.fe.ui.screens.myCard.CardItem
import com.example.fe.ui.screens.myCard.CardDetailScreen
import com.example.fe.ui.screens.myCard.CardManagementScreen
import android.util.Log
import com.example.fe.ui.screens.payment.components.QRScannerScreen
import com.example.fe.ui.screens.payment.components.PaymentInfoScreen
import com.example.fe.ui.screens.payment.PaymentCardInfo

// 네비게이션 경로 상수 추가
object NavRoutes {
    const val HOME_DETAIL = "home_detail"
    const val CARD_DETAIL = "card_detail/{cardId}"
    const val CARD_MANAGEMENT = "card_management"
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

    // QR 스캐너 표시 여부
    var showQRScanner by remember { mutableStateOf(false) }

    // 결제 정보 화면 표시 여부
    var showPaymentInfo by remember { mutableStateOf(false) }

    // 스캔된 QR 코드
    var scannedQRCode by remember { mutableStateOf("") }

    // 네비게이션 바 애니메이션을 위한 상태
    val bottomBarVisible = currentRoute != NavRoutes.HOME_DETAIL && 
                          !currentRoute.startsWith("card_detail") &&
                          !showQRScanner &&
                          !showPaymentInfo  // 결제 정보 화면에서도 네비게이션 바 숨김

    // 상단 바 표시 여부
    val topBarVisible = !showQRScanner && !showPaymentInfo  // 결제 정보 화면에서도 상단 바 숨김

    // 네비게이션 바 애니메이션 값
    val bottomBarAlpha by animateFloatAsState(
        targetValue = if (bottomBarVisible) 1f else 0f,
        animationSpec = tween(300, easing = EaseInOut),
        label = "bottomBarAlpha"
    )

    // 상단 바 애니메이션 값
    val topBarAlpha by animateFloatAsState(
        targetValue = if (topBarVisible) 1f else 0f,
        animationSpec = tween(300, easing = EaseInOut),
        label = "topBarAlpha"
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

    // BottomNavBar에 카메라 클릭 콜백 전달
    val isPaymentScreen = currentRoute == "payment"

    Scaffold(
        topBar = {
            // QR 스캐너가 표시될 때는 상단 바 숨김
            if (topBarVisible) {
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
        },
        bottomBar = {
            // QR 스캐너가 표시될 때는 네비게이션 바 숨김
            if (bottomBarVisible) {
                Box(
                    modifier = Modifier
                        .graphicsLayer(
                            alpha = bottomBarAlpha,
                            translationY = bottomBarOffset
                        )
                ) {
                    BottomNavBar(
                        navController = navController,
                        onTabSelected = { item ->
                            // 현재 경로와 선택한 경로가 다를 때만 처리
                            if (currentRoute != item.route) {
                                // 탭 선택 시 전환 방향 계산
                                val newIndex = tabIndices[item.route] ?: 0
                                previousTabIndex = currentTabIndex
                                currentTabIndex = newIndex

                                // 방향 계산 및 애니메이션 트리거
                                transitionDirection = if (newIndex > previousTabIndex) 1 else -1
                                
                                // 누적 오프셋 업데이트 (더 큰 값으로 변경)
                                cumulativeOffset += transitionDirection * backgroundMovementMultiplier
                                
                                animationCounter++ // 애니메이션 트리거
                            }
                        },
                        onCameraClick = {
                            // 카메라 아이콘 클릭 시 QR 스캐너 표시
                            showQRScanner = true
                        }
                    )
                }
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
                starCount = 150,  // 150에서 200으로 증가
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
                                    cumulativeOffset += transitionDirection * detailBackgroundMovementMultiplier
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
                        // 화면 전환 애니메이션을 위한 상태
                        val isNavigating = remember { mutableStateOf(false) }
                        val contentAlpha by animateFloatAsState(
                            targetValue = if (isNavigating.value) 0f else 1f,
                            animationSpec = tween(300),
                            label = "contentAlpha"
                        )

                        // 코루틴 스코프 추가
                        val coroutineScope = rememberCoroutineScope()

                        MyCardScreen(
                            modifier = Modifier.graphicsLayer(alpha = contentAlpha),
                            onScrollOffsetChange = { offset ->
                                scrollOffset = offset
                            },
                            onCardClick = { cardItem ->
                                // 현재 카드(중앙에 있는 카드)만 클릭 시 상세 화면으로 이동
                                isNavigating.value = true
                                // 애니메이션 방향 설정 없이 네비게이션만 실행
                                // 별 이동 효과 없이 페이드만 적용
                                coroutineScope.launch {
                                    navController.navigate("card_detail/${cardItem.id}")
                                }
                            },
                            onManageCardsClick = {
                                // 카드 관리 화면으로 이동
                                navController.navigate(NavRoutes.CARD_MANAGEMENT)
                            }
                        )
                    }

                    composable(
                        route = "payment",
                        enterTransition = { fadeIn() },
                        exitTransition = { fadeOut() }
                    ) {
                        // showQRScanner 상태를 AppNavigation에서 관리하는 방법
                        var showQRScanner by remember { mutableStateOf(false) }
                        
                        PaymentScreen(
                            onScrollOffsetChange = { scrollOffset = it },
                            onNavigateToHome = { navController.navigate("home") }
                        )
                        
                        // QR 스캐너 화면 (오버레이로 표시)
                        if (showQRScanner) {
                            QRScannerScreen(
                                onClose = { showQRScanner = false },
                                onQRCodeScanned = { qrCode ->
                                    // QR 코드 스캔 결과 처리
                                    Log.d("QRScanner", "스캔된 QR 코드: $qrCode")
                                    scannedQRCode = qrCode
                                    showQRScanner = false
                                    showPaymentInfo = true  // 결제 정보 화면 표시
                                }
                            )
                        }
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
                        CardRecommendScreen(
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

                    // 카드 관리 화면 추가
                    composable(
                        route = NavRoutes.CARD_MANAGEMENT,
                        enterTransition = {
                            // 오른쪽에서 들어오는 애니메이션 + 페이드인
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(500, easing = EaseInOut)
                            ) + fadeIn(
                                animationSpec = tween(400, easing = EaseInOut)
                            )
                        },
                        exitTransition = {
                            // 오른쪽으로 나가는 애니메이션 + 페이드아웃
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(500, easing = EaseInOut)
                            ) + fadeOut(
                                animationSpec = tween(400, easing = EaseInOut)
                            )
                        }
                    ) {
                        CardManagementScreen(
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(
                        route = NavRoutes.CARD_DETAIL,
                        arguments = listOf(navArgument("cardId") { type = NavType.IntType }),
                        enterTransition = {
                            // 슬라이드 애니메이션 완전 제거, 페이드 효과만 적용
                            fadeIn(
                                animationSpec = tween(300, easing = EaseInOut)
                            )
                        },
                        exitTransition = {
                            // 슬라이드 애니메이션 완전 제거, 페이드 효과만 적용
                            fadeOut(
                                animationSpec = tween(300, easing = EaseInOut)
                            )
                        }
                    ) { backStackEntry ->
                        val cardId = backStackEntry.arguments?.getInt("cardId") ?: 1
                        val card = getCardById(cardId)
                        CardDetailScreen(
                            cardItem = card,
                            onBackClick = {
                                // 뒤로가기 클릭 시 애니메이션 방향 설정
                                transitionDirection = -1
                                // 누적 오프셋 업데이트 (별들이 반대 방향으로 이동)
                                cumulativeOffset += transitionDirection * 800f
                                animationCounter++
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
        
        // QR 스캐너 화면 (오버레이로 표시)
        if (showQRScanner) {
            QRScannerScreen(
                onClose = { showQRScanner = false },
                onQRCodeScanned = { qrCode ->
                    // QR 코드 스캔 결과 처리
                    Log.d("QRScanner", "스캔된 QR 코드: $qrCode")
                    scannedQRCode = qrCode
                    showQRScanner = false
                    showPaymentInfo = true  // 결제 정보 화면 표시
                }
            )
        }

        // 결제 정보 화면 (오버레이로 표시)
        if (showPaymentInfo) {
            // 카드 목록 (PaymentScreen에서 가져온 더미 데이터)
            val cards = listOf(
                PaymentCardInfo(
                    id = "tes_card_unique_number",
                    cardNumber = "8801062318551",
                    cardName = "토스 신한카드 Mr.Life"
                ),
                PaymentCardInfo(
                    id = "000",
                    cardNumber = "8801062318552",
                    cardName = "삼성카드 taptap O"
                ),
                PaymentCardInfo(
                    id = "card3",
                    cardNumber = "8801062318553",
                    cardName = "현대카드 ZERO"
                ),
                PaymentCardInfo(
                    id = "card4",
                    cardNumber = "8801062318554",
                    cardName = "KB국민 톡톡"
                ),
                PaymentCardInfo(
                    id = "card5",
                    cardNumber = "8801062318555",
                    cardName = "하나 원큐"
                )
            )
            
            PaymentInfoScreen(
                qrCode = scannedQRCode,
                cards = cards,
                onClose = { showPaymentInfo = false },
                onPaymentComplete = {
                    // 결제 완료 후 홈 화면으로 이동
                    showPaymentInfo = false
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onScrollOffsetChange = { offset ->
                    scrollOffset = offset
                }
            )
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