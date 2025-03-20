package com.qingdai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingdai.dto.GroupPhotoDTO;
import com.qingdai.entity.GroupPhoto;
import com.qingdai.mapper.GroupPhotoMapper;
import com.qingdai.service.GroupPhotoService;
import com.qingdai.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-03-19
 */
@Service
public class GroupPhotoServiceImpl extends ServiceImpl<GroupPhotoMapper, GroupPhoto> implements GroupPhotoService {
    @Autowired
    private PhotoService photoService;

    @Override
    public GroupPhotoDTO convertToDTO(GroupPhoto groupPhoto) {
        GroupPhotoDTO dto = new GroupPhotoDTO();
        dto.setGroupPhoto(groupPhoto);

        List<String> photoIds = Arrays.asList(groupPhoto.getPhotos().split(","));
        if (groupPhoto.getCover() >= 0 && groupPhoto.getCover() < photoIds.size()) {
            Long coverId = Long.parseLong(photoIds.get(groupPhoto.getCover()));
            dto.setPhoto(photoService.getById(coverId));
        }
        return dto;
    }
}
