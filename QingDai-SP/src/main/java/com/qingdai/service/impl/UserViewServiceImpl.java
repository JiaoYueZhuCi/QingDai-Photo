package com.qingdai.service.impl;

import com.qingdai.config.RedisConfig;
import com.qingdai.service.UserViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class UserViewServiceImpl implements UserViewService {

    @Value("${qingdai.redis.key.total-view}")
    private String totalViewsKey;
    
    @Value("${qingdai.redis.ttl.uv-hours:0}")
    private int uvTtlHours;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private RedisConfig redisConfig;
    
    /**
     * 初始化方法，设置用户访问统计key的过期时间
     */
    @PostConstruct
    public void init() {
        // 确保totalViewsKey永不过期
        redisConfig.setKeyTTL(redisTemplate, totalViewsKey, uvTtlHours);
    }

    @Override
    public long getTotalViewCount() {
        return redisTemplate.opsForHyperLogLog().size(totalViewsKey);
    }

    @Override
    public long incrementViewCount(String ip) {
        redisTemplate.opsForHyperLogLog().add(totalViewsKey, ip);
        
        // 设置或更新过期时间
        redisConfig.setKeyTTL(redisTemplate, totalViewsKey, uvTtlHours);
        
        return getTotalViewCount();
    }
} 