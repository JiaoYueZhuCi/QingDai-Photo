package com.qingdai.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.qingdai.service.PhotoService;

/**
 * 照片缓存定时器
 * 定时刷新照片统计数据缓存
 */
@Component
public class PhotoCacheTimer {
    
    private static final Logger log = LoggerFactory.getLogger(PhotoCacheTimer.class);
    
    @Autowired
    private PhotoService photoService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 每天0点刷新照片统计数据缓存
     * 清除旧的缓存，并重新加载
     */
    @Scheduled(cron = "0 0 0 * * ?") // 秒 分 时 日 月 周
    public void refreshPhotoDashboardStatsCache() {
        log.info("定时器开始执行照片统计数据缓存刷新任务");
        try {
            // 删除旧的缓存
            boolean deleted = redisTemplate.delete("photo::dashboardStats");
            log.info("定时器删除旧缓存结果：{}", deleted ? "成功" : "缓存不存在");
            
            // 重新加载缓存（调用service方法，会触发缓存生成）
            long startTime = System.currentTimeMillis();
            photoService.getPhotoDashboardStats();
            long endTime = System.currentTimeMillis();
            
            log.info("定时器照片统计数据缓存刷新成功，耗时{}ms", endTime - startTime);
        } catch (Exception e) {
            log.error("定时器照片统计数据缓存刷新失败：{}", e.getMessage(), e);
        }
    }
} 