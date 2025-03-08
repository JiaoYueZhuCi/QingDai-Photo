package com.qingdai.service;

import com.qingdai.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.naming.AuthenticationException;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-03-08
 */
public interface UserService extends IService<User> {
    User authenticate(String username, String password) throws AuthenticationException;
}
