package com.example.fe.ui.screens.onboard.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.ui.screens.onboard.components.PatternGrid
import com.example.fe.ui.screens.onboard.screen.setup.security.AdditionalSecurityStep

@Composable
fun LoginPatternAuth(
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

        Text(
            "패턴을 입력해주세요",
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.weight(0.15f))

        PatternGrid(
            onPatternComplete = { pattern ->
                if (pattern.size >= 4) {
                    onPatternConfirmed(pattern)
                } else {
                    Toast.makeText(context, "패턴이 너무 짧습니다", Toast.LENGTH_SHORT).show()
                }
            },
            showConfirmButton = false
        )

        Spacer(modifier = Modifier.weight(0.05f))
    }
}