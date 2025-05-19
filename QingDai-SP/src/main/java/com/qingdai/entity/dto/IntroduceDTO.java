package com.qingdai.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "用户介绍信息")
public class IntroduceDTO {
    @Schema(description = "用户昵称")
    private String nickname;
    
    @Schema(description = "用户介绍")
    private String description;
} 