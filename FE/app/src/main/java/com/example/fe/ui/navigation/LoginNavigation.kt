package com.example.fe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.fe.ui.screens.onboard.OnboardingViewModel
import com.example.fe.ui.screens.onboard.screen.login.PinLoginScreen
import com.example.fe.ui.screens.onboard.screen.login.PatternLoginScreen
import com.example.fe.ui.screens.onboard.screen.login.FingerprintLoginScreen
import com.example.fe.ui.screens.home.HomeScreen

@Composable
fun LoginNavigation(
    navController: NavHostController,
    viewModel: OnboardingViewModel,
    startDestination: String,
    onLoginSuccess: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("pin_login") {
            PinLoginScreen(
                navController = navController,
                viewModel = viewModel,
                onLoginSuccess = onLoginSuccess
            )
        }
        composable("pattern_login") {
            PatternLoginScreen(
                navController = navController,
                viewModel = viewModel,
                onLoginSuccess = onLoginSuccess
            )
        }
        composable("fingerprint_login") {
            FingerprintLoginScreen(
                navController = navController,
                onboardingViewModel = viewModel,
                onLoginSuccess = onLoginSuccess
            )
        }
        composable("home") {
            onLoginSuccess()
        }
    }
} 