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
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
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
fun extractCardInfo(visionText: Text): Triple<String, String, String> {
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

@Composable
fun CardConfirmationScreen(
    cardNumber: String,
    expiryDate: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    // 입력 상태 관리
    var cardNumberInput by remember { mutableStateOf(cardNumber) }
    var expiryDateInput by remember { mutableStateOf(expiryDate) }
    var cardPinPrefix by remember { mutableStateOf("") }
    var cvcInput by remember { mutableStateOf("") }

    // 현재 포커스된 필드 관리
    val focusManager = LocalFocusManager.current
    val cardPinFocusRequester = remember { FocusRequester() }
    val cvcFocusRequester = remember { FocusRequester() }
    val expiryDateFocusRequester = remember { FocusRequester() }
    val cardNumberFocusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()) // 네이티브 하단 바 고려
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

        // 카드 정보 입력 폼 - 하나의 카드 형태로 변경
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 카드 비밀번호 앞 두자리
                Text(
                    text = "카드 비밀번호",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

// 비밀번호 표시 - 앞 두자리만 입력받고 뒤 두자리는 항상 ●●로 표시
                Text(
                    text = if (cardPinPrefix.isEmpty()) "앞 2자리●●" else "${cardPinPrefix}●●",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (cardPinPrefix.isEmpty()) Color.LightGray else Color.Black,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable { cardPinFocusRequester.requestFocus() }
                )

                BasicTextField(
                    value = cardPinPrefix,
                    onValueChange = {
                        if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                            cardPinPrefix = it
                            // 2자리 입력 완료 시 다음 필드로 포커스 이동
                            if (it.length == 2) {
                                cvcFocusRequester.requestFocus()
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.dp)
                        .focusRequester(cardPinFocusRequester),
                    textStyle = TextStyle(fontSize = 0.sp) // 보이지 않게 설정
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // CVC
                Text(
                    text = "CVC",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                // CVC 표시 - 입력한 만큼만 마스킹
                Text(
                    text = when (cvcInput.length) {
                        0 -> "뒷면 3자리"
                        1 -> "●"
                        2 -> "●●"
                        3 -> "●●●"
                        else -> "뒷면 3자리"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (cvcInput.isEmpty()) Color.LightGray else Color.Black,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable { cvcFocusRequester.requestFocus() }
                )

                BasicTextField(
                    value = cvcInput,
                    onValueChange = {
                        if (it.length <= 3 && it.all { char -> char.isDigit() }) {
                            cvcInput = it
                            // 3자리 입력 완료 시 다음 필드로 포커스 이동
                            if (it.length == 3) {
                                expiryDateFocusRequester.requestFocus()
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.dp)
                        .focusRequester(cvcFocusRequester),
                    textStyle = TextStyle(fontSize = 0.sp) // 보이지 않게 설정
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // 유효기간
                Text(
                    text = "유효기간",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Text(
                    text = if (expiryDateInput.isEmpty()) "MM/YY" else expiryDateInput,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (expiryDateInput.isEmpty()) Color.LightGray else Color.Black,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable { expiryDateFocusRequester.requestFocus() }
                )

                BasicTextField(
                    value = expiryDateInput,
                    onValueChange = { input ->
                        // 숫자만 허용
                        val filtered = input.replace(Regex("[^0-9/]"), "")

                        // 자동으로 슬래시 추가
                        if (filtered.replace("/", "").length <= 4) {
                            if (filtered.replace("/", "").length >= 2 && !filtered.contains("/")) {
                                // MM/YY 형식으로 변환
                                val month = filtered.take(2)
                                val year = filtered.drop(2)
                                expiryDateInput = if (year.isNotEmpty()) "$month/$year" else month
                            } else {
                                expiryDateInput = filtered
                            }

                            // 유효기간 입력 완료 시 다음 필드로 포커스 이동
                            if (filtered.replace("/", "").length == 4) {
                                cardNumberFocusRequester.requestFocus()
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.dp)
                        .focusRequester(expiryDateFocusRequester),
                    textStyle = TextStyle(fontSize = 0.sp) // 보이지 않게 설정
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // 카드번호
                Text(
                    text = "카드번호",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Text(
                    text = if (cardNumberInput.isEmpty()) "0000-0000-0000-0000" else cardNumberInput,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (cardNumberInput.isEmpty()) Color.LightGray else Color.Black,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable { cardNumberFocusRequester.requestFocus() }
                )

                BasicTextField(
                    value = cardNumberInput,
                    onValueChange = { input ->
                        // 숫자만 허용
                        val filtered = input.replace(Regex("[^0-9-]"), "")

                        // 자동으로 하이픈 추가
                        if (filtered.replace("-", "").length <= 16) {
                            cardNumberInput = formatCardNumber(filtered.replace("-", ""))
                        }

                        // 카드번호 입력 완료 시 키보드 숨기기
                        if (filtered.replace("-", "").length == 16) {
                            focusManager.clearFocus()
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.dp)
                        .focusRequester(cardNumberFocusRequester),
                    textStyle = TextStyle(fontSize = 0.sp) // 보이지 않게 설정
                )
            }
        }

        // 여백
        Spacer(modifier = Modifier.weight(1f))

        // 확인 버튼
        Button(
            onClick = {
                // 카드 등록 함수 호출
                onConfirm()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = cardPinPrefix.length == 2 && cvcInput.length == 3 &&
                    cardNumberInput.replace("-", "").length == 16 &&
                    expiryDateInput.replace("/", "").length == 4
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

// 카드 번호 포맷팅 함수 수정
internal fun formatCardNumber(cardNumber: String): String {
    // 하이픈 제거 후 4자리씩 그룹화
    val cleaned = cardNumber.replace("-", "")
    val formatted = StringBuilder()

    for (i in cleaned.indices) {
        if (i > 0 && i % 4 == 0) {
            formatted.append("-")
        }
        formatted.append(cleaned[i])
    }

    return formatted.toString()
}