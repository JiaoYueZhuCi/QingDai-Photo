package com.qingdai.utils;

import com.qingdai.entity.User;
import com.qingdai.service.UserService;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Autowired
    UserService userService;

    public String generateToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        // 使用 Jwts 构建器创建 JWT 令牌
        return Jwts.builder()
                // 设置 JWT 的主题为用户的用户名
                .setSubject(user.getUsername())
                // 在 JWT 中添加用户 ID 的声明
                .claim("userId", user.getId())
                // 在 JWT 中添加用户角色的声明
                .claim("roles", userService.getRolesByUserId(user.getId()))
                // 在 JWT 中添加用户权限的声明
                .claim("permissions", userService.getPermissionsByUserId(user.getId()))
                // 设置 JWT 的签发时间为当前时间
                .setIssuedAt(new Date())
                // 设置 JWT 的过期时间为当前时间加上配置的过期时间（单位：秒）
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
//                .setExpiration(null)
                // 使用指定的密钥和签名算法对 JWT 进行签名
                .signWith(key, SignatureAlgorithm.HS256)
                // 压缩并生成最终的 JWT 字符串
                .compact();
    }

    public Jws<Claims> parseToken(String token) throws JwtException {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}