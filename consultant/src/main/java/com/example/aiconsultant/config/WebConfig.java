package com.example.aiconsultant.config;

import com.example.aiconsultant.intercepter.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // 设置需要拦截的路径
                .addPathPatterns("/chat/**")
                .addPathPatterns("/conversation/**")
                // 排除不需要拦截的路径（登录、注册等）
                .excludePathPatterns("/auth/login")
                .excludePathPatterns("/auth/register");
    }
}