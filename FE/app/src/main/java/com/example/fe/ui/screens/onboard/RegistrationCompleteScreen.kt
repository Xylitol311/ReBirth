package com.example.fe.ui.screens.onboard

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton

import androidx.compose.material3.Scaffold
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
 import androidx.compose.foundation.layout.WindowInsets
 import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.layout.ContentScale
import coil.request.ImageRequest


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
                    val previousMonth = if (month == 1) 12 else month - 1

                    isLoading = true
                    viewModel.getUserPatternType(
                        year = year,
                        month = previousMonth,
                        onSuccess = {
                            reportData = it
                            isLoading = false
                            screenState = CompleteScreenState.SPENDING_TYPE
                            Log.e("AuthReport","그 후 리포트 가져올 준비중")
                            viewModel.generateAllReportFromMyData(
                                onSuccess={
                                    Log.d("AuthReport","그 후 리포트 가져옴!")
                                },
                            onFailure={
                                Log.e("AuthReport","그 후 리포트 못 가져옴")
                            }
                            )

                        },
                        onFailure = {
                            errorMessage = it
                            isLoading = false
                        }
                    )
                },
                onSkip = {
                    viewModel.setLoggedInState(true)
                    val today = LocalDate.now()
                    val year = today.year
                    val month = today.monthValue
                    val previousMonth = if (month == 1) 12 else month - 1

                    isLoading = true
                    viewModel.getUserPatternType(
                        year = year,
                        month = previousMonth,
                        onSuccess = {
                            reportData = it
                            isLoading = false
                            screenState = CompleteScreenState.SPENDING_TYPE
                            Log.e("AuthReport","그 후 리포트 가져올 준비중")
                            viewModel.generateAllReportFromMyData(
                                onSuccess={
                                    Log.d("AuthReport","그 후 리포트 가져옴!")
                                },
                                onFailure={
                                    Log.e("AuthReport","그 후 리포트 못 가져옴")
                                }
                            )

                        },
                        onFailure = {
                            errorMessage = it
                            isLoading = false
                        }
                    )
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


    Scaffold(
        // Make sure bottomBar respects navigation bar insets
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Button(
                    onClick = onCheckSpendingType,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00D9FF), // 버튼 배경 색상 변경
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "내 소비 유형 확인하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    border = BorderStroke(1.dp, Color(0xFF00D9FF)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF00D9FF)
                    )
                ) {
                    Text(
                        text = "홈으로 가기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        // Make the Scaffold content area respect navigation bars as well
        contentWindowInsets = WindowInsets.navigationBars
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(240.dp))

                Text(
                    text = "등록이 완료됐어요!",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "내 소비 유형을 확인해보세요",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
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

    val groupName = report.groupName ?: "소비 유형"
    val variation = (report.variation ?: 0)
    val extrovert = (report.extrovert ?: 0)
    val overConsumption = (report.overConsumption ?: 0)
    val patternName = pattern?.patternName ?: "소비 유형"
    val reportDesc = report.reportDescription ?: ""
    val patternDesc = pattern?.description ?: ""
    val imgUrl = pattern?.imgUrl

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {

        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding() // 하단 시스템 UI 고려
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = onGoHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00C4E8),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "홈으로 가기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        StarryBackground(scrollOffset = 0f, starCount = 150, horizontalOffset = 0f) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(70.dp))

                Text(
                    text = "당신에게 딱맞는\n소비 행성은 $patternName 입니다.",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Start,
                    lineHeight = 32.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 50.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))


                val imageUrl = imgUrl

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "소비 유형 이미지",
                    modifier = Modifier
                        .size(180.dp)
                        .padding(vertical = 4.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 유형별 수치 제목
                Text(
                    text = "유형별 수치",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Score bars 영역 (패딩 적용)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp)
                ) {
                    ImprovedScoreBar(title = "외향성", value = extrovert)
                    Spacer(modifier = Modifier.height(16.dp))
                    ImprovedScoreBar(title = "안정성", value = variation)
                    Spacer(modifier = Modifier.height(16.dp))
                    ImprovedScoreBar(title = "저축성", value = overConsumption)
                }
                Spacer(modifier = Modifier.height(24.dp))

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                    color = Color(0xFF00C6E3),
                    thickness = 0.5.dp
                )            // 구분선 추가

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = patternDesc,
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 25.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp)
                )


                Spacer(modifier = Modifier.height(40.dp)) // 하단 버튼 영역 확보
            }
        }
    }
}

@Composable
fun ImprovedScoreBar(title: String, value: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽에 타이틀
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            modifier = Modifier.width(60.dp)
        )

        // 중간에 프로그레스 바
        Box(
            modifier = Modifier
                .weight(0.7f)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((value / 100f).coerceIn(0f, 1f))
                    .height(20.dp)
                    .background(Color(0xFF00C4E8))
            )
        }

        // 오른쪽에 수치
        Text(
            text = "${value}/100",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.width(50.dp),
            textAlign = TextAlign.End
        )
    }
}