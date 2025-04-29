package com.kkulmoo.rebirth.auth.jwt;

import com.kkulmoo.rebirth.common.annotation.JwtUserId;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class JwtUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtProvider jwtProvider;

    public JwtUserIdArgumentResolver(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(JwtUserId.class);
    }

    @Override
    public Integer resolveArgument(MethodParameter parameter,
                                   ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest,
                                   WebDataBinderFactory binderFactory) throws Exception {
        String token = ((HttpServletRequest) webRequest.getNativeRequest()).getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            return (jwtProvider.getUserIdFromToken(jwt));
        }
        return null;
    }
}
