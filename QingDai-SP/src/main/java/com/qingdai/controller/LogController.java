package com.qingdai.controller;

import com.qingdai.config.DynamicLogConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/log")
@Tag(name = "日志管理", description = "日志相关操作接口")
public class LogController {

    @GetMapping("/sql/status")
    @Operation(summary = "获取SQL日志状态", description = "获取当前SQL日志是否启用")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> getSqlLogStatus() {
        return ResponseEntity.ok(DynamicLogConfig.isSqlLoggingEnabled());
    }

    @PutMapping("/sql/toggle")
    @Operation(summary = "切换SQL日志状态", description = "启用或禁用SQL日志输出")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> toggleSqlLog(
            @Parameter(description = "是否启用SQL日志") @RequestParam boolean enabled) {
        DynamicLogConfig.setSqlLoggingEnabled(enabled);
        return ResponseEntity.ok(enabled ? "SQL日志已启用" : "SQL日志已禁用");
    }
} 