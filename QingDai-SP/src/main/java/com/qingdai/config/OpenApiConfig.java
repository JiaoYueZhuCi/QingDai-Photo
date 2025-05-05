package com.qingdai.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(title = "青黛 API 文档", version = "1.0", description = "系统接口说明文档"),
    security = {@SecurityRequirement(name = "BearerAuth")},
    servers = {
        @Server(url = "http://localhost:8080/QingDai", description = "本地环境"),
        @Server(url = "https://qingdai.art:8080/QingDai", description = "正式环境")
    }
)
public class OpenApiConfig {


    @Bean
    public OpenApiCustomizer securityOpenApiCustomizer() {
        return openApi -> openApi.getComponents()
                .addSecuritySchemes("BearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("从配置读取永久令牌"));
    }

}
