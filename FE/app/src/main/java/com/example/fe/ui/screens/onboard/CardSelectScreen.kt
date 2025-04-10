package com.example.fe.ui.screens.onboard

import android.R.color.white
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fe.R
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel
import com.example.fe.ui.theme.SkyBlue
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSelectScreen(navController: NavController, viewModel: OnboardingViewModel) {
    val cardList = listOf("현대카드", "우리카드", "삼성카드", "신한카드", "국민카드", "농협카드")
    val selectedCards = remember { mutableStateListOf<String>() }
    val coroutineScope = rememberCoroutineScope()

    val allSelected = selectedCards.size == cardList.size


    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {}
            )
        },
        bottomBar = {
            Box(modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(16.dp)
            )  {
                Button(
                    onClick = {
                        navController.navigate("additional_security_setup")

                        coroutineScope.launch {
                            viewModel.loadAllMyData(
                                onSuccess = {
                                    viewModel.generateReportFromMyData(
                                        onSuccess = {
                                            Log.e("AuthAPI", "리포트 받아오기 완료!")
                                        },
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
                        .height(60.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkyBlue,
                        contentColor = Color.White,
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    Text(
                        text = "다음",
                        fontSize = 22.sp
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal =30.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text("연결할 카드사를 선택해주세요",  fontSize = 24.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally))

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (allSelected) Color(0xFFE6F7FF) else LightGray)
                        .size(20.dp)
                        .clickable {
                            if (allSelected) {
                                selectedCards.clear()
                            } else {
                                selectedCards.clear()
                                selectedCards.addAll(cardList)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (allSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "체크됨",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text("카드사 전체선택", fontSize = 14.sp)
            }


            Spacer(modifier = Modifier.height(16.dp))

            // 카드들을 그리드로 배치
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(cardList.size) { index ->
                    val card = cardList[index]
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
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFE6F7FF) else Color.Transparent)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFF00E5FF) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Box {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = getCardLogoResId(cardName)),
                    contentDescription = "$cardName 로고",
                    modifier = Modifier.size(60.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = cardName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "선택됨",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .absoluteOffset(x = 4.dp, y = (-4).dp)
                )
            }
        }
    }
}

fun getCardLogoResId(cardName: String): Int {
    return when (cardName) {
        "현대카드" -> R.drawable.card_hyundai
        "우리카드" -> R.drawable.card_woori
        "삼성카드" -> R.drawable.card_samsung
        "신한카드" -> R.drawable.card_shinhan
        "국민카드" -> R.drawable.card_kb
        "농협카드" -> R.drawable.card_nh
        else -> R.drawable.card_nh // 예외 처리용 기본 이미지
    }
}