package com.qingdai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.FieldFill;

import java.time.LocalDateTime;

@Data
@TableName("photo")
@Schema(name = "Photo实体")
public class Photo {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    @JsonSerialize(using = ToStringSerializer.class) // 确保序列化为字符串
    private String id;
    @TableField(value = "title")
    @Schema(description = "标题")
    private String title;
    @TableField(value = "file_name")
    @Schema(description = "原图地址")
    private String fileName;
    @TableField(value = "author")
    @Schema(description = "作者")
    private String author;
    @TableField(value = "width")
    @Schema(description = "原图宽度")
    private Integer width;
    @TableField(value = "height")
    @Schema(description = "原图高度")
    private Integer height;
    @TableField(value = "shoot_time")
    @Schema(description = "拍摄时间")
    private String shootTime;
    @TableField(value = "aperture")
    @Schema(description = "光圈")
    private String aperture;
    @TableField(value = "shutter")
    @Schema(description = "快门")
    private String shutter;
    @TableField(value = "ISO")
    @Schema(description = "iso")
    private String iso;
    @TableField(value = "camera")
    @Schema(description = "相机")
    private String camera;
    @TableField(value = "lens")
    @Schema(description = "镜头")
    private String lens;
    @TableField(value = "focal_length")
    @Schema(description = "焦距")
    private String focalLength;
    @TableField(value = "introduce")
    @Schema(description = "照片介绍")
    private String introduce;
    @TableField(value = "start_rating")
    @Schema(description = "星标")
    private Integer startRating;
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}