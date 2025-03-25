package com.example.fe.ui.screens.onboard

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.fe.R
import java.util.concurrent.Executor
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.fragment.app.FragmentActivity

enum class SecurityStep { PIN, PIN_CONFIRM, METHOD, DONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySetupScreen(navController: NavController, viewModel: OnboardingViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor: Executor = ContextCompat.getMainExecutor(context)

    var currentStep by remember { mutableStateOf(SecurityStep.PIN) }
    var pinInput by remember { mutableStateOf("") }
    var confirmInput by remember { mutableStateOf("") }
    val shuffledNumbers = remember { (0..9).shuffled() }

    Scaffold(
        topBar = {
            if (currentStep == SecurityStep.PIN_CONFIRM) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = {
                            currentStep = SecurityStep.PIN
                            confirmInput = ""
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (currentStep) {
                SecurityStep.PIN -> {
                    Text("비밀번호를 설정해주세요", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    PinDots(pinInput.length)
                    Spacer(modifier = Modifier.height(32.dp))
                    NumberPad(
                        numbers = shuffledNumbers,
                        input = pinInput,
                        onInputChange = { pinInput = it },
                        onComplete = {
                            currentStep = SecurityStep.PIN_CONFIRM
                        }
                    )
                }

                SecurityStep.PIN_CONFIRM -> {
                    Text("비밀번호를 다시 입력해주세요", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    PinDots(confirmInput.length)
                    Spacer(modifier = Modifier.height(32.dp))
                    NumberPad(
                        numbers = shuffledNumbers,
                        input = confirmInput,
                        onInputChange = { confirmInput = it },
                        onComplete = {
                            if (confirmInput == pinInput) {
                                currentStep = SecurityStep.METHOD
                            } else {
                                Toast.makeText(context, "비밀번호가 일치하지 않아요", Toast.LENGTH_SHORT).show()
                                confirmInput = ""
                            }
                        }
                    )
                }

                SecurityStep.METHOD -> {
                    Text("추가 인증수단을 선택해주세요", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.fingerprint),
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                                .clickable {
                                    val activity = context as? FragmentActivity
                                    if (activity == null) {
                                        Toast.makeText(context, "지문 인증을 사용할 수 없습니다", Toast.LENGTH_SHORT).show()
                                        return@clickable
                                    }

                                    val biometricManager = BiometricManager.from(context)
                                    if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                                        != BiometricManager.BIOMETRIC_SUCCESS
                                    ) {
                                        Toast.makeText(context, "지문 인증을 사용할 수 없습니다", Toast.LENGTH_SHORT).show()
                                        return@clickable
                                    }

                                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                        .setTitle("지문 인증")
                                        .setSubtitle("지문을 등록해주세요")
                                        .setNegativeButtonText("취소")
                                        .build()

                                    val biometricPrompt = androidx.biometric.BiometricPrompt(
                                        activity,
                                        executor,
                                        object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                                            override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                                                currentStep = SecurityStep.DONE
                                            }

                                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                                Toast.makeText(context, "인증 실패: $errString", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                    biometricPrompt.authenticate(promptInfo)
                                }
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.apps),
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                                .clickable {
                                    currentStep = SecurityStep.DONE
                                }
                        )
                    }
                }

                SecurityStep.DONE -> {
                    Text("등록이 완료됐어요!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = {
                        viewModel.setLoggedInState(true)
                        navController.navigate("home")
                    }) {
                        Text("홈으로 가기")
                    }
                }
            }
        }
    }
}

@Composable
fun PinDots(count: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(6) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (it < count) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
            )
        }
    }
}

@Composable
fun NumberPad(
    numbers: List<Int>,
    input: String,
    onInputChange: (String) -> Unit,
    onComplete: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        numbers.chunked(3).take(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                row.forEach { num ->
                    Text(
                        text = num.toString(),
                        fontSize = 26.sp,
                        modifier = Modifier
                            .size(80.dp)
                            .clickable {
                                if (input.length < 6) {
                                    onInputChange(input + num)
                                    if (input.length + 1 == 6) onComplete()
                                }
                            },
                        color = Color.Black
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.size(80.dp)) // Empty Left
            Text(
                text = "0",
                fontSize = 26.sp,
                modifier = Modifier
                    .size(80.dp)
                    .clickable {
                        if (input.length < 6) {
                            onInputChange(input + "0")
                            if (input.length + 1 == 6) onComplete()
                        }
                    },
                color = Color.Black
            )
            Text(
                text = "←",
                fontSize = 26.sp,
                modifier = Modifier
                    .size(80.dp)
                    .clickable {
                        if (input.isNotEmpty()) {
                            onInputChange(input.dropLast(1))
                        }
                    },
                color = Color.Black
            )
        }
    }
}
