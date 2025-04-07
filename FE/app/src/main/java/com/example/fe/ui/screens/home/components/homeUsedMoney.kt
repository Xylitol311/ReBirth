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

// 임시 데이터 클래스
data class TempSpendingItem(
    val category: String,
    val amount: Int,
    val benefit: Int,
    val iconResId: Int = R.drawable.ic_shopping // 기본 아이콘
)

@Composable
fun HomeUsedMoney(
    modifier: Modifier = Modifier,
    onDetailClick: () -> Unit = {},
    viewModel: HomeViewModel
) {
    // API 데이터 (사용하지 않음)
    val totalSpendingAmount: StateFlow<Int> = viewModel.totalSpendingAmount
    val totalBenefitAmount: StateFlow<Int> = viewModel.totalBenefitAmount
    val goodList: StateFlow<List<SpendingItem>> = viewModel.goodList
    val badList: StateFlow<List<SpendingItem>> = viewModel.badList

    // API 데이터 수집 (사용하지 않음)
    val spendingAmount by totalSpendingAmount.collectAsState()
    val benefitAmount by totalBenefitAmount.collectAsState()
    val goodItems by goodList.collectAsState()
    val badItems by badList.collectAsState()

    // 임시 데이터 설정
    // 사진에 표시된 금액대로 설정
    val tempSpendingAmount = 150000
    val tempBenefitAmount = 1000
    
    // 임시 카테고리 데이터
    val tempGoodItems = listOf(
        TempSpendingItem("카페", 50000, 500, R.drawable.ic_shopping),
        TempSpendingItem("음식점", 30000, 300, R.drawable.ic_shopping),
        TempSpendingItem("편의점", 20000, 200, R.drawable.ic_shopping)
    )
    
    val tempBadItems = emptyList<TempSpendingItem>() // 아쉬움 탭은 비어있게 설정

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
                .padding(vertical = 32.dp, horizontal = 56.dp),
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
                        fontSize = 22.sp,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${tempSpendingAmount}원", // 임시 데이터 사용
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E1FF)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "받은 혜택 ${tempBenefitAmount}원", // 임시 데이터 사용
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                // 오른쪽에 위치한 버튼
                IconButton(
                    onClick = onDetailClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 20.dp)
                        .offset(y = -10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "상세보기",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
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
                        fontSize = 20.sp,
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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // 카테고리별 소비 내역 - 임시 데이터 사용
            TempCategorySpendingList(
                selectedTabIndex = selectedTabIndex,
                goodList = tempGoodItems,
                badList = tempBadItems
            )
        }
    }
}

// 임시 데이터용 컴포넌트
@Composable
private fun TempCategorySpendingList(
    selectedTabIndex: Int,
    goodList: List<TempSpendingItem>,
    badList: List<TempSpendingItem>
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
                TempCategoryRow(
                    category = item.category,
                    amount = item.amount,
                    benefit = item.benefit,
                    isGoodTab = selectedTabIndex == 1,
                    iconResId = item.iconResId
                )
            }
        }
    }
}

// 임시 데이터용 카테고리 행 컴포넌트
@Composable
private fun TempCategoryRow(
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
                fontSize = 28.sp,
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
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            
            // 혜택 금액 텍스트 색상 - 좋음(파란색)/아쉬움(빨간색)
            val benefitColor = if (isGoodTab) Color(0xFF00E1FF) else Color(0xFFFF5252)
            val benefitPrefix = if (isGoodTab) "+" else "-"
            
            Text(
                text = "$benefitPrefix${benefit}원 혜택",
                fontSize = 18.sp,
                color = benefitColor
            )
        }
    }
}

// 기존 컴포넌트는 유지 (사용하지 않음)
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
                fontSize = 28.sp,
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
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            
            if (isGoodTab) {
                Text(
                    text = "+${benefit}원",
                    fontSize = 18.sp,
                    color = Color(0xFF4285F4)
                )
            } else {
                Text(
                    text = "-${benefit}원",
                    fontSize = 18.sp,
                    color = Color(0xFFFF5252)
                )
            }
        }
    }
}

