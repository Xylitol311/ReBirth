package com.example.fe

import android.graphics.Color
import android.os.Build
import android.os.Bundle
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
import com.example.fe.ui.navigation.AppNavigation
import com.example.fe.ui.navigation.OnboardingNavHost
import com.example.fe.ui.screens.onboard.OnboardingViewModel
import com.example.fe.ui.screens.onboard.OnboardingViewModelFactory
import com.example.fe.ui.screens.splash.SplashScreen

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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