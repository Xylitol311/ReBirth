package com.example.fe.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fe.R
import com.example.fe.ui.components.backgrounds.StarryBackground
import com.example.fe.ui.navigation.NavRoutes
import com.example.fe.ui.screens.home.components.HomeRecCard
import com.example.fe.ui.screens.home.components.HomeTransaction
import com.example.fe.ui.screens.home.components.HomeUsedMoney

val LightBlue = Color(0xFFADD8E6)

@Composable
fun HomeHeader(
    userName: String = "김싸피님",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        // 행성 이미지를 오른쪽 상단에 배치하고 위로 올림
        Image(
            painter = painterResource(id = R.drawable.earth),
            contentDescription = "Earth",
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(y = (-20).dp) // padding 대신 offset 사용
        )
        
        // 텍스트를 왼쪽 하단에 배치하고 아래로 내림
        Column(
            modifier = Modifier
                .padding(top = 60.dp)
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
    navController: NavController,
    modifier: Modifier = Modifier,
    onScrollOffsetChange: (Float) -> Unit = {} // 파라미터 추가
) {
    val viewModel = remember { HomeViewModel() }

    val scrollState = rememberScrollState()
    var scrollOffset by remember { mutableStateOf(0f) }

    // 스크롤 오프셋 변경 감지
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value.toFloat() }.collect { offset ->
            onScrollOffsetChange(offset)  // 콜백 호출
        }
    }


    StarryBackground(scrollOffset = scrollOffset) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 헤더 (사용자 이름과 행성)
            HomeHeader()

            // 이번 달 소비 금액
            HomeUsedMoney(
                onDetailClick = {
                    navController.navigate(NavRoutes.HOME_DETAIL)
                },
                viewModel = viewModel
            )

            // 혜택을 놓친 거래 내역
            HomeTransaction()

            // 추천 카드
            HomeRecCard()
        }

    }



    data class Quadruple<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )

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
}