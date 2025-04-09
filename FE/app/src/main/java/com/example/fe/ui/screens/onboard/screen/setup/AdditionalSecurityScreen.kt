package com.example.fe.ui.screens.onboard.screen.setup

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.example.fe.R
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel
import com.example.fe.ui.screens.onboard.auth.FingerprintAuth
import com.example.fe.ui.screens.onboard.auth.PatternAuth
import com.example.fe.ui.screens.onboard.components.AuthMethodOption
import com.example.fe.ui.screens.onboard.screen.setup.security.AdditionalSecurityStep

/**
 * FragmentActivity를 찾기 위한 확장 함수
 * 지문 인증에 필요한 FragmentActivity를 Context로부터 찾아서 반환
 */
fun Context.findActivity(): FragmentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) return context
        context = context.baseContext
    }
    return null
}

/**
 * 추가 보안 설정 화면
 * 사용자가 지문이나 패턴 인증을 추가로 설정할 수 있는 메인 화면
 * 
 * @param navController 화면 전환을 위한 네비게이션 컨트롤러
 * @param viewModel 보안 설정 상태를 관리하는 뷰모델
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdditionalSecurityScreen(
    navController: NavController,
    viewModel: OnboardingViewModel
) {
    val context = LocalContext.current
    // 현재 단계를 관리하는 상태 (METHOD: 선택 화면, PATTERN: 패턴 설정, COMPLETE: 완료 화면)
    var currentStep by remember { mutableStateOf(AdditionalSecurityStep.METHOD) }
    // 패턴 인증 시 첫 번째 입력한 패턴을 저장하는 상태
    var savedPattern by remember { mutableStateOf<List<Int>?>(null) }

    Scaffold(
        topBar = {
            // 메인 선택 화면과 완료 화면을 제외한 화면에서만 뒤로가기 버튼 표시
            if (currentStep != AdditionalSecurityStep.METHOD && currentStep != AdditionalSecurityStep.COMPLETE) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(
                            onClick = { currentStep = AdditionalSecurityStep.METHOD },
                            modifier = Modifier.size(54.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "뒤로가기",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (currentStep) {
                AdditionalSecurityStep.METHOD -> {
                    // 추가 인증 수단 선택 화면
                    SecurityMethodSelectionScreen(
                        onFingerprintSelected = {
                            // 지문 인증 선택 시 처리
                            val activity = context.findActivity()
                            if (activity != null) {
                                FingerprintAuth.authenticate(activity) { success ->
                                    if (success) {
                                        viewModel.setBiometricAuthState(true)
                                        currentStep = AdditionalSecurityStep.COMPLETE
                                    } else {
                                        Toast.makeText(context, "지문 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        onPatternSelected = {
                            // 패턴 인증 선택 시 패턴 설정 화면으로 이동
                            currentStep = AdditionalSecurityStep.PATTERN
                        },
                        onSkip = {
                            // 건너뛰기 선택 시 완료 화면으로 이동
                            currentStep = AdditionalSecurityStep.COMPLETE
                        }
                    )
                }
                AdditionalSecurityStep.PATTERN -> {
                    // 패턴 첫 입력 화면
                    PatternAuth(
                        currentStep = currentStep,
                        onPatternConfirmed = { pattern ->
                            savedPattern = pattern
                            currentStep = AdditionalSecurityStep.PATTERN_CONFIRM
                        },
                        onStepChange = { step -> currentStep = step }
                    )
                }
                AdditionalSecurityStep.PATTERN_CONFIRM -> {
                    // 패턴 확인 화면 - 첫 입력과 동일한지 확인
                    PatternAuth(
                        currentStep = AdditionalSecurityStep.PATTERN_CONFIRM,
                        onPatternConfirmed = { pattern ->
                            if (savedPattern != null && pattern.size == savedPattern!!.size &&
                                pattern.zip(savedPattern!!).all { (a, b) -> a == b }) {
                                // 패턴이 일치하면 저장하고 완료 화면으로 이동
                                Log.d("AuthPattern","savedpattern ${savedPattern}")
                                viewModel.registerPattern(
                                    pattern = savedPattern!!,
                                    onSuccess = {
                                        Log.d("AuthPattern","패턴저장")
                                        currentStep = AdditionalSecurityStep.COMPLETE
                                    },
                                    onFailure = { error ->
                                        Log.d("AuthPattern","${error}")
                                        Toast.makeText(context, "패턴 등록 중 오류 발생: $error", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                viewModel.setUserPattern()
                                currentStep = AdditionalSecurityStep.COMPLETE
                            } else {
                                // 패턴이 일치하지 않으면 다시 처음부터
                                Toast.makeText(context, "패턴이 일치하지 않습니다. 다시 시도하세요.", Toast.LENGTH_SHORT).show()
                                currentStep = AdditionalSecurityStep.PATTERN
                            }
                        },
                        onStepChange = { step -> currentStep = step }
                    )
                }
                AdditionalSecurityStep.COMPLETE -> {
                    // 보안 설정 완료 화면
                    SecurityCompleteScreen(
                        onNext = {
                            // 다음 버튼 클릭 시 회원가입 완료 화면으로 이동
                            navController.navigate("registration_complete")
                        }
                    )
                }
                else -> {
                    currentStep = AdditionalSecurityStep.METHOD
                }
            }
        }
    }
}

/**
 * 추가 인증 수단 선택 화면
 * 지문 인증과 패턴 인증 중 선택할 수 있는 화면
 * 
 * @param onFingerprintSelected 지문 인증 선택 시 콜백
 * @param onPatternSelected 패턴 인증 선택 시 콜백
 * @param onSkip 건너뛰기 선택 시 콜백
 */
@Composable
private fun SecurityMethodSelectionScreen(
    onFingerprintSelected: () -> Unit,
    onPatternSelected: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9FB))
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "추가 인증수단 선택",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "지문인증 또는 패턴인증으로\n보다 간편하게 로그인 해보세요.",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(36.dp))

        AuthCardItem(
            iconResId = R.drawable.fingerprint,
            title = "지문인증",
            description = "휴대폰에 등록되어 있는 지문정보를\n사용하여 사용자 인증을 합니다.",
            onClick = onFingerprintSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthCardItem(
            iconResId = R.drawable.pattern,
            title = "패턴인증",
            description = "내가 설정한 패턴을 이용해서\n사용자 인증을 합니다.",
            onClick = onPatternSelected
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSkip,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00D1FF)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "건너뛰기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }


        Spacer(modifier = Modifier.height(20.dp))
    }
}
@Composable
fun AuthCardItem(
    iconResId: Int,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "화살표",
                modifier = Modifier.size(16.dp),
                tint = Color.Gray
            )
        }
    }
}

/**
 * 보안 설정 완료 화면
 * 선택한 보안 설정이 완료되었음을 알리는 화면
 * 
 * @param onNext 다음 버튼 클릭 시 콜백
 */
@Composable
private fun SecurityCompleteScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(120.dp))

        Text(
            text = "보안 설정 완료",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "앱을 더욱 안전하게 보호할 수 있어요",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00D9FF)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "다음",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}