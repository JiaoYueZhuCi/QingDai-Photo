package com.qingdai.service.impl;

import com.qingdai.entity.User;
import com.qingdai.mapper.RoleMapper;
import com.qingdai.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingdai.mapper.UserRoleMapper;
import com.qingdai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingdai.entity.UserRole;
import com.qingdai.entity.Role;
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
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private RoleMapper roleMapper;

    @Override
    public User getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    /**
     * 根据用户ID获取用户的角色列表
     *
     * @param userId 用户ID
     * @return 角色名称列表
     */
    public List<String> getRolesByUserId(Long userId) {
        // 查询用户角色关联表，获取该用户对应的所有角色ID
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)) // 筛选出指定用户ID的记录
                .stream()
                .map(UserRole::getRoleId) // 提取每条记录中的角色ID
                .collect(Collectors.toList());

        // 根据角色ID列表查询角色表，获取对应的角色信息，并提取角色名称
        return roleMapper.selectBatchIds(roleIds)
                .stream()
                .map(Role::getName) // 提取每个角色的名称
                .collect(Collectors.toList());
    }
}
