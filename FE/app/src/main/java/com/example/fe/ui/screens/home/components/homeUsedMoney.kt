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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.R
import com.example.fe.ui.components.backgrounds.GlassSurface
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.fe.ui.screens.home.HomeViewModel
import kotlinx.coroutines.flow.StateFlow
import com.example.fe.data.model.SpendingItem

@Composable
fun HomeUsedMoney(
    modifier: Modifier = Modifier,
    onDetailClick: () -> Unit = {},
    viewModel: HomeViewModel
) {
    val totalSpendingAmount: StateFlow<Int> = viewModel.totalSpendingAmount
    val totalBenefitAmount: StateFlow<Int> = viewModel.totalBenefitAmount
    val goodList: StateFlow<List<SpendingItem>> = viewModel.goodList
    val badList: StateFlow<List<SpendingItem>> = viewModel.badList

    val spendingAmount by totalSpendingAmount.collectAsState()
    val benefitAmount by totalBenefitAmount.collectAsState()
    val goodItems by goodList.collectAsState()
    val badItems by badList.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        cornerRadius = 16f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 중앙 정렬된 금액 정보
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "이번 달 소비",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${spendingAmount}원",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E1FF)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "받은 혜택 ${benefitAmount}원",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                // 오른쪽에 위치한 버튼
                IconButton(
                    onClick = onDetailClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 60.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "상세보기",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // 구분선 추가
            Divider(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.2f),
                thickness = 1.dp
            )
            
            // 탭 레이아웃을 토글 버튼으로 변경
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF2B3674)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 아쉬운 소비 버튼
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (selectedTabIndex == 0) Color.White
                            else Color.Transparent
                        )
                        .clickable { selectedTabIndex = 0 },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "아쉬움",
                        color = if (selectedTabIndex == 0) Color(0xFF2B3674) else Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 잘받은 소비 버튼
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (selectedTabIndex == 1) Color.White
                            else Color.Transparent
                        )
                        .clickable { selectedTabIndex = 1 },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "잘받음",
                        color = if (selectedTabIndex == 1) Color(0xFF2B3674) else Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // 카테고리별 소비 내역
            CategorySpendingList(
                selectedTabIndex = selectedTabIndex,
                goodList = goodItems,
                badList = badItems
            )
        }
    }
}

@Composable
private fun CategorySpendingList(
    selectedTabIndex: Int,
    goodList: List<SpendingItem>,
    badList: List<SpendingItem>
) {
    val items = if (selectedTabIndex == 0) badList else goodList

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        if (items.isEmpty()) {
            Text(
                text = "기록이 없습니다",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )
        } else {
            items.forEach { item ->
                CategoryRow(
                    category = item.category,
                    amount = item.amount,
                    benefit = item.benefit,
                    isGoodTab = selectedTabIndex == 1
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: String,
    amount: Int,
    benefit: Int,
    isGoodTab: Boolean
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
                painter = painterResource(id = R.drawable.ic_shopping),
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

