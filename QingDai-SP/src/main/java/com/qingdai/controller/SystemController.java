package com.qingdai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统信息控制器
 */
@Slf4j
@RestController
@RequestMapping("/system")
public class SystemController {

    // 服务启动时间点
    private final LocalDateTime startTime = LocalDateTime.now();
    private final LocalDate startDate = startTime.toLocalDate();

    /**
     * 获取系统信息
     * @return 系统基本信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取系统信息", description = "获取系统基本信息")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        try {
            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("startDate", startDate.toString());
            systemInfo.put("startTime", startTime.format(DateTimeFormatter.ISO_DATE_TIME));
            systemInfo.put("currentTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            
            // 获取详细运行时间信息
            Map<String, Long> runningTime = getDetailedRunningTime();
            systemInfo.put("runningTime", runningTime);
            
            return ResponseEntity.ok(systemInfo);
        } catch (Exception e) {
            log.error("获取系统信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * 获取详细的运行时间信息，包括天、小时、分钟和秒
     * @return 包含天、小时、分钟和秒的Map
     */
    private Map<String, Long> getDetailedRunningTime() {
        Map<String, Long> result = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        long totalSeconds = ChronoUnit.SECONDS.between(startTime, now);
        
        // 计算天数
        long days = totalSeconds / (24 * 3600);
        result.put("days", days);
        
        // 计算小时
        long remainingSeconds = totalSeconds % (24 * 3600);
        long hours = remainingSeconds / 3600;
        result.put("hours", hours);
        
        // 计算分钟
        remainingSeconds = remainingSeconds % 3600;
        long minutes = remainingSeconds / 60;
        result.put("minutes", minutes);
        
        // 计算秒
        long seconds = remainingSeconds % 60;
        result.put("seconds", seconds);
        
        // 总秒数
        result.put("totalSeconds", totalSeconds);
        
        return result;
    }
} 