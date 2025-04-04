package com.example.fe.ui.navigation

import android.util.Log
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.content.MediaType.Companion.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.fe.ui.screens.cardRecommend.CardInfo

import com.example.fe.ui.screens.home.HomeDetailScreen
import com.example.fe.ui.screens.home.HomeScreen
import com.example.fe.ui.screens.myCard.CardDetailScreen
import com.example.fe.ui.screens.myCard.CardItem
import com.example.fe.ui.screens.myCard.CardManagementScreen
import com.example.fe.ui.screens.myCard.MyCardScreen
import com.example.fe.ui.screens.mypage.MyPageScreen
import com.example.fe.ui.screens.onboard.OnboardingScreen
import com.example.fe.ui.screens.onboard.components.device.AndroidDeviceInfoManager
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel

import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModelFactory

import com.example.fe.ui.screens.payment.PaymentViewModel
import com.example.fe.ui.screens.payment.components.PaymentInfoScreen
import com.example.fe.ui.screens.payment.components.PaymentResultPopup
import com.example.fe.ui.screens.payment.components.QRScannerScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
// 네비게이션 경로 상수 추가
object NavRoutes {
    const val ONBOARDING = "onboarding"  // 온보딩/로그인 화면 경로 추가
    const val HOME_DETAIL = "home_detail"
    const val CARD_DETAIL = "card_detail/{cardId}"
    const val CARD_MANAGEMENT = "card_management"
    const val CARD_DETAIL_INFO = "card_detail_info/{cardId}"
    const val MY_PAGE = "my_page"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
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

    // ViewModel을 상위 Composable 함수에서 가져오기
    val paymentViewModel: PaymentViewModel = viewModel()
    
    // QR 스캐너 표시 여부
    var showQRScanner by remember { mutableStateOf(false) }
    
    // 결제 정보 화면 표시 여부
    var showPaymentInfo by remember { mutableStateOf(false) }

    // 스캔된 QR 코드
    var scannedQRCode by remember { mutableStateOf("") }

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

    // 결제 결과 팝업 표시
    var showPaymentResultPopup by remember { mutableStateOf(false) }
    val paymentResult by paymentViewModel.paymentResult.collectAsState()

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
                        onLogoutClick = handleLogout
                    )
                }
                currentRoute == NavRoutes.MY_PAGE -> {  // 마이페이지 TopBar
                    TopBar(
                        title = "마이페이지",
                        showBackButton = true,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onLogoutClick = handleLogout
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
                        onLogoutClick = handleLogout
                    )
                }
                else -> {
                    TopBar(
                        onProfileClick = {
                            navController.navigate(NavRoutes.MY_PAGE)
                        },
                        onLogoutClick = handleLogout
                    )
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
                    // 온보딩/로그인 화면 추가
                    composable(NavRoutes.ONBOARDING) {
                        OnboardingScreen(
                            navController = navController,
                            viewModel = viewModel
                        )
                    }
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
                            },
                            onShowQRScanner = {
                                showQRScanner = true  // QR 스캐너 표시
                            }
                        )
                    }
                    composable(BottomNavItem.Calendar.route) {
                        CalendarScreen()
                    }
                    composable(BottomNavItem.CardRecommend.route) {
                        CardRecommendScreen(
                            onCardClick = { cardInfo ->
                                navController.navigate("card_detail_info/${cardInfo}")
                            }
                        )
                    }
                    composable(NavRoutes.HOME_DETAIL) {
                        // 홈 디테일 화면으로 이동할 때 애니메이션 값 가져오기
                        val transitionDirection = navController.currentBackStackEntry?.savedStateHandle?.get<Int>("transitionDirection") ?: 1
                        val backgroundMovement = navController.currentBackStackEntry?.savedStateHandle?.get<Float>("backgroundMovement") ?: 1500f
                        
                        // 애니메이션 값이 있으면 적용
                        LaunchedEffect(transitionDirection, backgroundMovement) {
                            if (transitionDirection != 0) {
                                cumulativeOffset += transitionDirection * backgroundMovement
                                animationCounter++
                                // 사용 후 초기화
                                navController.currentBackStackEntry?.savedStateHandle?.set("transitionDirection", 0)
                                navController.currentBackStackEntry?.savedStateHandle?.set("backgroundMovement", 0f)
                            }
                        }
                        
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
                    composable(NavRoutes.MY_PAGE) {
                        MyPageScreen(
                            onBackClick = {
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
                    // 이미 가져온 ViewModel 사용
                    paymentViewModel.sendQRToken(qrCode)
                    showQRScanner = false
                    showPaymentInfo = true  // 결제 정보 화면 표시
                }
            )
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
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                    
                    // 결제 결과 팝업 표시
                    showPaymentResultPopup = true
                },
                onScrollOffsetChange = { offset ->
                    scrollOffset = offset
                },
                viewModel = paymentViewModel
            )
        }

        // 결제 결과 팝업 표시
        if (showPaymentResultPopup && paymentResult != null) {
            PaymentResultPopup(
                paymentResult = paymentResult!!,
                onDismiss = { showPaymentResultPopup = false }
            )
        }
    }
}

@Composable
fun CardInfo(
    id: Int,
    name: String,
    company: String,
    annualFee: String,
    minSpending: String,
    benefits: List<String>
) {
    TODO("Not yet implemented")
}

@Composable
fun CardRecommendScreen(
    onCardClick: (CardInfo) -> Unit  // Changed from ERROR to CardInfo
) {
    // Sample implementation
    Column {
        Text("Card Recommendations")
        // Example card items
        CardInfo(
            id = 1,
            name = "Sample Card",
            company = "Sample Bank",
            annualFee = "30,000 won",
            minSpending = "300,000 won",
            benefits = listOf("Benefit 1", "Benefit 2")
        ).let { card ->
            Card(
                onClick = { onCardClick(card) },
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(card.name, style = MaterialTheme.typography.headlineSmall)
                    Text(card.company, style = MaterialTheme.typography.bodyMedium)
                    Text("Annual Fee: ${card.annualFee}")
                    Text("Min Spending: ${card.minSpending}")
                }
            }
        }
    }
}

@Composable
fun PaymentScreen(onNavigateToHome: () -> Unit, onShowQRScanner: () -> Unit) {
    TODO("Not yet implemented")
}

private fun getCardById(cardId: Int): CardItem {
    val cards = listOf(
        CardItem(1, "토스 신한카드 Mr.Life", "•••• •••• •••• 3456"),
        CardItem(2, "현대카드", "•••• •••• •••• 4567"),
        CardItem(3, "삼성카드", "•••• •••• •••• 5678")
    )
    return cards.find { it.id == cardId } ?: cards[0]
}