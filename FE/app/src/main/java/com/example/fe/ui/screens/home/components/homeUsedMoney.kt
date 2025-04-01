package com.example.fe.ui.screens.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.R
import com.example.fe.ui.components.backgrounds.GlassSurface
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

@Composable
fun HomeUsedMoney(
    modifier: Modifier = Modifier,
    onDetailClick: () -> Unit = {}
) {
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        cornerRadius = 16f
    ) {
        Column(
            modifier = Modifier.padding(48.dp)
        ) {
            // 이번 달 소비 금액과 상세보기 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 60.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 50.dp, end = 0.dp),
                ) {
                    Text(
                        text = "이번 달 소비",
                        fontSize = 22.sp,
                        color = Color.White
                    )
                    Text(
                        text = "150,000원",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    // 받은 혜택
                    Text(
                        text = "받은 혜택 1,000원",
                        fontSize = 18.sp,
                        color = Color(0xFF64B5F6),
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                    )

                }

                IconButton(
                    onClick = onDetailClick,
                    modifier = Modifier
                        .offset(y = (-14).dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "상세보기",
                        tint = Color.White
                    )
                }
            }
            
            // 탭 레이아웃
            var selectedTabIndex by remember { mutableStateOf(0) }
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                divider = { /* 구분선 제거 */ },
                indicator = { tabPositions ->
                    // 선택된 탭 위치에 맞춰 인디케이터 표시
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .height(2.dp)
                            .padding(horizontal = 16.dp)
                            .background(color = Color.White)
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { 
                        Text(
                            "혜택이 아쉬운 소비",
                            fontSize = 16.sp
                        ) 
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { 
                        Text(
                            "혜택을 잘 받은 소비",
                            fontSize = 16.sp
                        ) 
                    }
                )
            }
            
            // 카테고리별 소비 내역
            CategorySpendingList(selectedTabIndex)
        }
    }
}

@Composable
private fun CategorySpendingList(selectedTabIndex: Int) {
    val categories = listOf(
        Triple("카페", 50000, 5000),
        Triple("음식점", 30000, 3000),
        Triple("편의점", 20000, 2000)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        categories.forEach { (category, amount, benefit) ->
            CategoryRow(
                category = category,
                amount = amount,
                benefit = benefit,
                isGoodTab = selectedTabIndex == 1,
                iconResId = when (category) {
                    "카페" -> R.drawable.ic_coffee
                    "음식점" -> R.drawable.ic_restaurant
                    else -> R.drawable.ic_shopping
                }
            )
        }
    }
}

@Composable
private fun CategoryRow(
    category: String,
    amount: Int,
    benefit: Int,
    isGoodTab: Boolean,
    iconResId: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 카테고리 아이콘과 이름
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = category,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = category,
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // 금액 정보
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${amount}원",
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            
            if (isGoodTab) {
                Text(
                    text = "+${benefit}원",
                    fontSize = 16.sp,
                    color = Color(0xFF4285F4)
                )
            } else {
                Text(
                    text = "-${benefit}원",
                    fontSize = 16.sp,
                    color = Color(0xFFFF5252)
                )
            }
        }
    }
}

