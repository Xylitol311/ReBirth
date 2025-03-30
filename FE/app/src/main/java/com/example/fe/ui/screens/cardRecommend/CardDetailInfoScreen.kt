package com.example.fe.ui.screens.cardRecommend

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.R
import com.example.fe.ui.components.backgrounds.GlassSurface
import com.example.fe.ui.components.backgrounds.StarryBackground
import com.example.fe.ui.components.navigation.TopBar
import kotlinx.coroutines.launch

@Composable
fun CardDetailInfoScreen(
    card: CardInfo,
    onBackClick: () -> Unit
) {
    // 화면 스크롤 상태
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // 스크롤 위치에 따라 '위로 가기' 버튼 표시 여부 결정
    val showScrollToTopButton by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 200
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 배경
        StarryBackground(
            scrollOffset = 0f,
            starCount = 150,
            modifier = Modifier.fillMaxSize()
        ) {
            // 배경만 표시
        }
        
        // 메인 콘텐츠
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 상단 앱바
            TopBar(
                title = "카드 상세 정보",
                showBackButton = true,
                onBackClick = onBackClick
            )
            
            // 스크롤 가능한 콘텐츠
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize()
            ) {
                // 카드 이미지 섹션
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.card),
                            contentDescription = card.name,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.height(240.dp)
                        )
                    }
                }
                
                // 카드 이름과 회사
                item {
                    GlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        cornerRadius = 16f
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = card.name,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = card.company,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                
                // 카드 혜택
                item {
                    GlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        cornerRadius = 16f
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "주요 혜택",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            // 혜택 목록
                            card.benefits.forEachIndexed { index, benefit ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 번호 원형 표시
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x40FFFFFF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    Text(
                                        text = benefit,
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                }
                                
                                // 마지막 항목이 아니면 구분선 추가
                                if (index < card.benefits.size - 1) {
                                    Divider(
                                        color = Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.padding(start = 48.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 카드 정보
                item {
                    GlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        cornerRadius = 16f
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "카드 정보",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            // 연회비
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "연회비",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                
                                Text(
                                    text = card.annualFee,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Divider(
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            // 전월 실적
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "전월 실적",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                
                                Text(
                                    text = card.minSpending,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                // 안내사항
                item {
                    GlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        cornerRadius = 16f
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "알아두세요",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            Text(
                                text = "• 카드 혜택은 전월 실적 충족 시 적용됩니다.",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            
                            Text(
                                text = "• 할인/적립 혜택은 월 한도 내에서 제공됩니다.",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            
                            Text(
                                text = "• 자세한 내용은 카드사 홈페이지를 참고해주세요.",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
                
                // 하단 여백
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        
        // 위로 가기 버튼
        if (showScrollToTopButton) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(0)
                    }
                },
                containerColor = Color(0xFF3F51B5),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "위로 가기"
                )
            }
        }
    }
} 