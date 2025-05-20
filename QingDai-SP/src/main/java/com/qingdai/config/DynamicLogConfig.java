package com.qingdai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 动态日志配置
 * 用于控制SQL日志的启用和禁用
 */
@Component
public class DynamicLogConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DynamicLogConfig.class);
    private static boolean sqlLoggingEnabled = false;

    /**
     * 检查SQL日志是否已启用
     */
    public static boolean isSqlLoggingEnabled() {
        return sqlLoggingEnabled;
    }

    /**
     * 设置SQL日志启用状态
     * @param enabled true表示启用，false表示禁用
     */
    public static void setSqlLoggingEnabled(boolean enabled) {
        if (enabled == sqlLoggingEnabled) {
            // 状态未改变，不需要操作
            logger.info("SQL日志状态未改变，当前状态：{}", enabled ? "已启用" : "已禁用");
            return;
        }

        sqlLoggingEnabled = enabled;
        if (enabled) {
            logger.info("========== SQL日志已启用 ==========");
        } else {
            logger.info("========== SQL日志已禁用 ==========");
        }
    }
} 