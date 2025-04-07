package com.example.fe

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.fe.data.network.Interceptor.TokenProvider
import com.example.fe.data.network.NetworkClient
import com.example.fe.ui.navigation.AppNavigation
import com.example.fe.ui.navigation.OnboardingNavHost

import com.example.fe.ui.screens.splash.SplashScreen

import com.example.fe.ui.navigation.LoginNavigation
import com.example.fe.ui.screens.onboard.components.device.AndroidDeviceInfoManager
import com.example.fe.ui.screens.onboard.viewmodel.AppTokenProvider
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModelFactory

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tokenProvider = AppTokenProvider(applicationContext)
        NetworkClient.init(tokenProvider)
        // 상태바 색상 강제 설정
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = Color.BLACK
        
        // 상태바 아이콘 색상 설정 (밝은 아이콘)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val flags = window.decorView.systemUiVisibility
            window.decorView.systemUiVisibility = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        
        // 시스템 바 컨트롤러 설정
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false
        
        // 엣지 투 엣지 활성화
        enableEdgeToEdge()
        setContent {
            MainContent()
        }
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
        // 스플래시 화면 이후 적절한 화면으로 이동
        Log.d("PinInputTest","로그인 여부 : ${viewModel.isLoggedIn}")
        if (!viewModel.isLoggedIn) {
            OnboardingNavHost(viewModel)
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