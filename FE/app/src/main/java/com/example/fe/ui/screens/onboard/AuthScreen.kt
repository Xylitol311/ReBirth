package com.example.fe.ui.screens.onboard

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.text.input.KeyboardType
import com.example.fe.ui.screens.onboard.OnboardingViewModel

enum class Step {
    NAME, SSN, TELECOM, PHONE, CODE, AGREEMENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(navController: NavController, viewModel: OnboardingViewModel) {
    val focusManager = LocalFocusManager.current

    var currentStep by remember { mutableStateOf(Step.NAME) }
    var name by remember { mutableStateOf("") }
    var ssnFront by remember { mutableStateOf("") }
    var ssnBack by remember { mutableStateOf("") }
    var telco by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var showTelcoSheet by remember { mutableStateOf(false) }
    var showAgreement by remember { mutableStateOf(false) }

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
                    IconButton(onClick = {
                        if (currentStep != Step.NAME) {
                            currentStep = Step.values()[currentStep.ordinal - 1]
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(text = if (currentStep == Step.CODE) "확인" else "다음")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            when (currentStep) {
                Step.NAME -> {
                    Text("이름을 알려주세요", fontSize = 20.sp)
                    UnderlineTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "이름"
                    )
                }
                Step.SSN -> {
                    Text("주민등록번호를 입력해주세요", fontSize = 20.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 앞 6자리 입력
                        UnderlineTextField(
                            value = ssnFront,
                            onValueChange = {
                                if (it.length <= 6) {
                                    ssnFront = it
                                    if (it.length == 6) focusManager.moveFocus(FocusDirection.Next)
                                }
                            },
                            label = "주민등록번호",
                            modifier = Modifier.weight(1.2f),
                            keyboardType = KeyboardType.Number
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        // - 기호
                        Text("-", fontSize = 20.sp, modifier = Modifier.padding(horizontal = 4.dp))

                        // 뒷 1자리 입력
                        UnderlineTextField(
                            value = ssnBack,
                            onValueChange = { if (it.length <= 1) ssnBack = it },
                            label = "", // 라벨 없애기
                            modifier = Modifier.width(48.dp),
                            keyboardType = KeyboardType.Number
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // ●●●●●
                        Row(
                            modifier = Modifier.height(56.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(6) {
                                Text("●", fontSize = 20.sp, color = Color.Gray)
                            }
                        }
                    }
                    DisplayInfo("이름", name)
                }
                Step.TELECOM -> {
                    Text("통신사를 선택해주세요", fontSize = 20.sp)
                    TelcoSelector(telco = telco) {
                        Log.d("TELCO_CLICK", "바텀시트 열림")
                        showTelcoSheet = true
                    }
                    DisplayInfo("이름", name)
                    DisplayInfo("주민등록번호", "$ssnFront-$ssnBack●●●●●●")
                }
                Step.PHONE -> {
                    Text("휴대폰 번호를 입력해주세요", fontSize = 20.sp)
                    UnderlineTextField(
                        value = phone,
                        onValueChange = {
                            if (it.length <= 11) phone = it
                        },
                        label = "휴대폰 번호",
                        keyboardType = KeyboardType.Phone
                    )
                    DisplayInfo("이름", name)
                    DisplayInfo("주민등록번호", "$ssnFront-$ssnBack●●●●●●")
                    DisplayInfo("통신사", telco)
                }
                Step.CODE -> {
                    Text("인증번호를 입력해주세요", fontSize = 20.sp)
                    UnderlineTextField(
                        value = code,
                        onValueChange = { if (it.length <= 6) code = it },
                        label = "인증번호",
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
        ModalBottomSheet(onDismissRequest = { showTelcoSheet = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("통신사를 선택해주세요", style = MaterialTheme.typography.titleMedium)
                telcos.forEach {
                    Text(
                        text = it,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                telco = it
                                showTelcoSheet = false
                            }
                            .padding(12.dp)
                    )
                }
            }
        }
    }

    if (showAgreement) {
        ModalBottomSheet(onDismissRequest = { showAgreement = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("서비스를 쓰려면 동의가 필요해요", style = MaterialTheme.typography.titleMedium)
                agreementItems.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (checkedItems.contains(item)) checkedItems.remove(item)
                                else checkedItems.add(item)
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        // 커스텀 체크박스처럼 보이는 텍스트
                        Text(
                            text = "✔",
                            color = if (checkedItems.contains(item)) Color(0xFF1976D2) else Color.Gray, // 파란색/회색
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(item)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        showAgreement = false
                        navController.navigate("card_select")
                    },
                    enabled = checkedItems.size == agreementItems.size,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("동의하기") }
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
        modifier = modifier,
        singleLine = true,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Blue,
            unfocusedIndicatorColor = Color.LightGray,
            disabledIndicatorColor = Color.LightGray,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        )
    )
}

@Composable
fun DisplayInfo(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp)
    }
}

@Composable
fun TelcoSelector(telco: String, onClick: () -> Unit) {
    Column {
        Text(text = "통신사", style = MaterialTheme.typography.labelMedium)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(1.dp, Color.Gray, MaterialTheme.shapes.medium)
                .clickable { onClick() }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = if (telco.isBlank()) "통신사를 선택해주세요" else telco,
                color = if (telco.isBlank()) Color.Gray else Color.Black
            )
        }
    }
}
