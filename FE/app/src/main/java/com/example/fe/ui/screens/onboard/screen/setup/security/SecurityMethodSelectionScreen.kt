package com.example.fe.ui.screens.onboard.screen.setup.security

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.R
import com.example.fe.ui.screens.onboard.components.AuthMethodOption

@Composable
fun SecurityMethodSelectionScreen(
    onFingerprintSelected: () -> Unit,
    onPatternSelected: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            "추가 인증수단 선택",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "지문인증 또는 패턴인증으로\n더욱 안전하게 로그인 하세요.",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))

        AuthMethodOption(
            title = "지문인증",
            description = "기기에 등록된 지문 인증으로\n빠르게 서비스를 이용할 수 있어요",
            iconResId = R.drawable.fingerprint,
            onClick = onFingerprintSelected
        )

        Spacer(modifier = Modifier.height(24.dp))

        AuthMethodOption(
            title = "패턴인증",
            description = "나만의 패턴을 그려서\n간편하게 서비스를 이용할 수 있어요",
            iconResId = R.drawable.arrow_right,
            onClick = onPatternSelected
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSkip,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF191E3F)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
        ) {
            Text(
                text = "건너뛰기",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
} 