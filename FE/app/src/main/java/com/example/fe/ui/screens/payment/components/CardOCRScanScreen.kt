package com.example.fe.ui.screens.payment.components

import android.Manifest
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import android.content.ContentValues
import com.example.fe.ui.screens.payment.PaymentViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import kotlinx.coroutines.delay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.Close

@Composable
fun CardOCRScanScreen(
    onBack: () -> Unit,
    onComplete: () -> Unit,
    viewModel: PaymentViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // 카메라 권한 상태
    var hasCameraPermission by remember { mutableStateOf(false) }

    // 스캔 상태
    var isScanning by remember { mutableStateOf(false) }

    // 카드 정보 확인 상태
    var showCardConfirmation by remember { mutableStateOf(false) }

    // 직접 입력 모드 상태 (직접 입력 버튼으로 진입했는지 여부)
    var isManualInputMode by remember { mutableStateOf(false) }

    // 하단 팝업 표시 상태
    var showBottomPopup by remember { mutableStateOf(false) }

    // 카드 정보
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var cardType by remember { mutableStateOf("") }
    var cardIssuer by remember { mutableStateOf("") }

    // 카메라 실행기
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var cameraController by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    // 텍스트 인식기
    val textRecognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    // 카메라 권한 요청 런처
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // 권한 확인 및 요청
    LaunchedEffect(Unit) {
        val permissionCheckResult = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        )
        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
            hasCameraPermission = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // 화면이 사라질 때 리소스 정리
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            textRecognizer.close()
        }
    }

    // 인식 결과 안정화를 위한 변수들
    var lastCardNumber by remember { mutableStateOf("") }
    var lastExpiryDate by remember { mutableStateOf("") }
    var lastCardholderName by remember { mutableStateOf("") }
    var stableFrameCount by remember { mutableIntStateOf(0) }

    // 인식 결과 캐싱을 위한 변수
    val cardNumberCache = remember { mutableListOf<String>() }
    val expiryDateCache = remember { mutableListOf<String>() }
    val cardholderNameCache = remember { mutableListOf<String>() }
    val maxCacheSize = 20

    // 가장 빈번한 결과 선택 함수
    fun getMostFrequentResult(cache: List<String>): String {
        return cache.groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: ""
    }
    // 카드 인식 상태 변수 추가
    var isCardDetected by remember { mutableStateOf(false) }

    // 인식 진행 상태 변수 추가
    var recognitionProgress by remember { mutableFloatStateOf(0f) }
    var isRecognitionComplete by remember { mutableStateOf(false) }
    var recognitionAttempts by remember { mutableIntStateOf(0) }
    var lastRecognitionTime by remember { mutableLongStateOf(0L) }

    val imageAnalyzer = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(cameraExecutor) { imageProxy ->
                    processImageWithTextRecognition(
                        imageProxy = imageProxy,
                        textRecognizer = textRecognizer
                    ) { number, expiry, name ->
                        // 현재 시간 확인
                        val currentTime = System.currentTimeMillis()
                        
                        // 3초마다 인식 시도 횟수 증가
                        if (currentTime - lastRecognitionTime > 3000) {
                            recognitionAttempts++
                            lastRecognitionTime = currentTime
                            
                            // 인식 시도가 5회 이상이고 아직 카드 번호가 인식되지 않았다면
                            if (recognitionAttempts >= 5 && cardNumberCache.isEmpty()) {
                                scope.launch {
                                    withContext(Dispatchers.Main) {
                                        // 에러 메시지 표시
                                        hasError = true
                                        errorMessage = "카드를 인식하기 어렵습니다.\n카드를 프레임 안에 잘 위치시키고 다시 시도해주세요."
                                        // 인식 초기화
                                        recognitionAttempts = 0
                                        cardNumberCache.clear()
                                        expiryDateCache.clear()
                                        recognitionProgress = 0f
                                    }
                                }
                            }
                        }
                        
                        // 카드가 감지되었는지 확인 - 카드 번호와 만료일만 확인
                        isCardDetected = number.isNotEmpty() || expiry.isNotEmpty()

                        // 결과 캐싱 - 카드 번호와 만료일만 캐싱
                        if (number.isNotEmpty()) {
                            cardNumberCache.add(number)
                            if (cardNumberCache.size > maxCacheSize) {
                                cardNumberCache.removeAt(0)
                            }
                            // 진행 상태 업데이트
                            recognitionProgress = (cardNumberCache.size.toFloat() / 10f).coerceAtMost(1f)
                        }

                        if (expiry.isNotEmpty()) {
                            expiryDateCache.add(expiry)
                            if (expiryDateCache.size > maxCacheSize) {
                                expiryDateCache.removeAt(0)
                            }
                        }

                        // 캐시에서 가장 빈번한 결과 선택
                        if (cardNumberCache.size >= 10 && expiryDateCache.size >= 5) {
                            val bestCardNumber = getMostFrequentResult(cardNumberCache)
                            val bestExpiryDate = getMostFrequentResult(expiryDateCache)

                            scope.launch {
                                withContext(Dispatchers.Main) {
                                    cardNumber = bestCardNumber
                                    expiryDate = bestExpiryDate
                                    isRecognitionComplete = true
                                    
                                    // 카드 정보가 모두 인식되면 하단 팝업 표시
                                    if (bestCardNumber.isNotEmpty() && bestExpiryDate.isNotEmpty()) {
                                        // 인식 완료 후 1초 후에 팝업 표시
                                        delay(1000)
                                        isScanning = false
                                        showBottomPopup = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }

    // previewView 변수를 컴포저블 레벨에서 정의
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    // resetScan 함수를 컴포저블 최상위 레벨에 정의
    fun resetScan() {
        // 카드 정보 초기화
        cardNumber = ""
        expiryDate = ""
        cardholderName = ""
        cardType = ""
        cardIssuer = ""

        // 캐시 데이터 초기화
        cardNumberCache.clear()
        expiryDateCache.clear()
        cardholderNameCache.clear()

        // 안정화 변수 초기화
        lastCardNumber = ""
        lastExpiryDate = ""
        lastCardholderName = ""
        stableFrameCount = 0

        // 인식 진행 상태 초기화
        recognitionProgress = 0f
        isRecognitionComplete = false
        recognitionAttempts = 0
        lastRecognitionTime = 0L

        // 인식 상태 초기화
        isCardDetected = false
        
        // 에러 상태 초기화
        hasError = false
        errorMessage = ""

        // 확인 화면 닫기
        showCardConfirmation = false
        showBottomPopup = false

        // 카메라 재설정
        try {
            // 기존 카메라 바인딩 해제
            cameraController?.unbindAll()

            // 카메라 재설정
            val preview = Preview.Builder().build()
            if (previewView != null) {
                preview.setSurfaceProvider(previewView!!.surfaceProvider)
            }

            camera = cameraController?.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalyzer
            )
        } catch (e: Exception) {
            Log.e("CardOCRScanScreen", "카메라 재설정 실패", e)
        }
    }



    // 카메라 설정
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraController = cameraProviderFuture.get()
        }
    }

    // 카메라 프리뷰 설정
    val preview = remember {
        Preview.Builder().build()
    }

    // 이미지 캡처 함수
    val captureImage = {
        isScanning = true

        // 현재 프리뷰에서 이미지 캡처 및 분석
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        cameraController?.unbindAll()
        camera = cameraController?.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageCapture
        )

        // 이미지 캡처
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues()
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // 저장된 이미지 분석
                    val imageUri = outputFileResults.savedUri
                    if (imageUri != null) {
                        val image = InputImage.fromFilePath(context, imageUri)
                        textRecognizer.process(image)
                            .addOnSuccessListener { visionText ->
                                // 인식 전에 이전 결과 초기화
                                cardNumber = ""
                                expiryDate = ""

                                // 인식된 텍스트에서 카드 정보 추출
                                val cardInfo = extractCardInfo(visionText)

                                // 인식 결과가 있을 때만 값 설정 - 카드 번호와 만료일만 설정
                                if (cardInfo.first.isNotEmpty()) cardNumber = cardInfo.first
                                if (cardInfo.second.isNotEmpty()) expiryDate = cardInfo.second

                                // 스캔 완료 후 상태 업데이트
                                isScanning = false

                                // 카드 번호가 인식된 경우에만 하단 팝업 표시
                                if (cardNumber.isNotEmpty()) {
                                    showBottomPopup = true
                                } else {
                                    // 인식 실패 시 메시지 표시 및 다시 스캔 준비
                                    hasError = true
                                    errorMessage = "카드 정보를 인식할 수 없습니다. 다시 시도해주세요."
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("CardOCRScanScreen", "텍스트 인식 실패", e)
                                isScanning = false

                                // 실패 시 메시지 표시 및 다시 스캔 준비
                                hasError = true
                                errorMessage = "텍스트 인식에 실패했습니다. 다시 시도해주세요."
                            }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CardOCRScanScreen", "이미지 캡처 실패", exception)
                    isScanning = false

                    // 실패 시 메시지 표시 및 다시 스캔 준비
                    hasError = true
                    errorMessage = "이미지 캡처에 실패했습니다. 다시 시도해주세요."
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 카드 정보 확인 화면
        if (showCardConfirmation) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
            ) {
                CardConfirmationScreen(
                    cardNumber = if (isManualInputMode) "" else cardNumber,
                    expiryDate = if (isManualInputMode) "" else expiryDate,
                    cardIssuer = if (isManualInputMode) "" else cardIssuer,
                    onConfirm = {
                        // 카드 정보 저장
                        viewModel.addCard(
                            cardName = "내 카드",
                            cardNumber = cardNumber,
                            expiryDate = expiryDate,
                            cvv = "123" // 임시 CVV
                        )
                        onComplete()
                    },
                    onCancel = {
                        showCardConfirmation = false
                        isManualInputMode = false // 모드 초기화
                        resetScan()
                    }
                )
            }
        } else {
            // 상단 바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .align(Alignment.TopCenter)
            ) {
                // 뒤로 가기 버튼
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로 가기",
                        tint = Color.White
                    )
                }

                // 제목
                Text(
                    text = "카드 스캔",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )

                // 도움말 버튼
                IconButton(
                    onClick = { /* 도움말 표시 */ },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                ) {
                    Text(
                        text = "?",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 카메라 미리보기
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val view = PreviewView(ctx).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        }

                        // previewView 변수에 할당
                        previewView = view

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView!!.surfaceProvider)
                            }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalyzer
                                )
                            } catch (e: Exception) {
                                Log.e("CardOCRScanScreen", "카메라 바인딩 실패", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        view
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // 카메라 권한이 없는 경우
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "카메라 권한이 필요합니다",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }

            // 중앙 영역 - 카드 스캔 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .aspectRatio(1.6f) // 카드 비율에 맞게 조정
                    .align(Alignment.Center)
            ) {
                // 스캔 영역 테두리 - 카드 감지 시 색상 변경
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 2.dp,
                            color = if (isCardDetected) Color(0xFF4CAF50) else Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                )

                // 안내 메시지 - 카드 감지 시 메시지 변경
                Text(
                    text = if (isCardDetected)
                           "카드가 감지되었습니다. 조금만 기다려주세요..."
                           else "박스 안에 카드가 들어오도록 맞춰주세요",
                    color = if (isCardDetected) Color(0xFF4CAF50) else Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                )

                // 카드 감지 시 표시할 아이콘
                if (isCardDetected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "카드 감지됨",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            // 실시간 인식 상태 표시
            if (isCardDetected && !isScanning && !showCardConfirmation && !showBottomPopup) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .background(
                            color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 120.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isRecognitionComplete) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "인식 완료",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFF4CAF50),
                                    strokeWidth = 2.dp,
                                    progress = recognitionProgress
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = if (isRecognitionComplete) "인식 완료! 잠시만 기다려주세요." 
                                       else "카드 정보 인식 중... ${(recognitionProgress * 100).toInt()}%",
                                color = Color(0xFF4CAF50),
                                fontSize = 14.sp
                            )
                        }
                        
                        // 진행 바 추가
                        if (!isRecognitionComplete) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = recognitionProgress,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                            )
                        }
                    }
                }
            }

            // 직접 입력 버튼 추가
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                Button(
                    onClick = {
                        // 직접 입력 모드로 설정하고 확인 화면으로 이동
                        isManualInputMode = true
                        showCardConfirmation = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "직접 입력해서 등록",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 하단 팝업 - 스캔 결과 표시
            if (showBottomPopup) {
                // 반투명 검정색 배경
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable(
                            onClick = { /* 배경 클릭 시 팝업 닫기 방지 */ },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                )
                
                // 하단 팝업
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    AnimatedVisibility(
                        visible = showBottomPopup,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        Column(
                            modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .padding(16.dp)
                        ) {
                            Text(
                                text = "스캔한 정보를 확인해주세요",
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // 카드 번호
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "카드 번호",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )

                                Text(
                                    text = formatCardNumber(cardNumber),
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // 만료일
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "유효기간",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )

                                Text(
                                    text = expiryDate,
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 버튼 영역
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // 재촬영 버튼
                                Button(
                                    onClick = { 
                                        showBottomPopup = false
                                        resetScan() 
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.LightGray
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                ) {
                                    Text("재촬영")
                                }

                                // 확인 버튼
                                Button(
                                    onClick = {
                                        // 팝업 닫기
                                        showBottomPopup = false
                                        
                                        // CardConfirmationScreen으로 이동
                                        showCardConfirmation = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                ) {
                                    Text("확인")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 스캔 중 로딩 표시
    if (isScanning) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "카드 정보 인식 중...",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }

    // 에러 발생 시 UI 표시
    if (hasError) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(24.dp)
                    .background(
                        color = Color.DarkGray,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "에러",
                    tint = Color.Red,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = errorMessage,
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        hasError = false
                        resetScan()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("다시 시도")
                }
            }
        }
    }
}

