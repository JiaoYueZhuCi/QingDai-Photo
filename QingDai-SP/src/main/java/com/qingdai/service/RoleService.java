package com.qingdai.service;

import com.qingdai.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-03-08
 */
public interface RoleService extends IService<Role> {

    List<String> getPermissionsByRole(Long roleId);
}
