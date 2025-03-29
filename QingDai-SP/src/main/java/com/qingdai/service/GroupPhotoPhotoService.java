package com.qingdai.service;

import com.qingdai.entity.dto.GroupPhotoDTO;
import com.qingdai.entity.GroupPhotoPhoto;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-03-29
 */
public interface GroupPhotoPhotoService extends IService<GroupPhotoPhoto> {
    public void updateGroupPhotoPhoto(GroupPhotoDTO groupPhotoDTO);
    
    List<String> getPhotoIdsByGroupPhotoId(String groupPhotoId);
        
    void deleteByGroupPhotoId(String groupPhotoId);
    
    int countByGroupPhotoId(String groupPhotoId);
}
