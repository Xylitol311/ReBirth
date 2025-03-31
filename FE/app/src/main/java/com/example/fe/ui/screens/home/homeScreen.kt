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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fe.ui.navigation.NavRoutes
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState

val LightBlue = Color(0xFFADD8E6)

@Composable
fun HomeHeader(
    userName: String = "김싸피님",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 64.dp, vertical = 16.dp)
    ) {
        // 행성 이미지를 오른쪽 상단에 배치
        Image(
            painter = painterResource(id = R.drawable.earth),
            contentDescription = "Earth",
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.TopEnd)
        )
        
        // 텍스트를 왼쪽 하단에 배치
        Column(
            modifier = Modifier
                .padding(top = 40.dp)
                .align(Alignment.BottomStart)
        ) {
            Text(
                text = userName,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = LightBlue
            )
            Text(
                text = "당신의 행성에서는\n어떤 소비가 있었을까요?",
                fontSize = 24.sp,
                color = Color.White,
                lineHeight = 32.sp
            )
        }
    }
}

@Composable
fun HomeScreen(
    navController: NavController
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        StarryBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 헤더 (사용자 이름과 행성)
                HomeHeader()
                
                // 이번 달 소비 금액
                HomeUsedMoney(
                    onDetailClick = {
                        navController.navigate(NavRoutes.HOME_DETAIL)
                    }
                )
                
                // 혜택을 놓친 거래 내역
                HomeTransaction()
                
                // 추천 카드
                HomeRecCard()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController())
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
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = category,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = category,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${amount}원",
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
            
            if (isGoodTab) {
                Text(
                    text = "${benefit}원 혜택",
                    fontSize = 14.sp,
                    color = Color(0xFF4285F4),
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text(
                    text = "혜택 0원",
                    fontSize = 14.sp,
                    color = Color(0xFFFF5252),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CategorySpendingList(selectedTabIndex: Int) {
    val categories = listOf(
        Quadruple("카페", 45000, 2500, R.drawable.ic_coffee),
        Quadruple("식당", 32000, 1800, R.drawable.ic_restaurant),
        Quadruple("쇼핑", 25000, 1500, R.drawable.ic_shopping)
    )
    
    if (selectedTabIndex == 0) {
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

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)