package com.example.fe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fe.R
import com.example.fe.ui.screens.onboard.AuthScreen
import com.example.fe.ui.screens.onboard.TutorialPage
import com.example.fe.ui.screens.onboard.TutorialScreen
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel

/**
 * TutorialNavHost.kt (수정된 Compose 버전)
 *
 * - 처음 실행 시 튜토리얼 화면(TutorialScreen)을 표시합니다.
 * - 마지막 페이지의 회원가입 버튼 클릭 시 navController를 통해 "auth" 경로로 이동하여 AuthScreen을 호출합니다.
 */
@Composable
fun TutorialNavHost(onboardingViewModel: OnboardingViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "tutorial") {
        // 튜토리얼 경로: 튜토리얼 페이지를 보여줌
        composable("tutorial") {
            // 튜토리얼 페이지 데이터 정의
            val tutorialPages = listOf(
                TutorialPage(
                    title = "당신의 소비는 조화로운 지구형입니다.",
                    description = "당신의 소비 유형을 분석하고\n나만의 행성을 가져보세요",
                    imageRes = R.drawable.earth
                ),
                TutorialPage(
                    title = "당신에게 필요한 별자리는 바로 이것!",
                    description = "특별한 별자리로\n내 카드를 표현하세요",
                    imageRes = R.drawable.shinhan_card
                ),
                TutorialPage(
                    title = "REBIRTH 슈퍼 카드로 가장\n최적의 별자리를 제안해드립니다.",
                    description = "별자리의 힘을 받아\n최고 혜택 카드로 자동 결제하세요.",
                    imageRes = R.drawable.constellation2
                )
            )

            // TutorialScreen에서 회원가입 버튼 클릭 시 처리:
            // 1. OnboardingViewModel의 튜토리얼 완료 상태 업데이트
            // 2. "auth" 경로로 네비게이션 (회원가입/인증 플로우로 전환)
            TutorialScreen(
                tutorialPages = tutorialPages,
                onSignUpClick = {
                    onboardingViewModel.setTutorialCompleted() // 튜토리얼 완료 처리
                    navController.navigate("auth") {
                        // 튜토리얼 화면을 스택에서 제거하여 뒤로 가기 시 튜토리얼로 돌아가지 않도록 함
                        popUpTo("tutorial") { inclusive = true }
                    }
                }
            )
        }
        // "auth" 경로: 회원가입(혹은 인증) 화면 호출
        composable("auth") {
            AuthScreen(
                navController = navController,
                viewModel = onboardingViewModel
            )
        }
    }
}