package com.example.fe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.fe.ui.screens.onboard.screen.login.PinLoginScreen
import com.example.fe.ui.screens.onboard.screen.login.FingerprintLoginScreen
import com.example.fe.ui.screens.home.HomeScreen
import com.example.fe.ui.screens.onboard.components.device.DeviceInfoManager
import com.example.fe.ui.screens.onboard.screen.setup.PatternLoginScreen
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel

@Composable
fun LoginNavigation(
    deviceInfoManager: DeviceInfoManager,
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
                deviceInfoManager = deviceInfoManager,
                viewModel = viewModel,
                onLoginSuccess = onLoginSuccess
            )
        }
        composable("pattern_login") {
            PatternLoginScreen(
                navController = navController,
                viewModel = viewModel,
                deviceInfoManager = deviceInfoManager,
                onLoginSuccess = onLoginSuccess  // 콜백 전달
            )
        }
        composable("fingerprint_login") {
            FingerprintLoginScreen(
                viewModel = viewModel,
                deviceInfoManager = deviceInfoManager,
                onLoginSuccess = onLoginSuccess
            )
        }
        composable("home") {
            onLoginSuccess()
        }
    }
} 