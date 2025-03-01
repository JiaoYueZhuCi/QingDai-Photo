package com.qingdai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许所有本地端口（如 5173、8080、3000 等）
                .allowedOriginPatterns("http://localhost:*")
                // 允许所有方法（GET、POST等）
                .allowedMethods("*")
                // 允许携带 Cookie、Authorization 等头
                .allowCredentials(true)
                // 允许所有请求头
                .allowedHeaders("*");
    }
}

