package com.qingdai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingdai.entity.Permission;
import com.qingdai.entity.Role;
import com.qingdai.entity.RolePermission;
import com.qingdai.mapper.PermissionMapper;
import com.qingdai.mapper.RoleMapper;
import com.qingdai.mapper.RolePermissionMapper;
import com.qingdai.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-03-08
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private PermissionMapper permissionMapper; // 注入 permissionMapper

    @Override
    public List<String> getPermissionsByRole(Long roleId) {
        // 获取角色对应的权限ID列表
        List<Long> permissionIds = rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, roleId))
                .stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());

        // 根据权限ID列表查询具体的权限名称
        return permissionMapper.selectList(new LambdaQueryWrapper<Permission>()
                        .in(Permission::getId, permissionIds))
                .stream()
                .map(Permission::getName)
                .collect(Collectors.toList());
    }

}