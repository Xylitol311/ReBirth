package com.example.fe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fe.ui.screens.onboard.OnboardingViewModel
import com.example.fe.ui.screens.onboard.OnboardingScreen
import com.example.fe.ui.screens.onboard.AuthScreen
import com.example.fe.ui.screens.onboard.CardSelectScreen
import com.example.fe.ui.screens.onboard.SecuritySetupScreen

@Composable
fun OnboardingNavHost(viewModel: OnboardingViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "onboarding") {
        composable("onboarding") { OnboardingScreen(navController, viewModel) }
        composable("auth") { AuthScreen(navController, viewModel) }
        composable("card_select") { CardSelectScreen(navController, viewModel) }
        composable("security_setup") { SecuritySetupScreen(navController, viewModel) }
//        composable("register_complete") {
//            CompleteScreen {
//                viewModel.setLoggedIn(true)
//            }
//        }
    }
}
