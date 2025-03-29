package com.qingdai.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "LoginRequest", description = "登录请求参数")
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", required = true)
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", required = true)
    private String password;
}