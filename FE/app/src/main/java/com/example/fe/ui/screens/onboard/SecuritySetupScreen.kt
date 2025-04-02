package com.example.app.ui.security

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.example.fe.R
import com.example.fe.ui.screens.onboard.OnboardingViewModel
import com.example.fe.ui.screens.onboard.auth.FingerprintAuth
import com.example.fe.ui.screens.onboard.auth.PatternAuth
import com.example.fe.ui.screens.onboard.auth.PinAuth
import com.example.fe.ui.screens.onboard.components.AuthMethodOption


enum class SecurityStep { PIN, PIN_CONFIRM, METHOD, PATTERN, PATTERN_CONFIRM, DONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySetupScreen(
    navController: NavController,
    viewModel: OnboardingViewModel
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(SecurityStep.PIN) }
    val activity = remember { context.findActivity() }
    var savedPattern by remember { mutableStateOf<List<Int>?>(null) }

    Scaffold(

        //상단바
        //PIN_CONFIRM하고 PATTERN_CONFIRM 일때만 상단바
        topBar = {
            SecuritySetupTopBar(currentStep) {
                when (currentStep) {
                    SecurityStep.PIN_CONFIRM -> currentStep = SecurityStep.PIN
                    SecurityStep.PATTERN_CONFIRM -> currentStep = SecurityStep.PATTERN
                    else -> {}
                }
            }
        }
    )
    //본문
    { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            //조건에 따라 다른 화면 표시
            when (currentStep) {

                //PIN 관련 화면일 때
                SecurityStep.PIN, SecurityStep.PIN_CONFIRM -> PinAuth(
                    currentStep = currentStep,
                    onPinConfirmed = { currentStep = SecurityStep.METHOD },
                    onStepChange = { currentStep = it }
                )

                //지문 인식 또는 패턴 인증 방식 선택하기
                SecurityStep.METHOD -> SecurityMethodSelection(
                    onFingerprintSelected = {
                        val biometricManager = BiometricManager.from(context)
                        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
                            BiometricManager.BIOMETRIC_SUCCESS -> {
                                activity?.let {
                                    FingerprintAuth.authenticate(it) { success ->
                                        if (success) {
                                            viewModel.hasFingerprintAuth = true
                                            currentStep = SecurityStep.DONE
                                        }
                                    }
                                } ?: Toast.makeText(context, "생체인증을 사용할 수 없습니다", Toast.LENGTH_SHORT).show()
                            }
                            else -> Toast.makeText(context, "지문 인증이 불가능합니다. 패턴 인증을 사용하세요.", Toast.LENGTH_LONG).show()
                        }
                    },
                    onPatternSelected = {
                        currentStep = SecurityStep.PATTERN

                                        },
                    onSkip = {
                        navController.navigate("registration_complete") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                )

                SecurityStep.PATTERN -> PatternAuth(

                    currentStep = currentStep,
                    onPatternConfirmed = {
                        Log.d("PatternAUTH","savedPattern11 : $savedPattern / it : $it")
                        savedPattern = it
                        currentStep = SecurityStep.PATTERN_CONFIRM
                    },
                    onStepChange = { currentStep = it }
                )

                SecurityStep.PATTERN_CONFIRM -> PatternAuth(
                    currentStep = currentStep,
                    onPatternConfirmed = {
                        Log.d("PatternAUTH","savedPattern : $savedPattern / it : $it")
                        // 리스트 내용을 명시적으로 비교
                        if (savedPattern != null && it.size == savedPattern!!.size &&
                            it.zip(savedPattern!!).all { (a, b) -> a == b }) {
                            viewModel.hasPatternAuth = true
                            currentStep = SecurityStep.DONE
                        } else {
                            Toast.makeText(context, "패턴이 틀렸습니다. 다시 시도하세요.", Toast.LENGTH_SHORT).show()
                            currentStep = SecurityStep.PATTERN
                        }
                    },
                    onStepChange = { currentStep = it }
                )

                SecurityStep.DONE -> SecurityCompleteScreen {
                    navController.navigate("registration_complete") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecuritySetupTopBar(currentStep: SecurityStep, onBack: () -> Unit) {
    TopAppBar(
        title = {},
        navigationIcon = {
            when (currentStep) {
                SecurityStep.PIN_CONFIRM, SecurityStep.PATTERN_CONFIRM -> {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                else -> {}
            }
        }
    )
}


fun Context.findActivity(): FragmentActivity? {
    var currentContext = this
    var level = 0

    while (currentContext is ContextWrapper) {
        if (currentContext is FragmentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext ?: break
        level++
    }
    Log.e("FingerAuth", "FragmentActivity를 찾지 못함")
    return null
}

@Composable
private fun SecurityMethodSelection(
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

@Composable
private fun SecurityCompleteScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "보안 설정이 완료되었습니다",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF191E3F)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
        ) {
            Text(text = "다음", fontSize = 22.sp)
        }
    }
}