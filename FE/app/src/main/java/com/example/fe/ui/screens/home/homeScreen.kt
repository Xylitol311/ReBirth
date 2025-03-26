package com.example.fe.ui.screens.home

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.R
import com.example.fe.ui.components.backgrounds.StarryBackground
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animateFloatAsState
import kotlinx.coroutines.delay
import com.example.fe.ui.screens.home.components.HomeUsedMoney
import com.example.fe.ui.screens.home.components.HomeTransaction
import com.example.fe.ui.screens.home.components.HomeRecCard

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    onScrollOffsetChange: (Float) -> Unit = {},
    onNavigateToDetail: () -> Unit = {}
) {
    var scrollOffset by remember { mutableStateOf(0f) }
    
    // 코루틴 스코프 추가
    val coroutineScope = rememberCoroutineScope()
    
    // 스크롤 상태
    val lazyListState = rememberLazyListState()
    
    // 스크롤 오프셋 변경 감지
    LaunchedEffect(lazyListState) {
        snapshotFlow { 
            lazyListState.firstVisibleItemIndex * 1000f + 
            (lazyListState.firstVisibleItemScrollOffset.toFloat())
        }.collect { offset ->
            scrollOffset = offset
            onScrollOffsetChange(offset)
        }
    }
    
    // 화면 전환 애니메이션을 위한 상태
    val isNavigating = remember { mutableStateOf(false) }

    // 애니메이션 값
    val contentAlpha by animateFloatAsState(
        targetValue = if (isNavigating.value) 0f else 1f,
        animationSpec = tween(300),
        label = "contentAlpha"
    )
    
    // 배경과 콘텐츠를 함께 배치
    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 (스크롤에 따라 움직임)
        StarryBackground(
            scrollOffset = scrollOffset,
            starCount = 150,
            modifier = Modifier.fillMaxSize()
        ) {
            // 빈 Box - 배경만 표시
        }
        
        // 실제 스크롤 가능한 콘텐츠
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .graphicsLayer(alpha = contentAlpha),
            state = lazyListState
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                // 지구 이미지와 메시지
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "당신의 행성에서는",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "어떤 소비가 이뤄졌을까요?",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Image(
                        painter = painterResource(id = R.drawable.earth),
                        contentDescription = "Earth",
                        modifier = Modifier.size(100.dp)
                    )
                }
                
                // 이번 달 소비 카드 (컴포넌트로 분리)
                HomeUsedMoney(
                    onDetailClick = {
                        // 페이드아웃 시작
                        isNavigating.value = true
                        // 약간의 지연 후 네비게이션
                        coroutineScope.launch {
                            delay(200)
                            onNavigateToDetail()
                        }
                    }
                )
                
                // 거래 내역 및 혜택 카드 (컴포넌트로 분리)
                HomeTransaction()
                
                // 카드 추천 섹션 (컴포넌트로 분리)
                HomeRecCard()
                
                // 하단 여백
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreenContent()
}

@Composable
fun CategorySpendingItem(
    category: String,
    amount: Int,
    benefit: Int = 0,
    isGoodTab: Boolean = false,
    iconResId: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 아이콘과 카테고리명 그룹
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 카테고리 아이콘
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = category,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            
            // 카테고리명
            Text(
                text = category,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        // 금액과 혜택 정보 그룹
        Column(
            horizontalAlignment = Alignment.End
        ) {
            // 소비 금액
            Text(
                text = "${amount}원",
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
            
            // 혜택 정보
            if (isGoodTab) {
                // 잘함 탭 - 받은 혜택 표시 (파란색)
                Text(
                    text = "${benefit}원 혜택",
                    fontSize = 14.sp,
                    color = Color(0xFF4285F4), // 파란색
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                // 아쉬움 탭 - 놓친 혜택 표시 (빨간색)
                Text(
                    text = "혜택 0원",
                    fontSize = 14.sp,
                    color = Color(0xFFFF5252), // 빨간색
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CategorySpendingList(selectedTabIndex: Int) {
    // 카테고리별 소비 데이터 (아이콘 리소스 ID 추가)
    val categories = listOf(
        Quadruple("카페", 45000, 2500, R.drawable.ic_coffee),
        Quadruple("식당", 32000, 1800, R.drawable.ic_restaurant),
        Quadruple("쇼핑", 25000, 1500, R.drawable.ic_shopping)
    )
    
    // 선택된 탭에 따라 다른 내용 표시
    if (selectedTabIndex == 0) {
        // 아쉬움 탭
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            categories.forEach { (category, amount, _, iconResId) ->
                CategorySpendingItem(
                    category = category,
                    amount = amount,
                    isGoodTab = false,
                    iconResId = iconResId
                )
            }
        }
    } else {
        // 잘함 탭
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            categories.forEach { (category, amount, benefit, iconResId) ->
                CategorySpendingItem(
                    category = category,
                    amount = amount,
                    benefit = benefit,
                    isGoodTab = true,
                    iconResId = iconResId
                )
            }
        }
    }
}

// 4개 값을 담는 데이터 클래스 추가
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)