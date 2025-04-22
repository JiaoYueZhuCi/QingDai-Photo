package com.qingdai.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 缓存配置类
 */
@Configuration
public class CacheConfig {

    /**
     * 自定义缓存键生成器 - 类名+方法名+参数
     */
    @Bean
    public KeyGenerator classMethodParamsKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getSimpleName()).append(":");
            sb.append(method.getName());
            if (params.length > 0) {
                sb.append(":");
                sb.append(Arrays.deepToString(params));
            }
            return sb.toString();
        };
    }
    
    /**
     * 自定义缓存键生成器 - 简单类名+方法名
     */
    @Bean
    public KeyGenerator classMethodKeyGenerator() {
        return (target, method, params) -> {
            return target.getClass().getSimpleName() + ":" + method.getName();
        };
    }
    
    /**
     * 自定义缓存键生成器 - 只使用方法参数
     */
    @Bean
    public KeyGenerator paramsKeyGenerator() {
        return (target, method, params) -> {
            if (params.length == 0) {
                return "no_args";
            }
            return Arrays.deepToString(params);
        };
    }
} 