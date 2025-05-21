package com.qingdai.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingdai.service.SharePhotoService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SharePhotoServiceImpl implements SharePhotoService {

    @Value("${qingdai.redis.key.share}")
    private String sharePrefix;

    @Value("${qingdai.redis.key.share-photo-ids}")
    private String photoIdsKey;

    @Value("${qingdai.redis.key.share-create-time}")
    private String createTimeKey;

    @Value("${qingdai.redis.key.share-expire-time}")
    private String expireTimeKey;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 存储使用的Hash数据结构：
    // photoIds:"287145287229771776"
    // createTime:1747730519393
    // expireTime:1747816919393
    @Override
    public String createShareLink(String[] photoIds, int expireDays) {
        if (photoIds == null || photoIds.length == 0) {
            throw new IllegalArgumentException("照片ID列表不能为空");
        }
        if (expireDays < 1 || expireDays > 7) {
            throw new IllegalArgumentException("过期天数必须在1-7天之间");
        }

        String shareId = UUID.randomUUID().toString().replace("-", "");
        String shareKey = sharePrefix + shareId;

        // 存储分享信息到Redis
        redisTemplate.opsForHash().put(shareKey, photoIdsKey, String.join(",", photoIds));
        
        // 区分创建时间和过期时间
        long currentTimeMillis = System.currentTimeMillis();
        long expireTimeMillis = currentTimeMillis + expireDays * 24 * 60 * 60 * 1000L;
        
        redisTemplate.opsForHash().put(shareKey, createTimeKey, currentTimeMillis);
        redisTemplate.opsForHash().put(shareKey, expireTimeKey, expireTimeMillis);

        // 设置过期时间
        redisTemplate.expire(shareKey, Duration.ofDays(expireDays));

        log.info("创建分享链接成功，shareId: {}, photoIds: {}, expireDays: {}", shareId, photoIds, expireDays);
        return shareId;
    }

    @Override
    public List<String> getSharePhotoIds(String shareId) {
        String shareKey = sharePrefix + shareId;
        
        // 检查分享是否存在
        if (!redisTemplate.hasKey(shareKey)) {
            log.warn("分享链接不存在，shareId: {}", shareId);
            return List.of();
        }

        // 检查是否过期
        Long expireTime = (Long) redisTemplate.opsForHash().get(shareKey, expireTimeKey);
        if (expireTime != null && System.currentTimeMillis() > expireTime) {
            log.warn("分享链接已过期，shareId: {}", shareId);
            redisTemplate.delete(shareKey);
            return List.of();
        }

        // 获取照片ID列表
        String photoIdsStr = (String) redisTemplate.opsForHash().get(shareKey, photoIdsKey);
        if (photoIdsStr == null || photoIdsStr.isEmpty()) {
            log.warn("分享链接中没有照片ID，shareId: {}", shareId);
            return List.of();
        }

        return Arrays.asList(photoIdsStr.split(","));
    }

    @Override
    public boolean validateShareLink(String shareId) {
        String shareKey = sharePrefix + shareId;
        
        // 检查分享是否存在
        if (!redisTemplate.hasKey(shareKey)) {
            log.warn("分享链接不存在，shareId: {}", shareId);
            return false;
        }

        // 检查是否过期
        Long expireTime = (Long) redisTemplate.opsForHash().get(shareKey, expireTimeKey);
        if (expireTime != null && System.currentTimeMillis() > expireTime) {
            log.warn("分享链接已过期，shareId: {}", shareId);
            redisTemplate.delete(shareKey);
            return false;
        }

        return true;
    }
    
    @Override
    public List<Map<String, Object>> getAllShares() {
        // 获取所有以sharePrefix开头的key
        Set<String> keys = redisTemplate.keys(sharePrefix + "*");
        List<Map<String, Object>> result = new ArrayList<>();
        
        if (keys == null || keys.isEmpty()) {
            log.info("未找到任何分享");
            return result;
        }
        
        long currentTime = System.currentTimeMillis();
        
        for (String key : keys) {
            String shareId = key.substring(sharePrefix.length());
            Map<Object, Object> shareData = redisTemplate.opsForHash().entries(key);
            
            if (shareData.isEmpty()) {
                log.warn("分享数据为空，shareId: {}", shareId);
                continue;
            }
            
            // 获取基本信息
            String photoIdsStr = (String) shareData.get(photoIdsKey);
            Long createTimeMs = (Long) shareData.get(createTimeKey);
            Long expireTimeMs = (Long) shareData.get(expireTimeKey);
            
            if (photoIdsStr == null || createTimeMs == null || expireTimeMs == null) {
                log.warn("分享数据不完整，shareId: {}", shareId);
                continue;
            }
            
            // 确保创建时间和过期时间使用正确的时间戳
            String createTime = formatTime(createTimeMs);
            String expireTime = formatTime(expireTimeMs);
            
            boolean isExpired = currentTime > expireTimeMs; 
            
            // 构建返回数据
            Map<String, Object> shareInfo = new HashMap<>();
            shareInfo.put("id", shareId);
            shareInfo.put("photoIds", Arrays.asList(photoIdsStr.split(",")));
            shareInfo.put("createTime", createTime);
            shareInfo.put("expireTime", expireTime);
            shareInfo.put("isExpired", isExpired);
            
            result.add(shareInfo);
        }
        
        return result;
    }
    
    @Override
    public Page<Map<String, Object>> getSharesByPage(int page, int pageSize) {
        // 获取所有分享信息
        List<Map<String, Object>> allShares = getAllShares();
        
        // 创建分页对象
        Page<Map<String, Object>> sharesPage = new Page<>(page, pageSize);
        
        // 计算总记录数
        int total = allShares.size();
        sharesPage.setTotal(total);
        
        // 计算总页数
        int pages = (total + pageSize - 1) / pageSize;
        sharesPage.setPages(pages);
        
        // 检查页码是否超出范围
        if (page > pages && pages > 0) {
            page = pages;
            sharesPage.setCurrent(page);
        }
        
        // 计算当前页的起始索引和结束索引
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        // 处理边界情况
        if (fromIndex >= total) {
            sharesPage.setRecords(Collections.emptyList());
        } else {
            // 提取当前页的数据
            List<Map<String, Object>> pageRecords = allShares.subList(fromIndex, toIndex);
            sharesPage.setRecords(pageRecords);
        }
        
        log.info("成功分页获取分享信息，页码: {}, 每页大小: {}, 总记录数: {}, 总页数: {}", 
                page, pageSize, sharesPage.getTotal(), sharesPage.getPages());
        
        return sharesPage;
    }
    
    @Override
    public boolean deleteShare(String shareId) {
        String shareKey = sharePrefix + shareId;
        
        // 检查分享是否存在
        if (!redisTemplate.hasKey(shareKey)) {
            log.warn("要删除的分享不存在，shareId: {}", shareId);
            return false;
        }
        
        // 删除分享
        Boolean deleted = redisTemplate.delete(shareKey);
        if (Boolean.TRUE.equals(deleted)) {
            log.info("成功删除分享，shareId: {}", shareId);
            return true;
        } else {
            log.error("删除分享失败，shareId: {}", shareId);
            return false;
        }
    }
    
    /**
     * 将毫秒时间戳格式化为日期时间字符串
     */
    private String formatTime(Long timeMillis) {
        if (timeMillis == null) {
            return "";
        }
        
        // 确保使用正确的时区和格式
        LocalDateTime dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timeMillis), 
            ZoneId.systemDefault()
        );
        
        return dateTime.format(DATE_FORMATTER);
    }
} 