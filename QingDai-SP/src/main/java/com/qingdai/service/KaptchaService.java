package com.qingdai.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.http.ResponseEntity;

/**
 * 验证码服务接口
 */
public interface KaptchaService {
    
    /**
     * 生成验证码
     * @param request HTTP请求
     * @param response HTTP响应
     * @return 验证码图片的Base64编码
     */
    ResponseEntity<String> generateKaptcha(HttpServletRequest request, HttpServletResponse response);
    
    /**
     * 验证验证码
     * @param params 包含用户输入验证码的参数
     * @param request HTTP请求
     * @return 验证结果
     */
    ResponseEntity<String> verifyKaptcha(Map<String, String> params, HttpServletRequest request);
} 