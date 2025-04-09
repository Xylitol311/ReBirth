package com.example.fe.ui.screens.onboard

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.text.style.TextAlign
import com.example.fe.ui.theme.SkyBlue
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.ripple.rememberRipple

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

    // 쿼리 파라미터에서 초기 단계와 데이터 가져오기
    val navBackStackEntry = navController.currentBackStackEntry
    val stepParam = navBackStackEntry?.arguments?.getString("step")
    val nameParam = navBackStackEntry?.arguments?.getString("name")
    val ssnFrontParam = navBackStackEntry?.arguments?.getString("ssnFront")
    val phoneParam = navBackStackEntry?.arguments?.getString("phone")

    // 초기 단계 설정
    var currentStep by remember { 
        mutableStateOf(
            when (stepParam) {
                "CODE" -> Step.CODE
                else -> Step.NAME
            }
        ) 
    }
    
    // 기존 데이터 복원
    var name by remember { mutableStateOf(nameParam ?: "") }
    var ssnFront by remember { mutableStateOf(ssnFrontParam ?: "") }
    var ssnBack by remember { mutableStateOf("") }
    var telco by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(phoneParam ?: "") }
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

    // 시스템 뒤로가기 처리
    BackHandler(enabled = currentStep != Step.NAME) {
        // 첫 단계가 아닌 경우에만 이전 단계로 이동
        if (currentStep != Step.NAME) {
            currentStep = Step.values()[currentStep.ordinal - 1]
        }
    }

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        Log.d("Click", "다음")
                        if (currentStep == Step.CODE) {
                            if (code.length == 6) {
                                showAgreement = true
                            }
                        }
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
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkyBlue,
                        contentColor = Color.White,
                        disabledContainerColor = Color.LightGray
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
                .padding(horizontal = 40.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            when (currentStep) {
                Step.NAME -> {
                    val density = LocalDensity.current
                    val fontScale = density.fontScale
                    val titleBaseFontSize = 28.sp
                    val titleDynamicFontSize = (titleBaseFontSize.value * fontScale).sp

                    Text(
                        "이름을 알려주세요", 
                        fontSize = titleDynamicFontSize,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(40.dp))
                    
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
                    val density = LocalDensity.current
                    val fontScale = density.fontScale
                    val titleBaseFontSize = 28.sp
                    val titleDynamicFontSize = (titleBaseFontSize.value * fontScale).sp

                    Text(
                        "주민등록번호를 입력해주세요", 
                        fontSize = titleDynamicFontSize,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(40.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // 앞 6자리 입력 (왼쪽)
                            Box(modifier = Modifier.weight(0.35f)) {
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

                            // 중앙 "-" 배치
                            Box(
                                modifier = Modifier.weight(0.1f),
                                contentAlignment = Alignment.Center
                            ) {
                                val density = LocalDensity.current
                                val fontScale = density.fontScale
                                val baseFontSize = 32.sp
                                val dynamicFontSize = (baseFontSize.value * fontScale).sp

                                Text(
                                    text = "-",
                                    fontSize = dynamicFontSize,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = Color.Black
                                )
                            }

                            // 뒷 1자리 입력 (오른쪽)
                            Box(
                                modifier = Modifier.weight(0.15f),
                                contentAlignment = Alignment.Center
                            ) {
                                UnderlineSingleDigitField(
                                    value = ssnBack,
                                    onValueChange = { if (it.length <= 1) ssnBack = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(ssnBackFocusRequester),
                                    keyboardType = KeyboardType.Number
                                )
                            }

                            // 동그라미들
                            Box(
                                modifier = Modifier.weight(0.4f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(70.dp)
                                        .padding(start = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val density = LocalDensity.current
                                    val fontScale = density.fontScale
                                    val baseFontSize = 24.sp
                                    val dynamicFontSize = (baseFontSize.value * fontScale).sp

                                    repeat(6) {
                                        Text(
                                            "●",
                                            fontSize = dynamicFontSize,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    DisplayInfo("이름", name)
                }
                Step.TELECOM -> {
                    val density = LocalDensity.current
                    val fontScale = density.fontScale
                    val titleBaseFontSize = 28.sp
                    val titleDynamicFontSize = (titleBaseFontSize.value * fontScale).sp

                    Text(
                        "통신사를 선택해주세요", 
                        fontSize = titleDynamicFontSize,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(40.dp))
                    
                    TelcoSelector(telco = telco) {
                        Log.d("TELCO_CLICK", "바텀시트 열림")
                        showTelcoSheet = true
                    }
                    DisplayInfo("이름", name)
                    DisplayInfo("주민등록번호", "$ssnFront-$ssnBack●●●●●●")
                }
                Step.PHONE -> {
                    val density = LocalDensity.current
                    val fontScale = density.fontScale
                    val titleBaseFontSize = 28.sp
                    val titleDynamicFontSize = (titleBaseFontSize.value * fontScale).sp

                    Text(
                        "휴대폰 번호를 입력해주세요", 
                        fontSize = titleDynamicFontSize,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(40.dp))
                    
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
                    val density = LocalDensity.current
                    val fontScale = density.fontScale
                    val titleBaseFontSize = 28.sp
                    val titleDynamicFontSize = (titleBaseFontSize.value * fontScale).sp

                    Text(
                        "인증번호를 입력해주세요", 
                        fontSize = titleDynamicFontSize,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(40.dp))
                    
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
                telcos.forEach {
                    TextButton(
                        onClick = {
                            telco = it
                            showTelcoSheet = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Black,
                            disabledContentColor = Color.Gray
                        ),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text(
                            text = it,
                            fontSize = 20.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
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
                    fontSize = 24.sp
                )
                agreementItems.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    if (checkedItems.contains(item)) checkedItems.remove(item)
                                    else checkedItems.add(item)
                                }
                                .padding(end = 16.dp)
                        ) {
                            Text(
                                text = "✔",
                                color = if (checkedItems.contains(item)) SkyBlue else Color.Gray,
                                fontSize = 26.sp
                            )
                        }
                        Text(item, fontSize = 20.sp)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        showAgreement = false
                        // 동의 완료 후 다음 화면으로 이동
                        navController.navigate(
                            "income_input/${name}/${phone}/${ssnFront}",
                            NavOptions.Builder()
                                .setEnterAnim(0)
                                .setExitAnim(0)
                                .setPopEnterAnim(0)
                                .setPopExitAnim(0)
                                .build()
                        )
                    },
                    enabled = checkedItems.size == agreementItems.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkyBlue,
                        contentColor = Color.White,
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    Text(
                        "동의하기",
                        fontSize = 22.sp,
                        color = Color.White
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
    val density = LocalDensity.current
    val fontScale = density.fontScale
    val baseFontSize = 20.sp
    val labelBaseFontSize = 14.sp
    val dynamicFontSize = (baseFontSize.value * fontScale).sp
    val labelDynamicFontSize = (labelBaseFontSize.value * fontScale).sp

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(70.dp),
        singleLine = true,
        label = { Text(label, fontSize = labelDynamicFontSize, color = SkyBlue) },
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = dynamicFontSize, color = Color.Black),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = SkyBlue,
            unfocusedIndicatorColor = Color.Gray,
            disabledIndicatorColor = Color.Gray,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            cursorColor = SkyBlue
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
    val density = LocalDensity.current
    val fontScale = density.fontScale
    val baseFontSize = 22.sp
    val labelBaseFontSize = 14.sp
    val dynamicFontSize = (baseFontSize.value * fontScale).sp
    val labelDynamicFontSize = (labelBaseFontSize.value * fontScale).sp

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        label = { Text("", fontSize = labelDynamicFontSize, color = SkyBlue) },
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = dynamicFontSize,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = Color.Black
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = SkyBlue,
            unfocusedIndicatorColor = Color.Gray,
            disabledIndicatorColor = Color.Gray,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            cursorColor = SkyBlue
        )
    )
}

@Composable
fun DisplayInfo(label: String, value: String) {
    val density = LocalDensity.current
    val fontScale = density.fontScale
    val baseFontSize = 20.sp
    val dynamicFontSize = (baseFontSize.value * fontScale).sp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = dynamicFontSize, color = Color.Gray)
        Text(value, fontSize = dynamicFontSize)
    }
}

@Composable
fun TelcoSelector(telco: String, onClick: () -> Unit) {
    val density = LocalDensity.current
    val fontScale = density.fontScale
    val labelBaseFontSize = 16.sp
    val textBaseFontSize = 16.sp
    val labelDynamicFontSize = (labelBaseFontSize.value * fontScale).sp
    val textDynamicFontSize = (textBaseFontSize.value * fontScale).sp

    Column {
        Text(
            text = "통신사",
            style = MaterialTheme.typography.labelMedium,
            fontSize = labelDynamicFontSize,
            color = SkyBlue
        )
        TextButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .border(1.dp, SkyBlue, MaterialTheme.shapes.medium),
            colors = ButtonDefaults.textButtonColors(
                containerColor = Color.Transparent,
                contentColor = if (telco.isBlank()) Color.Gray else Color.Black
            ),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (telco.isBlank()) "통신사를 선택해주세요" else telco,
                    fontSize = textDynamicFontSize
                )
            }
        }
    }
}