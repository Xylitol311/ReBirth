package com.example.fe.ui.screens.payment.components

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.ui.screens.payment.PaymentViewModel
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognizer
import java.util.regex.Pattern

@Composable
fun PaymentAddCardSection(
    onClose: () -> Unit,
    onAddCardComplete: () -> Unit,
    viewModel: PaymentViewModel
) {
    // OCR 스캔 화면 표시 여부
    var showOCRScreen by remember { mutableStateOf(true) }
    
    // 전체 화면 모드로 설정하여 네비게이션 바와 상단 바가 보이지 않도록 함
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)  // 배경색을 완전 검정으로 변경
    ) {
        // OCR 스캔 화면 표시
        if (showOCRScreen) {
            CardOCRScanScreen(
                onBack = onClose,
                onComplete = onAddCardComplete,
                viewModel = viewModel
            )
        }
    }
}


// 카드 추가 버튼 (가로 스크롤에 표시될 항목)
@Composable
fun AddCardButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(280.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2D2A57))
            .border(
                width = 2.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "카드 추가",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "새 카드 추가",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// 이미지 처리 함수
@OptIn(ExperimentalGetImage::class)
internal fun processImageWithTextRecognition(
    imageProxy: ImageProxy,
    textRecognizer: TextRecognizer,
    onCardDetected: (String, String, String) -> Unit
) {
    val mediaImage = imageProxy.image ?: run {
        imageProxy.close()
        return
    }

    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    textRecognizer.process(image)
        .addOnSuccessListener { visionText ->
            // 인식된 텍스트에서 카드 정보 추출
            val cardInfo = extractCardInfo(visionText)
            onCardDetected(cardInfo.first, cardInfo.second, cardInfo.third)
        }
        .addOnFailureListener { e ->
            Log.e("CardOCRScanScreen", "텍스트 인식 실패", e)
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

// 카드 정보 추출 함수
internal fun extractCardInfo(visionText: Text): Triple<String, String, String> {
    var cardNumber = ""
    var expiryDate = ""
    var cardholderName = ""

    // 카드 번호 패턴 (4자리 숫자 그룹)
    val cardNumberPattern = Pattern.compile("\\d{4}\\s*\\d{4}\\s*\\d{4}\\s*\\d{4}")

    // 만료일 패턴 (MM/YY 형식)
    val expiryDatePattern = Pattern.compile("(0[1-9]|1[0-2])/([0-9]{2})")

    // 텍스트 블록 순회
    for (block in visionText.textBlocks) {
        for (line in block.lines) {
            val lineText = line.text

            // 카드 번호 추출
            val cardNumberMatcher = cardNumberPattern.matcher(lineText)
            if (cardNumberMatcher.find() && cardNumber.isEmpty()) {
                cardNumber = cardNumberMatcher.group().replace("\\s".toRegex(), " ")
            }

            // 만료일 추출
            val expiryDateMatcher = expiryDatePattern.matcher(lineText)
            if (expiryDateMatcher.find() && expiryDate.isEmpty()) {
                expiryDate = expiryDateMatcher.group()
            }

            // 카드 소유자 이름 추출 (대문자로 된 이름 패턴)
            if (lineText.matches("^[A-Z]+(\\s[A-Z]+)+$".toRegex()) && cardholderName.isEmpty()) {
                cardholderName = lineText
            }
        }
    }

    return Triple(cardNumber, expiryDate, cardholderName)
}

// 카드 확인 화면
@Composable
fun CardConfirmationScreen(
    cardNumber: String,
    expiryDate: String,
    cardIssuer: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    // 입력 상태 관리
    var cardNumberInput by remember { mutableStateOf(cardNumber) }
    var expiryDateInput by remember { mutableStateOf(expiryDate) }
    var cardPinPrefix by remember { mutableStateOf("") }
    var cvcInput by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // 상단 바
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로 가기",
                    tint = Color.Black
                )
            }
            
            Text(
                text = "카드등록",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            
            // 오른쪽 여백을 위한 투명 아이콘
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        // 카드 정보 입력 폼
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            // 카드 비밀번호 앞 두자리
            Text(
                text = "카드 비밀번호",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = cardPinPrefix,
                onValueChange = { 
                    if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                        cardPinPrefix = it 
                    }
                },
                placeholder = { Text("앞 2자리") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword
                ),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    unfocusedBorderColor = Color.LightGray
                )
            )
            
            // CVC
            Text(
                text = "CVC",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = cvcInput,
                onValueChange = { 
                    if (it.length <= 3 && it.all { char -> char.isDigit() }) {
                        cvcInput = it 
                    }
                },
                placeholder = { Text("뒷면 3자리") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword
                ),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    unfocusedBorderColor = Color.LightGray
                )
            )
            
            // 유효기간
            Text(
                text = "유효기간",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = expiryDateInput,
                onValueChange = { 
                    // MM/YY 형식 유지
                    val filtered = it.replace(Regex("[^0-9/]"), "")
                    if (filtered.length <= 5) {
                        expiryDateInput = filtered
                    }
                },
                placeholder = { Text("MM/YY") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    unfocusedBorderColor = Color.LightGray
                )
            )
            
            // 카드번호
            Text(
                text = "카드번호",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = cardNumberInput,
                onValueChange = { 
                    // 숫자와 공백만 허용
                    val filtered = it.replace(Regex("[^0-9 ]"), "")
                    if (filtered.replace(" ", "").length <= 16) {
                        cardNumberInput = filtered
                    }
                },
                placeholder = { Text("0000-0000-0000-0000") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    unfocusedBorderColor = Color.LightGray
                ),
                trailingIcon = {
                    if (cardIssuer.isNotEmpty()) {
                        // 카드사 로고 (예시로 원형 아이콘 사용)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF1A73E8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "S",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        }
        
        // 여백
        Spacer(modifier = Modifier.weight(1f))
        
        // 확인 버튼
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "확인",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 카드 정보 항목
@Composable
fun CardInfoItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp
        )

        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// 카드 번호 포맷팅 함수
internal fun formatCardNumber(cardNumber: String): String {
    // 공백 제거 후 4자리씩 그룹화
    val cleaned = cardNumber.replace("\\s".toRegex(), "")
    val formatted = StringBuilder()

    for (i in cleaned.indices) {
        if (i > 0 && i % 4 == 0) {
            formatted.append(" ")
        }
        formatted.append(cleaned[i])
    }

    return formatted.toString()
}