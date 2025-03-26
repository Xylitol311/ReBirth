package com.example.fe.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.R
import com.example.fe.ui.components.backgrounds.StarryBackground
import com.example.fe.ui.components.cards.VerticalCardLayout
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun HomeDetailScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("카드별", "카테고리별")
    
    // 코루틴 스코프 추가
    val coroutineScope = rememberCoroutineScope()
    
    // 화면 전환 애니메이션을 위한 상태
    val isNavigatingBack = remember { mutableStateOf(false) }
    
    // 애니메이션 값
    val contentAlpha by animateFloatAsState(
        targetValue = if (isNavigatingBack.value) 0f else 1f,
        animationSpec = tween(300),
        label = "contentAlpha"
    )
    
    // 더미 데이터 - 카드별 사용 내역
    val cardUsages = remember {
        listOf(
            CardUsage(
                cardImage = R.drawable.card,
                cardName = "하나 VIVA e Platinum 카드",
                usageAmount = 50000,
                benefit = 3000,
                annualFee = 10000
            ),
            CardUsage(
                cardImage = R.drawable.card,
                cardName = "신한 Deep Dream 카드",
                usageAmount = 35000,
                benefit = 2000,
                annualFee = 15000
            ),
            CardUsage(
                cardImage = R.drawable.card,
                cardName = "KB 국민 톡톡 카드",
                usageAmount = 25000,
                benefit = 1500,
                annualFee = 5000
            ),
            CardUsage(
                cardImage = R.drawable.card,
                cardName = "삼성 taptap O 카드",
                usageAmount = 15000,
                benefit = 1000,
                annualFee = 8000
            )
        ).sortedByDescending { it.usageAmount }
    }
    
    // 더미 데이터 - 카테고리별 사용 내역
    val categoryUsages = remember {
        listOf(
            CategoryUsage(
                category = "카페",
                percentage = 35f,
                usageAmount = 45000,
                benefit = 2500
            ),
            CategoryUsage(
                category = "식당",
                percentage = 25f,
                usageAmount = 32000,
                benefit = 1800
            ),
            CategoryUsage(
                category = "쇼핑",
                percentage = 20f,
                usageAmount = 25000,
                benefit = 1500
            ),
            CategoryUsage(
                category = "교통",
                percentage = 15f,
                usageAmount = 18000,
                benefit = 1000
            ),
            CategoryUsage(
                category = "기타",
                percentage = 5f,
                usageAmount = 5000,
                benefit = 200
            )
        ).sortedByDescending { it.usageAmount }
    }
    
    // 배경과 콘텐츠를 함께 배치
    Box(modifier = Modifier.fillMaxSize()) {
        // 배경
        StarryBackground(
            scrollOffset = 0f,
            starCount = 150,
            modifier = Modifier.fillMaxSize()
        ) {
            // 빈 Box - 배경만 표시
        }
        
        // 실제 콘텐츠 - TopBar 제거
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = contentAlpha) // 페이드 효과 적용
                .padding(top = 56.dp) // TopBar 높이만큼 패딩 추가
        ) {
            // 나머지 콘텐츠
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 총 사용 금액 카드
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "총 사용 금액",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "125,000원",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "받은 혜택 6,000원",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // 탭 선택
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color(0xFF2D2A57),
                    contentColor = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.background(
                                if (selectedTabIndex == index) Color(0xFF2D2A57) else Color(0xFF1A1834)
                            )
                        )
                    }
                }
                
                // 탭 내용
                when (selectedTabIndex) {
                    0 -> CardUsageList(cardUsages)
                    1 -> CategoryUsageList(categoryUsages)
                }
            }
        }
    }
}

@Composable
fun CardUsageList(cardUsages: List<CardUsage>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        items(cardUsages) { cardUsage ->
            CardUsageItem(cardUsage)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CardUsageItem(cardUsage: CardUsage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 카드 이미지 (세로 레이아웃)
            VerticalCardLayout(
                cardImage = painterResource(id = cardUsage.cardImage),
                height = 120.dp,
                width = 80.dp,
                cornerRadius = 8.dp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 카드 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = cardUsage.cardName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "사용 금액",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    Text(
                        text = "${cardUsage.usageAmount}원",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "받은 혜택",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    Text(
                        text = "${cardUsage.benefit}원",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "연회비",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    Text(
                        text = "${cardUsage.annualFee}원",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryUsageList(categoryUsages: List<CategoryUsage>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        items(categoryUsages) { categoryUsage ->
            CategoryUsageItem(categoryUsage)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CategoryUsageItem(categoryUsage: CategoryUsage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 카테고리 이름과 퍼센트
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryUsage.category,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Text(
                    text = "전체 소비의 ${categoryUsage.percentage.toInt()}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D2A57)
                )
            }
            
            // 진행 바
            LinearProgressIndicator(
                progress = { categoryUsage.percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF2D2A57),
                trackColor = Color(0xFFE0E0E0)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                color = Color(0xFFE0E0E0)
            )
            
            // 소비 금액과 혜택
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "소비 금액",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = "${categoryUsage.usageAmount}원",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "받은 혜택",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = "${categoryUsage.benefit}원",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

// 데이터 클래스
data class CardUsage(
    val cardImage: Int,
    val cardName: String,
    val usageAmount: Int,
    val benefit: Int,
    val annualFee: Int
)

data class CategoryUsage(
    val category: String,
    val percentage: Float,
    val usageAmount: Int,
    val benefit: Int
)

@Preview(showBackground = true)
@Composable
fun HomeDetailScreenPreview() {
    HomeDetailScreen()
}
