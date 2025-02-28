package com.qingdai.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.io.Serializable;

@Getter
@Setter
@ToString
@Schema(name = "Photo", description = "照片实体类，包含图片元数据及存储信息")
public class Photo implements Serializable {
    @Schema(description = "唯一标识", requiredMode = RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "缩略图地址（URL）", requiredMode = RequiredMode.REQUIRED)
    private String thumbnail;

    @Schema(description = "原图地址（URL）", requiredMode = RequiredMode.REQUIRED)
    private String fullSize;

    @Schema(description = "作者名称", requiredMode = RequiredMode.REQUIRED)
    private String author;

    @Schema(description = "原图宽度（像素）")
    private Integer width;

    @Schema(description = "原图高度（像素）")
    private Integer height;

    @Schema(description = "拍摄时间（格式：yyyy-MM-dd HH:mm:ss）")
    private String time;

    @Schema(description = "拍摄参数（JSON格式）")
    private String data;

    @Schema(description = "照片描述")
    private String introduce;

    @Schema(description = "星标等级（0-5）", allowableValues = {"0", "1"})
    private Integer start;
}