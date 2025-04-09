package com.example.fe.ui.screens.onboard

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val cardList = listOf("신한카드", "국민카드", "삼성카드", "현대카드", "우리카드", "농협카드")
    val selectedCards = remember { mutableStateListOf<String>() }
    val coroutineScope = rememberCoroutineScope()
    var isAllSelected by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.size(54.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            modifier = Modifier.size(32.dp)
                        )
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
                        navController.navigate("additional_security_setup")
                        coroutineScope.launch {
                            viewModel.loadAllMyData(
                                onSuccess = {
                                    viewModel.generateReportFromMyData(
                                        userId = 2,
                                        onSuccess = { Log.e("AuthAPI", "리포트 받아오기 완료!") },
                                        onFailure = { error -> Log.e("API", "리포트 생성 실패: $error") }
                                    )
                                },
                                onFailure = { error -> Log.e("API", "마이데이터 로드 실패: $error") }
                            )
                        }
                    },
                    enabled = selectedCards.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkyBlue,
                        contentColor = Color.White,
                        disabledContainerColor = Color.LightGray
                    )
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
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            val density = LocalDensity.current
            val fontScale = density.fontScale
            val titleBaseFontSize = 28.sp
            val titleDynamicFontSize = (titleBaseFontSize.value * fontScale).sp

            Text(
                "연결할 카드를 선택해주세요",
                fontSize = titleDynamicFontSize,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 전체 선택 버튼과 텍스트를 포함하는 Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                        .clickable {
                            isAllSelected = !isAllSelected
                            if (isAllSelected) {
                                selectedCards.clear()
                                selectedCards.addAll(cardList)
                            } else {
                                selectedCards.clear()
                            }
                        }
                        .background(
                            if (isAllSelected) SkyBlue.copy(alpha = 0.1f) else Color.Transparent,
                            RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAllSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "전체 선택",
                            tint = SkyBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    "카드사 전체선택",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            ) {
                items(cardList) { card ->
                    CardItem(
                        cardName = card,
                        isSelected = selectedCards.contains(card),
                        onClick = {
                            if (selectedCards.contains(card)) {
                                selectedCards.remove(card)
                                isAllSelected = false
                            } else {
                                selectedCards.add(card)
                                if (selectedCards.size == cardList.size) {
                                    isAllSelected = true
                                }
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
    val density = LocalDensity.current
    val fontScale = density.fontScale
    val textBaseFontSize = 16.sp
    val textDynamicFontSize = (textBaseFontSize.value * fontScale).sp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .border(
                width = if (isSelected) 3.dp else 1.5.dp,
                color = if (isSelected) SkyBlue else Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = if (isSelected) SkyBlue.copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.card),
                contentDescription = "카드 이미지",
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = cardName,
                fontSize = textDynamicFontSize,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "선택됨",
                tint = SkyBlue,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
            )
        }
    }
}
