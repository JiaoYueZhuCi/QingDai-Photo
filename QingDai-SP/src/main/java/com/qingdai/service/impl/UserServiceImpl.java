package com.qingdai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingdai.entity.User;
import com.qingdai.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingdai.mapper.UserMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.naming.AuthenticationException;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    UserMapper userMapper;

    @Override
    public User authenticate(String username, String password) throws AuthenticationException {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getPassword, password));

        if (user == null) {
            throw new AuthenticationException("用户不存在或密码错误");
        }

        return user;
    }
}
