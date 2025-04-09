package com.example.fe.ui.screens.onboard

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.fe.ui.theme.SkyBlue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun IncomeInputScreen(
    navController: NavController,
    name: String,
    phone: String,
    ssnFront: String
) {
    var income by remember { mutableStateOf("") }
    val incomeFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // 알림 표시 상태 관리
    var showAlert by remember { mutableStateOf(false) }

    // 뒤로가기 기능 비활성화
    BackHandler(enabled = true) {
        // 아무 동작도 하지 않음 (뒤로가기 동작 차단)
    }

    LaunchedEffect(Unit) {
        incomeFocusRequester.requestFocus()
        keyboardController?.show()
    }

    // 알림 다이얼로그
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = {
                Text(
                    text = "입력 제한",
                    fontSize = 18.sp // 글자 크기 줄임
                )
            },
            text = {
                Text(
                    text = "2,100,000,000원 이상 기입할 수 없습니다."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAlert = false
                        income = "2099999999"
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Black // 확인 버튼 텍스트 색상
                    )
                ) {
                    Text("확인")
                }
            },
            containerColor = Color.White,
            titleContentColor = SkyBlue,
            textContentColor = Color.Black
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigate("auth")
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
            )
        },
        bottomBar = {
            Box(modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(16.dp)) {
                Button(
                    onClick = {
                        // 확인 버튼 클릭 시 PIN 설정 화면으로 애니메이션 없이 이동
                        navController.navigate(
                            "pin_setup/${name}/${phone}/${ssnFront}/${income}",
                            NavOptions.Builder()
                                .setEnterAnim(0)
                                .setExitAnim(0)
                                .setPopEnterAnim(0)
                                .setPopExitAnim(0)
                                .build()
                        )
                    },
                    enabled = income.isNotBlank(),
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
                        text = "확인",
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
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Top, // Changed from Center to Top
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp)) // Add space at the top for better positioning

            Text("소비패턴 분석을 위해서",
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text("월 평균 수입을 알려주세요",
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Add a smaller spacer between text and TextField
            Spacer(Modifier.height(32.dp))

            // Removed weight modifier from Row
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = income,
                    onValueChange = { newValue ->
                        // 숫자만 입력 가능하도록
                        if (newValue.all { char -> char.isDigit() }) {
                            // 21억 이상인지 확인
                            if (newValue.length > 10 || (newValue.length == 10 && newValue > "2100000000")) {
                                showAlert = true
                            } else {
                                income = newValue
                            }
                        }
                    },
                    label = { Text("", color = SkyBlue) },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 20.sp,
                        color = Color.Black,
                        textAlign = TextAlign.End
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = SkyBlue,
                        unfocusedIndicatorColor = Color.Gray,
                        cursorColor = SkyBlue
                    ),
                    modifier = Modifier
                        .weight(0.6f)
                        .focusRequester(incomeFocusRequester)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "원",
                    fontSize = 25.sp,
                )
            }

            // Add more space at the bottom to push content up
            Spacer(Modifier.weight(1f))
        }
    }
}