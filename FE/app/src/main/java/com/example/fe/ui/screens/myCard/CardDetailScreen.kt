package com.example.fe.ui.screens.myCard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.R
import androidx.compose.foundation.layout.offset
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.delay
import com.example.fe.ui.components.backgrounds.StarryBackground

data class TransactionItem(
    val date: String,
    val time: String,
    val storeName: String,
    val amount: String,
    val category: String,
    val isCredit: Boolean = true
)

data class BenefitItem(
    val category: String,
    val percentage: String,
    val limit: String,
    val used: String,
    val total: String
)

data class TabItem(
    val title: String,
    val icon: ImageVector
)

@Composable
fun CardDetailScreen(
    cardItem: CardItem,
    onBackClick: () -> Unit
) {
    var selectedMonth by remember { mutableIntStateOf(3) } // 3월로 초기화
    var selectedTab by remember { mutableIntStateOf(0) } // 0: 내역, 1: 혜택
    
    // 애니메이션 시작 상태
    var animationStarted by remember { mutableStateOf(false) }
    
    // 요소별 표시 상태
    var showHeader by remember { mutableStateOf(false) }
    var showMonthSelector by remember { mutableStateOf(false) }
    var showCardName by remember { mutableStateOf(false) }
    var showTabs by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    
    // 애니메이션 시작 - 지연 적용
    LaunchedEffect(key1 = true) {
        // 처음에는 애니메이션 시작하지 않음 (카드가 원래 위치에서 시작)
        delay(100) // 화면이 보이고 약간의 지연 후 애니메이션 시작
        animationStarted = true
        delay(700) // 카드 애니메이션이 완료될 시간을 기다림
        showHeader = true
        delay(100)
        showMonthSelector = true
        delay(100)
        showCardName = true
        delay(100)
        showTabs = true
        delay(100)
        showContent = true
    }
    
    // 네비게이션 바 숨기기 효과 전달
    LaunchedEffect(key1 = Unit) {
        // 화면이 그려질 때 네비게이션 바 숨기기
        // 여기서는 함수가 없으므로 주석으로 표시합니다.
        // 실제로는 AppNavigation.kt에서 이 화면에 진입할 때 네비게이션 바를 숨기도록 
        // 설정해야 합니다.
    }

    // 카드 크기 조정 (비율 유지)
    val cardScale by animateFloatAsState(
        targetValue = if (animationStarted) 0.9f else 1.0f,
        animationSpec = tween(600, easing = EaseOutQuart),
        label = "cardScale"
    )

    // 카드 위치 이동 효과 (초기에는 화면 하단에서 시작, 그 다음 상단으로 이동)
    val cardYOffset by animateFloatAsState(
        targetValue = if (animationStarted) 320f else 1300f,
        animationSpec = tween(700, easing = EaseInOut),
        label = "cardYOffset"
    )

    // 카드 회전 효과 (세로에서 가로로)
    val cardRotation by animateFloatAsState(
        targetValue = if (animationStarted) 0f else 90f,
        animationSpec = tween(700, easing = EaseInOut),
        label = "cardRotation"
    )

    // 배경 투명도 효과
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.7f,
        animationSpec = tween(700),
        label = "backgroundAlpha"
    )

    // 샘플 거래 데이터
    val transactions = remember {
        listOf(
            TransactionItem("03.13(월)", "09:34", "토스", "5000원", "카페", true),
            TransactionItem("03.12(일)", "15:34", "다이소", "5000원", "쇼핑", true),
            TransactionItem("03.10(금)", "19:34", "롯데리아", "3000원", "음식점", true),
            TransactionItem("03.10(금)", "12:34", "세븐일레븐", "2000원", "편의점", false)
        )
    }

    // 샘플 혜택 데이터
    val benefits = remember {
        listOf(
            BenefitItem("롯데리아", "1.2% 할인", "전액 : 5000원", "5000원 남음", ""),
            BenefitItem("소칼", "2% 할인", "전액 : 10000원", "", "")
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = backgroundAlpha
            }
            .background(Color(0xFF0F0F1E))
    ) {
        // 배경
        StarryBackground(
            scrollOffset = 0f,
            starCount = 150,
            horizontalOffset = 0f,
            modifier = Modifier.fillMaxSize()
        ) {
            // 빈 Box - 배경만 표시
        }
        
        // 상단 UI 요소들
        Column(modifier = Modifier.fillMaxSize()) {
            // 상단 앱바
            AnimatedVisibility(
                visible = showHeader,
                enter = fadeIn(animationSpec = tween(300)) + 
                       slideInVertically(animationSpec = tween(300)) { -40 }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color.White,
                        modifier = Modifier
                            .clickable { onBackClick() }
                            .size(24.dp)
                    )
    
                    Spacer(modifier = Modifier.width(16.dp))
    
                    Text(
                        text = "카드 상세 정보",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
    
                    // 우측 여백을 위한 빈 공간
                    Spacer(modifier = Modifier.width(40.dp))
                }
            }

            // 월 선택 네비게이터
            AnimatedVisibility(
                visible = showMonthSelector,
                enter = fadeIn(animationSpec = tween(300)) + 
                       slideInVertically(animationSpec = tween(300)) { -40 }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "이전 달",
                        tint = Color.White,
                        modifier = Modifier
                            .clickable {
                                if (selectedMonth > 1) selectedMonth--
                            }
                            .size(30.dp)
                    )
    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedMonth}월",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
    
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "다음 달",
                        tint = Color.White,
                        modifier = Modifier
                            .clickable {
                                if (selectedMonth < 12) selectedMonth++
                            }
                            .size(30.dp)
                    )
                }
            }

            // 카드를 위한 빈 공간 (애니메이션이 완료된 후의 위치)
            Spacer(modifier = Modifier.height(200.dp))

            // 카드 이름
            AnimatedVisibility(
                visible = showCardName,
                enter = fadeIn(animationSpec = tween(300)) + 
                       slideInVertically(animationSpec = tween(300)) { 40 }
            ) {
                Text(
                    text = cardItem.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            // 내역/혜택 탭
            AnimatedVisibility(
                visible = showTabs,
                enter = fadeIn(animationSpec = tween(300)) + 
                       slideInVertically(animationSpec = tween(300)) { 40 }
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    indicator = {},
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "내역",
                                color = if (selectedTab == 0) Color.White else Color.Gray,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
    
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "혜택",
                                color = if (selectedTab == 1) Color.White else Color.Gray,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // 내역/혜택 내용
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(300)) + 
                       slideInVertically(animationSpec = tween(300)) { 40 }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // 남은 공간 모두 차지
                ) {
                    when (selectedTab) {
                        0 -> TransactionsContent(transactions)
                        1 -> BenefitsContent(benefits)
                    }
                }
            }
        }
        
        // 카드 이미지 - UI와 분리된 절대 위치에 배치
        Card(
            modifier = Modifier
                .width(250.dp)
                .height(150.dp) // 가로 방향 카드 높이
                .align(Alignment.TopCenter) // 상단 중앙 정렬
                .graphicsLayer(
                    scaleX = cardScale,
                    scaleY = cardScale,
                    translationY = cardYOffset, // 화면 하단에서 시작해서 상단으로 이동
                    rotationZ = cardRotation // 카드 회전 (세로 -> 가로)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF673AB7) // 보라색 카드
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.card),
                    contentDescription = "카드 이미지",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun TransactionsContent(transactions: List<TransactionItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 총액 정보
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "총 소비",
                color = Color.White,
                fontSize = 14.sp
            )

            Text(
                text = "15000원",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "혜택 합은 금액",
                color = Color.White,
                fontSize = 12.sp
            )

            Text(
                text = "250원",
                color = Color(0xFFCCFF00), // 연두색
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 프로그레스 바
        LinearProgressIndicator(
            progress = { 0.5f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = Color.White,
            trackColor = Color.Gray.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 거래 내역 리스트
        LazyColumn {
            items(transactions) { transaction ->
                TransactionItemView(transaction = transaction)
                
                if (transaction != transactions.last()) {
                    Divider(
                        color = Color.Gray.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BenefitsContent(benefits: List<BenefitItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 적립/사용액 정보
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "적립 / 사용액",
                color = Color.White,
                fontSize = 14.sp
            )

            Text(
                text = "15000원 / 15000원",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "다음 구간까지",
                color = Color.White,
                fontSize = 12.sp
            )

            Text(
                text = "5000원 남음",
                color = Color.White,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 프로그레스 바
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = { 0.7f },
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp),
                color = Color.White,
                trackColor = Color.Gray.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "2",
                    color = Color(0xFF191970),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 1구간 혜택
        Text(
            text = "1구간 혜택",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 혜택 리스트
        for (benefit in benefits) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = benefit.category,
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    Text(
                        text = benefit.percentage,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (benefit.limit.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = benefit.limit,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )

                        if (benefit.used.isNotEmpty()) {
                            Text(
                                text = benefit.used,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Divider(
                    color = Color.Gray.copy(alpha = 0.3f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun TransactionItemView(transaction: TransactionItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 날짜/시간 정보
        Column(
            modifier = Modifier.width(90.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = transaction.date,
                color = Color.LightGray,
                fontSize = 12.sp
            )
            
            Text(
                text = transaction.time,
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }
        
        // 거래 정보
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction.storeName,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = transaction.category,
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }
        
        // 금액 정보
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = transaction.amount,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = if (transaction.isCredit) "신용" else "체크",
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun BenefitCard(
    title: String,
    description: String,
    highlight: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        ),
        border = BorderStroke(1.dp, color)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 컬러 마커
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "혜택",
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 혜택 텍스트
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = description,
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
                
                Text(
                    text = highlight,
                    color = color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryBar(
    category: String,
    amount: Int,
    color: Color,
    ratio: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category,
                color = Color.White,
                fontSize = 14.sp
            )
            
            Text(
                text = "${amount / 10000}만원",
                color = Color.White,
                fontSize = 14.sp
            )
        }
        
        // 막대 그래프
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .padding(vertical = 4.dp)
                .background(Color(0xFF2D2D3A), RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .height(12.dp)
                    .background(color, RoundedCornerShape(6.dp))
            )
        }
    }
}

@Composable
fun MonthBar(
    month: String,
    amount: Int,
    color: Color,
    ratio: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = month,
                color = Color.White,
                fontSize = 14.sp
            )
            
            Text(
                text = "${amount / 10000}만원",
                color = Color.White,
                fontSize = 14.sp
            )
        }
        
        // 막대 그래프
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .padding(vertical = 4.dp)
                .background(Color(0xFF2D2D3A), RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .height(12.dp)
                    .background(color, RoundedCornerShape(6.dp))
            )
        }
    }
}

@Composable
fun ServiceItem(title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "서비스",
                tint = Color(0xFF6200EE)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = description,
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.LightGray,
            fontSize = 14.sp
        )
        
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp
        )
    }
    
    Divider(
        color = Color(0xFF2D2D3A),
        modifier = Modifier.padding(vertical = 2.dp)
    )
}