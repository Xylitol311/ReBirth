package com.example.fe.ui.components.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
val calendarBlue = Color(0xFF2DD0FA)

// BottomNavItem을 sealed class로 정의하여 네비게이션 탭별 정보를 한 곳에 모아둠
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
    // 미리보기용 네비게이션 컨트롤러 생성
    val navController = rememberNavController()
    BottomNavBar(navController = navController)
}

@Composable
fun BottomNavBar(
    navController: NavController,
    onTabSelected: (BottomNavItem) -> Unit = {},
    onCameraClick: () -> Unit = {}
) {
    // 네비게이션 탭 목록 (왼쪽부터 오른쪽까지 배치됨)
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.MyCard,
        BottomNavItem.Payment,      // 가운데 버튼
        BottomNavItem.Calendar,
        BottomNavItem.CardRecommend
    )

    // 현재 네비게이션 백스택 상태를 관찰하여 현재 선택된 라우트를 파악
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // NavigationBar
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(80.dp),
        containerColor = Color(0xFF0F1425),
        tonalElevation = 0.dp
    ) {
        // 1) NavigationBar의 자식으로 Row를 하나 만든다.
        // 2) Row 안에서 5개의 NavigationBarItem을 중첩해서 배치한다.
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            // 버튼들을 "결제 버튼" 중심으로 모아두기 위해서
            // Arrangement.Center + spacedBy(...)로 간격 조정
            horizontalArrangement = Arrangement.spacedBy(
                space = -7.dp, // 버튼 간 간격
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                // 현재 탭이 선택되었는지 여부 판단
                val isSelected = currentRoute == item.route

                // NavigationBarItem은 기본 Material3 스타일 그대로 사용
                NavigationBarItem(
                    icon = {
                        // 결제(가운데) 버튼만 아이콘 위치를 조금 위/아래 offset
                        val iconModifier = if (item == BottomNavItem.Payment) {
                            Modifier.offset(y = 0.5.dp)
                        } else {
                            Modifier
                        }

                        // Box를 사용하여 아이콘 정렬 및 상하 패딩
                        Box(
                            modifier = iconModifier.padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(28.dp),
                                painter = painterResource(id = item.icon),
                                contentDescription = null,
                                tint = if (isSelected) calendarBlue else Color.White
                            )
                        }
                    },
                    label = {
                        // 탭 라벨
                        Text(
                            modifier = Modifier.offset(y = -6.5.dp),
                            text = item.label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = if (isSelected) calendarBlue else Color.White
                        )
                    },
                    selected = isSelected,
                    onClick = {
                        // 탭 클릭 시 콜백 + 네비게이션 처리
                        onTabSelected(item)
                        navController.navigate(item.route) {
                            navController.graph.startDestinationRoute?.let { route ->
                                popUpTo(route) { saveState = true }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
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
}
