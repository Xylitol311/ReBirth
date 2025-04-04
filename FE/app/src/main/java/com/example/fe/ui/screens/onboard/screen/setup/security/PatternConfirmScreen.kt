package com.example.fe.ui.screens.onboard.screen.setup.security

import androidx.compose.runtime.Composable
import com.example.fe.ui.screens.onboard.auth.PatternAuth

@Composable
fun PatternConfirmScreen(
    onPatternConfirmed: (List<Int>) -> Unit,
    onStepChange: (AdditionalSecurityStep) -> Unit
) {
    PatternAuth(
        currentStep = AdditionalSecurityStep.PATTERN_CONFIRM,
        onPatternConfirmed = onPatternConfirmed,
        onStepChange = onStepChange
    )
} 