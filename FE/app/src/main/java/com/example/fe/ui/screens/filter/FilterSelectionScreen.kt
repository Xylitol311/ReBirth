package com.example.fe.ui.screens.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import java.text.NumberFormat
import java.util.Locale

// 필터 선택 화면 컴포넌트
@Composable
fun FilterSelectionScreen(
    category: String,
    onClose: () -> Unit,
    onOptionSelected: (String, String) -> Unit
) {
    // 카테고리 표시 이름
    val categoryDisplayName = when(category) {
        "타입" -> "혜택 타입"
        else -> category
    }
    
    // 선택된 필터 옵션을 추적하기 위한 상태
    val selectedOptions = remember { mutableStateMapOf<String, Boolean>() }
    
    // 슬라이더 범위 상태
    val performanceRange = remember { mutableStateOf(0f..1000000f) }
    val annualFeeRange = remember { mutableStateOf(0f..20000f) }
    
    // 카테고리별 옵션 정의
    val options = when(category) {
        "타입" -> listOf("할인", "적립", "쿠폰")
        "카드사" -> listOf("농협", "IBK", "신한", "우리", "삼성")
        "카테고리" -> listOf("외식", "쇼핑", "교통", "여가", "의료", "통신", "교육")
        else -> emptyList()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E2649))
    ) {
        // 상단 바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            // 중앙 제목
            Text(
                text = "필터",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
            
            // X 버튼 (우측 상단)
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterEnd)
            ) {
                Text(
                    text = "✕",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }
        }
        
        // 필터 내용
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 110.dp, start = 16.dp, end = 16.dp, bottom = 80.dp)  // 상단 여백도 증가
                .verticalScroll(rememberScrollState())
        ) {
            // 혜택 타입 섹션
            FilterSection(
                title = "혜택 타입",
                options = listOf("할인", "적립", "쿠폰"),
                selectedOptions = selectedOptions,
                onOptionClick = { option, isSelected ->
                    selectedOptions["혜택 타입:$option"] = isSelected
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 카드사 섹션
            FilterSection(
                title = "카드사",
                options = listOf("농협", "IBK", "신한", "우리", "삼성"),
                selectedOptions = selectedOptions,
                onOptionClick = { option, isSelected ->
                    selectedOptions["카드사:$option"] = isSelected
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 카테고리 섹션
            FilterSection(
                title = "카테고리",
                options = listOf("외식", "쇼핑", "교통", "여가", "의료", "통신", "교육"),
                selectedOptions = selectedOptions,
                onOptionClick = { option, isSelected ->
                    selectedOptions["카테고리:$option"] = isSelected
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 전월 실적 슬라이더
            RangeSliderSection(
                title = "전월실적",
                currentRange = performanceRange.value,
                maxValue = 1000000f,
                onRangeChange = { newRange ->
                    performanceRange.value = newRange
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 연회비 슬라이더
            RangeSliderSection(
                title = "연회비",
                currentRange = annualFeeRange.value,
                maxValue = 20000f,
                onRangeChange = { newRange ->
                    annualFeeRange.value = newRange
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // 확인 버튼 (하단 고정)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color(0xFF1E2649))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 초기화 버튼
                Button(
                    onClick = {
                        // 모든 선택 옵션 초기화
                        selectedOptions.clear()
                        
                        // 슬라이더 값 초기화
                        performanceRange.value = 0f..1000000f
                        annualFeeRange.value = 0f..20000f
                    },
                    modifier = Modifier
                        .weight(4f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1E2649)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "초기화",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // 확인 버튼
                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .weight(6f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00D1FF),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "확인",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RangeSliderSection(
    title: String,
    currentRange: ClosedFloatingPointRange<Float>,
    maxValue: Float,
    onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.KOREA) }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 섹션 제목
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // 최소-최대 금액 표시
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 최소값 박스
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${formatter.format(currentRange.start.toInt())}원",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
            
            // 구분선
            Text(
                text = "—",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            // 최대값 박스
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${formatter.format(currentRange.endInclusive.toInt())}원",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
        
        // 슬라이더
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // 배경 바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color.Gray.copy(alpha = 0.5f))
                    .align(Alignment.Center)
            )
            
            // 활성화된 범위 바
            Box(
                modifier = Modifier
                    .fillMaxWidth(currentRange.endInclusive / maxValue)
                    .height(2.dp)
                    .background(Color(0xFF00D1FF))
                    .align(Alignment.CenterStart)
            )
            
            // 슬라이더
            androidx.compose.material3.RangeSlider(
                value = currentRange,
                onValueChange = onRangeChange,
                valueRange = 0f..maxValue,
                steps = 100,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFF00D1FF),
                    inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun FilterSection(
    title: String,
    options: List<String>,
    selectedOptions: Map<String, Boolean>,
    onOptionClick: (String, Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 섹션 제목
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // 필터 버튼 그리드 (2열)
        for (i in options.indices step 2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 첫 번째 버튼
                FilterButton(
                    text = options[i],
                    isSelected = selectedOptions["$title:${options[i]}"] == true,
                    onClick = { isSelected ->
                        onOptionClick(options[i], isSelected)
                    },
                    modifier = Modifier.weight(1f)
                )
                
                // 두 번째 버튼 (있는 경우)
                if (i + 1 < options.size) {
                    FilterButton(
                        text = options[i + 1],
                        isSelected = selectedOptions["$title:${options[i + 1]}"] == true,
                        onClick = { isSelected ->
                            onOptionClick(options[i + 1], isSelected)
                        },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // 짝이 없는 경우 빈 공간 추가
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF00D1FF) else Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable {
                onClick(!isSelected)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = if (isSelected) Color(0xFF00D1FF) else Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
} 