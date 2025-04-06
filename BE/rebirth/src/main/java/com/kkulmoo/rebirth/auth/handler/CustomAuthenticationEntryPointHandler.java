package com.kkulmoo.rebirth.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkulmoo.rebirth.common.ApiResponseDTO.ApiResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom Authentication Entry Point Handler
 */
@Component
public class CustomAuthenticationEntryPointHandler implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // 401 Unauthorized 상태 코드 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        // ApiResponseDTO를 사용하여 일관된 형식의 응답 생성
        ApiResponseDTO<Void> errorResponse = ApiResponseDTO.error("인증에 실패했습니다. 로그인이 필요합니다.");

        // JSON으로 변환하여 응답
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}