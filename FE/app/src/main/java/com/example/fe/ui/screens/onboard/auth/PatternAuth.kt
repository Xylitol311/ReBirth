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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.ui.security.SecurityStep
import com.example.fe.ui.screens.onboard.components.PatternGrid
@Composable
fun PatternAuth(
    currentStep: SecurityStep,
    onPatternConfirmed: (List<Int>) -> Unit,
    onStepChange: (SecurityStep) -> Unit
) {
    val context = LocalContext.current
    // 불필요한 내부 상태 제거

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.1f))

        Text(
            if (currentStep == SecurityStep.PATTERN) "패턴을 설정해주세요"
            else "패턴을 다시 입력해주세요",
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(32.dp))

        PatternGrid(
            onPatternComplete = { pattern ->
                if (pattern is List<*> && pattern.all { it is Int }) {
                    val validPattern = pattern.filterIsInstance<Int>()

                    if (validPattern.size < 4) {
                        Toast.makeText(
                            context,
                            "최소 4개 이상의 점을 연결해주세요",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@PatternGrid
                    }

                    // 패턴을 부모 컴포넌트로 전달
                    onPatternConfirmed(validPattern)
                }
            },
            showConfirmButton = false
        )
    }
}