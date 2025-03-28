package com.example.fe.ui.screens.onboard

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.KeyboardType
import com.example.fe.ui.screens.onboard.OnboardingViewModel

enum class Step {
    NAME, SSN, TELECOM, PHONE, CODE, AGREEMENT
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AuthScreen(navController: NavController, viewModel: OnboardingViewModel) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var currentStep by remember { mutableStateOf(Step.NAME) }
    var name by remember { mutableStateOf("") }
    var ssnFront by remember { mutableStateOf("") }
    var ssnBack by remember { mutableStateOf("") }
    var telco by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var showTelcoSheet by remember { mutableStateOf(false) }
    var showAgreement by remember { mutableStateOf(false) }

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

    val telcos = listOf("SKT", "KT", "LGU+", "SKT 알뜰폰", "KT 알뜰폰", "LGU+ 알뜰폰")
    val agreementItems = listOf(
        "[필수] 서비스 이용 동의",
        "[필수] 본인 확인 서비스 약관",
        "[필수] 마이데이터 제공 동의"
    )
    val checkedItems = remember { mutableStateListOf<String>() }

    Scaffold(
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
                .padding(16.dp)) {
                Button(
                    onClick = {
                        Log.d("Click", "다음")
                        if (currentStep == Step.CODE) showAgreement = true
                        else currentStep = Step.values()[currentStep.ordinal + 1]
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
                        .height(60.dp), // 버튼 높이 증가
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = if (currentStep == Step.CODE) "확인" else "다음",
                        fontSize = 22.sp // 20sp → 22sp로 증가
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
            verticalArrangement = Arrangement.spacedBy(20.dp) // 간격 증가
        ) {
            Spacer(Modifier.height(24.dp))

            when (currentStep) {
                Step.NAME -> {
                    Text("이름을 알려주세요", fontSize = 28.sp) // 26sp → 28sp로 증가
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
                    Text("주민등록번호를 입력해주세요", fontSize = 28.sp) // 26sp → 28sp로 증가
                    
                    // 모든 크기와 패딩을 반응형으로 변경
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // 중앙 "-" 배치 (더 크고 진하게)
                        Text(
                            text = "-", 
                            fontSize = 32.sp, // 더 크게
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, // 더 진하게
                            color = Color.Black, // 더 진한 색상
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(bottom = 12.dp) // 위치 조정
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 앞 6자리 입력 (왼쪽)
                            Box(modifier = Modifier.weight(0.45f)) { // 비율 기반 너비
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
                            
                            // 중앙 공간 (하이픈용)
                            Spacer(modifier = Modifier.weight(0.1f)) // 비율 기반 공간
                            
                            // 뒷 1자리 입력 (오른쪽)
                            Box(modifier = Modifier.weight(0.1f)) { // 비율 기반 너비
                                UnderlineSingleDigitField(
                                    value = ssnBack,
                                    onValueChange = { if (it.length <= 1) ssnBack = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(ssnBackFocusRequester),
                                    keyboardType = KeyboardType.Number
                                )
                            }
                            
                            // 동그라미들 (화면 끝까지 균일하게)
                            Box(
                                modifier = Modifier.weight(0.35f) // 비율 기반 너비
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(70.dp)
                                        .padding(start = 12.dp, bottom = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween, // 균등 분포
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(6) {
                                        Text(
                                            "●", 
                                            fontSize = 28.sp, // 더 크게
                                            color = Color.DarkGray // 더 진하게
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    DisplayInfo("이름", name)
                }
                Step.TELECOM -> {
                    Text("통신사를 선택해주세요", fontSize = 28.sp) // 26sp → 28sp로 증가
                    TelcoSelector(telco = telco) {
                        Log.d("TELCO_CLICK", "바텀시트 열림")
                        showTelcoSheet = true
                    }
                    DisplayInfo("이름", name)
                    DisplayInfo("주민등록번호", "$ssnFront-$ssnBack●●●●●●")
                }
                Step.PHONE -> {
                    Text("휴대폰 번호를 입력해주세요", fontSize = 28.sp) // 26sp → 28sp로 증가
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
                    Text("인증번호를 입력해주세요", fontSize = 28.sp) // 26sp → 28sp로 증가
                    UnderlineTextField(
                        value = code,
                        onValueChange = { if (it.length <= 6) code = it },
                        label = "인증번호",
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(codeFocusRequester),
                        keyboardType = KeyboardType.Number
                    )
                    DisplayInfo("이름", name)
                    DisplayInfo("주민등록번호", "$ssnFront-$ssnBack●●●●●●")
                    DisplayInfo("통신사", telco)
                    DisplayInfo("휴대폰 번호", phone)
                }
                else -> {}
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
                    fontSize = 24.sp // 22sp → 24sp로 증가
                )
                telcos.forEach {
                    Text(
                        text = it,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                telco = it
                                showTelcoSheet = false
                            }
                            .padding(16.dp), // 패딩 증가
                        fontSize = 20.sp // 18sp → 20sp로 증가
                    )
                }
            }
        }
    }

    if (showAgreement) {
        ModalBottomSheet(onDismissRequest = { showAgreement = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "서비스를 쓰려면 동의가 필요해요", 
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 24.sp // 22sp → 24sp로 증가
                )
                agreementItems.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp) // 패딩 증가
                    ) {
                        // 체크박스 부분만 클릭 가능하도록 수정 + 리플 효과 제거
                        Box(
                            modifier = Modifier
                                .clickable(
                                    indication = null, // 리플 효과 제거
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                ) {
                                    if (checkedItems.contains(item)) checkedItems.remove(item)
                                    else checkedItems.add(item)
                                }
                                .padding(end = 16.dp) // 패딩 증가
                        ) {
                            Text(
                                text = "✔",
                                color = if (checkedItems.contains(item)) Color(0xFF1976D2) else Color.Gray,
                                fontSize = 26.sp // 24sp → 26sp로 증가
                            )
                        }
                        // 텍스트 부분 (클릭 불가)
                        Text(item, fontSize = 20.sp) // 18sp → 20sp로 증가
                    }
                }
                Spacer(Modifier.height(24.dp)) // 간격 증가
                Button(
                    onClick = {
                        showAgreement = false
                        navController.navigate("card_select")
                    },
                    enabled = checkedItems.size == agreementItems.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp), // 버튼 높이 증가
                ) { 
                    Text(
                        "동의하기",
                        fontSize = 22.sp // 20sp → 22sp로 증가
                    ) 
                }
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
    keyboardType: KeyboardType = KeyboardType.Text
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(70.dp),
        singleLine = true,
        label = { Text(label, fontSize = 20.sp) }, // 18sp → 20sp로 증가
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp), // 18sp → 20sp로 증가
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Blue,
            unfocusedIndicatorColor = Color.Gray,
            disabledIndicatorColor = Color.Gray,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        )
    )
}

@Composable
fun UnderlineSingleDigitField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Number
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(70.dp),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 20.sp, // 크기는 앞자리와 동일하게
            fontWeight = androidx.compose.ui.text.font.FontWeight.Normal, // Bold에서 Normal로 변경
            textAlign = androidx.compose.ui.text.style.TextAlign.Center // 중앙 정렬 유지
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Blue,
            unfocusedIndicatorColor = Color.Gray, // DarkGray에서 Gray로 변경
            disabledIndicatorColor = Color.Gray,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        )
    )
}

@Composable
fun DisplayInfo(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 20.sp, color = Color.Gray) // 18sp → 20sp로 증가
        Text(value, fontSize = 20.sp) // 18sp → 20sp로 증가
    }
}

@Composable
fun TelcoSelector(telco: String, onClick: () -> Unit) {
    Column {
        Text(
            text = "통신사", 
            style = MaterialTheme.typography.labelMedium,
            fontSize = 20.sp // 18sp → 20sp로 증가
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .border(1.dp, Color.Gray, MaterialTheme.shapes.medium)
                .clickable { onClick() }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = if (telco.isBlank()) "통신사를 선택해주세요" else telco,
                color = if (telco.isBlank()) Color.Gray else Color.Black,
                fontSize = 20.sp // 18sp → 20sp로 증가
            )
        }
    }
}