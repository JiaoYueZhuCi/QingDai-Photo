package com.qingdai.controller;

import com.qingdai.config.DynamicLogConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/log")
@Tag(name = "日志管理", description = "日志相关操作接口")
public class LogController {

    @GetMapping("/sql/status")
    @Operation(summary = "获取SQL日志状态", description = "获取当前SQL日志是否启用")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> getSqlLogStatus() {
        try {
            boolean status = DynamicLogConfig.isSqlLoggingEnabled();
            log.info("成功获取SQL日志状态: {}", status);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("获取SQL日志状态时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(false);
        }
    }

    @PutMapping("/sql/toggle")
    @Operation(summary = "切换SQL日志状态", description = "启用或禁用SQL日志输出")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> toggleSqlLog(
            @Parameter(description = "是否启用SQL日志") @RequestParam boolean enabled) {
        try {
            log.info("开始切换SQL日志状态为: {}", enabled);
            DynamicLogConfig.setSqlLoggingEnabled(enabled);
            log.info("成功切换SQL日志状态为: {}", enabled);
            return ResponseEntity.ok(enabled ? "SQL日志已启用" : "SQL日志已禁用");
        } catch (Exception e) {
            log.error("切换SQL日志状态时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("切换SQL日志状态失败");
        }
    }
} 