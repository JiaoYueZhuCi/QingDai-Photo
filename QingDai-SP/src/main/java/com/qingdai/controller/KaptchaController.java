package com.qingdai.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/captcha")
public class KaptchaController {
   
    @Autowired
    private DefaultKaptcha defaultKaptcha;
    
    private static final String CAPTCHA_SESSION_KEY = "captchaCode";
    
    /**
     * 生成验证码
     */
    @GetMapping("/generate")
    public ResponseEntity<String> generateCaptcha(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 生成验证码文本
            String captchaText = defaultKaptcha.createText();
            
            // 获取或创建会话
            HttpSession session = request.getSession(true);
            String sessionId = session.getId();
            
            // 将验证码存入session
            session.setAttribute(CAPTCHA_SESSION_KEY, captchaText);
            session.setMaxInactiveInterval(300); // 设置session过期时间为5分钟
            
            log.info("生成验证码: {}, 会话ID: {}", captchaText, sessionId);
            
            // 确保会话ID在响应中作为cookie返回
            Cookie sessionCookie = new Cookie("JSESSIONID", sessionId);
            sessionCookie.setPath("/");
            sessionCookie.setHttpOnly(true);
            response.addCookie(sessionCookie);
            
            // 设置响应头，禁止缓存
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
            // 根据验证码文本生成验证码图片
            BufferedImage captchaImage = defaultKaptcha.createImage(captchaText);
            
            // 将图片转为Base64字符串
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(captchaImage, "jpg", outputStream);
            String base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            
            // 直接返回Base64编码的图片
            return ResponseEntity.ok("data:image/jpg;base64," + base64Image);
        } catch (IOException e) {
            log.error("生成验证码失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 验证验证码
     */
    @PostMapping("/verify")
    public ResponseEntity<String> verifyCaptcha(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String userCaptcha = params.get("captcha");
        
        // 获取会话，不创建新会话
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            log.warn("验证失败：未找到会话");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("验证码已过期，请刷新");
        }
        
        String sessionCaptcha = (String) session.getAttribute(CAPTCHA_SESSION_KEY);
        log.info("验证验证码：用户输入 = '{}', 会话中的验证码 = '{}'", userCaptcha, sessionCaptcha);
        
        if (sessionCaptcha == null) {
            log.warn("验证失败：会话中未找到用户输入的验证码");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("验证码已过期，请刷新");
        }
        
        // 验证码验证成功后，删除session中的验证码
        session.removeAttribute(CAPTCHA_SESSION_KEY);
        
        if (userCaptcha.equalsIgnoreCase(sessionCaptcha)) {
            log.info("验证成功");
            return ResponseEntity.ok("验证码正确");
        } else {
            log.info("验证失败：验证码不匹配");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("验证码错误");
        }
    }
}