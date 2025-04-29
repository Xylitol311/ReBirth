package com.kkulmoo.rebirth.auth.config;

import com.kkulmoo.rebirth.auth.jwt.JwtUserIdArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtUserIdArgumentResolver jwtUserIdArgumentResolver;

    public WebConfig(JwtUserIdArgumentResolver jwtUserIdArgumentResolver) {
        this.jwtUserIdArgumentResolver = jwtUserIdArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(jwtUserIdArgumentResolver);
    }
}
