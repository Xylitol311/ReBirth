package com.example.fe.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.example.fe.ui.components.cards.HorizontalCardLayout
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.gestures.ScrollableDefaults

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onScrollOffsetChange: (Float) -> Unit = {}
) {
    var scrollOffset by remember { mutableStateOf(0f) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    val selectedCardIndex = remember { mutableStateOf(0) }
    
    val defaultFlingBehavior = ScrollableDefaults.flingBehavior()
    
    // 스크롤 상태
    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    // 스크롤 오프셋 변경 감지 및 콜백 호출
    LaunchedEffect(lazyListState) {
        snapshotFlow { 
            lazyListState.firstVisibleItemIndex * 1000f + lazyListState.firstVisibleItemScrollOffset 
        }.collect { offset ->
            scrollOffset = offset
            onScrollOffsetChange(offset)
        }
    }
    
    StarryBackground(
        scrollOffset = scrollOffset,
        starCount = 150
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
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
                
                // 이번 달 소비 카드
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
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
                            text = "이번 달 소비",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "150,000원",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_right),
                                contentDescription = "Details",
                                tint = Color.Black,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(24.dp)
                            )
                        }
                        
                        Text(
                            text = "받은 혜택 1,000원",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF2D2A57))
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (selectedTabIndex == 0) Color.White else Color.Transparent
                                    )
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "아쉬움",
                                    color = if (selectedTabIndex == 0) Color(0xFF2D2A57) else Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    modifier = Modifier.clickable { selectedTabIndex = 0 }
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (selectedTabIndex == 1) Color.White else Color.Transparent
                                    )
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "잘함",
                                    color = if (selectedTabIndex == 1) Color(0xFF2D2A57) else Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    modifier = Modifier.clickable { selectedTabIndex = 1 }
                                )
                            }
                        }
                        
                        // 소비 카테고리 목록
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_coffee),
                                contentDescription = "Coffee",
                                tint = Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Text(
                                text = "카페",
                                fontSize = 16.sp,
                                color = Color.Black,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            )
                            
                            Text(
                                text = "50,000원",
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Text(
                            text = "500원 혜택",
                            fontSize = 12.sp,
                            color = Color.Blue,
                            modifier = Modifier.align(Alignment.End)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_restaurant),
                                contentDescription = "Restaurant",
                                tint = Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Text(
                                text = "음식점",
                                fontSize = 16.sp,
                                color = Color.Black,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            )
                            
                            Text(
                                text = "30,000원",
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Text(
                            text = "300원 혜택",
                            fontSize = 12.sp,
                            color = Color.Blue,
                            modifier = Modifier.align(Alignment.End)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_store),
                                contentDescription = "Store",
                                tint = Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Text(
                                text = "편의점",
                                fontSize = 16.sp,
                                color = Color.Black,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            )
                            
                            Text(
                                text = "20,000원",
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Text(
                            text = "200원 혜택",
                            fontSize = 12.sp,
                            color = Color.Blue,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
                
                // 거래 내역 및 혜택 카드
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
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
                        HorizontalCardLayout(
                            cardImage = painterResource(id = R.drawable.card),
                            height = 80.dp,
                            width = 140.dp,
                            cornerRadius = 8.dp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Image(
                            painter = painterResource(id = R.drawable.sad_emoji),
                            contentDescription = "Sad Emoji",
                            modifier = Modifier
                                .size(40.dp)
                                .offset(y = (-30).dp)
                        )
                        
                        Text(
                            text = "혜택을 놓쳤어요",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .offset(y = (-20).dp)
                        )
                        
                        Text(
                            text = "GS25지에스역삼점",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .offset(y = (-16).dp)
                        )
                        
                        Text(
                            text = "4,200원 결제",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .offset(y = (-12).dp)
                        )
                        
                        HorizontalDivider(
                            color = Color.LightGray,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        HorizontalCardLayout(
                            cardImage = painterResource(id = R.drawable.card),
                            height = 80.dp,
                            width = 140.dp,
                            cornerRadius = 8.dp,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )
                        
                        Text(
                            text = "삼성 IDONE 카드를 사용했다면",
                            fontSize = 14.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "420원",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            
                            Text(
                                text = "의 혜택을 볼 수도 있었어요",
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
                
                // 카드 추천 섹션 
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
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
                            text = "카드 추천",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black, 
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        
                        val cardWidth = 220.dp
                        val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
                        val horizontalPadding = 16.dp
                        val cardHorizontalPadding = 4.dp

                        
                        val startPadding = (screenWidth - cardWidth) / 2 - horizontalPadding - cardHorizontalPadding

                        val density = LocalDensity.current
                        val coroutineScope = rememberCoroutineScope()

                        
                        LaunchedEffect(lazyListState) {
                            snapshotFlow { lazyListState.firstVisibleItemIndex to lazyListState.firstVisibleItemScrollOffset }
                                .collect { (index, offset) ->
                                    
                                    if (!lazyListState.isScrollInProgress) {
                                        val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
                                        if (visibleItems.isNotEmpty()) {
                                            val itemWidth = visibleItems[0].size + with(density) { 16.dp.toPx() }
                                            
                                            
                                            var targetItem = if (offset > itemWidth / 2) {
                                                index + 1
                                            } else {
                                                index
                                            }
                                            
                                            val itemCount = lazyListState.layoutInfo.totalItemsCount
                                            targetItem = targetItem.coerceIn(0, itemCount - 1)
                                            
                                            
                                            selectedCardIndex.value = targetItem
                                            
                                            coroutineScope.launch {
                                                lazyListState.animateScrollToItem(targetItem)
                                            }
                                        }
                                    }
                                }
                        }

                        // 사용자 스크롤 감지 및 처리
                        DisposableEffect(lazyListState) {
                            val scrollListener = object : ScrollScope {
                                override fun scrollBy(pixels: Float): Float {
                                    // 스크롤 이벤트 처리
                                    return 0f
                                }
                            }
                            
                            onDispose {
                                // 나중에 리소스 정리
                            }
                        }

                        //커스텀 FlingBehavior 생성
                        val customFlingBehavior = remember(defaultFlingBehavior) {
                            object : FlingBehavior {
                                override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                                    // 스크롤 종료 시 스냅 효과 적용
                                    coroutineScope.launch {
                                        val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
                                        if (visibleItems.isNotEmpty()) {
                                            val itemWidth = visibleItems[0].size + with(density) { 16.dp.toPx() }
                                            val offset = lazyListState.firstVisibleItemScrollOffset
                                            
                                            var targetItem = if (offset > itemWidth / 2) {
                                                lazyListState.firstVisibleItemIndex + 1
                                            } else {
                                                lazyListState.firstVisibleItemIndex
                                            }
                                            
                                            val itemCount = lazyListState.layoutInfo.totalItemsCount
                                            targetItem = targetItem.coerceIn(0, itemCount - 1)
                                            
                                            selectedCardIndex.value = targetItem
                                            lazyListState.animateScrollToItem(targetItem)
                                        }
                                    }
                                    // 4. 미리 생성한 defaultFlingBehavior 사용
                                    return with(defaultFlingBehavior) { performFling(initialVelocity) }
                                }
                            }
                        }

                        androidx.compose.foundation.lazy.LazyRow(
                            state = lazyListState,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                start = startPadding,
                                end = startPadding
                            ),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            flingBehavior = customFlingBehavior
                        ) {
                            items(5) { index ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    HorizontalCardLayout(
                                        cardImage = painterResource(id = R.drawable.card),
                                        height = 140.dp,
                                        width = cardWidth,
                                        cornerRadius = 12.dp
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "카드 ${index + 1}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // 혜택 설명 텍스트는 현재 선택된 카드에 따라 변경
                        val benefitText = when (selectedCardIndex.value) {
                            0 -> "300원을 더 아낄 수 있어요!"
                            1 -> "500원의 추가 혜택이 있어요!"
                            2 -> "1,000원 캐시백을 받을 수 있어요!"
                            3 -> "포인트 2배 적립 혜택이 있어요!"
                            else -> "특별 할인 혜택이 있어요!"
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 혜택 설명 텍스트 (중앙 정렬)
                        Text(
                            text = "카페에서 이 카드를 사용한다면",
                            fontSize = 14.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = benefitText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp)) // 하단 여백
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}