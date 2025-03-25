package com.example.fe.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fe.ui.components.backgrounds.StarryBackground
import com.example.fe.ui.components.navigation.BottomNavBar
import com.example.fe.ui.components.navigation.BottomNavItem
import com.example.fe.ui.components.navigation.TopBar
import com.example.fe.ui.screens.calendar.CalendarScreen
import com.example.fe.ui.screens.recCard.recCard
import com.example.fe.ui.screens.home.HomeScreen
import com.example.fe.ui.screens.mycard.MyCardScreen
import com.example.fe.ui.screens.payment.PaymentScreen
import androidx.compose.animation.core.animateFloatAsState

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
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
    
    Scaffold(
        topBar = { TopBar() },
        bottomBar = { 
            BottomNavBar(
                navController = navController,
                onTabSelected = { item: BottomNavItem ->
                    // 탭 선택 시 전환 방향 계산
                    val newIndex = tabIndices[item.route] ?: 0
                    previousTabIndex = currentTabIndex
                    currentTabIndex = newIndex
                    
                    // 방향 계산 및 애니메이션 트리거
                    transitionDirection = if (newIndex > previousTabIndex) -1 else 1
                    
                    // 누적 오프셋 업데이트 (300f는 한 번의 전환에 대한 이동 거리)
                    cumulativeOffset += transitionDirection * 300f
                    
                    animationCounter++ // 애니메이션 트리거
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 공유 StarryBackground
            StarryBackground(
                scrollOffset = scrollOffset,
                starCount = 150,
                horizontalOffset = animatedHorizontalOffset // 누적된 오프셋 사용
            ) {
                // 내비게이션 호스트
                NavHost(
                    navController = navController,
                    startDestination = BottomNavItem.Home.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(
                        route = BottomNavItem.Home.route,
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
                        HomeScreen(
                            onScrollOffsetChange = { offset ->
                                scrollOffset = offset
                            }
                        )
                    }
                    
                    // 다른 화면들도 동일한 패턴으로 구현
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
                    
                    // 나머지 화면들도 동일한 패턴으로 구현
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
                }
            }
        }
    }
} 