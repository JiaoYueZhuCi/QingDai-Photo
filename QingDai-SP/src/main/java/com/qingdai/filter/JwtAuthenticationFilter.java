package com.qingdai.filter;

import com.qingdai.utils.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JwtAuthenticationFilter 类用于在每个请求中验证 JWT 令牌。
 * 它继承自 OncePerRequestFilter，确保每个请求只被过滤一次。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 处理每个请求的内部过滤逻辑。
     *
     * @param request  当前的 HTTP 请求。
     * @param response 当前的 HTTP 响应。
     * @param chain    过滤器链，用于将请求传递给下一个过滤器。
     * @throws ServletException 如果在处理请求时发生 Servlet 异常。
     * @throws IOException      如果在处理请求时发生 I/O 异常。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.replace("Bearer ", "");
        try {
            Jws<Claims> claims = jwtTokenUtil.parseToken(token);
            String username = claims.getBody().getSubject();
            // 获取角色列表
            List<String> roles = claims.getBody().get("roles", List.class);
            // 获取权限列表
            List<String> permissions = claims.getBody().get("permissions", List.class);

            // 使用stream处理多个角色
            List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

            // 检查权限列表是否为空，如果不为空则处理多个权限
            if (permissions != null && !permissions.isEmpty()) {
                List<SimpleGrantedAuthority> permissionAuthorities = permissions.stream()
                    .map(permission -> new SimpleGrantedAuthority(permission))
                    .collect(Collectors.toList());

                // 合并角色和权限
                authorities.addAll(permissionAuthorities);
            }

            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    username, 
                    null, 
                    authorities  // 包含多个权限的列表
                );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            return;
        }
        chain.doFilter(request, response);
    }
}