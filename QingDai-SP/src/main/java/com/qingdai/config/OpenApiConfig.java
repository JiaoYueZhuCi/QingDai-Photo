package com.qingdai.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "API 文档",
        version = "1.0",
        description = "系统接口说明文档"
    )
)
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer securityOpenApiCustomizer() {
        return openApi -> openApi.getComponents()
            .addSecuritySchemes("BearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"));
    }
}
