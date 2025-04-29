package com.example.fe.ui.screens.onboard

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.Color.Companion.hsl
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.fe.ui.theme.SkyBlue
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

enum class Step {
    NAME, SSN, TELECOM, PHONE, CODE
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: OnboardingViewModel
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(Step.NAME) }
    var name by remember { mutableStateOf("") }
    var ssnFront by remember { mutableStateOf("") }
    var ssnBack by remember { mutableStateOf("") }
    var telco by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var showTelcoSheet by remember { mutableStateOf(false) }
    var showAgreement by remember { mutableStateOf(false) }
    var verificationError by remember { mutableStateOf(false) } // 인증 오류 상태 추가
    val telcoSelectedColor = Color(0xFFE0F7FF)
    // 각 단계별 포커스 요청을 위한 FocusRequester 객체들
    val nameFocusRequester = remember { FocusRequester() }
    val ssnFrontFocusRequester = remember { FocusRequester() }
    val ssnBackFocusRequester = remember { FocusRequester() }
    val phoneFocusRequester = remember { FocusRequester() }
    val codeFocusRequester = remember { FocusRequester() }

    // 단계 변경 시 포커스 및 키보드 자동 표시 또는 바텀시트 표시
    LaunchedEffect(currentStep) {
        when(currentStep) {
            Step.NAME -> {
                nameFocusRequester.requestFocus()
                keyboardController?.show()
            }
            Step.SSN -> {
                ssnFrontFocusRequester.requestFocus()
                keyboardController?.show()
            }
            Step.TELECOM -> {
                // 통신사 선택 단계로 이동하면 자동으로 바텀시트 표시
                showTelcoSheet = true
            }
            Step.PHONE -> {
                phoneFocusRequester.requestFocus()
                keyboardController?.show()
            }
            Step.CODE -> {
                codeFocusRequester.requestFocus()
                keyboardController?.show()
            }
            else -> {}
        }
    }

    // 코드 입력 시 오류 상태 초기화
    LaunchedEffect(code) {
        if (verificationError && code.isNotEmpty()) {
            verificationError = false
        }
    }

    val telcos = listOf("SKT", "KT", "LGU+", "SKT 알뜰폰", "KT 알뜰폰", "LGU+ 알뜰폰")
    val agreementItems = listOf(
        "[필수] 서비스 이용 동의",
        "[필수] 본인 확인 서비스 약관",
        "[필수] 마이데이터 제공 동의"
    )
    val checkedItems = remember { mutableStateListOf<String>() }

    // 시스템 뒤로가기 처리
    BackHandler(enabled = currentStep != Step.NAME) {
        // 첫 단계가 아닌 경우에만 이전 단계로 이동
        if (currentStep != Step.NAME) {
            currentStep = Step.values()[currentStep.ordinal - 1]
        }
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding(), // 시스템 바 영역을 고려한 패딩 추가
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    // 이름 입력 화면(첫 화면)에서는 뒤로가기 버튼 숨김
                    if (currentStep != Step.NAME) {
                        IconButton(
                            onClick = {
                                currentStep = Step.values()[currentStep.ordinal - 1]
                            },
                            modifier = Modifier.size(54.dp) // 아이콘 버튼 크기 증가
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "뒤로가기",
                                modifier = Modifier.size(32.dp) // 아이콘 크기 증가
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Box(modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(16.dp)) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()

                        when (currentStep) {
                            Step.PHONE -> {
                                // 휴대폰 번호 입력 후 다음 버튼 클릭 시 SMS 인증 요청
                                viewModel.sendSmsVerification(
                                    phoneNumber = phone,
                                    onSuccess = {
                                        Log.d("AuthSMS","전송완료 ${phone}")
                                        currentStep = Step.values()[currentStep.ordinal + 1]
                                    },
                                    onFailure = { error ->
                                        // 에러 처리 (예: 토스트 메시지 표시)

                                        Log.e("AuthSMS", error)
                                    }
                                )

                            }
                            Step.CODE -> {
                                // 인증번호 입력 후 다음 버튼 클릭 시 SMS 인증 확인
                                viewModel.verifySmsCode(
                                    phoneNumber = phone,
                                    verificationCode = code,
                                    onSuccess = {
                                        Log.d("AuthSMS","인증 완료 ${phone}")
                                        showAgreement = true
                                    },
                                    onFailure = { error ->
                                        // 인증 실패 시 오류 표시
                                        verificationError = true
                                        Log.e("Verify Error", error)
                                    }
                                )
                                // 인증 성공 시에만 동의 화면으로 이동하도록 수정

                            }
                            else -> {
                                currentStep = Step.values()[currentStep.ordinal + 1]
                            }
                        }
                    },
                    enabled = when (currentStep) {
                        Step.NAME -> name.isNotBlank()
                        Step.SSN -> ssnFront.length == 6 && ssnBack.isNotBlank()
                        Step.TELECOM -> telco.isNotBlank()
                        Step.PHONE -> phone.length >= 10
                        Step.CODE -> code.length == 6
                        else -> false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkyBlue,
                        contentColor = Color.White,
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    Text(
                        text = if (currentStep == Step.PHONE) "인증번호 받기" else "다음",
                        fontSize = 22.sp
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .systemBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp) // 간격 증가
        ) {
            Spacer(Modifier.height(24.dp))

            when (currentStep) {
                Step.NAME -> {
                    Text("이름을 알려주세요",
                        fontSize = 25.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(24.dp)) // 텍스트와 필드 사이 간격
                    UnderlineTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "이름",
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(nameFocusRequester)
                    )
                }
                Step.SSN -> {
                    Text("주민등록번호를 입력해주세요", fontSize = 25.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom  // 모든 요소를 하단에 맞춤
                    ) {
                        // 앞 6자리 입력 (왼쪽)
                        Box(modifier = Modifier.weight(0.45f)) {
                            UnderlineTextField(
                                value = ssnFront,
                                onValueChange = {
                                    if (it.length <= 6) {
                                        ssnFront = it
                                        if (it.length == 6) focusManager.moveFocus(FocusDirection.Next)
                                    }
                                },
                                label = "주민등록번호",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(ssnFrontFocusRequester),
                                keyboardType = KeyboardType.Number
                            )
                        }

                        // 하이픈
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .offset(y = (-8).dp)  // 위로 8dp 올림
                        ) {
                            Text(
                                text = "-",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        // 뒷 1자리 입력 (오른쪽) - 앞 필드와 동일한 스타일 적용
                        Box(modifier = Modifier.weight(0.1f)) {
                            // UnderlineSingleDigitField 대신 동일한 스타일의 커스텀 필드 사용
                            UnderlineTextField(
                                value = ssnBack,
                                onValueChange = { if (it.length <= 1) ssnBack = it },
                                label = "",  // 레이블 없음
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(ssnBackFocusRequester),
                                keyboardType = KeyboardType.Number
                            )
                        }

                        // 동그라미들
                        Row(
                            modifier = Modifier
                                .weight(0.35f)
                                .padding(bottom = 8.dp),  // 밑줄과 동일한 위치에 표시되도록 패딩 조정
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(6) {
                                Text(
                                    "●",
                                    fontSize = 20.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }

                    DisplayInfo("이름", name)
                }
                Step.TELECOM -> {
                    Text("통신사를 선택해주세요",fontSize = 25.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(modifier = Modifier.height(24.dp))
                    TelcoSelector(telco = telco) {
                        Log.d("TELCO_CLICK", "바텀시트 열림")
                        showTelcoSheet = true
                    }
                    DisplayInfo("이름", name)
                    DisplayInfo("주민등록번호", "$ssnFront-$ssnBack●●●●●●")
                }
                Step.PHONE -> {
                    Text("휴대폰 번호를 입력해주세요", fontSize = 25.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(modifier = Modifier.height(24.dp))
                    UnderlineTextField(
                        value = phone,
                        onValueChange = {
                            if (it.length <= 11) phone = it
                        },
                        label = "휴대폰 번호",
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(phoneFocusRequester),
                        keyboardType = KeyboardType.Phone
                    )
                    DisplayInfo("이름", name)
                    DisplayInfo("주민등록번호", "$ssnFront-$ssnBack●●●●●●")
                    DisplayInfo("통신사", telco)
                }
                Step.CODE -> {
                    Text("인증번호를 입력해주세요", fontSize = 25.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(modifier = Modifier.height(24.dp))

                    // 인증번호 입력 필드와 재전송 버튼을 나란히 배치
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 인증번호 입력 필드 (왼쪽)
                            Box(modifier = Modifier.weight(0.65f)) {
                                UnderlineTextField(
                                    value = code,
                                    onValueChange = { if (it.length <= 6) code = it },
                                    label = "인증번호",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(codeFocusRequester),
                                    keyboardType = KeyboardType.Number,
                                    isError = verificationError
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // 재전송 버튼 (오른쪽) - 이미지와 같은 형태
                            Box(
                                modifier = Modifier
                                    .weight(0.45f)
                                    .align(Alignment.Bottom)
                                    .height(48.dp)
                                    .border(
                                        width = 1.dp,
                                        color = SkyBlue,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        // 인증번호 재전송 로직
                                        viewModel.sendSmsVerification(
                                            phoneNumber = phone,
                                            onSuccess = {
                                                // 재전송 성공 메시지
                                            },
                                            onFailure = { error ->
                                                // 에러 처리
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "인증번호 재전송",
                                    color = SkyBlue,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // 오류 메시지 표시
                        if (verificationError) {
                            Text(
                                text = "인증번호가 틀렸습니다",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }
                    }

                    DisplayInfo("이름", name)
                    DisplayInfo("주민등록번호", "$ssnFront-$ssnBack●●●●●●")
                    DisplayInfo("통신사", telco)
                    DisplayInfo("휴대폰 번호", phone)
                }
            }
        }
    }


    if (showTelcoSheet) {
        Log.d("TELCO_SHOW", "바텀시트 표시됨")
        ModalBottomSheet(
            onDismissRequest = {
                showTelcoSheet = false
                // 만약 통신사를 선택하지 않고 닫을 경우 이전 단계로 돌아감
                if (telco.isBlank() && currentStep == Step.TELECOM) {
                    currentStep = Step.SSN
                }
            }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "통신사를 선택해주세요",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 24.sp
                )
                telcos.forEach { telcoItem ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            // 선택한 통신사에 배경색 적용
                            .background(
                                color = if (telcoItem == telco) telcoSelectedColor else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                telco = telcoItem
                                showTelcoSheet = false
                            }
                            .padding(16.dp)
                    ) {
                        Text(
                            text = telcoItem,
                            fontSize = 20.sp
                        )
                    }}
            }
        }
    }

    if (showAgreement) {
        ModalBottomSheet(onDismissRequest = { showAgreement = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "서비스를 쓰려면 동의가 필요해요",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 24.sp
                )
                Spacer(Modifier.height(24.dp))
                agreementItems.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                ) {
                                    if (checkedItems.contains(item)) checkedItems.remove(item)
                                    else checkedItems.add(item)
                                }
                                .padding(end = 16.dp)
                        ) {
                            Text(
                                text = "✔",
                                color = if (checkedItems.contains(item)) SkyBlue else Color.Gray,
                                fontSize = 20.sp
                            )
                        }
                        Text(item, fontSize = 18.sp)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        showAgreement = false
                        // 동의 완료
                        if (code.length == 6) {
                            // 코드가 이미 입력되어 있다면 소득 입력 화면으로 이동 (애니메이션 없이)
                            navController.navigate(
                                "income_input/${name}/${phone}/${ssnFront}",
                                NavOptions.Builder()
                                    .setEnterAnim(0)
                                    .setExitAnim(0)
                                    .setPopEnterAnim(0)
                                    .setPopExitAnim(0)
                                    .build()
                            )
                        }
                    },
                    enabled = checkedItems.size == agreementItems.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkyBlue,
                        contentColor = Color.White,
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    Text(
                        "동의하기",
                        fontSize = 22.sp
                    )
                }




            }
        }
    }

    LaunchedEffect(viewModel.errorMessage) {
        if (viewModel.errorMessage.isNotBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = viewModel.errorMessage,
                    duration = SnackbarDuration.Short
                )
                viewModel.errorMessage = "" // 메시지 표시 후 초기화
            }
        }
    }
}


@Composable
fun UnderlineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false // 오류 상태 매개변수 추가
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(70.dp)
            .background(
                color = Color.Transparent,
                shape = RectangleShape
            ),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, color = Color.Black),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        decorationBox = { innerTextField ->
            Column {
                // 라벨이 선의 맨 왼쪽에 위치하도록 설정
                Text(
                    text = label,
                    fontSize = 20.sp,
                    color = if (isError) Color.Red else SkyBlue,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    innerTextField()
                }

                // 밑줄
                Divider(
                    color = if (isError) Color.Red else if (value.isNotEmpty()) SkyBlue else Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
fun UnderlineSingleDigitField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Number
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(70.dp)
            .width(50.dp)
            .background(
                color = Color.Transparent,
                shape = RectangleShape
            ),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = Color.Black
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        decorationBox = { innerTextField ->
            Column(
                modifier = modifier
                    .height(70.dp)
                    .width(50.dp)
                    .padding(bottom = 2.5.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                        decorationBox = { innerTextField -> innerTextField() },
                    )
                }

                Divider(
                    color = if (value.isNotEmpty()) SkyBlue else Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
fun DisplayInfo(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 20.sp, color = Color.Gray)
        Text(value, fontSize = 20.sp)
    }
}

@Composable
fun TelcoSelector(telco: String, onClick: () -> Unit) {
    Column {
        Text(
            text = "통신사",
            style = MaterialTheme.typography.labelMedium,
            fontSize = 20.sp,
            color = Color(0xFF21CDFD)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .border(
                    1.dp, Color(0xFF21CDFD), MaterialTheme.shapes.medium
                )
                .clickable { onClick() }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = if (telco.isBlank()) "통신사를 선택해주세요" else telco,
                color = if (telco.isBlank()) Color.Gray else Color.Black,
                fontSize = 20.sp
            )
        }
    }
}