package com.qingdai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
       registry.addMapping("/**")
            .allowedOriginPatterns(
                "http://localhost:80",
                "http://127.0.0.1:80",
                "http://localhost:[*]" // 按需添加其他端口
            )
            .allowedMethods("*")
            .allowCredentials(true)
            .allowedHeaders("*")
            .exposedHeaders("Content-Type", "Content-Length", "Content-Disposition", "Vary")
            .maxAge(3600);
    }
}

