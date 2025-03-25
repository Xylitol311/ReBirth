package com.example.fe.ui.screens.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    onScrollOffsetChange: (Float) -> Unit = {}
) {
    var scrollOffset by remember { mutableStateOf(0f) }
    
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
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            state = lazyListState
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "가계부",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "소비 내역을 확인하세요",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    
                    // 여기에 가계부 내용 추가
                }
                
                Spacer(modifier = Modifier.height(80.dp)) // 하단 여백
            }
        }
    }
} 