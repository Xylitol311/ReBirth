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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fe.R

sealed class BottomNavItem(
    val route: String,
    val icon: Int,
) {
    object Home : BottomNavItem(
        route = "home",
        icon = R.drawable.ic_home,
    )

    object MyCard : BottomNavItem(
        route = "mycard",
        icon = R.drawable.ic_card,
    )

    object Payment : BottomNavItem(
        route = "payment",
        icon = R.drawable.ic_qr,
    )

    object Calendar : BottomNavItem(
        route = "calendar",
        icon = R.drawable.ic_calendar,
    )

    object CardRecommend : BottomNavItem(
        route = "cardrecommend",
        icon = R.drawable.ic_search,
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
    onTabSelected: (BottomNavItem) -> Unit = {}
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.MyCard,
        BottomNavItem.Payment,
        BottomNavItem.Calendar,
        BottomNavItem.CardRecommend
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        containerColor = Color.Black,
        tonalElevation = 0.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Box(
                        modifier = Modifier.padding(top = 0.dp, bottom = 16.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (item == BottomNavItem.Payment) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        color = Color(0xFF2196F3),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    modifier = Modifier.size(40.dp),
                                    painter = painterResource(id = item.icon),
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        } else {
                            Icon(
                                modifier = Modifier.size(30.dp),
                                painter = painterResource(id = item.icon),
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                },
                label = null,
                selected = currentRoute == item.route,
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
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                    unselectedTextColor = Color.White.copy(alpha = 0.6f),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}