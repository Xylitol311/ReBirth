package com.example.fe.ui.screens.onboard

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fe.R
import com.example.fe.data.model.auth.ReportWithPatternDTO
import com.example.fe.ui.components.backgrounds.StarryBackground
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel
import java.time.LocalDate

enum class CompleteScreenState {
    REGISTRATION_COMPLETE,
    SPENDING_TYPE
}

@Composable
fun RegistrationCompleteScreen(navController: NavController, viewModel: OnboardingViewModel) {
    var screenState by remember { mutableStateOf(CompleteScreenState.REGISTRATION_COMPLETE) }
    var reportData by remember { mutableStateOf<ReportWithPatternDTO?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    when (screenState) {
        CompleteScreenState.REGISTRATION_COMPLETE -> {
            viewModel.setIsLogged()
            RegistrationCompleteContent(
                onCheckSpendingType = {
                    // 날짜 계산
                    val today = LocalDate.now()
                    val year = today.year
                    val month = today.monthValue

                    isLoading = true
                    viewModel.getUserPatternType(
                        userId = "2",
                        year = year,
                        month = month-1,
                        onSuccess = {
                            reportData = it
                            isLoading = false
                            screenState = CompleteScreenState.SPENDING_TYPE
                        },
                        onFailure = {
                            errorMessage = it
                            isLoading = false
                        }
                    )
                },
                onSkip = {
                    viewModel.setLoggedInState(true)
                }
            )
        }

        CompleteScreenState.SPENDING_TYPE -> {
            reportData?.let { report ->
                SpendingTypeContent(
                    report = report,
                    onGoHome = {
                        viewModel.setLoggedInState(true)
                    }
                )
            } ?: run {
                // 로딩 중이거나 데이터가 없을 경우 예외 처리
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("소비 패턴 정보를 불러올 수 없습니다.")
                }
            }
        }
    }

    // 로딩 인디케이터
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF3366FF))
        }
    }

    // 에러 메시지 토스트 (선택사항)
    errorMessage?.let {
        LaunchedEffect(it) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            errorMessage = null // 한 번만 표시되도록 초기화
        }
    }
}

@Composable
fun RegistrationCompleteContent(
    onCheckSpendingType: () -> Unit,
    onSkip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "등록이 완료됐어요!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "내 소비 유형을 확인해보세요",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onCheckSpendingType,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF191E3F)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("내 소비 유형 확인하기")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSkip,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF5F5F5),
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("홈으로 가기")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeInputDialog(
    income: String,
    onIncomeChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "월 평균 수입을 입력해주세요",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = income,
                    onValueChange = { newValue ->
                        // 숫자만 입력 가능하도록
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            onIncomeChange(newValue)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3366FF),
                        unfocusedBorderColor = Color.Gray
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    suffix = { Text("원") }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF191E3F)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("입력")
                }
            }
        }
    }
}
@Composable
fun SpendingTypeContent(
    report: ReportWithPatternDTO,
    onGoHome: () -> Unit
) {
    val pattern = report.consumptionPattern

    // 안전하게 값 가져오기
    val groupName = report.groupName ?: "소비 유형"
    val variation = (report.variation ?: 0) / 100f
    val extrovert = (report.extrovert ?: 0) / 100f
    val overConsumption = (report.overConsumption ?: 0) / 100f
    val patternName = pattern?.patternName ?: "소비 유형"
    val reportDesc = report.reportDescription ?: ""
    val patternDesc = pattern?.description ?: ""
    val imgUrl = pattern?.imgUrl

    // 별 배경 추가
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        StarryBackground(scrollOffset = 0f, starCount = 150, horizontalOffset = 0f) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "당신의 소비는\n$groupName 입니다.",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 지구 이미지 (URL 이미지 로딩)
                imgUrl?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = patternName,
                        modifier = Modifier.size(150.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 소비 점수 표시
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("유형별", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = extrovert.coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF3366FF),
                        trackColor = Color.Gray.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("안정성", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = variation.coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF3366FF),
                        trackColor = Color.Gray.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("저축성", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = overConsumption.coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF3366FF),
                        trackColor = Color.Gray.copy(alpha = 0.3f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "#여행 #교육 #문화생활",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "$reportDesc\n\n$patternDesc",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onGoHome,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF191E3F)
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("홈으로 가기")
                }
            }
        }
    }
}