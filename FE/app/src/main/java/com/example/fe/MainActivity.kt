package com.example.fe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fe.ui.navigation.AppNavigation
import com.example.fe.ui.navigation.OnboardingNavHost
import com.example.fe.ui.screens.onboard.OnboardingViewModel
import com.example.fe.ui.screens.onboard.OnboardingViewModelFactory
import com.example.fe.ui.screens.splash.SplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainContent()
        }
    }
}

@Composable
fun MainContent() {
    val context = LocalContext.current
    val viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModelFactory(context)
    )

    // 앱 상태 관리 (스플래시 화면 표시 여부)
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        // 스플래시 화면 표시
        SplashScreen(
            onSplashComplete = { isUserLoggedIn ->
                showSplash = false
                // 스플래시가 끝날 때 전달받은 로그인 상태를 다시 확인 (필요에 따라)
                // viewModel.setLoggedInState(isUserLoggedIn)
            },
            isLoggedIn = viewModel.isLoggedIn
        )
    } else {
        // 스플래시 화면 이후 적절한 화면으로 이동
        if (!viewModel.isLoggedIn) {
            OnboardingNavHost(viewModel)
        } else {
            AppNavigation()
        }
    }
}