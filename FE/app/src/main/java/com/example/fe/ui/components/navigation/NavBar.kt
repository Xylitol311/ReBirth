package com.example.fe.ui.components.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fe.R

// 하늘색 색상 정의 (선택된 상태에서 사용)
val calendarBlue = Color(0xFF00E1FF)

// BottomNavItem을 sealed class로 정의하여 내비게이션 탭별 정보를 한 곳에 모아둠
sealed class BottomNavItem(
    val route: String,
    val icon: Int,
    val label: String
) {
    object Home : BottomNavItem(
        route = "home",
        icon = R.drawable.ic_home_new, // 홈 아이콘
        label = "홈"
    )

    object MyCard : BottomNavItem(
        route = "mycard",
        icon = R.drawable.ic_card, // 카드 아이콘
        label = "카드"
    )

    object Payment : BottomNavItem(
        route = "payment",
        icon = R.drawable.ic_barcode_new, // 결제/QR 스캔 아이콘
        label = "결제"
    )

    object Calendar : BottomNavItem(
        route = "calendar",
        icon = R.drawable.ic_calendar_new, // 달력 아이콘
        label = "가계부"
    )

    object CardRecommend : BottomNavItem(
        route = "cardrecommend",
        icon = R.drawable.ic_star_card_new, // 추천 아이콘
        label = "추천"
    )
}

@Preview(showBackground = true)
@Composable
fun BottomNavBarPreview() {
    // 미리보기용 내비게이션 컨트롤러 생성
    val navController = rememberNavController()
    BottomNavBar(navController = navController)
}

@Composable
fun BottomNavBar(
    navController: NavController,
    onTabSelected: (BottomNavItem) -> Unit = {},
    onCameraClick: () -> Unit = {}
) {
    // 내비게이션 탭 목록 (왼쪽부터 오른쪽까지 배치됨)
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.MyCard,
        BottomNavItem.Payment,      // 가운데 버튼 (별도 처리 예정)
        BottomNavItem.Calendar,
        BottomNavItem.CardRecommend
    )

    // 현재 내비게이션 백스택 상태를 관찰하여 현재 선택된 라우트를 파악
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp), // 내비게이션 바 전체 높이 (세로 간격을 조금 줄임)
        containerColor = Color.Black, // 바텀 내비게이션 배경색
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            // 현재 탭이 선택되었는지 여부 판단
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                icon = {
                    // Box를 사용하여 아이콘의 정렬 및 패딩을 제어
                    // 모든 아이템의 하단 패딩을 줄여서 세로 간격을 최소화합니다.
                    // 결제 아이콘은 Modifier.offset을 사용하여 상단으로 이동 시킵니다.
                    val modifier = if (item == BottomNavItem.Payment) {
                        Modifier.offset(y = (-1).dp) // 결제 아이콘을 위로 4.dp 이동
                    } else {
                        Modifier // 다른 아이콘은 기본 위치 유지
                    }

                    Box(
                        modifier = modifier.padding(vertical = 2.dp), // 상하 패딩을 2.dp로 줄여서 아이콘 간 간격 조정
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Icon(
                            modifier = Modifier.size(26.dp), // 아이콘 크기를 약간 크게 (26.dp)
                            painter = painterResource(id = item.icon),
                            contentDescription = null,
                            tint = if (isSelected) calendarBlue else Color.White
                        )
                    }
                },
                label = {
                    // Text 컴포저블로 탭의 라벨 표시 (글자 크기는 12.sp 유지)
                    Text(
                        text = item.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = if (isSelected) calendarBlue else Color.White
                    )
                },
                selected = isSelected,
                onClick = {
                    // 탭 클릭 시 콜백 호출 및 내비게이션 전환
                    onTabSelected(item)
                    navController.navigate(item.route) {
                        // 내비게이션 스택의 시작 지점을 기준으로 popUpTo 수행 (중복 쌓임 방지)
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true   // 동일 화면이 중복 쌓이지 않도록 보장
                        restoreState = true        // 이전 상태 복원
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    // 내비게이션 아이템 선택/비선택 상태에 따른 색상 정의
                    selectedIconColor = calendarBlue,
                    selectedTextColor = calendarBlue,
                    unselectedIconColor = Color.White.copy(alpha = 0.9f),
                    unselectedTextColor = Color.White.copy(alpha = 0.9f),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
