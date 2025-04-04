package com.example.fe.ui.screens.home

import android.util.Log
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
import com.example.fe.config.AppConfig
import com.example.fe.data.network.CardSummary
import com.example.fe.data.network.CategorySummary
import com.example.fe.data.network.SummaryService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import coil.compose.AsyncImage

@Composable
fun HomeDetailScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("카드별", "카테고리별")
    val coroutineScope = rememberCoroutineScope()
    val isNavigatingBack = remember { mutableStateOf(false) }
    
    val viewModel = remember { HomeDetailViewModel() }
    val cardList by viewModel.cardList.collectAsState()
    val categoryList by viewModel.categoryList.collectAsState()
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (isNavigatingBack.value) 0f else 1f,
        animationSpec = tween(300),
        label = "contentAlpha"
    )

    // API 호출
    LaunchedEffect(Unit) {
        viewModel.fetchSummaryData()
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
                // 총 사용 금액 카드
                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 16.dp),
                    cornerRadius = 16f,
                    isTopPanel = true
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
                            text = "${cardList.sumOf { it.spendingAmount }}원",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "받은 혜택 ${cardList.sumOf { it.benefitAmount }}원",
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
                    0 -> CardUsageList(cardList)
                    1 -> CategoryUsageList(categoryList)
                }
            }
        }
    }
}

@Composable
fun CardUsageList(cardUsages: List<CardSummary>) {
    if (cardUsages.isEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(96.dp))  // 이미지 공간만큼 여백

            GlassSurface(
                modifier = Modifier.weight(1f),
                cornerRadius = 12f
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "기록이 없습니다",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    } else {
        LazyColumn {
            items(cardUsages) { cardUsage ->
                CardUsageItem(cardUsage)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CardUsageItem(cardUsage: CardSummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 카드 이미지
        AsyncImage(
            model = cardUsage.cardImgUrl,
            contentDescription = "Card Image",
            modifier = Modifier
                .width(80.dp)
                .aspectRatio(0.63f)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.width(16.dp))

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
                        text = "${cardUsage.spendingAmount}원",
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
                        text = "${cardUsage.benefitAmount}원",
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
fun CategoryUsageList(categoryUsages: List<CategorySummary>) {
    if (categoryUsages.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassSurface(
                modifier = Modifier.wrapContentSize(),
                cornerRadius = 16f
            ) {
                Text(
                    text = "기록이 없습니다",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
    } else {
        LazyColumn {
            items(categoryUsages) { categoryUsage ->
                CategoryUsageItem(categoryUsage)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CategoryUsageItem(categoryUsage: CategorySummary) {
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
            }
            
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
                        text = "${categoryUsage.amount}원",
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
