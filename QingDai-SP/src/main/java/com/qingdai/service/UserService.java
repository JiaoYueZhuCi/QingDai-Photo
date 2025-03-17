package com.qingdai.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.qingdai.entity.User;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-03-08
 */
public interface UserService extends IService<User> {
    List<String> getRolesByUserId(Long userId);

    List<String> getPermissionsByUserId(Long userId);

    User getByUsername(String username);
}
