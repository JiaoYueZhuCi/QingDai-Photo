package com.qingdai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

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
    @TableField(value = "time")
    @Schema(description = "拍摄时间")
    private String time;
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
    @TableField(value = "introduce")
    @Schema(description = "照片介绍")
    private String introduce;
    @TableField(value = "start")
    @Schema(description = "星标")
    private Integer start;
}