package com.example.fe.config

/**
 * 애플리케이션 환경 설정을 관리하는 객체
 */
object AppConfig {
    // 서버 환경 설정
    object Server {
        // 기본 서버 URL
//        const val BASE_URL = "http://localhost:8080"
        const val BASE_URL = "https://j12a602.p.ssafy.io/rebirth/"
        
        // API 엔드포인트
        object Endpoints {
            // 결제 관련 엔드포인트
            const val PAYMENT_TOKEN = "api/payment/disposabletoken"
            const val PAYMENT_EVENTS = "api/payment/sse/subscribe"
        }
    }

    // 앱 설정
    object App {
        // 앱 버전
        const val VERSION = "1.0.0"
        
        // 개발 모드 여부 (실제 서버 연동 시 false로 설정)
        const val DEBUG_MODE = false
    }
    
    // 타임아웃 설정
    object Timeout {
        const val CONNECT_TIMEOUT_SECONDS = 30L
        const val READ_TIMEOUT_SECONDS = 60L
        const val WRITE_TIMEOUT_SECONDS = 30L
    }
} 