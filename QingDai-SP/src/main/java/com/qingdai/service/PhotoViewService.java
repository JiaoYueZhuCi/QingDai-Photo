package com.qingdai.service;

import java.util.List;
import java.util.Map;

/**
 * 照片浏览量服务接口
 */
public interface PhotoViewService {
    
    /**
     * 增加照片浏览量
     * @param photoId 照片ID
     * @param ip 访问者IP
     * @return 增加后的浏览量
     */
    long incrementViewCount(String photoId, String ip);
    
    /**
     * 获取照片浏览量
     * @param photoId 照片ID
     * @return 当前浏览量
     */
    long getViewCount(String photoId);

    /**
     * 获取所有照片的浏览量统计
     * @return 包含照片ID和浏览量的统计列表
     */
    List<Map<String, Object>> getAllPhotoViewStats();
} 