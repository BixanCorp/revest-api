package com.bixan.revest.config;

import com.bixan.revest.auth.interceptor.SessionValidationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private SessionValidationInterceptor sessionValidationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionValidationInterceptor)
                .addPathPatterns("/**") // Apply to all paths
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/logout",
                        "/auth/health",
                        "/auth/validate",
                        "/health/**",
                        "/error/**",
                        "/static/**",
                        "/webjars/**");
    }
}