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
@TableName("group_photo")
@Schema(name = "组图实体")
public class GroupPhoto {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    @JsonSerialize(using = ToStringSerializer.class) // 确保序列化为字符串
    private String id;
    @TableField(value = "title")
    @Schema(description = "标题")
    private String title;
    @TableField(value = "introduce")
    @Schema(description = "介绍")
    private String introduce;
    @TableField(value = "coverPhotoId")
    @Schema(description = "封面图照片id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long coverPhotoId;
}