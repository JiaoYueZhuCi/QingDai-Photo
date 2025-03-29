package com.qingdai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("group_photo_photo")
@Schema(name = "GroupPhotoPhoto实体")
public class GroupPhotoPhoto {
    @TableField(value = "groupPhotoId")
    @Schema(description = "")
    @JsonSerialize(using = ToStringSerializer.class)
    private String groupPhotoId;
    @TableField(value = "photoId")
    @Schema(description = "")
    @JsonSerialize(using = ToStringSerializer.class)
    private String photoId;
}