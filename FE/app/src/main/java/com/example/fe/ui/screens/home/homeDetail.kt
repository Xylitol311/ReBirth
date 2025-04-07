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
    
    // 임시 카테고리 데이터 추가
    val tempCategoryList = listOf(
        TempCategorySummary("카페", 33, 50000, 500),
        TempCategorySummary("음식점", 33, 50000, 500),
        TempCategorySummary("지하철", 33, 50000, 500),
        TempCategorySummary("영화", 33, 50000, 500)
    )
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (isNavigatingBack.value) 0f else 1f,
        animationSpec = tween(300),
        label = "contentAlpha"
    )

    // API 호출
    LaunchedEffect(Unit) {
        viewModel.fetchSummaryData()
    }

    // StarryBackground 대신 단색 배경 적용
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A1931)) // 어두운 파란색 배경으로 통일
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 탭 선택 - 더 가깝게 배치
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                // 탭 버튼들을 중앙에 배치하고 간격 줄임
                tabs.forEachIndexed { index, title ->
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .clickable { selectedTabIndex = index },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,
                            color = if (selectedTabIndex == index) Color(0xFF00E1FF) else Color.Gray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        // 선택된 탭 아래 밑줄
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(2.dp)
                                .background(
                                    color = if (selectedTabIndex == index) Color(0xFF00E1FF) else Color.Transparent
                                )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 탭 내용
            when (selectedTabIndex) {
                0 -> CardUsageList(cardList)
                1 -> CategoryUsageList(categoryList)
            }
        }
    }
}

@Composable
fun CardUsageList(cardUsages: List<CardSummary>) {
    if (cardUsages.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "기록이 없습니다",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    } else {
        LazyColumn {
            items(cardUsages) { cardUsage ->
                CardUsageItem(cardUsage)
                
                // 카드 사이에 구분선 추가 (마지막이 아니면)
                if (cardUsage != cardUsages.last()) {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        color = Color(0xFF1E2D4D),  // 사진과 비슷한 색상
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

@Composable
fun CardUsageItem(cardUsage: CardSummary) {
    // 사진에 맞게 어두운 파란색 배경 추가
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A1931))  // 어두운 파란색 배경
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 24.dp, top = 16.dp, bottom = 16.dp), // 왼쪽 패딩 감소
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 카드 이미지 - 크기 증가 및 90도 회전
            AsyncImage(
                model = cardUsage.cardImgUrl,
                contentDescription = "Card Image",
                modifier = Modifier
                    .width(120.dp) // 80dp에서 120dp로 증가
                    .aspectRatio(0.8f) // 비율 조정
                    .clip(RoundedCornerShape(12.dp))
                    .graphicsLayer {
                        rotationZ = 90f  // 시계 방향으로 90도 회전
                    },
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(20.dp)) // 간격 약간 감소

            // 카드 정보 (GlassSurface 제거)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = cardUsage.cardName,
                    fontSize = 22.sp, // 폰트 크기 증가
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
                        fontSize = 18.sp, // 폰트 크기 증가
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${cardUsage.spendingAmount}원",
                        fontSize = 18.sp, // 폰트 크기 증가
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
                        fontSize = 18.sp, // 폰트 크기 증가
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${cardUsage.benefitAmount}원",
                        fontSize = 18.sp, // 폰트 크기 증가
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E1FF)  // 혜택 색상을 밝은 파란색으로 변경
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "연회비",
                        fontSize = 18.sp, // 폰트 크기 증가
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${cardUsage.annualFee}원",
                        fontSize = 18.sp, // 폰트 크기 증가
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun TempCategoryUsageList(categoryUsages: List<TempCategorySummary>) {
    if (categoryUsages.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "기록이 없습니다",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    } else {
        LazyColumn {
            items(categoryUsages) { categoryUsage ->
                TempCategoryUsageItem(categoryUsage)
                
                // 카테고리 사이에 구분선 추가 (마지막이 아니면)
                if (categoryUsage != categoryUsages.last()) {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = Color(0xFF1E2D4D),  // 사진과 비슷한 색상
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

@Composable
fun TempCategoryUsageItem(categoryUsage: TempCategorySummary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 24.dp)
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
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E1FF)
            )
            
            Text(
                text = "${categoryUsage.percentage}%",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E1FF)
            )
        }
        
        // 소비 금액
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "소비금액",
                fontSize = 18.sp,
                color = Color.White
            )
            
            Text(
                text = "${categoryUsage.amount}원",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
        
        // 받은 혜택
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "받은 혜택",
                fontSize = 18.sp,
                color = Color.White
            )
            
            Text(
                text = "${categoryUsage.benefit}원",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
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
            Text(
                text = "기록이 없습니다",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    } else {
        LazyColumn {
            items(categoryUsages) { categoryUsage ->
                CategoryUsageItem(categoryUsage)
                
                // 카테고리 사이에 구분선 추가 (마지막이 아니면)
                if (categoryUsage != categoryUsages.last()) {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = Color(0xFF1E2D4D),  // 사진과 비슷한 색상
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryUsageItem(categoryUsage: CategorySummary) {
    // 임시 컴포넌트와 같은 스타일로 변경
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 24.dp)  // 임시 컴포넌트와 동일한 패딩
    ) {
        // 카테고리 이름과 퍼센트 (값 임의 설정)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoryUsage.category,
                fontSize = 28.sp,  // 임시 컴포넌트와 동일한 크기
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E1FF)  // 밝은 파란색으로 변경
            )
            
            // 퍼센트 임의 설정 (API 응답에 없으면 33%로 고정)
            Text(
                text = "33%",
                fontSize = 28.sp,  // 임시 컴포넌트와 동일한 크기
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E1FF)  // 밝은 파란색으로 변경
            )
        }
        
        // 소비 금액
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "소비금액",
                fontSize = 18.sp,  // 임시 컴포넌트와 동일한 크기
                color = Color.White
            )
            
            Text(
                text = "${categoryUsage.amount}원",
                fontSize = 20.sp,  // 임시 컴포넌트와 동일한 크기
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
        
        // 받은 혜택
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "받은 혜택",
                fontSize = 18.sp,  // 임시 컴포넌트와 동일한 크기
                color = Color.White
            )
            
            Text(
                text = "${categoryUsage.benefit}원",
                fontSize = 20.sp,  // 임시 컴포넌트와 동일한 크기
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
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

// 임시 카테고리 데이터 클래스
data class TempCategorySummary(
    val category: String,
    val percentage: Int,
    val amount: Int,
    val benefit: Int
)

@Preview(showBackground = true)
@Composable
fun HomeDetailScreenPreview() {
    HomeDetailScreen()
}
