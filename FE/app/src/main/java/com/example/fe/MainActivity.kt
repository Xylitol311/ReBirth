package com.example.fe

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.fe.data.network.NetworkClient
import com.example.fe.ui.navigation.AppNavigation
import com.example.fe.ui.navigation.OnboardingNavHost
import com.example.fe.ui.screens.splash.SplashScreen
import com.example.fe.ui.navigation.LoginNavigation
import com.example.fe.ui.navigation.TutorialNavHost
import com.example.fe.ui.screens.onboard.components.device.AndroidDeviceInfoManager
import com.example.fe.ui.screens.onboard.viewmodel.AppTokenProvider
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModelFactory

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // *** 상태바 설정을 가장 먼저 처리 ***
        setupTransparentStatusBar()
        
        val tokenProvider = AppTokenProvider(applicationContext)
        NetworkClient.init(tokenProvider)
        
        setContent {
            MainContent()
        }
    }
    
    private fun setupTransparentStatusBar() {
        // 상태바를 투명하게 만들기
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
        
        // 상태바 아이콘을 흰색으로 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        
        // 화면 확장 (상태바까지 콘텐츠 표시)
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}

@Composable
fun MainContent() {
    val context = LocalContext.current
    val deviceInfoManager = remember { AndroidDeviceInfoManager(context) }
    val viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModelFactory(deviceInfoManager,context)
    )

    // 앱 상태 관리 (스플래시 화면 표시 여부)
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        // 스플래시 화면 표시
        SplashScreen(
            onSplashComplete = { isUserLoggedIn ->
                showSplash = false
            },
            isLoggedIn = viewModel.isLoggedIn
        )
    } else {
        // 스플래시 후 화면 분기 처리
        Log.d("MainContent", "로그인 여부 : ${viewModel.isLoggedIn}")

        if (!viewModel.isLoggedIn) {
            // 로그인되어 있지 않은 경우:
            // 튜토리얼 미완료 시 -> 튜토리얼 플로우 호출
            // 튜토리얼 완료 시 -> 기존 Onboarding(회원가입) 플로우 호출
            if (!viewModel.hasCompletedTutorial) {
                // 튜토리얼 플로우 호출 (TutorialNavHost)
                TutorialNavHost(onboardingViewModel = viewModel)
            } else {
                // 튜토리얼 완료 후 온보딩 플로우 호출
                OnboardingNavHost(viewModel)
            }
        } else {
            // 로그인된 경우 인증 수단에 따라 분기
            val navController = rememberNavController()
            val startDestination = when {
                viewModel.hasPatternAuth -> "pattern_login"
                viewModel.hasBiometricAuth -> "fingerprint_login"
                else -> "pin_login"  // PIN은 기본 인증 수단
            }
            
            // 로그인 성공 여부 상태
            var isLoginSuccessful by remember { mutableStateOf(false) }
            
            if (!isLoginSuccessful) {
                LoginNavigation(
                    deviceInfoManager = deviceInfoManager,
                    navController = navController,
                    viewModel = viewModel,
                    startDestination = startDestination,
                    onLoginSuccess = {
                        isLoginSuccessful = true
                    }
                )
            } else {
                AppNavigation()
            }
        }
    }
}