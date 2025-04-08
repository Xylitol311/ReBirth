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
import com.example.fe.ui.screens.onboard.Step
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
    
    // 뒤로가기 기능 활성화 - 이전 단계(인증번호 화면)로 돌아가도록 설정
    BackHandler(enabled = true) {
        // 인증번호 화면(AUTH)으로 돌아가고 CODE 단계로 설정하기 위해 특수 파라미터 추가
        navController.navigate("auth?step=CODE&name=${name}&ssnFront=${ssnFront}&phone=${phone}") {
            popUpTo("income_input/{name}/{phone}/{ssnFront}") { inclusive = true }
        }
    }
    
    LaunchedEffect(Unit) {
        incomeFocusRequester.requestFocus()
        keyboardController?.show()
    }

    Scaffold(
        topBar = {
            // 상단 앱바 추가 (뒤로가기 버튼 포함)
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { 
                        // 인증번호 화면(AUTH)으로 돌아가고 CODE 단계로 설정하기 위해 특수 파라미터 추가
                        navController.navigate("auth?step=CODE&name=${name}&ssnFront=${ssnFront}&phone=${phone}") {
                            popUpTo("income_input/{name}/{phone}/{ssnFront}") { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
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
                    shape = RoundedCornerShape(16.dp), // 모서리를 둥글게
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkyBlue, // 하늘색 배경
                        contentColor = Color.White, // 흰색 텍스트
                        disabledContainerColor = Color.LightGray // 비활성화시 밝은 회색
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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            Text(
                text = "소비패턴 분석을 위해서\n월 평균 수입을 알려주세요", 
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )
            
            Spacer(Modifier.height(40.dp))
            
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = income,
                    onValueChange = { newValue -> 
                        // 숫자만 입력 가능하도록
                        if (newValue.all { char -> char.isDigit() }) {
                            income = newValue 
                        }
                    },
                    label = { Text("", color = SkyBlue) },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 20.sp,
                        color = Color.Black
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
                        .weight(1f)
                        .focusRequester(incomeFocusRequester)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "원",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
} 