package com.qingdai.controller;

import com.qingdai.entity.dto.LoginRequest;
import com.qingdai.entity.dto.UserCreateDTO;
import com.qingdai.entity.dto.UserInfoDTO;
import com.qingdai.entity.User;
import com.qingdai.entity.dto.UserUpdateDTO;
import com.qingdai.entity.dto.IntroduceDTO;
import com.qingdai.service.UserService;
import com.qingdai.utils.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-03-08
 */
@Slf4j
@RestController
@Tag(name = "用户管理", description = "用户增删改查接口")
@SecurityRequirement(name = "BearerAuth")
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;
    
    @Value("${qingdai.url.introduceUrl}")
    private String introduceUrl;

    @Operation(summary = "获取所有用户", description = "需管理员权限")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userService.list();
            log.info("成功获取所有用户信息，共{}个用户", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("获取用户列表时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "通过ID查询用户")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(
            @Parameter(name = "id", description = "用户ID", example = "1001", in = ParameterIn.PATH, required = true) @PathVariable String id) {
        try {
            User user = userService.getById(id);
            if (user != null) {
                log.info("成功获取用户信息，ID: {}", id);
                return ResponseEntity.ok(user);
            } else {
                log.warn("未找到ID为{}的用户", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("查询用户信息时发生错误，ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "用户注册", description = "无需认证")
    @PostMapping("/register")
    @PreAuthorize("permitAll()")
    public ResponseEntity<User> register(
            @Parameter(description = "用户注册信息", required = true) @RequestBody UserCreateDTO userDTO) {
        try {
            User user = userService.register(userDTO);
            log.info("用户注册成功，用户名: {}", user.getUsername());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("用户注册过程中发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "删除用户", description = "需 DELETE 权限")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @Parameter(name = "id", description = "用户ID", in = ParameterIn.PATH) @PathVariable Long id) {
        try {
            if (userService.getById(id) == null) {
                log.warn("未找到ID为{}的用户", id);
                return ResponseEntity.notFound().build();
            }
            boolean isDeleted = userService.removeById(id);
            if (isDeleted) {
                log.info("成功删除用户，ID: {}", id);
                return ResponseEntity.noContent().build();
            } else {
                log.error("删除用户失败，ID: {}", id);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            log.error("删除用户时发生错误，ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "用户登录", description = "用户登录接口，验证用户名和密码")
    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> login(
            @Parameter(description = "用户登录信息", required = true) @Valid @RequestBody LoginRequest user) {
        try {
            String token = userService.login(user);
            if (token == null) {
                // 用户名或密码错误，返回401状态码
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "用户名或密码错误"));
            }
            log.info("用户登录成功，用户名: {}", user.getUsername());
            // 登录成功，返回200状态码和token
            return ResponseEntity.ok()
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                    "message", "登录成功",
                    "token", token
                ));
        } catch (Exception e) {
            log.error("用户登录过程中发生错误: {}", e.getMessage(), e);
            // 系统错误，返回500状态码
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "登录失败，系统错误"));
        }
    }

    @Operation(summary = "获取当前用户信息", description = "通过JWT令牌获取用户详情")
    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserInfoDTO> getUserInfo(
            @RequestParam(value = "testToken", required = false) String tokenParam) {
        try {
            UserInfoDTO userInfo = userService.getUserInfo(tokenParam);
            if (userInfo == null) {
                log.warn("未找到用户信息");
                return ResponseEntity.notFound().build();
            }
            log.info("成功获取当前用户信息");
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "根据header获取用户角色及权限", description = "通过JWT令牌获取用户角色及权限信息")
    @GetMapping("/me/roles-permissions")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> getRolesAndPermissions(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            Map<String, Object> result = userService.getRolesAndPermissions(authorization);
            if (result == null) {
                log.warn("未找到用户角色及权限信息");
                return ResponseEntity.notFound().build();
            }
            log.info("成功获取用户角色及权限信息");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取用户角色及权限信息时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "更新用户信息", description = "根据用户ID更新用户信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUserInfo(
            @Parameter(description = "用户ID", required = true) @PathVariable String id,
            @Parameter(description = "用户更新信息", required = true) @RequestBody UserUpdateDTO userDTO) {
        try {
            User updatedUser = userService.updateUserInfo(id, userDTO);
            log.info("成功更新用户信息，ID: {}", id);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("更新用户信息时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "获取介绍信息", description = "获取个人介绍信息，包括昵称、介绍、头像和背景图")
    @GetMapping("/introduce")
    public ResponseEntity<IntroduceDTO> getIntroduceInfo() {
        try {
            IntroduceDTO info = userService.getIntroduceInfo();
            log.info("成功获取介绍信息");
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            log.error("获取介绍信息时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(summary = "获取头像", description = "获取头像图片")
    @GetMapping("/introduce/avatar")
    public ResponseEntity<Resource> getAvatar() throws IOException {
        try {
            Resource resource = userService.getAvatarResource();
            if (resource == null) {
                log.warn("获取头像失败：文件不存在");
                return ResponseEntity.notFound().build();
            }
            
            String contentType = Files.probeContentType(resource.getFile().toPath());
            log.info("成功获取头像");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            log.error("获取头像时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(summary = "获取背景图", description = "获取背景图片")
    @GetMapping("/introduce/background")
    public ResponseEntity<Resource> getBackground() throws IOException {
        try {
            Resource resource = userService.getBackgroundResource();
            if (resource == null) {
                log.warn("获取背景图失败：文件不存在");
                return ResponseEntity.notFound().build();
            }
            
            String contentType = Files.probeContentType(resource.getFile().toPath());
            log.info("成功获取背景图");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            log.error("获取背景图时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "上传头像", description = "上传头像图片，自动压缩到100K以下")
    @PostMapping("/introduce/avatar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                log.warn("上传头像失败：文件为空");
                return ResponseEntity.badRequest().body("请选择要上传的文件");
            }
            
            if (!file.getContentType().startsWith("image/")) {
                log.warn("上传头像失败：文件类型不是图片");
                return ResponseEntity.badRequest().body("只能上传图片文件");
            }
            
            userService.saveAvatar(file);
            log.info("头像上传成功");
            return ResponseEntity.ok("头像上传成功");
        } catch (Exception e) {
            log.error("上传头像时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("上传失败：" + e.getMessage());
        }
    }

    @Operation(summary = "上传背景图", description = "上传背景图片，自动压缩到500K以下")
    @PostMapping("/introduce/background")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> uploadBackground(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                log.warn("上传背景图失败：文件为空");
                return ResponseEntity.badRequest().body("请选择要上传的文件");
            }
            
            if (!file.getContentType().startsWith("image/")) {
                log.warn("上传背景图失败：文件类型不是图片");
                return ResponseEntity.badRequest().body("只能上传图片文件");
            }
            
            userService.saveBackground(file);
            log.info("背景图上传成功");
            return ResponseEntity.ok("背景图上传成功");
        } catch (Exception e) {
            log.error("上传背景图时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("上传失败：" + e.getMessage());
        }
    }
}
