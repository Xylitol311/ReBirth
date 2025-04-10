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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
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
    onCancel: () -> Unit,
    viewModel: PaymentViewModel
) {
    // 입력 상태 관리
    var cardNumberInput by remember {
        mutableStateOf(formatCardNumber(cardNumber.replace(Regex("[^0-9]"), "")))
    }
    var expiryDateInput by remember { mutableStateOf(expiryDate) }
    var cardPinPrefix by remember { mutableStateOf("") }
    var cvcInput by remember { mutableStateOf("") }

    // 현재 포커스된 필드 관리
    val focusManager = LocalFocusManager.current
    val cardPinFocusRequester = remember { FocusRequester() }
    val cvcFocusRequester = remember { FocusRequester() }
    val expiryDateFocusRequester = remember { FocusRequester() }
    val cardNumberFocusRequester = remember { FocusRequester() }

    // 현재 포커스된 필드 추적
    var focusedField by remember { mutableStateOf("none") }

    var isLoading by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
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

                // 비밀번호 입력 필드 - 수정된 부분
                OutlinedTextField(
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(cardPinFocusRequester)
                        .onFocusChanged { focusedField = if (it.isFocused) "pin" else focusedField },
                    placeholder = { Text("앞 2자리") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = if (focusedField == "pin") Color(0xFF2196F3) else Color.LightGray,
                        focusedPlaceholderColor = Color.Gray.copy(alpha = 0.3f), // 포커스 상태일 때 플레이스홀더 색상
                        unfocusedPlaceholderColor = Color.Gray.copy(alpha = 0.3f) // 포커스가 없을 때 플레이스홀더 색상
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // CVC
                Text(
                    text = "CVC",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                // CVC 입력 필드 - 수정된 부분
                OutlinedTextField(
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(cvcFocusRequester)
                        .onFocusChanged { focusedField = if (it.isFocused) "cvc" else focusedField },
                    placeholder = { Text("뒷면 3자리") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = if (focusedField == "cvc") Color(0xFF2196F3) else Color.LightGray,
                        focusedPlaceholderColor = Color.Gray.copy(alpha = 0.3f), // 포커스 상태일 때 플레이스홀더 색상
                        unfocusedPlaceholderColor = Color.Gray.copy(alpha = 0.3f) // 포커스가 없을 때 플레이스홀더 색상
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 유효기간
                Text(
                    text = "유효기간",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                // 유효기간 입력 필드 - 수정된 부분
                OutlinedTextField(
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(expiryDateFocusRequester)
                        .onFocusChanged { focusedField = if (it.isFocused) "expiry" else focusedField },
                    placeholder = { Text("MM/YY") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = if (focusedField == "expiry") Color(0xFF2196F3) else Color.LightGray,
                        focusedPlaceholderColor = Color.Gray.copy(alpha = 0.3f), // 포커스 상태일 때 플레이스홀더 색상
                        unfocusedPlaceholderColor = Color.Gray.copy(alpha = 0.3f) // 포커스가 없을 때 플레이스홀더 색상
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 카드번호
                Text(
                    text = "카드번호",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                // 카드번호 입력 필드 - 수정된 부분
                // TextFieldValue도 포매팅된 초기값으로 설정
                var cardNumberTextFieldValue by remember {
                    mutableStateOf(TextFieldValue(
                        text = formatCardNumber(cardNumber.replace(Regex("[^0-9]"), "")),
                        selection = TextRange(0)  // 커서를 처음 위치에 설정
                    ))
                }

                OutlinedTextField(
                    value = cardNumberTextFieldValue,
                    onValueChange = { newValue ->
                        // 현재 커서 위치 저장
                        val cursorPosition = newValue.selection.start

                        // 숫자만 허용
                        val digitsOnly = newValue.text.replace(Regex("[^0-9]"), "")

                        // 최대 16자리까지만 허용
                        if (digitsOnly.length <= 16) {
                            // 포맷팅된 텍스트
                            val formatted = formatCardNumber(digitsOnly)

                            // 새 커서 위치 계산 (하이픈 추가 고려)
                            val newCursorPosition = when {
                                // 삭제 중인 경우
                                formatted.length < cardNumberTextFieldValue.text.length -> cursorPosition.coerceAtMost(formatted.length)
                                // 입력 중인 경우
                                else -> {
                                    // 하이픈이 추가된 위치 이후라면 커서 위치 조정
                                    val hyphensBefore = formatted.substring(0, minOf(cursorPosition, formatted.length))
                                        .count { it == '-' }
                                    val oldHyphensBefore = cardNumberTextFieldValue.text
                                        .substring(0, minOf(cursorPosition, cardNumberTextFieldValue.text.length))
                                        .count { it == '-' }
                                    cursorPosition + (hyphensBefore - oldHyphensBefore)
                                }
                            }.coerceIn(0, formatted.length)

                            // TextFieldValue 업데이트 (텍스트와 커서 위치)
                            cardNumberTextFieldValue = TextFieldValue(
                                text = formatted,
                                selection = TextRange(newCursorPosition)
                            )

                            // 내부 상태 업데이트 (제출용)
                            cardNumberInput = formatted

                            // 카드번호 입력 완료 시 키보드 숨기기
                            if (digitsOnly.length == 16) {
                                focusManager.clearFocus()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(cardNumberFocusRequester)
                        .onFocusChanged { focusedField = if (it.isFocused) "cardNumber" else focusedField },
                    placeholder = { Text("0000-0000-0000-0000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = if (focusedField == "cardNumber") Color(0xFF2196F3) else Color.LightGray,
                        focusedPlaceholderColor = Color.Gray.copy(alpha = 0.3f), // 포커스 상태일 때 플레이스홀더 색상
                        unfocusedPlaceholderColor = Color.Gray.copy(alpha = 0.3f) // 포커스가 없을 때 플레이스홀더 색상
                    )
                )
            }
        }

        // 여백
        Spacer(modifier = Modifier.weight(1f))

        // 확인 버튼
        Button(
            onClick = {
                // 로딩 상태 활성화
                isLoading = true

                // 카드 등록 함수 호출
                viewModel.registCard(
                    cardNumber = cardNumberInput,
                    password = cardPinPrefix,
                    cvc = cvcInput
                )

                // 토큰 새로고침
                viewModel.refreshTokens()

                // 완료 콜백 호출
                onConfirm()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = !isLoading && cardPinPrefix.length == 2 && cvcInput.length == 3 &&
                    cardNumberInput.replace("-", "").length == 16 &&
                    expiryDateInput.replace("/", "").length == 4
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "확인",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// 카드 번호 포맷팅 함수 수정
internal fun formatCardNumber(number: String): String {
    // 모든 하이픈 제거 후 숫자만 추출
    val digitsOnly = number.replace(Regex("[^0-9]"), "")

    // 4자리씩 그룹화하여 하이픈으로 연결
    return digitsOnly.chunked(4).joinToString("-")
}