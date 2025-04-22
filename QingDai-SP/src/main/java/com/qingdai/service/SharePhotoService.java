package com.qingdai.service;

import java.util.List;
import java.util.Map;

public interface SharePhotoService {
    /**
     * 创建分享链接
     * @param photoIds 照片ID列表
     * @param expireDays 过期天数
     * @return 分享ID
     */
    String createShareLink(String[] photoIds, int expireDays);

    /**
     * 获取分享的照片ID列表
     * @param shareId 分享ID
     * @return 照片ID列表
     */
    List<String> getSharePhotoIds(String shareId);

    /**
     * 验证分享链接是否有效
     * @param shareId 分享ID
     * @return 是否有效
     */
    boolean validateShareLink(String shareId);
    
    /**
     * 获取所有分享
     * @return 所有分享信息的列表
     */
    List<Map<String, Object>> getAllShares();
    
    /**
     * 删除分享
     * @param shareId 分享ID
     * @return 是否删除成功
     */
    boolean deleteShare(String shareId);
} 