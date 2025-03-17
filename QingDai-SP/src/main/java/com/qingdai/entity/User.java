package com.qingdai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@TableName("sys_user")
@Schema(name = "User实体")
public class User {
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "")
    private Long id;
    @TableField(value = "username")
    @Schema(description = "")
    private String username;
    @TableField(value = "password")
    @Schema(description = "")
    private String password;
    @TableField(value = "status")
    @Schema(description = "0-禁用, 1-启用")
    private Byte status;
    @TableField(value = "created_time")
    @Schema(description = "")
    private LocalDateTime createdTime;
    @TableField(value = "updated_time")
    @Schema(description = "")
    private LocalDateTime updatedTime;
}