package com.example.fe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import com.example.fe.ui.components.navigation.BottomNavBar
import com.example.fe.ui.screens.home.HomeScreen
import com.example.fe.ui.theme.FETheme
import com.example.fe.ui.components.navigation.TopBar
import com.example.fe.ui.components.navigation.BottomNavItem

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FETheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomNavBar(navController = navController) },
        content = { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Home.route,
                modifier = Modifier.padding(
                    bottom = paddingValues.calculateBottomPadding(),
                    top = paddingValues.calculateTopPadding()
                )
            ) {
                composable(BottomNavItem.Home.route) {
                    HomeScreen()
                }
                composable(BottomNavItem.MyCard.route) {
                    // MyCard 화면
                }
                composable(BottomNavItem.Payment.route) {
                    // Payment 화면
                }
                composable(BottomNavItem.Calendar.route) {
                    // Calendar 화면
                }
                composable(BottomNavItem.CardRecommend.route) {
                    // CardRecommend 화면
                }
            }
        }
    )
}