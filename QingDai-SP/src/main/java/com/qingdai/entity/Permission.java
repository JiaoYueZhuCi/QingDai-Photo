package com.qingdai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_permission")
@Schema(name = "Permission实体")
public class Permission {
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "")
    private Long id;
    @TableField(value = "code")
    @Schema(description = "")
    private String code;
    @TableField(value = "name")
    @Schema(description = "")
    private String name;
    @TableField(value = "description")
    @Schema(description = "")
    private String description;
    @TableField(value = "created_time")
    @Schema(description = "")
    private LocalDateTime createdTime;
    @TableField(value = "updated_time")
    @Schema(description = "")
    private LocalDateTime updatedTime;
}