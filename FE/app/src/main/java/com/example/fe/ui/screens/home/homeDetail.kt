package com.example.fe.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import com.example.fe.ui.components.backgrounds.GlassSurface
import androidx.compose.foundation.clickable

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

    Box(modifier = Modifier.fillMaxSize()) {
        StarryBackground(
            scrollOffset = 0f,
            starCount = 150,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 총 사용 금액 카드 - isTopPanel = true로 설정
                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 16.dp),
                    cornerRadius = 16f,
                    isTopPanel = true  // 상단 패널임을 명시
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "총 사용 금액",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "125,000원",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "받은 혜택 6,000원",
                            fontSize = 16.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
                
                // 탭 선택
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    tabs.forEachIndexed { index, title ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTabIndex = index },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = title,
                                color = if (selectedTabIndex == index) Color.White else Color.Gray,
                                fontSize = 16.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            // 인디케이터
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(2.dp)
                                    .background(
                                        color = if (selectedTabIndex == index) Color.White else Color.Transparent
                                    )
                            )
                        }
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
    LazyColumn {
        items(cardUsages) { cardUsage ->
            CardUsageItem(cardUsage)
        }
    }
}

@Composable
fun CardUsageItem(cardUsage: CardUsage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 카드 이미지 - 높이를 동적으로 조정하기 위해 AspectRatio 사용
        Box(
            modifier = Modifier
                .width(80.dp)  // 너비 줄임
                .aspectRatio(0.63f)  // 카드 비율 (가로:세로 = 1:1.6)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Image(
                painter = painterResource(id = cardUsage.cardImage),
                contentDescription = "Card Image",
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF6200EE)), // 보라색 배경 (카드에 맞게)
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 글래스 서피스는 크기 유지
        GlassSurface(
            modifier = Modifier.weight(1f),
            cornerRadius = 12f
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = cardUsage.cardName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "사용 금액",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${cardUsage.usageAmount}원",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "받은 혜택",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
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
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${cardUsage.annualFee}원",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryUsageList(categoryUsages: List<CategoryUsage>) {
    LazyColumn {
        items(categoryUsages) { categoryUsage ->
            CategoryUsageItem(categoryUsage)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CategoryUsageItem(categoryUsage: CategoryUsage) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16f
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
                    color = Color.White
                )
                
                Text(
                    text = "전체 소비의 ${categoryUsage.percentage.toInt()}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            // 진행 바
            LinearProgressIndicator(
                progress = { categoryUsage.percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF00E1FF),
                trackColor = Color(0x33FFFFFF)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0x33FFFFFF)
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
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = "${categoryUsage.usageAmount}원",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "받은 혜택",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
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
