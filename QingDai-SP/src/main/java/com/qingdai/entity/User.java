package com.qingdai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;

@Data
@TableName("user")
@Schema(name = "User实体")
public class User {
    @TableField(value = "id")
    @Schema(description = "id")
    private Integer id;
    @TableField(value = "username")
    @Schema(description = "用户名")
    private String username;
    @TableField(value = "password")
    @Schema(description = "密码")
    private String password;
    @TableField(value = "role")
    @Schema(description = "角色")
    private Integer role;
}