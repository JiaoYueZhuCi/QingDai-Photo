package com.qingdai.service.impl;

import com.qingdai.config.RedisConfig;
import com.qingdai.service.PhotoViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class PhotoViewServiceImpl implements PhotoViewService {

    @Value("${qingdai.redis.key.photo-view}")
    private String viewKeyPrefix;
    
    @Value("${qingdai.redis.ttl.photo-view-hours:0}")
    private int photoViewTtlHours;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private RedisConfig redisConfig;
    
    /**
     * 初始化方法，设置照片浏览量key的过期时间
     */
    @PostConstruct
    public void init() {
        // 获取所有浏览量key
        Set<String> keys = redisTemplate.keys(viewKeyPrefix + "*");
        if (keys != null && !keys.isEmpty()) {
            // 设置或更新所有key的过期时间
            for (String key : keys) {
                redisConfig.setKeyTTL(redisTemplate, key, photoViewTtlHours);
            }
            log.info("已为{}个照片浏览量key设置过期时间", keys.size());
        }
    }

    @Override
    public long incrementViewCount(String photoId, String ip) {
        String key = viewKeyPrefix + photoId;
        
        try {
            // 使用HyperLogLog记录访问
            redisTemplate.opsForHyperLogLog().add(key, ip);
            
            // 设置或更新过期时间
            redisConfig.setKeyTTL(redisTemplate, key, photoViewTtlHours);
            
            // 返回访问量
            return redisTemplate.opsForHyperLogLog().size(key);
        } catch (Exception e) {
            log.error("增加照片浏览量失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public long getViewCount(String photoId) {
        String key = viewKeyPrefix + photoId;
        
        try {
            // 返回HyperLogLog统计的访问量
            return redisTemplate.opsForHyperLogLog().size(key);
        } catch (Exception e) {
            log.error("获取照片浏览量失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public List<Map<String, Object>> getAllPhotoViewStats() {
        try {
            // 从Redis获取所有以配置的key前缀开头的key
            Set<String> keys = redisTemplate.keys(viewKeyPrefix + "*");
            List<Map<String, Object>> stats = new ArrayList<>();
            
            if (keys != null) {
                for (String key : keys) {
                    // 从key中提取photoId
                    String photoId = key.substring(viewKeyPrefix.length());
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("photoId", photoId);
                    stat.put("viewCount", getViewCount(photoId));
                    stats.add(stat);
                }
            }
            
            // 按浏览量降序排序
            stats.sort((a, b) -> {
                Long countA = (Long) a.get("viewCount");
                Long countB = (Long) b.get("viewCount");
                return countB.compareTo(countA);
            });
            
            log.info("成功获取所有照片浏览量统计，共{}条记录", stats.size());
            return stats;
        } catch (Exception e) {
            log.error("获取所有照片浏览量统计时发生错误: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
} 