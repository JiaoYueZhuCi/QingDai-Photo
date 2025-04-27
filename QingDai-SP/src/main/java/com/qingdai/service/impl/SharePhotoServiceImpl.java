package com.qingdai.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingdai.service.SharePhotoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SharePhotoServiceImpl implements SharePhotoService {

    private static final String SHARE_PREFIX = "share::";
    private static final String PHOTO_IDS_KEY = "photoIds";
    private static final String CREATE_TIME_KEY = "createTime";
    private static final String EXPIRE_TIME_KEY = "expireTime";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public String createShareLink(String[] photoIds, int expireDays) {
        if (photoIds == null || photoIds.length == 0) {
            throw new IllegalArgumentException("照片ID列表不能为空");
        }
        if (expireDays < 1 || expireDays > 7) {
            throw new IllegalArgumentException("过期天数必须在1-7天之间");
        }

        String shareId = UUID.randomUUID().toString().replace("-", "");
        String shareKey = SHARE_PREFIX + shareId;

        // 存储分享信息到Redis
        redisTemplate.opsForHash().put(shareKey, PHOTO_IDS_KEY, String.join(",", photoIds));
        
        // 区分创建时间和过期时间
        long currentTimeMillis = System.currentTimeMillis();
        long expireTimeMillis = currentTimeMillis + expireDays * 24 * 60 * 60 * 1000L;
        
        redisTemplate.opsForHash().put(shareKey, CREATE_TIME_KEY, currentTimeMillis);
        redisTemplate.opsForHash().put(shareKey, EXPIRE_TIME_KEY, expireTimeMillis);

        // 设置过期时间
        redisTemplate.expire(shareKey, Duration.ofDays(expireDays));

        log.info("创建分享链接成功，shareId: {}, photoIds: {}, expireDays: {}", shareId, photoIds, expireDays);
        return shareId;
    }

    @Override
    public List<String> getSharePhotoIds(String shareId) {
        String shareKey = SHARE_PREFIX + shareId;
        
        // 检查分享是否存在
        if (!redisTemplate.hasKey(shareKey)) {
            log.warn("分享链接不存在，shareId: {}", shareId);
            return List.of();
        }

        // 检查是否过期
        Long expireTime = (Long) redisTemplate.opsForHash().get(shareKey, EXPIRE_TIME_KEY);
        if (expireTime != null && System.currentTimeMillis() > expireTime) {
            log.warn("分享链接已过期，shareId: {}", shareId);
            redisTemplate.delete(shareKey);
            return List.of();
        }

        // 获取照片ID列表
        String photoIdsStr = (String) redisTemplate.opsForHash().get(shareKey, PHOTO_IDS_KEY);
        if (photoIdsStr == null || photoIdsStr.isEmpty()) {
            log.warn("分享链接中没有照片ID，shareId: {}", shareId);
            return List.of();
        }

        return Arrays.asList(photoIdsStr.split(","));
    }

    @Override
    public boolean validateShareLink(String shareId) {
        String shareKey = SHARE_PREFIX + shareId;
        
        // 检查分享是否存在
        if (!redisTemplate.hasKey(shareKey)) {
            log.warn("分享链接不存在，shareId: {}", shareId);
            return false;
        }

        // 检查是否过期
        Long expireTime = (Long) redisTemplate.opsForHash().get(shareKey, EXPIRE_TIME_KEY);
        if (expireTime != null && System.currentTimeMillis() > expireTime) {
            log.warn("分享链接已过期，shareId: {}", shareId);
            redisTemplate.delete(shareKey);
            return false;
        }

        return true;
    }
    
    @Override
    public List<Map<String, Object>> getAllShares() {
        // 获取所有以SHARE_PREFIX开头的key
        Set<String> keys = redisTemplate.keys(SHARE_PREFIX + "*");
        List<Map<String, Object>> result = new ArrayList<>();
        
        if (keys == null || keys.isEmpty()) {
            log.info("未找到任何分享");
            return result;
        }
        
        long currentTime = System.currentTimeMillis();
        
        for (String key : keys) {
            String shareId = key.substring(SHARE_PREFIX.length());
            Map<Object, Object> shareData = redisTemplate.opsForHash().entries(key);
            
            if (shareData.isEmpty()) {
                log.warn("分享数据为空，shareId: {}", shareId);
                continue;
            }
            
            // 获取基本信息
            String photoIdsStr = (String) shareData.get(PHOTO_IDS_KEY);
            Long createTimeMs = (Long) shareData.get(CREATE_TIME_KEY);
            Long expireTimeMs = (Long) shareData.get(EXPIRE_TIME_KEY);
            
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
        String shareKey = SHARE_PREFIX + shareId;
        
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