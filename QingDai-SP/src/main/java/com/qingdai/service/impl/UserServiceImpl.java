package com.qingdai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingdai.entity.Role;
import com.qingdai.entity.User;
import com.qingdai.entity.UserRole;
import com.qingdai.entity.dto.IntroduceDTO;
import com.qingdai.entity.dto.LoginRequest;
import com.qingdai.entity.dto.UserCreateDTO;
import com.qingdai.entity.dto.UserInfoDTO;
import com.qingdai.entity.dto.UserUpdateDTO;
import com.qingdai.mapper.RoleMapper;
import com.qingdai.mapper.UserMapper;
import com.qingdai.mapper.UserRoleMapper;
import com.qingdai.service.FileProcessService;
import com.qingdai.service.RoleService;
import com.qingdai.service.UserService;
import com.qingdai.service.base.BaseCachedServiceImpl;
import com.qingdai.utils.DateUtils;
import com.qingdai.utils.JwtTokenUtil;
import com.qingdai.utils.SnowflakeIdGenerator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-03-08
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "user")
public class UserServiceImpl extends BaseCachedServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private RoleService roleService;
    
    @Lazy
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Lazy
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Value("${qingdai.photographer.id}")
    private String photographerId;

    @Value("${qingdai.url.introduceUrl}")
    private String introduceUrl;

    // 雪花算法生成器
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);

    @Autowired
    private FileProcessService fileProcessService;

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

    @Override
    public IntroduceDTO getIntroduceInfo() {
        // 获取摄影师用户信息
        User user = getById(photographerId);
        
        if (user == null) {
            return new IntroduceDTO();
        }
        
        IntroduceDTO introduceDTO = new IntroduceDTO();
        introduceDTO.setNickname(user.getNickname());
        introduceDTO.setDescription(user.getDescription());
        
        return introduceDTO;
    }
    
    @Override
    public String getAvatarFileName() {
        // 获取摄影师用户
        User user = getById(photographerId);
        
        if (user == null) {
            return null;
        }
        
        return user.getAvatar();
    }
    
    @Override
    public String getBackgroundFileName() {
        // 获取摄影师用户
        User user = getById(photographerId);
        
        if (user == null) {
            return null;
        }
        
        return user.getBackground();
    }

    @Override
    public User register(UserCreateDTO userDTO) {
        String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
        userDTO.setPassword(encodedPassword);

        LocalDateTime nowWithZone = DateUtils.getLocalDateTime();
        User user = new User(idGenerator.nextId(), userDTO.getUsername(), userDTO.getPassword(), (byte) 1,
                "", "", "", "",
                nowWithZone, nowWithZone);
        
        if (save(user)) {
            log.info("用户注册成功，用户名: {}", user.getUsername());
            return user;
        } else {
            log.error("用户注册失败，用户名: {}", user.getUsername());
            throw new RuntimeException("用户注册失败");
        }
    }

    @Override
    public String login(LoginRequest loginRequest) {
        log.info("用户登录请求，用户名: {}", loginRequest.getUsername());
        User storedUser = getByUsername(loginRequest.getUsername());
        if (storedUser == null) {
            log.warn("用户登录失败，用户不存在，用户名: {}", loginRequest.getUsername());
            return null; // 返回null表示登录失败
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), storedUser.getPassword())) {
            log.warn("用户登录失败，用户名或密码错误，用户名: {}", loginRequest.getUsername());
            return null; // 返回null表示登录失败
        }

        log.info("用户登录成功，用户名: {}", loginRequest.getUsername());
        return jwtTokenUtil.generateToken(storedUser);
    }

    @Override
    public UserInfoDTO getUserInfo(String token) {
        if (token == null) {
            log.warn("未提供有效的token");
            return null;
        }

        try {
            Jws<Claims> claims = jwtTokenUtil.parseToken(token);
            String username = claims.getBody().getSubject();
            User user = getByUsername(username);

            if (user == null) {
                log.warn("未找到用户: {}", username);
                return null;
            }

            String userId = user.getId();
            List<String> roles = getRolesByUserId(userId);
            List<String> permissions = getPermissionsByUserId(userId);

            return new UserInfoDTO(user, roles, permissions);
        } catch (io.jsonwebtoken.JwtException e) {
            log.error("JWT Token解析失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, Object> getRolesAndPermissions(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("无效的Authorization头");
            return null;
        }

        token = token.replace("Bearer ", "");
        try {
            Jws<Claims> claims = jwtTokenUtil.parseToken(token);
            String username = claims.getBody().getSubject();
            User user = getByUsername(username);
            String userId = user.getId();
            List<String> roles = getRolesByUserId(userId);
            List<String> permissions = getPermissionsByUserId(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("roles", roles);
            result.put("permissions", permissions);

            return result;
        } catch (io.jsonwebtoken.JwtException e) {
            log.error("JWT Token解析失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public User updateUserInfo(String id, UserUpdateDTO userDTO) {
        User existingUser = getById(id);
        if (existingUser == null) {
            throw new RuntimeException("未找到用户");
        }

        // 更新基本信息
        if (userDTO != null) {
            if (userDTO.getUsername() != null) {
                existingUser.setUsername(userDTO.getUsername());
            }
            if (userDTO.getPassword() != null) {
                existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            }
            if (userDTO.getNickname() != null) {
                existingUser.setNickname(userDTO.getNickname());
            }
            if (userDTO.getDescription() != null) {
                existingUser.setDescription(userDTO.getDescription());
            }
        }

        if (updateById(existingUser)) {
            return existingUser;
        } else {
            throw new RuntimeException("更新用户信息失败");
        }
    }

    @Override
    public Resource getAvatarResource() throws IOException {
        String avatarFileName = getAvatarFileName();
        if (avatarFileName == null || avatarFileName.isEmpty()) {
            return null;
        }
        
        Path imagePath = Paths.get(introduceUrl + avatarFileName);
        if (!Files.exists(imagePath)) {
            return null;
        }
        
        return new FileSystemResource(imagePath.toFile());
    }
    
    @Override
    public Resource getBackgroundResource() throws IOException {
        String backgroundFileName = getBackgroundFileName();
        if (backgroundFileName == null || backgroundFileName.isEmpty()) {
            return null;
        }
        
        Path imagePath = Paths.get(introduceUrl + backgroundFileName);
        if (!Files.exists(imagePath)) {
            return null;
        }
        
        return new FileSystemResource(imagePath.toFile());
    }

    @Override
    public void saveAvatar(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String newFileName = "avatar.jpg";
        Path targetPath = Paths.get(introduceUrl + newFileName);
        
        // 确保目录存在
        Files.createDirectories(targetPath.getParent());
        
        // 创建临时文件
        File tempFile = new File(System.getProperty("java.io.tmpdir"), newFileName);
        file.transferTo(tempFile);
        
        try {
            // 使用FileProcessService压缩图片到100K以下
            fileProcessService.thumbnailPhotosFromFolderToFolder(
                tempFile.getParentFile(),
                targetPath.getParent().toFile(),
                100,  // 100KB
                true  // 覆盖已存在的文件
            );
            
            // 更新用户头像文件名
            User user = getById(photographerId);
            if (user != null) {
                user.setAvatar(newFileName);
                updateById(user);
            }
        } finally {
            // 清理临时文件
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @Override
    public void saveBackground(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String newFileName = "background.jpg";
        Path targetPath = Paths.get(introduceUrl + newFileName);
        
        // 确保目录存在
        Files.createDirectories(targetPath.getParent());
        
        // 创建临时文件
        File tempFile = new File(System.getProperty("java.io.tmpdir"), newFileName);
        file.transferTo(tempFile);
        
        try {
            // 使用FileProcessService压缩图片到500K以下
            fileProcessService.thumbnailPhotosFromFolderToFolder(
                tempFile.getParentFile(),
                targetPath.getParent().toFile(),
                500,  // 500KB
                true  // 覆盖已存在的文件
            );
            
            // 更新用户背景图文件名
            User user = getById(photographerId);
            if (user != null) {
                user.setBackground(newFileName);
                updateById(user);
            }
        } finally {
            // 清理临时文件
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
