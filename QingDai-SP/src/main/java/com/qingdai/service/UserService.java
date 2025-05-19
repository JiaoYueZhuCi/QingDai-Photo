package com.qingdai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qingdai.entity.User;
import com.qingdai.entity.dto.UserUpdateDTO;
import com.qingdai.entity.dto.IntroduceDTO;
import com.qingdai.entity.dto.LoginRequest;
import com.qingdai.entity.dto.UserCreateDTO;
import com.qingdai.entity.dto.UserInfoDTO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-03-08
 */
public interface UserService extends IService<User> {
    List<String> getRolesByUserId(String userId);

    List<String> getPermissionsByUserId(String userId);

    User getByUsername(String username);
    
    /**
     * 获取用户介绍信息
     * @return 用户介绍信息
     */
    IntroduceDTO getIntroduceInfo();
    
    /**
     * 更新用户信息
     * @param id 用户ID
     * @param userDTO 用户更新信息
     * @return 更新后的用户信息
     */
    User updateUserInfo(String id, UserUpdateDTO userDTO);

    /**
     * 获取头像文件名
     * @return 头像文件名
     */
    String getAvatarFileName();
    
    /**
     * 获取背景图文件名
     * @return 背景图文件名
     */
    String getBackgroundFileName();

    /**
     * 用户注册
     * @param userDTO 用户注册信息
     * @return 注册成功的用户信息
     */
    User register(UserCreateDTO userDTO);

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录token
     */
    String login(LoginRequest loginRequest);

    /**
     * 获取用户信息
     * @param token JWT token
     * @return 用户信息DTO
     */
    UserInfoDTO getUserInfo(String token);

    /**
     * 获取用户角色和权限
     * @param token JWT token
     * @return 包含角色和权限的Map
     */
    Map<String, Object> getRolesAndPermissions(String token);

    /**
     * 获取头像资源
     * @return 头像资源
     * @throws IOException 如果文件不存在或无法读取
     */
    Resource getAvatarResource() throws IOException;

    /**
     * 获取背景图资源
     * @return 背景图资源
     * @throws IOException 如果文件不存在或无法读取
     */
    Resource getBackgroundResource() throws IOException;

    /**
     * 保存头像
     * @param file 头像文件
     * @throws IOException 如果文件处理失败
     */
    void saveAvatar(MultipartFile file) throws IOException;

    /**
     * 保存背景图
     * @param file 背景图文件
     * @throws IOException 如果文件处理失败
     */
    void saveBackground(MultipartFile file) throws IOException;
}
