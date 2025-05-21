package com.qingdai.controller;

import com.qingdai.service.UserViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@Tag(name = "网站访问统计", description = "网站访问量相关操作接口")
@RequestMapping("/views")
public class UserViewController {

    @Autowired
    private UserViewService userViewService;

    @GetMapping("/total")
    @PreAuthorize("permitAll()")
    @Operation(summary = "获取网站总浏览量", description = "获取网站的总访问量")
    public ResponseEntity<Long> getTotalViewCount() {
        try {
            long count = userViewService.getTotalViewCount();
            log.info("成功获取网站总浏览量: {}", count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("获取网站总浏览量时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/increment")
    @PreAuthorize("permitAll()")
    @Operation(summary = "增加网站浏览量", description = "增加网站的总访问量")
    public ResponseEntity<Long> incrementViewCount(HttpServletRequest request) {
        try {
            String ip = getClientIp(request);
            long count = userViewService.incrementViewCount(ip);
            log.info("成功增加网站浏览量，IP: {}, 当前总浏览量: {}", ip, count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("增加网站浏览量时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP，多个IP按照','分割
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
} 