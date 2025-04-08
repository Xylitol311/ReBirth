package com.example.fe.ui.screens.onboard.screen.setup.security

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.example.fe.ui.screens.onboard.auth.PatternAuth

@Composable
fun PatternInputScreen(
    onPatternConfirmed: (List<Int>) -> Unit,
    onStepChange: (AdditionalSecurityStep) -> Unit
) {
    // 뒤로가기 기능 비활성화
    BackHandler(enabled = true) {
        // 아무 동작도 하지 않음 (뒤로가기 동작 차단)
    }
    
    PatternAuth(
        currentStep = AdditionalSecurityStep.PATTERN,
        onPatternConfirmed = onPatternConfirmed,
        onStepChange = onStepChange
    )
} 