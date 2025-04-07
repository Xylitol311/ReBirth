package com.example.fe.ui.screens.onboard.screen.setup

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel
import com.example.fe.ui.screens.onboard.auth.PatternAuth
import com.example.fe.ui.screens.onboard.auth.PatternLockView
import com.example.fe.ui.screens.onboard.screen.setup.security.AdditionalSecurityStep
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternLoginScreen(
    navController: NavController,
    viewModel: OnboardingViewModel,
    onLoginSuccess: () -> Unit
) {
    Scaffold {
        Box(modifier = Modifier.padding(it)) {
            PatternLoginContent(
                viewModel = viewModel,
                onLoginSuccess = onLoginSuccess
            )
        }
    }
}

@Composable
fun PatternLoginContent(
    viewModel: OnboardingViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val savedPattern by remember { mutableStateOf(viewModel.getUserPattern()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "패턴을 입력해주세요",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // 실제 프로젝트의 PatternLockView 사용
        PatternLockView(
            modifier = Modifier
                .size(300.dp)
                .padding(16.dp),
            patternSize = 3,
            onPatternComplete = { pattern ->
                if (savedPattern != null && pattern.size == savedPattern.size &&
                    pattern.zip(savedPattern).all { (a, b) -> a == b }) {
                    onLoginSuccess()
                } else {
                    Toast.makeText(
                        context,
                        "패턴이 일치하지 않습니다",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}

// 기존 SimplePatternLockView 대신 실제 프로젝트의 PatternLockView 사용
// (라이브러리 또는 직접 구현한 컴포넌트)