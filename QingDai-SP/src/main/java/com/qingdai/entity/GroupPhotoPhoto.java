package com.qingdai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

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
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;

    public GroupPhotoPhoto(String groupPhotoId, String photoId) {
        this.groupPhotoId = groupPhotoId;
        this.photoId = photoId;
    }
}