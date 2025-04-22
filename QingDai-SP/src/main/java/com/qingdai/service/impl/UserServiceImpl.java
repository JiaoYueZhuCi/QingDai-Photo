package com.qingdai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingdai.entity.Role;
import com.qingdai.entity.User;
import com.qingdai.entity.UserRole;
import com.qingdai.mapper.RoleMapper;
import com.qingdai.mapper.UserMapper;
import com.qingdai.mapper.UserRoleMapper;
import com.qingdai.service.RoleService;
import com.qingdai.service.UserService;
import com.qingdai.service.base.BaseCachedServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
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
@CacheConfig(cacheNames = "user")
public class UserServiceImpl extends BaseCachedServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private RoleService roleService;

    @Override
    @Cacheable(key = "'roles_' + #userId")
    public List<String> getRolesByUserId(String userId) {
        // 查询用户角色关联
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userId)
        );

        if (userRoles.isEmpty()) {
            return List.of(); // 返回空列表
        }

        // 获取角色ID列表
        List<String> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());

        // 查询角色名称
        return roleMapper.selectList(
                        new LambdaQueryWrapper<Role>()
                                .in(Role::getId, roleIds)
                )
                .stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(key = "'permissions_' + #userId")
    public List<String> getPermissionsByUserId(String userId) {
        // 获取用户角色ID列表
        List<String> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userId)
                )
                .stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return List.of();
        }

        // 根据角色ID查询权限
        return roleIds.stream()
                .flatMap(roleId -> roleService.getPermissionsByRole(roleId).stream())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(key = "'username_' + #username")
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }
}
