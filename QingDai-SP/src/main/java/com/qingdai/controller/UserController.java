package com.qingdai.controller;

import com.qingdai.dto.LoginRequest;
import com.qingdai.entity.User;
import com.qingdai.service.UserService;
import com.qingdai.utils.JwtTokenUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Claims;

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
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    // 用户列表查询
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

    // 用户详情查询
    @Operation(summary = "通过ID查询用户")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(
            @Parameter(name = "id", description = "用户ID", example = "1001", in = ParameterIn.PATH, required = true) @PathVariable String id) {
        try {
            log.info("开始查询用户信息，ID: {}", id);
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

    // 用户注册
    @Operation(summary = "用户注册", description = "无需认证")
    @PostMapping("/register")
    @PreAuthorize("permitAll()")
    public ResponseEntity<User> register(
            @Parameter(description = "用户注册信息", required = true) @Valid @RequestBody User user) {
        try {
            log.info("开始注册新用户，用户名: {}", user.getUsername());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            boolean isSaved = userService.save(user);
            if (isSaved) {
                log.info("用户注册成功，用户名: {}", user.getUsername());
                return ResponseEntity.ok(user);
            } else {
                log.error("用户注册失败，用户名: {}", user.getUsername());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            log.error("用户注册过程中发生错误，用户名: {}, 错误: {}", user.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 用户删除
    @Operation(summary = "删除用户", description = "需 DELETE 权限")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @Parameter(name = "id", description = "用户ID", in = ParameterIn.PATH) @PathVariable Long id) {
        try {
            log.info("开始删除用户，ID: {}", id);
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

    // 用户登录
    @Operation(summary = "用户登录", description = "用户登录接口，验证用户名和密码")
    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> login(
            @Parameter(description = "用户登录信息", required = true) @Valid @RequestBody LoginRequest user) {
        try {
            log.info("用户登录请求，用户名: {}", user.getUsername());
            User storedUser = userService.getByUsername(user.getUsername());
            Boolean matches = passwordEncoder.matches(user.getPassword(), storedUser.getPassword());
            
            if (matches) {
                String token = jwtTokenUtil.generateToken(storedUser);
                log.info("用户登录成功，用户名: {}", user.getUsername());
                return ResponseEntity.ok().header("Authorization", "Bearer " + token).body("登录成功");
            } else {
                log.warn("用户登录失败，用户名或密码错误，用户名: {}", user.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名或密码错误");
            }
        } catch (Exception e) {
            log.error("用户登录过程中发生错误，用户名: {}, 错误: {}", user.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("登录过程中发生错误");
        }
    }

    // 开发环境方法 只用于校验token信息
    @Operation(summary = "获取当前用户信息", description = "通过JWT令牌获取用户详情")
    @GetMapping("/info")
    // @PreAuthorize("permitAll()")
    @PreAuthorize("denyAll()")
    public ResponseEntity<User> getUserInfo(
            @RequestParam(value = "Authorization", required = false) String authorization) {
        try {
            log.info("开始获取用户信息，Authorization: {}", authorization);
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                log.warn("无效的Authorization头");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authorization.replace("Bearer ", "");
            try {
                Jws<Claims> claims = jwtTokenUtil.parseToken(token);
                String username = claims.getBody().getSubject();
                User user = userService.getByUsername(username);

                if (user != null) {
                    log.info("成功获取用户信息，用户名: {}", username);
                    return ResponseEntity.ok(user);
                } else {
                    log.warn("未找到用户名为{}的用户", username);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
            } catch (io.jsonwebtoken.JwtException e) {
                log.error("JWT Token解析失败: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            log.error("获取用户信息时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
