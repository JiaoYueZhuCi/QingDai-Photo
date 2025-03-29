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
@TableName("sys_role")
@Schema(name = "Role实体")
public class Role {
    @TableId(value = "id")
    @Schema(description = "")
    private String id;
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