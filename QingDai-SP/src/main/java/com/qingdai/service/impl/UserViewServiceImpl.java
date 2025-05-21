package com.qingdai.service.impl;

import com.qingdai.service.UserViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserViewServiceImpl implements UserViewService {

    @Value("${qingdai.redis.key.total-view}")
    private String totalViewsKey;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public long getTotalViewCount() {
        return redisTemplate.opsForHyperLogLog().size(totalViewsKey);
    }

    @Override
    public long incrementViewCount(String ip) {
        redisTemplate.opsForHyperLogLog().add(totalViewsKey, ip);
        return getTotalViewCount();
    }
} 