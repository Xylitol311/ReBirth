package com.example.fe.ui.screens.onboard

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fe.R
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSelectScreen(navController: NavController, viewModel: OnboardingViewModel) {
    val cardList = listOf("신한카드", "국민카드", "삼성카드", "현대카드", "우리카드")
    val selectedCards = remember { mutableStateListOf<String>() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* 타이틀 제거 */ },
                navigationIcon = { /* 뒤로가기 버튼 제거 */ }
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
                        // 1. 버튼 클릭 시 즉시 화면 전환
                        navController.navigate("additional_security_setup")

                        // 2. 백그라운드에서 API 순차 호출 (결과와 무관)
                        coroutineScope.launch {
                            viewModel.loadAllMyData(
                                onSuccess = {

                                    viewModel.generateReportFromMyData(
//                                        userId = viewModel.userId,
                                        userId =2,
                                        onSuccess = { Log.e("AuthAPI", "리포트 받아오기 완료!")/* 리포트 생성 성공 로직 (필요 시) */ },
                                        onFailure = { error ->
                                            Log.e("API", "리포트 생성 실패: $error")
                                        }
                                    )
                                },
                                onFailure = { error ->
                                    Log.e("API", "마이데이터 로드 실패: $error")
                                }
                            )
                        }
                    },
                    enabled = selectedCards.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text("다음", fontSize = 22.sp)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("연결할 카드를 선택해주세요", fontSize = 28.sp)

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(cardList) { card ->
                    CardItem(
                        cardName = card,
                        isSelected = selectedCards.contains(card),
                        onClick = {
                            if (selectedCards.contains(card)) {
                                selectedCards.remove(card)
                            } else {
                                selectedCards.add(card)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CardItem(
    cardName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.card),
            contentDescription = "카드 이미지",
            modifier = Modifier.size(60.dp)
        )

        Spacer(modifier = Modifier.width(20.dp))

        Text(
            text = cardName, 
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "선택됨",
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
