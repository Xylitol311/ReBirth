package com.example.fe.ui.screens.onboard.screen.setup.security

import androidx.compose.runtime.Composable
import com.example.fe.ui.screens.onboard.auth.PatternAuth

@Composable
fun PatternInputScreen(
    onPatternConfirmed: (List<Int>) -> Unit,
    onStepChange: (AdditionalSecurityStep) -> Unit
) {
    PatternAuth(
        currentStep = AdditionalSecurityStep.PATTERN,
        onPatternConfirmed = onPatternConfirmed,
        onStepChange = onStepChange
    )
} 