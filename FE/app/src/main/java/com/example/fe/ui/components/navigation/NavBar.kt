package com.example.fe.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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

// 하늘색 색상 정의
val calendarBlue = Color(0xFF00E1FF)

sealed class BottomNavItem(
    val route: String,
    val icon: Int,
    val label: String
) {
    object Home : BottomNavItem(
        route = "home",
        icon = R.drawable.ic_home, // 홈 모양의 아이콘으로 교체 필요 (home 또는 home_filled)
        label = "홈"
    )

    object MyCard : BottomNavItem(
        route = "mycard",
        icon = R.drawable.ic_card, // 카드/신용카드 모양의 아이콘으로 교체 필요 (credit_card 또는 payment)
        label = "카드"
    )

    object Payment : BottomNavItem(
        route = "payment",
        icon = R.drawable.ic_qr, // QR 스캐너/결제 아이콘으로 교체 필요 (qr_code_scanner)
        label = "결제"
    )

    object Calendar : BottomNavItem(
        route = "calendar",
        icon = R.drawable.ic_calendar, // 달력 아이콘으로 교체 필요 (calendar_month 또는 date_range)
        label = "가계부"
    )

    object CardRecommend : BottomNavItem(
        route = "cardrecommend",
        icon = R.drawable.ic_recstar, // 추천 아이콘을 ic_recstar.xml로 변경
        label = "추천"
    )
}

@Preview(showBackground = true)
@Composable
fun BottomNavBarPreview() {
    val navController = rememberNavController()
    BottomNavBar(navController = navController)
}

@Composable
fun BottomNavBar(
    navController: NavController,
    onTabSelected: (BottomNavItem) -> Unit = {},
    onCameraClick: () -> Unit = {}
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.MyCard,
        BottomNavItem.Payment,
        BottomNavItem.Calendar,
        BottomNavItem.CardRecommend
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isPaymentScreen = currentRoute == "payment"

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        containerColor = Color.Black,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            
            NavigationBarItem(
                icon = {
                    Box(
                        modifier = Modifier.padding(top = 0.dp, bottom = 4.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        // 모든 아이콘을 동일한 방식으로 처리, 크기 키움
                        Icon(
                            modifier = Modifier.size(28.dp),
                            painter = painterResource(id = item.icon),
                            contentDescription = null,
                            tint = if (isSelected) calendarBlue else Color.White
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = if (isSelected) calendarBlue else Color.White
                    )
                },
                selected = isSelected,
                onClick = {
                    onTabSelected(item)
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
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