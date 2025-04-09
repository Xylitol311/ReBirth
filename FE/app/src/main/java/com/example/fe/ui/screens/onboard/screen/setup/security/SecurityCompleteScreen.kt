package com.example.fe.ui.screens.onboard.screen.setup.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fe.ui.theme.SkyBlue

@Composable
fun SecurityCompleteScreen(
    navController: NavController
) {
    val density = LocalDensity.current
    val fontScale = density.fontScale
    val titleBaseFontSize = 28.sp
    val titleDynamicFontSize = (titleBaseFontSize.value * fontScale).sp

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        navController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkyBlue, // 하늘색 배경
                        contentColor = Color.White // 흰색 텍스트
                    )
                ) {
                    Text(
                        text = "다음",
                        fontSize = 22.sp,
                        color = Color.White
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            Text(
                "보안 설정이 완료되었습니다",
                fontSize = titleDynamicFontSize,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // 여기에 필요한 경우 추가 콘텐츠 배치 가능
        }
    }
} 