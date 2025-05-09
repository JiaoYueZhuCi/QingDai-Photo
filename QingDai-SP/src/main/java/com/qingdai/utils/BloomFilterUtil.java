package com.qingdai.utils;

import com.qingdai.redis.RedisBloomCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 布隆过滤器工具类
 */
@Component
public class BloomFilterUtil {
    
    private static final Logger log = LoggerFactory.getLogger(BloomFilterUtil.class);
    
    @Autowired
    private RedisBloomCommands redisBloomCommands;
    
    private static final String PHOTO_BLOOM_FILTER = "photo:bloom:filter";
    private static final double ERROR_RATE = 0.01; // 1%的误判率
    private static final long INITIAL_SIZE = 1000000; // 初始容量100万
    
    /**
     * 初始化布隆过滤器
     */
    @PostConstruct
    public void init() {
        log.info("初始化布隆过滤器: {}", PHOTO_BLOOM_FILTER);
        try {
            Boolean result = redisBloomCommands.reserve(PHOTO_BLOOM_FILTER, ERROR_RATE, INITIAL_SIZE);
            log.info("布隆过滤器初始化结果: {}", result != null ? result : "过滤器已存在");
        } catch (Exception e) {
            // 如果布隆过滤器已存在，会抛出异常，这是正常的
            log.info("布隆过滤器可能已存在: {}", e.getMessage());
        }
    }
    
    /**
     * 添加元素到布隆过滤器
     * @param value 要添加的值
     * @return 如果值已存在返回false，否则返回true
     */
    public boolean add(String value) {
        try {
            Boolean result = redisBloomCommands.add(PHOTO_BLOOM_FILTER, value);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("添加元素到布隆过滤器失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 批量添加元素到布隆过滤器
     * @param values 要添加的值集合
     */
    public void addBatch(Iterable<String> values) {
        for (String value : values) {
            add(value);
        }
    }
    
    /**
     * 检查元素是否可能存在于布隆过滤器中
     * @param value 要检查的值
     * @return 如果值可能存在返回true，否则返回false
     */
    public boolean exists(String value) {
        try {
            Boolean result = redisBloomCommands.exists(PHOTO_BLOOM_FILTER, value);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("检查元素是否存在于布隆过滤器失败: {}", e.getMessage(), e);
            // 在查询操作发生异常时，为了安全起见，默认返回true，
            // 这样会回退到直接查询数据库，避免误判
            return true;
        }
    }
} 