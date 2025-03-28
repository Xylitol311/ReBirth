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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset

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
                            IconButton(
                                onClick = {
                                    currentStep = SecurityStep.PIN
                                    pinInput = ""
                                    confirmInput = ""
                                },
                                modifier = Modifier.size(54.dp) // 아이콘 크기 증가
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack, 
                                    contentDescription = "뒤로가기",
                                    modifier = Modifier.size(32.dp) // 아이콘 크기 증가
                                )
                            }
                        }
                        SecurityStep.PATTERN_CONFIRM -> {
                            IconButton(
                                onClick = {
                                    currentStep = SecurityStep.PATTERN
                                    patternPoints = listOf()
                                    confirmPatternPoints = listOf()
                                },
                                modifier = Modifier.size(54.dp) // 아이콘 크기 증가
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack, 
                                    contentDescription = "뒤로가기",
                                    modifier = Modifier.size(32.dp) // 아이콘 크기 증가
                                )
                            }
                        }
                        else -> {}
                    }
                }
            )
        }
    ) { padding ->
        // 전체 화면 구성 - 반응형 레이아웃으로 수정
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // METHOD 단계가 아닐 때만 기존 레이아웃 유지
            if (currentStep != SecurityStep.METHOD) {
                // 상단 콘텐츠 영역 (반응형으로 조정)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 상단 여백 (비율 기반)
                    Spacer(modifier = Modifier.weight(0.1f))

                    when (currentStep) {
                        SecurityStep.PIN -> {
                            Text(
                                "비밀번호를 설정해주세요", 
                                fontSize = 28.sp, // 18sp에서 28sp로 증가
                                fontWeight = FontWeight.Medium
                            )
                            
                            // 비밀번호 점들을 중상단에 배치하기 위한 여백
                            Spacer(modifier = Modifier.weight(0.15f))
                            
                            ShowPinDots(pinInput.length)
                            
                            // 키패드와 점 사이의 여백
                            Spacer(modifier = Modifier.weight(0.3f))
                            
                            // 키패드 배치
                            ShowNumberPad(
                                numbers = pinShuffledNumbers,
                                input = pinInput,
                                onInputChange = { newInput ->
                                    pinInput = newInput
                                },
                                onComplete = {
                                    if (pinInput.length == 6) {
                                        currentStep = SecurityStep.PIN_CONFIRM
                                    }
                                }
                            )
                            
                            // 하단 여백
                            Spacer(modifier = Modifier.weight(0.05f))
                        }

                        SecurityStep.PIN_CONFIRM -> {
                            Text(
                                "비밀번호를 다시 입력해주세요", 
                                fontSize = 28.sp, // 18sp에서 28sp로 증가
                                fontWeight = FontWeight.Medium
                            )
                            
                            // 비밀번호 점들을 중상단에 배치하기 위한 여백
                            Spacer(modifier = Modifier.weight(0.15f))
                            
                            ShowPinDots(confirmInput.length)
                            
                            // 키패드와 점 사이의 여백
                            Spacer(modifier = Modifier.weight(0.3f))
                            
                            // 키패드 배치
                            ShowNumberPad(
                                numbers = confirmShuffledNumbers,
                                input = confirmInput,
                                onInputChange = { newInput ->
                                    confirmInput = newInput
                                },
                                onComplete = {
                                    if (confirmInput.length == 6) {
                                        if (pinInput == confirmInput) {
                                            currentStep = SecurityStep.METHOD
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "비밀번호가 일치하지 않습니다",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            confirmInput = ""
                                        }
                                    }
                                }
                            )
                            
                            // 하단 여백
                            Spacer(modifier = Modifier.weight(0.05f))
                        }

                        SecurityStep.PATTERN -> {
                            Text(
                                "패턴을 설정해주세요",
                                fontSize = 28.sp, // 18sp에서 28sp로 증가
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp)) // 24dp에서 32dp로 증가

                            PatternGrid(
                                onPatternComplete = { pattern ->
                                    if (pattern.size < 4) {
                                        Toast.makeText(
                                            context,
                                            "최소 4개 이상의 점을 연결해주세요",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        patternPoints = pattern
                                        currentStep = SecurityStep.PATTERN_CONFIRM
                                    }
                                }
                            )
                        }

                        SecurityStep.PATTERN_CONFIRM -> {
                            Text(
                                "패턴을 다시 입력해주세요",
                                fontSize = 28.sp, // 18sp에서 28sp로 증가
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp)) // 24dp에서 32dp로 증가

                            PatternGrid(
                                onPatternComplete = { pattern ->
                                    if (pattern.size < 4) {
                                        Toast.makeText(
                                            context,
                                            "최소 4개 이상의 점을 연결해주세요",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        confirmPatternPoints = pattern
                                        if (patternPoints == confirmPatternPoints) {
                                            // 뷰모델에 패턴 저장
                                            viewModel.hasPatternAuth = true
                                            currentStep = SecurityStep.DONE
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "패턴이 일치하지 않습니다",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            confirmPatternPoints = listOf()
                                        }
                                    }
                                }
                            )
                        }

                        SecurityStep.DONE -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "보안 설정이 완료되었습니다",
                                    fontSize = 28.sp, // 18sp에서 28sp로 증가
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(32.dp)) // 24dp에서 32dp로 증가
                                
                                Button(
                                    onClick = {
                                        navController.navigate("registration_complete") {
                                            popUpTo("security_setup") { inclusive = true }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF191E3F)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp), // 버튼 높이 증가
                                ) {
                                    Text(text = "다음", fontSize = 22.sp) // 글씨 크기 증가
                                }
                            }
                        }
                        
                        else -> {}
                    }
                }
            } else {
                // METHOD 단계일 때 새로운 레이아웃
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top // 중앙 정렬에서 상단 정렬로 변경
                ) {
                    Spacer(modifier = Modifier.height(60.dp)) // 상단 고정 여백으로 변경
                    
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // 패턴인증 옵션
                    AuthMethodOption(
                        title = "패턴인증",
                        description = "나만의 패턴을 그려서\n간편하게 서비스를 이용할 수 있어요",
                        iconResId = R.drawable.fingerprint,
                        onClick = {
                            currentStep = SecurityStep.PATTERN
                        }
                    )
                    
                    Spacer(modifier = Modifier.weight(1f)) // 남은 공간을 모두 차지하는 여백
                    
                    // 건너뛰기 버튼 - 화면 하단에 위치
                    Button(
                        onClick = {
                            navController.navigate("registration_complete")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF191E3F)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(65.dp) // 버튼 높이 증가
                            .padding(horizontal = 0.dp, vertical = 0.dp) // 패딩 조정
                    ) {
                        Text(
                            text = "건너뛰기", 
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold // 글자 두껍게
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp)) // 하단 여백 증가
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF191E3F)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = title,
                modifier = Modifier
                    .size(56.dp)
                    .padding(end = 20.dp),
                tint = Color.White
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun PatternGrid(
    onPatternComplete: (List<Int>) -> Unit
) {
    val rows = 3
    val columns = 3
    val pointRadius = 24.dp
    val pointCount = rows * columns
    val touchSlop = 20f

    var currentPattern by remember { mutableStateOf(listOf<Int>()) }
    var isDragging by remember { mutableStateOf(false) }
    var currentDragPoint by remember { mutableStateOf<Offset?>(null) }
    
    val points = remember {
        List(pointCount) { idx ->
            val row = idx / columns
            val col = idx % columns
            val centerX = col * 120f + 60f // 중앙 위치 X (3x3 그리드에서 간격은 120dp로 가정)
            val centerY = row * 120f + 60f // 중앙 위치 Y
            Offset(centerX, centerY)
        }
    }

    val density = LocalDensity.current
    val pointRadiusPx = with(density) { pointRadius.toPx() }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp) // 360dp에서 400dp로 증가
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        currentDragPoint = offset
                        currentPattern = listOf()

                        // 시작점 찾기
                        points.forEachIndexed { index, point ->
                            if (hypot(offset.x - point.x, offset.y - point.y) <= pointRadiusPx + touchSlop) {
                                currentPattern = listOf(index)
                            }
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentDragPoint = change.position

                        // 현재 드래그 중인 위치와 가장 가까운 점 찾기
                        points.forEachIndexed { index, point ->
                            if (!currentPattern.contains(index) && 
                                hypot(change.position.x - point.x, change.position.y - point.y) <= pointRadiusPx + touchSlop) {
                                currentPattern = currentPattern + index
                            }
                        }
                    },
                    onDragEnd = {
                        isDragging = false
                        currentDragPoint = null
                        onPatternComplete(currentPattern)
                    },
                    onDragCancel = {
                        isDragging = false
                        currentDragPoint = null
                        // 패턴 입력 취소
                        currentPattern = listOf()
                    }
                )
            }
    ) {
        // 선 그리기
        Canvas(modifier = Modifier.matchParentSize()) {
            if (currentPattern.isNotEmpty()) {
                for (i in 0 until currentPattern.size - 1) {
                    val start = points[currentPattern[i]]
                    val end = points[currentPattern[i + 1]]
                    
                    drawLine(
                        color = Color(0xFF7B68EE), // 보라색 선
                        start = start,
                        end = end,
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )
                }
                
                // 현재 드래그 중인 선 그리기
                if (isDragging && currentPattern.isNotEmpty() && currentDragPoint != null) {
                    val start = points[currentPattern.last()]
                    drawLine(
                        color = Color(0xFF7B68EE), // 보라색 선
                        start = start,
                        end = currentDragPoint!!,
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
        
        // 점들 그리기
        points.forEachIndexed { index, offset ->
            val isSelected = currentPattern.contains(index)
            
            Box(
                modifier = Modifier
                    .size(pointRadius * 2)
                    .offset(
                        x = with(density) { offset.x.toDp() } - pointRadius,
                        y = with(density) { offset.y.toDp() } - pointRadius
                    )
                    .background(
                        color = if (isSelected) Color(0xFF7B68EE) else Color.LightGray.copy(alpha = 0.5f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Text(
                        text = (currentPattern.indexOf(index) + 1).toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ShowPinDots(count: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(6) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        if (it < count) Color(0xFF191E3F) else Color(0xFFE0E0E0)
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
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp), // 12dp에서 16dp로 증가
        modifier = Modifier.fillMaxWidth() // 가로 전체 사용
    ) {
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
            Spacer(modifier = Modifier.size(80.dp)) // 70dp에서 80dp로 증가

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
            .size(80.dp) // 70dp에서 80dp로 증가
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 32.sp, // 26sp에서 32sp로 증가 
            textAlign = TextAlign.Center,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}