package com.qingdai.controller;

import com.qingdai.service.KaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 验证码控制器
 */
@Slf4j
@RestController
@RequestMapping("/captcha")
public class KaptchaController {
   
    @Autowired
    private KaptchaService captchaService;
    
    /**
     * 生成验证码
     */
    @GetMapping("/generate")
    public ResponseEntity<String> generateCaptcha(HttpServletRequest request, HttpServletResponse response) {
        return captchaService.generateKaptcha(request, response);
    }
    
    /**
     * 验证验证码
     */
    @PostMapping("/verify")
    public ResponseEntity<String> verifyCaptcha(@RequestBody Map<String, String> params, HttpServletRequest request) {
        return captchaService.verifyKaptcha(params, request);
    }
}