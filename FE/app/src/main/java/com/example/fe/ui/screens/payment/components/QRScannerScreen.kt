package com.example.fe.ui.screens.payment.components

import android.Manifest
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QRScannerScreen(
    onClose: () -> Unit,
    onQRCodeScanned: (String) -> Unit,
    hideSystemBars: Boolean = true  // 시스템 바 숨김 여부
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    
    // 카메라 권한 요청
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    // QR 코드 스캔 결과
    var qrCodeValue by remember { mutableStateOf<String?>(null) }
    
    // 권한 요청 다이얼로그 표시 여부
    var showPermissionDialog by remember { mutableStateOf(!cameraPermissionState.status.isGranted) }
    
    // 권한 요청 효과
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            showPermissionDialog = true
        }
    }
    
    // QR 코드 스캔 결과 처리
    LaunchedEffect(qrCodeValue) {
        qrCodeValue?.let {
            onQRCodeScanned(it)
        }
    }
    
    // 권한 요청 다이얼로그
    if (showPermissionDialog) {
        Dialog(
            onDismissRequest = { 
                // 권한이 없으면 화면 닫기
                if (!cameraPermissionState.status.isGranted) {
                    onClose()
                }
                showPermissionDialog = false
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF2D2A57)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "카메라 권한 필요",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "QR 코드를 스캔하기 위해 카메라 접근 권한이 필요합니다.",
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { 
                                showPermissionDialog = false
                                onClose() 
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("취소")
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Button(
                            onClick = { 
                                cameraPermissionState.launchPermissionRequest()
                                showPermissionDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("권한 허용")
                        }
                    }
                }
            }
        }
    }
    
    // 전체 화면 컨테이너
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 상단 바 - 항상 표시되도록 수정
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF2D2A57))
                .align(Alignment.TopCenter)
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로 가기",
                    tint = Color.White
                )
            }
            
            Text(
                text = "QR 결제",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
            
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = Color.White
                )
            }
        }
        
        // 카메라 미리보기 - 상단 바 아래에 표시되도록 수정
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp) // 상단 바 높이만큼 패딩 추가
        ) {
            if (cameraPermissionState.status.isGranted) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val executor = Executors.newSingleThreadExecutor()
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            
                            // 미리보기 설정
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            
                            // 이미지 분석 설정
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(executor, QRCodeAnalyzer { barcode ->
                                        barcode?.rawValue?.let { value ->
                                            if (qrCodeValue == null) {
                                                qrCodeValue = value
                                            }
                                        }
                                    })
                                }
                            
                            try {
                                // 카메라 바인딩 해제
                                cameraProvider.unbindAll()
                                
                                // 카메라 바인딩
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                Log.e("QRScanner", "카메라 바인딩 실패", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // QR 코드 스캔 영역 표시
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 2.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .align(Alignment.Center)
                )
                
                // 안내 텍스트
                Text(
                    text = "QR 코드를 스캔해주세요",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 100.dp)
                )
            } else if (!showPermissionDialog) {
                // 권한이 없고 다이얼로그도 표시되지 않는 경우 안내 메시지
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "카메라 권한이 필요합니다",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showPermissionDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2D2A57)
                        )
                    ) {
                        Text("권한 요청")
                    }
                }
            }
        }
    }
}

// QR 코드 분석기
class QRCodeAnalyzer(private val onQRCodeScanned: (Barcode?) -> Unit) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient()
    
    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )
            
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        onQRCodeScanned(barcodes[0])
                    }
                }
                .addOnFailureListener {
                    Log.e("QRScanner", "바코드 스캔 실패", it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
} 