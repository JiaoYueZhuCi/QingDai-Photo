package com.qingdai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;

@Data
@TableName("sys_role_permission")
@Schema(name = "RolePermission实体")
public class RolePermission {
    @TableField(value = "role_id")
    @Schema(description = "")
    private String roleId;
    @TableField(value = "permission_id")
    @Schema(description = "")
    private String permissionId;
}