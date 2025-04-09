package com.example.fe.ui.screens.cardRecommend

import android.util.Log
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
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
    viewModel: CardRecommendViewModel,
    cardId: Int,
    onBackClick: () -> Unit,
    navController: androidx.navigation.NavController? = null // 옵션으로 NavController 추가
) {
    // 화면이 표시될 때 카드 상세 정보 로드 - 오류 처리 개선
    LaunchedEffect(cardId) {
        try {
            viewModel.loadCardDetail(cardId)
            Log.d("CardDetailInfoScreen", "Loading card detail for id: $cardId")
        } catch (e: Exception) {
            Log.e("CardDetailInfoScreen", "Error loading card detail", e)
        }
    }

    // UI 상태 가져오기
    val uiState = viewModel.uiState
    val cardDetail = try {
        viewModel.getSelectedCardDetailForUI()
    } catch (e: Exception) {
        Log.e("CardDetailInfoScreen", "Error getting card detail UI", e)
        null
    }

    // 로딩 상태 처리
    if (uiState.isLoadingCardDetail) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // 오류 상태 처리
    if (uiState.errorCardDetail != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "오류: ${uiState.errorCardDetail}",
                    color = Color.Red
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onBackClick
                ) {
                    Text("뒤로 가기")
                }
            }
        }
        return
    }

    // 카드 정보가 없는 경우 처리
    if (cardDetail == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "카드 정보를 찾을 수 없습니다.",
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onBackClick
                ) {
                    Text("뒤로 가기")
                }
            }
        }
        return
    }

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
            // 뒤로가기 버튼 추가
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 뒤로가기 아이콘
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로 가기",
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onBackClick() }
                        .padding(4.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 화면 타이틀
                Text(
                    text = "카드 상세 정보",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
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
                        // 실제 카드 이미지가 있으면 사용, 없으면 기본 이미지 사용
                        if (cardDetail.cardImage.isNullOrBlank()) {
                            Image(
                                painter = painterResource(id = R.drawable.card),
                                contentDescription = cardDetail.name,
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier.height(240.dp)
                            )
                        } else {
                            // 네트워크 이미지 로드 (Coil 등의 라이브러리 사용)
                            // 여기서는 기본 이미지로 대체
                            Image(
                                painter = painterResource(id = R.drawable.card),
                                contentDescription = cardDetail.name,
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier.height(240.dp)
                            )
                        }
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
                                text = cardDetail.name,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = cardDetail.company,
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
                            cardDetail.benefits.forEachIndexed { index, benefit ->
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
                                if (index < cardDetail.benefits.size - 1) {
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
                                    text = cardDetail.annualFee,
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
                                    text = cardDetail.minSpending,
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
            AnimatedVisibility(
                visible = showScrollToTopButton,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                        .clickable {
                            coroutineScope.launch {
                                lazyListState.animateScrollToItem(0)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "맨 위로 가기",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}