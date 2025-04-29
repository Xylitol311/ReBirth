package com.example.fe.ui.screens.onboard.screen.setup.security

/**
 * 추가 보안 설정 화면의 각 단계를 나타내는 열거형
 * 
 * METHOD: 인증 방식 선택 화면 (지문/패턴 선택)
 * PATTERN: 패턴 최초 입력 화면
 * PATTERN_CONFIRM: 패턴 확인 입력 화면
 * COMPLETE: 보안 설정 완료 화면
 */
enum class AdditionalSecurityStep {
    METHOD,
    PATTERN,
    PATTERN_CONFIRM,
    COMPLETE
} 