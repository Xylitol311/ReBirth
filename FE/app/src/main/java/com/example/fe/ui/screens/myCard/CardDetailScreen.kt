package com.example.fe.ui.screens.myCard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.blur
import kotlinx.coroutines.delay
import com.example.fe.ui.components.backgrounds.StarryBackground
import com.example.fe.ui.components.backgrounds.GlassSurface
import kotlin.math.sin
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.indication
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.HorizontalDivider

data class TransactionItem(
    val date: String,
    val time: String,
    val place: String,
    val amount: String,
    val category: String,
    val isApproved: Boolean = true,
    val benefitAmount: String = "혜택 0원"
)

data class BenefitItem(
    val storeName: String,
    val percentage: String,
    val totalAmount: String,
    val remainingAmount: String,
    val usedAmount: String,
    val progressRate: Float = 0.3f
)

data class TabItem(
    val title: String,
    val icon: ImageVector
)

@Composable
fun CardDetailScreen(
    cardItem: CardItem,
    onBackClick: () -> Unit,
    onNavigationBarVisibilityChange: (Boolean) -> Unit = {}
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
        // 화면이 그려질 때 네비게이션 바 숨기기 함수 호출
        onNavigationBarVisibilityChange(false)
    }

    // 화면이 종료될 때 네비게이션 바 다시 표시
    DisposableEffect(key1 = Unit) {
        onDispose {
            onNavigationBarVisibilityChange(true)
        }
    }

    // 카드 크기 조정 (비율 유지)
    val cardScale by animateFloatAsState(
        targetValue = if (animationStarted) 0.85f else 1.3f,
        animationSpec = tween(600, easing = EaseOutQuart),
        label = "cardScale"
    )

    // 카드 위치 이동 효과 (초기에는 화면 하단에서 시작, 그 다음 상단으로 이동)
    val cardYOffset by animateFloatAsState(
        targetValue = if (animationStarted) 200f else 1850f,
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
            TransactionItem("03.13(월)", "09:34", "카페", "5000원", "카페", true, "혜택 100원"),
            TransactionItem("03.12(일)", "15:34", "다이소", "5000원", "쇼핑", true, "혜택 100원"),
            TransactionItem("03.10(금)", "19:34", "롯데리아", "3000원", "음식점", true, "혜택 50원"),
            TransactionItem("03.10(금)", "12:34", "세븐일레븐", "2000원", "편의점", false)
        )
    }

    // 샘플 혜택 데이터
    val benefits = remember {
        listOf(
            BenefitItem("롯데리아", "1.2% 할인", "전액 : 5000원", "잔여 : 5000원", "50원", 0.3f),
            BenefitItem("소칼", "2% 할인", "전액 : 10000원", "잔여 : 10000원", "200원", 0.5f)
        )
    }

    // 탑바의 알파값 애니메이션
    val topBarAlpha by animateFloatAsState(
        targetValue = if (showHeader) 1f else 0f,
        animationSpec = tween(300),
        label = "topBarAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 콘텐츠 부분 - TopBar는 AppNavigation에서 관리됨
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp, bottom = 16.dp) // TopBar 높이 패딩 감소
        ) {
            // 월 선택 네비게이터
            AnimatedVisibility(
                visible = showMonthSelector,
                enter = fadeIn(animationSpec = tween(300)) + 
                       slideInVertically(animationSpec = tween(300)) { -40 }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 2.dp), // 상하 패딩 더 줄임
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
                            fontSize = 22.sp,
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
            Spacer(modifier = Modifier.height(125.dp)) // 약간 더 줄임

            // 카드 이름
            AnimatedVisibility(
                visible = showCardName,
                enter = fadeIn(animationSpec = tween(300)) + 
                       slideInVertically(animationSpec = tween(300)) { 40 }
            ) {
                Text(
                    text = cardItem.name,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 45.dp, bottom = 4.dp),
                    textAlign = TextAlign.Center
                )
            }

            // 내역/혜택 탭
            AnimatedVisibility(
                visible = showTabs,
                enter = fadeIn(animationSpec = tween(300)) + 
                       slideInVertically(animationSpec = tween(300)) { 40 }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 왼쪽 탭 (내역)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 0 },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "내역",
                            color = if (selectedTab == 0) Color.White else Color.Gray,
                            fontSize = 26.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        // 인디케이터
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(2.dp)
                                .align(Alignment.CenterHorizontally)
                                .background(
                                    color = if (selectedTab == 0) Color.White else Color.Transparent
                                )
                        )
                    }
                    
                    // 오른쪽 탭 (혜택)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 1 },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "혜택",
                            color = if (selectedTab == 1) Color.White else Color.Gray,
                            fontSize = 26.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        // 인디케이터
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(2.dp)
                                .align(Alignment.CenterHorizontally)
                                .background(
                                    color = if (selectedTab == 1) Color.White else Color.Transparent
                                )
                        )
                    }
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
                        .padding(top = 8.dp) // 상단 여백 추가
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
                .width(220.dp)
                .height(140.dp)
                .align(Alignment.TopCenter)
                .graphicsLayer(
                    scaleX = cardScale,
                    scaleY = cardScale,
                    translationY = cardYOffset,
                    rotationZ = cardRotation
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF673AB7)
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
    // 날짜별로 거래 내역 그룹화
    val transactionsByDate = transactions.groupBy { it.date }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 총 소비 및 혜택 금액 헤더
        item {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                cornerRadius = 16f,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)  // 패딩 값 키움 (16.dp -> 24.dp)
                ) {
                    // 총액 정보
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "총 소비",
                            color = Color.White,
                            fontSize = 26.sp  // 폰트 크기 더 증가
                        )
                        
                        Text(
                            text = "15000원",
                            color = Color.White,
                            fontSize = 26.sp,  // 폰트 크기 더 증가
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))  // 간격 약간 증가
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "혜택 받은 금액",
                            color = Color.White,
                            fontSize = 24.sp  // 폰트 크기 더 증가
                        )
                        
                        Text(
                            text = "250원",
                            color = Color(0xFFCCFF00), // 연두색
                            fontSize = 24.sp,  // 폰트 크기 더 증가
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // 날짜별로 거래 내역 표시
        transactionsByDate.forEach { (date, dateTransactions) ->
            item {
                // 날짜별 패널
                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    cornerRadius = 16f,
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)  // 패딩 값 키움 (16.dp -> 24.dp)
                    ) {
                        // 날짜 헤더
                        Text(
                            text = date,
                            color = Color.White,
                            fontSize = 20.sp,  // 폰트 크기 증가
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // 날짜에 해당하는 모든 거래 내역
                        dateTransactions.forEach { transaction ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 왼쪽: 시간
                                Text(
                                    text = transaction.time,
                                    color = Color.White,
                                    fontSize = 18.sp,  // 폰트 크기 증가
                                    modifier = Modifier.width(60.dp)
                                )
                                
                                // 중앙: 아이콘과 장소 (좌측 정렬)
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 아이콘
                                    val iconColor = when (transaction.category) {
                                        "카페" -> Color(0xFFFFD700) // 금색
                                        "쇼핑" -> Color(0xFFFFA500) // 주황색
                                        "음식점" -> Color(0xFFFF6347) // 토마토색
                                        "편의점" -> Color(0xFF00CED1) // 청록색
                                        else -> Color(0xFFFFD700) // 기본 금색
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(iconColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = transaction.category.take(1),
                                            color = Color.Black,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    // 장소
                                    Text(
                                        text = transaction.place,
                                        color = Color.White,
                                        fontSize = 20.sp,  // 폰트 크기 증가
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                // 오른쪽: 금액
                                Column(
                                    modifier = Modifier.width(90.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = transaction.amount,
                                        color = Color.White,
                                        fontSize = 20.sp,  // 폰트 크기 증가
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Text(
                                        text = transaction.benefitAmount,
                                        color = Color(0xFFCCFF00),
                                        fontSize = 18.sp  // 폰트 크기 증가
                                    )
                                }
                            }
                            
                            // 마지막 항목이 아니면 구분선 추가
                            if (transaction != dateTransactions.last()) {
                                HorizontalDivider(
                                    color = Color.Gray.copy(alpha = 0.2f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BenefitsContent(benefits: List<BenefitItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(benefits) { benefit ->
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                cornerRadius = 16f,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)  // 패딩 값 키움 (16.dp -> 24.dp)
                ) {
                    // 혜택 이름 및 퍼센트
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = benefit.storeName,
                            color = Color.White,
                            fontSize = 22.sp,  // 폰트 크기 증가
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = benefit.percentage,
                            color = Color.White,
                            fontSize = 22.sp,  // 폰트 크기 증가
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 혜택 프로그레스 바
                    val progress = benefit.progressRate
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)  // 프로그레스 바 높이 증가
                            .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(10.dp)  // 프로그레스 바 높이 증가
                                .background(Color(0xFF5F77F5), RoundedCornerShape(4.dp)) // 파란색 프로그레스 바
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 혜택 상세 금액
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 왼쪽에 사용한 혜택 금액
                        Text(
                            text = benefit.usedAmount,
                            color = Color.White,
                            fontSize = 20.sp  // 폰트 크기 증가
                        )
                        
                        // 오른쪽에 전체 금액
                        Text(
                            text = benefit.remainingAmount,
                            color = Color.White,
                            fontSize = 20.sp  // 폰트 크기 증가
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemView(transaction: TransactionItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 날짜/시간 정보
        Column(
            modifier = Modifier.width(100.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = transaction.date,
                color = Color.LightGray,
                fontSize = 18.sp
            )
            
            Text(
                text = transaction.time,
                color = Color.LightGray,
                fontSize = 18.sp
            )
        }
        
        // 거래 정보
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction.place,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = transaction.category,
                color = Color.LightGray,
                fontSize = 18.sp
            )
        }
        
        // 금액 정보
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = transaction.amount,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = if (transaction.isApproved) "승인" else "취소",
                color = Color.LightGray,
                fontSize = 18.sp
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
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        ),
        border = BorderStroke(1.dp, color)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 컬러 마커
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "혜택",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            // 혜택 텍스트
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = description,
                    color = Color.LightGray,
                    fontSize = 16.sp
                )
                
                Text(
                    text = highlight,
                    color = color,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
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
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category,
                color = Color.White,
                fontSize = 18.sp
            )
            
            Text(
                text = "${amount / 10000}만원",
                color = Color.White,
                fontSize = 18.sp
            )
        }
        
        // 막대 그래프
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .padding(vertical = 6.dp)
                .background(Color(0xFF2D2D3A), RoundedCornerShape(8.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .height(16.dp)
                    .background(color, RoundedCornerShape(8.dp))
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
            .padding(vertical = 10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "서비스",
                tint = Color(0xFF6200EE),
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = description,
                    color = Color.LightGray,
                    fontSize = 16.sp
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
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.LightGray,
            fontSize = 18.sp
        )
        
        Text(
            text = value,
            color = Color.White,
            fontSize = 18.sp
        )
    }
    
    HorizontalDivider(
        color = Color(0xFF2D2D3A),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}