package com.qingdai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
     * 分页获取分享
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<Map<String, Object>> getSharesByPage(int page, int pageSize);
    
    /**
     * 删除分享
     * @param shareId 分享ID
     * @return 是否删除成功
     */
    boolean deleteShare(String shareId);
} 