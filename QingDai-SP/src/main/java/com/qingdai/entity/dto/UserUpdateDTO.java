package com.qingdai.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户更新信息")
public class UserUpdateDTO {
    @Schema(description = "用户名")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    private String username;

    @Schema(description = "密码")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;

    @Schema(description = "昵称")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @Schema(description = "个人介绍")
    @Size(max = 100, message = "个人介绍长度不能超过100个字符")
    private String description;
} 