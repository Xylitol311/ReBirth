package com.example.fe.ui.screens.onboard

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.example.fe.R
import java.util.concurrent.Executor
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.fragment.app.FragmentActivity

enum class SecurityStep { PIN, PIN_CONFIRM, METHOD, PATTERN, PATTERN_CONFIRM, DONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySetupScreen(navController: NavController, viewModel: OnboardingViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor: Executor = ContextCompat.getMainExecutor(context)

    var currentStep by remember { mutableStateOf(SecurityStep.PIN) }
    var pinInput by remember { mutableStateOf("") }
    var confirmInput by remember { mutableStateOf("") }
    var patternPoints by remember { mutableStateOf(listOf<Int>()) }
    var confirmPatternPoints by remember { mutableStateOf(listOf<Int>()) }

    // 각 단계마다 다른 랜덤 숫자패드 제공 (0은 하단에 고정, 1-9만 섞음)
    val pinShuffledNumbers = remember { (1..9).toList().shuffled() }
    val confirmShuffledNumbers = remember { (1..9).toList().shuffled() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    when (currentStep) {
                        SecurityStep.PIN_CONFIRM -> {
                            IconButton(onClick = {
                                currentStep = SecurityStep.PIN
                                pinInput = ""
                                confirmInput = ""
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                            }
                        }
                        SecurityStep.PATTERN_CONFIRM -> {
                            IconButton(onClick = {
                                currentStep = SecurityStep.PATTERN
                                patternPoints = listOf()
                                confirmPatternPoints = listOf()
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                            }
                        }
                        else -> {}
                    }
                }
            )
        }
    ) { padding ->
        // 상단 영역과 키패드 영역을 분리
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 방법 선택 화면일 때 건너뛰기 버튼을 하단에 배치
            if (currentStep == SecurityStep.METHOD) {
                // 건너뛰기 버튼
                Button(
                    onClick = {
                        navController.navigate("registration_complete")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF191E3F)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp)
                ) {
                    Text("건너뛰기")
                }
            }

            // 상단 콘텐츠 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (currentStep) {
                    SecurityStep.PIN -> {
                        Text("비밀번호를 설정해주세요", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(24.dp))
                        ShowPinDots(pinInput.length)
                    }

                    SecurityStep.PIN_CONFIRM -> {
                        Text("비밀번호를 다시 입력해주세요", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(24.dp))
                        ShowPinDots(confirmInput.length)
                    }

                    SecurityStep.METHOD -> {
                        Text("추가 인증수단 선택", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "지문인증 또는 패턴인증으로\n더욱 안전하게 로그인 하세요.",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // 지문인증 옵션
                        AuthMethodOption(
                            title = "지문인증",
                            description = "기기에 등록된 지문 인증으로\n빠르게 서비스를 이용할 수 있어요",
                            iconResId = R.drawable.fingerprint,
                            onClick = {
                                val activity = context as? FragmentActivity
                                if (activity == null) {
                                    Toast.makeText(context, "지문 인증을 사용할 수 없습니다", Toast.LENGTH_SHORT).show()
                                    return@AuthMethodOption
                                }

                                val biometricManager = BiometricManager.from(context)
                                if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                                    != BiometricManager.BIOMETRIC_SUCCESS
                                ) {
                                    Toast.makeText(context, "지문 인증을 사용할 수 없습니다", Toast.LENGTH_SHORT).show()
                                    return@AuthMethodOption
                                }

                                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                    .setTitle("지문 인증")
                                    .setSubtitle("지문을 등록해주세요")
                                    .setNegativeButtonText("취소")
                                    .build()

                                val biometricPrompt = BiometricPrompt(
                                    activity,
                                    executor,
                                    object : BiometricPrompt.AuthenticationCallback() {
                                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // 패턴인증 옵션
                        AuthMethodOption(
                            title = "패턴인증",
                            description = "간단하고 편리한 패턴으로\n보다 간편하게 이용할 수 있어요",
                            iconResId = R.drawable.apps,
                            onClick = {
                                currentStep = SecurityStep.PATTERN
                            }
                        )

                        // 건너뛰기 버튼이 하단에 별도로 배치되므로 여기에서는 제거
                    }

                    SecurityStep.PATTERN -> {
                        Text("패턴 등록", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "인증에 사용할 패턴을 등록해주세요",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        PatternGrid(
                            selectedPoints = patternPoints,
                            onPatternComplete = { pattern ->
                                patternPoints = pattern
                                currentStep = SecurityStep.PATTERN_CONFIRM
                            }
                        )
                    }

                    SecurityStep.PATTERN_CONFIRM -> {
                        Text("패턴 확인", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "인증에 사용할 패턴을 다시 등록해주세요",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        PatternGrid(
                            selectedPoints = confirmPatternPoints,
                            onPatternComplete = { pattern ->
                                confirmPatternPoints = pattern
                                if (patternPoints == confirmPatternPoints) {
                                    currentStep = SecurityStep.DONE
                                } else {
                                    Toast.makeText(context, "패턴이 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                                    confirmPatternPoints = listOf()
                                }
                            }
                        )
                    }

                    SecurityStep.DONE -> {
                        Text("등록이 완료됐어요!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                navController.navigate("registration_complete")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF191E3F)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("확인")
                        }
                    }
                }
            }

            // 키패드 영역 (하단에 배치)
            if (currentStep == SecurityStep.PIN || currentStep == SecurityStep.PIN_CONFIRM) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                ) {
                    // 현재 단계에 맞는 키패드 표시
                    ShowNumberPad(
                        numbers = if (currentStep == SecurityStep.PIN) pinShuffledNumbers else confirmShuffledNumbers,
                        input = if (currentStep == SecurityStep.PIN) pinInput else confirmInput,
                        onInputChange = { newInput ->
                            if (currentStep == SecurityStep.PIN) {
                                pinInput = newInput
                            } else {
                                confirmInput = newInput
                            }
                        },
                        onComplete = {
                            if (currentStep == SecurityStep.PIN) {
                                currentStep = SecurityStep.PIN_CONFIRM
                            } else {
                                if (confirmInput == pinInput) {
                                    currentStep = SecurityStep.METHOD
                                } else {
                                    Toast.makeText(context, "비밀번호가 일치하지 않아요", Toast.LENGTH_SHORT).show()
                                    confirmInput = ""
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AuthMethodOption(
    title: String,
    description: String,
    iconResId: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color.White)
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = title,
            modifier = Modifier.size(48.dp),
            tint = Color(0xFF191E3F)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.arrow_right),
            contentDescription = "선택",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp) // 아이콘 크기 조정
        )
    }
}

@Composable
fun PatternGrid(
    selectedPoints: List<Int>,
    onPatternComplete: (List<Int>) -> Unit
) {
    val context = LocalContext.current
    var currentPattern by remember(selectedPoints) { mutableStateOf(selectedPoints) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 3x3 그리드 생성
        Grid(
            rows = 3,
            columns = 3,
            selectedPoints = currentPattern,
            onPointClick = { point ->
                // 이미 선택된 점이면 무시
                if (!currentPattern.contains(point)) {
                    // 새로운 점 추가
                    currentPattern = currentPattern + point
                }
            },
            onComplete = {
                if (currentPattern.size >= 4) {
                    onPatternComplete(currentPattern)
                } else {
                    Toast.makeText(
                        context,
                        "최소 4개 이상의 점을 연결해주세요",
                        Toast.LENGTH_SHORT
                    ).show()
                    currentPattern = listOf()
                }
            }
        )
    }
}

@Composable
fun Grid(
    rows: Int,
    columns: Int,
    selectedPoints: List<Int>,
    onPointClick: (Int) -> Unit,
    onComplete: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        repeat(rows) { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                repeat(columns) { col ->
                    val pointIndex = row * columns + col
                    val isSelected = selectedPoints.contains(pointIndex)

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFF191E3F) else Color.LightGray.copy(alpha = 0.5f))
                            .clickable { onPointClick(pointIndex) },
                        contentAlignment = Alignment.Center
                    ) {
                        // 선택된 순서 표시 (선택 순서대로 숫자 표시)
                        if (isSelected) {
                            val order = selectedPoints.indexOf(pointIndex) + 1
                            Text(
                                text = order.toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onComplete() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF191E3F)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("확인")
        }
    }
}

@Composable
fun ShowPinDots(count: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(6) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        if (it < count) Color(0xFF3366FF) else Color(0xFFE0E0E0)
                    )
            )
        }
    }
}

@Composable
fun ShowNumberPad(
    numbers: List<Int>,
    input: String,
    onInputChange: (String) -> Unit,
    onComplete: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 1-9 숫자 패드 (이미 1-9로 구성됨)
        numbers.chunked(3).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { num ->
                    NumberKey(
                        text = num.toString(),
                        onClick = {
                            if (input.length < 6) {
                                onInputChange(input + num)
                                if (input.length + 1 == 6) onComplete()
                            }
                        }
                    )
                }
            }
        }

        // 마지막 줄 (빈칸, 0, 삭제)
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.size(70.dp)) // 빈 공간

            // 0 버튼
            NumberKey(
                text = "0",
                onClick = {
                    if (input.length < 6) {
                        onInputChange(input + "0")
                        if (input.length + 1 == 6) onComplete()
                    }
                }
            )

            // 삭제 버튼
            NumberKey(
                text = "←",
                onClick = {
                    if (input.isNotEmpty()) {
                        onInputChange(input.dropLast(1))
                    }
                }
            )
        }
    }
}

@Composable
fun NumberKey(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 26.sp,
            textAlign = TextAlign.Center,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}