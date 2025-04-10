package com.example.fe.ui.screens.onboard.screen.login

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fe.ui.components.backgrounds.StarryBackground

import com.example.fe.ui.screens.onboard.auth.FingerprintAuthComposable
import com.example.fe.ui.screens.onboard.components.device.DeviceInfoManager
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FingerprintLoginScreen(
    viewModel: OnboardingViewModel,
    deviceInfoManager: DeviceInfoManager,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var showError by remember { mutableStateOf(false) }

    // 메시지를 자동으로 사라지게
    LaunchedEffect(showError) {
        if (showError) {
            delay(3000)
            showError = false
        }
    }

    StarryBackground {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(0.1f))

                    Text(
                        "생체 인증을 진행해 주세요",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 지문 인증 컴포넌트
                    FingerprintAuthComposable { success ->
                        if (success) {
                            viewModel.login(
                                type = "fingerprint",
                                number = null,
                                phoneSerialNumber = deviceInfoManager.getDeviceId(),
                                onSuccess = {
                                    onLoginSuccess()
                                },
                                onFailure = { error ->
                                    Log.e("bioLoginPin", "$error")
                                    Toast.makeText(context, "로그인 실패: $error", Toast.LENGTH_SHORT).show()
                                }
                            )
                            onLoginSuccess()
                        } else {
                            showError = true
                        }
                    }
                }

                // 하단 알림
                AnimatedVisibility(
                    visible = showError,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "지문 인증에 실패했습니다. 다시 시도해 주세요.",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .background(Color(0xAA000000), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}