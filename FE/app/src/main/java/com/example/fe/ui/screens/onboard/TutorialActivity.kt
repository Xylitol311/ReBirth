package com.example.fe.ui.screens.onboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fe.R
import com.example.fe.ui.screens.onboard.components.device.AndroidDeviceInfoManager
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModelFactory

/**
 * TutorialActivity.kt (수정된 Compose 버전)
 *
 * - 처음 실행 시 튜토리얼 화면(TutorialScreen)을 표시합니다.
 * - 마지막 페이지의 회원가입 버튼 클릭 시 navController를 통해 "auth" 경로로 이동하여 AuthScreen을 호출합니다.
 */
class TutorialActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TutorialNavHost()
        }
    }
}

@Composable
fun TutorialNavHost() {
    // Context 및 온보딩 관련 ViewModel 설정
    val context = LocalContext.current
    val deviceInfoManager = AndroidDeviceInfoManager(context)
    val onboardingViewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModelFactory(deviceInfoManager, context)
    )
    // NavController 생성
    val navController = rememberNavController()

    // NavHost 구성: startDestination를 "tutorial"로 지정
    androidx.navigation.compose.NavHost(
        navController = navController,
        startDestination = "tutorial"
    ) {
        // "tutorial" 경로: 튜토리얼 화면 표시
        composable("tutorial") {
            // 튜토리얼 페이지 리스트 생성 (이미지 리소스는 기존 R.drawable.xxx 사용)
            val tutorialPages = listOf(
                TutorialPage(
                    title = "당신의 소비는 조화로운 지구형입니다.",
                    description = "당신의 소비 유형을 분석하고 나만의 행성을 가져보세요",
                    imageRes = R.drawable.earth
                ),
                TutorialPage(
                    title = "당신에게 필요한 별자리는 삼성카드 taptap O",
                    description = "특별한 별자리로 내 카드를 표현하세요",
                    imageRes = R.drawable.card_samsung
                ),
                TutorialPage(
                    title = "REBIRTH 슈퍼 카드로 가장 최적의 별자리를 제안해드립니다.",
                    description = "별자리의 힘을 받아 최고 혜택 카드로 자동 결제하세요.",
                    imageRes = R.drawable.constellation
                )
            )
            TutorialScreen(
                tutorialPages = tutorialPages,
                onSignUpClick = {
                    // 회원가입 버튼 클릭 시 "auth" 경로로 이동
                    navController.navigate("auth")
                }
            )
        }
        // "auth" 경로: AuthScreen 호출 (이미 제공된 AuthScreen 파일 사용)
        composable("auth") {
            AuthScreen(
                navController = navController,
                viewModel = onboardingViewModel
            )
        }

        // 온보딩 플로우 내 다른 화면이 필요하다면 OnboardingNavHost에 등록된 경로와 유사하게 추가 가능합니다.
        // 예) composable("onboarding") { OnboardingScreen(navController, onboardingViewModel) }
    }
}
