package com.example.fe.ui.screens.onboard.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.ui.screens.onboard.components.PatternGrid
import com.example.fe.ui.screens.onboard.screen.setup.security.AdditionalSecurityStep

/**
 * 패턴 인증 화면 컴포저블
 * 사용자가 패턴을 입력하고 확인하는 화면을 제공
 * 
 * @param currentStep 현재 패턴 입력 단계 (최초 입력 또는 확인 입력)
 * @param onPatternConfirmed 패턴 입력이 완료되었을 때 호출되는 콜백
 * @param onStepChange 패턴 입력 단계 변경 시 호출되는 콜백
 */
@Composable
fun PatternAuth(
    currentStep: AdditionalSecurityStep,
    onPatternConfirmed: (List<Int>) -> Unit,
    onStepChange: (AdditionalSecurityStep) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.1f))

        // 단계에 따른 안내 텍스트 표시
        Text(
            text = when (currentStep) {
                AdditionalSecurityStep.PATTERN -> "패턴을 입력해주세요"
                AdditionalSecurityStep.PATTERN_CONFIRM -> "패턴을 한 번 더 입력해주세요"
                else -> ""
            },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 패턴 입력 설명 텍스트
        Text(
            text = when (currentStep) {
                AdditionalSecurityStep.PATTERN -> "4개 이상의 점을 연결하여\n나만의 패턴을 만들어주세요"
                AdditionalSecurityStep.PATTERN_CONFIRM -> "방금 입력한 패턴을\n한 번 더 그려주세요"
                else -> ""
            },
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        // 패턴 입력 그리드
        PatternGrid(
            onPatternComplete = { pattern ->
                if (pattern.size >= 4) {
                    onPatternConfirmed(pattern)
                }
            },
            showConfirmButton = false
        )
    }
}