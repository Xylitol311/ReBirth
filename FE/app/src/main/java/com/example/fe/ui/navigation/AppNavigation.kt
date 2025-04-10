package com.example.fe.ui.navigation

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fe.ui.components.backgrounds.StarryBackground
import com.example.fe.ui.components.navigation.BottomNavBar
import com.example.fe.ui.components.navigation.BottomNavItem
import com.example.fe.ui.components.navigation.TopBar
import com.example.fe.ui.screens.calendar.CalendarScreen
import com.example.fe.ui.screens.cardRecommend.CardDetailInfoScreen
import com.example.fe.ui.screens.cardRecommend.CardRecommendScreen
import com.example.fe.ui.screens.cardRecommend.CardRecommendViewModel
import com.example.fe.ui.screens.home.HomeDetailScreen
import com.example.fe.ui.screens.home.HomeScreen
import com.example.fe.ui.screens.myCard.CardDetailScreen
import com.example.fe.ui.screens.myCard.CardManagementScreen
import com.example.fe.ui.screens.myCard.MyCardScreen
import com.example.fe.ui.screens.myCard.MyCardViewModel.CardOrderManager.getCardById
import com.example.fe.ui.screens.mypage.MyPageScreen
import com.example.fe.ui.screens.onboard.OnboardingScreen
import com.example.fe.ui.screens.onboard.components.device.AndroidDeviceInfoManager
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModelFactory
import com.example.fe.ui.screens.payment.PaymentScreen
import com.example.fe.ui.screens.payment.PaymentViewModel
import com.example.fe.ui.screens.payment.components.CardOCRScanScreen
import com.example.fe.ui.screens.payment.components.PaymentInfoScreen
import com.example.fe.ui.screens.filter.FilterSelectionScreen

// 네비게이션 경로 상수 추가
object NavRoutes {
    const val ONBOARDING = "onboarding"  // 온보딩/로그인 화면 경로 추가
    const val HOME_DETAIL = "home_detail"
    const val CARD_DETAIL = "card_detail/{cardId}"
    const val CARD_MANAGEMENT = "card_management"
    const val CARD_DETAIL_INFO = "card_detail_info/{cardId}"
    const val MY_PAGE = "my_page"
    const val FILTER_SELECTION = "filter_selection/{category}" // 필터 선택 화면 경로 추가
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val context = LocalContext.current
    // ViewModel을 상위 Composable 함수에서 가져오기
    val paymentViewModel: PaymentViewModel = viewModel(
        factory = PaymentViewModel.Factory(context)
    )

    val deviceInfoManager = remember { AndroidDeviceInfoManager(context) }
    val viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModelFactory(deviceInfoManager,context)
    )

    // 로그아웃을 위한 ViewModel

    // 로그아웃 처리 함수
    val handleLogout = {
        viewModel.logout()
        // 로그인 화면으로 이동하고 백스택 클리어
        navController.navigate(NavRoutes.ONBOARDING) {
            popUpTo(0) { inclusive = true }
        }
    }

    // 현재 경로 추적
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: BottomNavItem.Home.route

    // 네비게이션 바 표시 여부 상태
    var bottomBarVisible by remember { mutableStateOf(true) }

    // 누적 가로 오프셋 (별들의 전체 이동 거리)
    var cumulativeOffset by remember { mutableStateOf(0f) }

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

    // 현재 화면의 스크롤 오프셋을 저장할 상태
    var currentScreenScrollOffset by remember { mutableStateOf(0f) }

    // 현재 화면의 가로 스크롤 오프셋을 저장할 상태
    var currentScreenHorizontalOffset by remember { mutableStateOf(0f) }

    // 스크롤 오프셋을 업데이트하는 콜백 함수
    val updateScrollOffset: (Float) -> Unit = { offset ->
        currentScreenScrollOffset = offset
    }

    // 가로 스크롤 오프셋을 업데이트하는 콜백 함수
    val updateHorizontalOffset: (Float) -> Unit = { offset ->
        currentScreenHorizontalOffset = offset
    }

    // 배경 이동 거리 배율 (더 큰 값으로 변경)
    val backgroundMovementMultiplier = 2500f

    // 상세 화면 이동 시 배경 이동 거리 배율 (더 큰 값으로 변경)
    val detailBackgroundMovementMultiplier = 1000f

    // 배경 이동 방향 (1: 위로, -1: 아래로, 0: 이동 없음)
    var backgroundVerticalDirection by remember { mutableStateOf(0) }

    // 화면 전환 방향 (-1: 왼쪽으로, 1: 오른쪽으로)
    var transitionDirection by remember { mutableStateOf(0) }

    // 애니메이션 적용된 가로 오프셋
    val animatedHorizontalOffset by animateFloatAsState(
        // 누적 오프셋 + 현재 전환에 의한 오프셋 + 현재 화면의 가로 스크롤 오프셋
        targetValue = cumulativeOffset + currentScreenHorizontalOffset * transitionDirection,
        // 애니메이션 속도 증가 (300ms에서 200ms로 감소)
        animationSpec = tween(700, easing = EaseInOut),
        label = "horizontalOffset",
        finishedListener = {
            // 애니메이션이 끝나면 방향 초기화 (다음 애니메이션을 위해)
            transitionDirection = 0
        }

    )


    // 애니메이션 적용된 세로 오프셋
    val animatedVerticalOffset by animateFloatAsState(
        // 현재 화면의 세로 스크롤 오프셋 + 배경 이동 방향에 따른 오프셋
        targetValue = currentScreenScrollOffset + backgroundVerticalDirection * detailBackgroundMovementMultiplier,
        // 애니메이션 속도
        animationSpec = tween(500, easing = EaseInOut),
        label = "verticalOffset",
        finishedListener = {
            // 애니메이션이 끝나면 방향 초기화 (다음 애니메이션을 위해)
            backgroundVerticalDirection = 0
        }
    )


    // QR 스캐너와 카드 OCR 스캔 화면 표시 여부
    var showQRScanner by remember { mutableStateOf(false) }
    var showCardOCRScan by remember { mutableStateOf(false) }

    // PaymentScreen에서 QR 스캔 모드 상태를 받아오는 변수 추가
    var isQRScanMode by remember { mutableStateOf(false) }
    // 결제 정보 화면 표시 여부
    var showPaymentInfo by remember { mutableStateOf(false) }

    // 네비게이션 바와 상단 바 표시 여부 결정
    val shouldShowUI = remember(showQRScanner, showCardOCRScan, isQRScanMode, showPaymentInfo, currentRoute) {
        !showQRScanner && 
        !showCardOCRScan && 
        !isQRScanMode && 
        !showPaymentInfo &&
        !currentRoute.startsWith("filter_selection")
    }

    // 스캔된 QR 코드
    var scannedQRCode by remember { mutableStateOf("") }

    val shouldShowBottomBar = remember(bottomBarVisible, currentRoute, isQRScanMode) {
        bottomBarVisible &&
                currentRoute != NavRoutes.HOME_DETAIL &&
                !currentRoute.startsWith("card_detail") &&
                !currentRoute.contains("payment_info") &&
                !currentRoute.contains("payment_result") &&
                !isQRScanMode &&
                !currentRoute.contains("card_ocr_scan")

    }

    // TopBar 표시 여부 결정
    val shouldShowTopBar by remember(currentRoute, isQRScanMode) {
        mutableStateOf(
                    !currentRoute.contains("payment_info") &&
                    !currentRoute.contains("payment_result") &&
                    !isQRScanMode &&
                    !currentRoute.contains("card_ocr_scan")
        )
    }

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

    // 결제 결과 팝업 표시
    var showPaymentResultPopup by remember { mutableStateOf(false) }
    val paymentResult by paymentViewModel.paymentResult.collectAsState()

    // 메인 탭 화면 목록 (뒤로가기 버튼이 표시되지 않아야 하는 화면들)
    val mainTabScreens = listOf(
        BottomNavItem.Home.route,
        BottomNavItem.MyCard.route,
        BottomNavItem.CardRecommend.route,
        BottomNavItem.Calendar.route,
        BottomNavItem.Payment.route
    )
    
    // 현재 화면이 메인 탭 화면인지 확인
    val isMainTabScreen = currentRoute in mainTabScreens

    // 현재 화면의 제목 설정
    val screenTitle = when {
        currentRoute == NavRoutes.HOME_DETAIL -> "이번 달 사용 내역"
        currentRoute.startsWith("card_detail") -> "카드 상세"
        else -> ""
    }

    // 전체 화면 구성
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. 별 배경 (가장 밑에 깔림)
        StarryBackground(
            scrollOffset = animatedVerticalOffset,
            horizontalOffset = animatedHorizontalOffset,
            animationCounter = animationCounter
        ) {
            // 별 배경만 그리기
        }
        
        // 2. 메인 UI 콘텐츠
        Scaffold(
            // 상단바 설정
            topBar = {
                if (shouldShowUI) {
                    TopBar(
                        title = screenTitle,
                        showBackButton = !isMainTabScreen,
                        onBackClick = {
                            // 뒤로가기 시 배경 이동 방향 설정 (아래로 이동)
                            if (currentRoute.startsWith("card_detail")) {
                                backgroundVerticalDirection = 0
                            } else if (currentRoute == NavRoutes.HOME_DETAIL) {
                                // 홈 상세에서 뒤로가기 시 배경 이동 방향 설정 (왼쪽으로 이동)
                                transitionDirection = -1
                                // 누적 오프셋 업데이트
                                cumulativeOffset += transitionDirection * detailBackgroundMovementMultiplier
                                // 애니메이션 트리거
                                animationCounter++
                            }
                            
                            // 뒤로가기
                            navController.popBackStack()
                        },
                        onProfileClick = {
                            navController.navigate(NavRoutes.MY_PAGE)
                        },
                        onLogoutClick = handleLogout
                    )
                }
            },
            
            // 하단바 설정
            bottomBar = {
                if (shouldShowUI) {
                    BottomNavBar(
                        navController = navController,
                        onTabSelected = { item ->
                            // 현재 선택된 탭과 다른 탭을 선택했을 때만 처리
                            if (item.route != currentRoute) {
                                // 탭 선택 시 전환 방향 계산
                                val newIndex = tabIndices[item.route] ?: 0
                    
                                // 이전 인덱스 저장
                                previousTabIndex = currentTabIndex
                    
                                // 전환 방향 계산 (새 인덱스가 현재보다 크면 오른쪽, 작으면 왼쪽)
                                transitionDirection = if (newIndex > currentTabIndex) 1 else -1
                    
                                // 현재 인덱스 업데이트
                                currentTabIndex = newIndex
                    
                                // 누적 오프셋 업데이트 (별들이 반대 방향으로 이동)
                                cumulativeOffset += transitionDirection * backgroundMovementMultiplier
                    
                                // 애니메이션 카운터 증가 (애니메이션 트리거)
                                animationCounter++
                                
                                // 네비게이션
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            },
            
            // 스캐폴드 설정
            containerColor = Color.Transparent, // 배경을 투명하게 설정
            contentColor = Color.White, // 내용물 색상을 흰색으로 설정
            contentWindowInsets = WindowInsets(0, 0, 0, 0), // 인셋 없음
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            // NavHost 설정
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                composable(BottomNavItem.Home.route) {
                    HomeScreen(
                        navController = navController,
                        onScrollOffsetChange = updateScrollOffset  // 콜백 전달
                    )
                }

                composable(BottomNavItem.MyCard.route) {
                    MyCardScreen(
                        navController = navController,
                        onScrollOffsetChange = updateScrollOffset,
                        onHorizontalOffsetChange = updateHorizontalOffset,  // 가로 스크롤 오프셋 콜백 추가
                        onCardClick = { cardItem ->
                            backgroundVerticalDirection = 0
                            navController.navigate("card_detail/${cardItem.id}")
                        },
                        onManageCardsClick = {
                            navController.navigate(NavRoutes.CARD_MANAGEMENT)
                        }
                    )
                }

                // 온보딩/로그인 화면 추가
                composable(NavRoutes.ONBOARDING) {
                    OnboardingScreen(
                        navController = navController,
                        viewModel = viewModel
                    )
                }

                composable(BottomNavItem.Payment.route) {

                    PaymentScreen(
                        onScrollOffsetChange = { offset ->
                            currentScreenHorizontalOffset = offset
                        },
                        viewModel = paymentViewModel,
                        onNavigateToHome = {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                        onShowQRScanner = {
                            showQRScanner = true  // QR 스캐너 화면 표시
                        },
                        onShowCardOCRScan = {
                            showCardOCRScan = true  // 카드 OCR 스캔 화면 표시
                        },
                        // QR 스캔 모드 상태 콜백 추가
                        onQRScanModeChange = { isInQRScanMode ->
                            isQRScanMode = isInQRScanMode
                        },
                        onShowPaymentInfo = {
                            showPaymentInfo = true  // 결제 정보 화면 표시
                        },
                        onReturnFromCardOCRScan = {
                            paymentViewModel.refreshCards()
                        }
                    )
                }

                composable(BottomNavItem.Calendar.route) {
                    CalendarScreen()
                }
                composable(BottomNavItem.CardRecommend.route) {
                    CardRecommendScreen(
                        onCardClick = { cardId ->
                            navController.navigate("card_detail_info/$cardId")
                        },
                        navController = navController
                    )
                }

                composable(NavRoutes.HOME_DETAIL) {
                    LaunchedEffect(Unit) {
                        // 방향을 1로 설정 (오른쪽으로 이동)
                        transitionDirection = 1
                        // 누적 오프셋 업데이트 (별들이 반대 방향으로 이동)
                        cumulativeOffset += transitionDirection * detailBackgroundMovementMultiplier
                        // 애니메이션 트리거
                        animationCounter++
                    }

                    HomeDetailScreen(
                        onBackClick = {
                            // 뒤로가기 시 애니메이션 방향 설정
                            transitionDirection = -1
                            // 누적 오프셋 업데이트 (별들이 반대 방향으로 이동)
                            cumulativeOffset += transitionDirection * detailBackgroundMovementMultiplier
                            animationCounter++
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = "card_detail/{cardId}",
                    arguments = listOf(
                        navArgument("cardId") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val cardId = backStackEntry.arguments?.getInt("cardId") ?: 1

                    CardDetailScreen(
                        cardId = getCardById(cardId)?.card?.id ?:0,
                        onBackClick = {
                            // 뒤로가기 시 배경 이동 방향 설정 (아래로 이동)
                            backgroundVerticalDirection = 0
                            // 뒤로가기
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
                    arguments = listOf(navArgument("cardId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val cardId = backStackEntry.arguments?.getInt("cardId") ?: 0
                    val cardRecommendViewModel: CardRecommendViewModel = viewModel()
                    
                    CardDetailInfoScreen(
                        viewModel = cardRecommendViewModel,
                        cardId = cardId,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        navController = navController
                    )
                }

                composable(NavRoutes.MY_PAGE) {
                    MyPageScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                // 필터 선택 화면 추가
                composable(
                    route = NavRoutes.FILTER_SELECTION,
                    arguments = listOf(
                        navArgument("category") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    // 탑바와 하단 네비바를 숨깁니다
                    LaunchedEffect(Unit) {
                        bottomBarVisible = false
                    }
                    
                    // 화면이 종료될 때 다시 보이게 합니다
                    DisposableEffect(key1 = Unit) {
                        onDispose {
                            bottomBarVisible = true
                        }
                    }
                    
                    val category = backStackEntry.arguments?.getString("category") ?: ""
                    FilterSelectionScreen(
                        category = category,
                        onClose = {
                            navController.popBackStack()
                        },
                        onOptionSelected = { category, option ->
                            // ViewModel로 옵션 선택 정보 전달하는 로직 여기에 추가
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
        
        // 3. 오버레이 UI (가장 위에 표시)
        // 카드 OCR 스캔 화면 (오버레이로 표시)
        if (showCardOCRScan) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                CardOCRScanScreen(
                    onBack = {
                        showCardOCRScan = false
                    },
                    onComplete = {
                        showCardOCRScan = false
                        // 카드 추가 완료 후 처리
                        paymentViewModel.refreshCards() // refreshTokens() 대신 refreshCards() 호출
                    },
                    viewModel = paymentViewModel
                )
            }
        }

        // 결제 정보 화면 (오버레이로 표시)
        if (showPaymentInfo) {
            val cards by paymentViewModel.cards.collectAsState()
            val paymentState by paymentViewModel.paymentState.collectAsState()

            PaymentInfoScreen(
                cards = cards,
                onClose = { showPaymentInfo = false },
                onPaymentComplete = {
                    // 결제 완료 후 홈 화면으로 이동
                    showPaymentInfo = false
                    // 결제 결과 팝업 표시
                    showPaymentResultPopup = true
                },
                onScrollOffsetChange = { offset ->
                    currentScreenHorizontalOffset = offset
                },
                viewModel = paymentViewModel
            )
        }
    }
}