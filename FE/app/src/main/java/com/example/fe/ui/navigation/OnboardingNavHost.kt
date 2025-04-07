package com.example.fe.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fe.data.network.api.AuthApiService
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel
import com.example.fe.ui.screens.onboard.OnboardingScreen
import com.example.fe.ui.screens.onboard.AuthScreen
import com.example.fe.ui.screens.onboard.CardSelectScreen

import com.example.fe.ui.screens.onboard.RegistrationCompleteScreen
import com.example.fe.ui.screens.onboard.components.device.DeviceInfoManager
import com.example.fe.ui.screens.onboard.screen.setup.AdditionalSecurityScreen
import com.example.fe.ui.screens.onboard.screen.setup.PinSetupScreen

@Composable
fun OnboardingNavHost(
    onboardingViewModel: OnboardingViewModel,
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "onboarding") {
        // 온보딩 시작 화면
        composable("onboarding") {
            OnboardingScreen(navController, onboardingViewModel)
        }

        // 인증 화면
        composable("auth") {
            AuthScreen(navController, onboardingViewModel)
        }

        // PIN 설정 화면
        composable(
            "pin_setup/{name}/{phone}/{ssnFront}/{income}",
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("phone") { type = NavType.StringType },
                navArgument("ssnFront") { type = NavType.StringType },
                navArgument("income") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            PinSetupScreen(
                navController = navController,
                name = backStackEntry.arguments?.getString("name") ?: "",
                phone = backStackEntry.arguments?.getString("phone") ?: "",
                ssnFront = backStackEntry.arguments?.getString("ssnFront") ?: "",
                income = backStackEntry.arguments?.getString("income") ?: ""
            )
        }

        // 기타 화면들
        composable("card_select") {
            CardSelectScreen(navController, onboardingViewModel)
        }
        composable("additional_security_setup") {
            AdditionalSecurityScreen(navController, onboardingViewModel)
        }
        composable("registration_complete") {
            RegistrationCompleteScreen(navController, onboardingViewModel)
        }
    }
}

