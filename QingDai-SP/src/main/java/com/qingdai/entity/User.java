package com.qingdai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user")
@Schema(name = "User实体")
public class User {
    @TableId(value = "id")
    @Schema(description = "")
    private String id;
    @TableField(value = "username")
    @Schema(description = "")
    private String username;
    @TableField(value = "password")
    @Schema(description = "")
    private String password;
    @TableField(value = "status")
    @Schema(description = "0-禁用, 1-启用")
    private Byte status;
    @TableField(value = "nickname")
    @Schema(description = "用户昵称")
    private String nickname;
    @TableField(value = "description")
    @Schema(description = "用户介绍")
    private String description;
    @TableField(value = "avatar")
    @Schema(description = "头像文件名")
    private String avatar;
    @TableField(value = "background")
    @Schema(description = "背景图文件名")
    private String background;
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}