package com.qingdai.service;

import com.qingdai.dto.GroupPhotoDTO;
import com.qingdai.entity.GroupPhoto;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-03-19
 */
public interface GroupPhotoService extends IService<GroupPhoto> {
    GroupPhotoDTO convertToDTO(GroupPhoto groupPhoto);
}
