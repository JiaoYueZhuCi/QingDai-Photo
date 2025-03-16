package com.qingdai.config;

import org.apache.ibatis.logging.LogFactory;
import org.springframework.stereotype.Component;

@Component
public class DynamicLogConfig {
    
    private static boolean sqlLoggingEnabled = true;

    public static boolean isSqlLoggingEnabled() {
        return sqlLoggingEnabled;
    }

    public static void setSqlLoggingEnabled(boolean enabled) {
        sqlLoggingEnabled = enabled;
        if (enabled) {
            LogFactory.useStdOutLogging();
        } else {
            LogFactory.useNoLogging();
        }
    }
} 