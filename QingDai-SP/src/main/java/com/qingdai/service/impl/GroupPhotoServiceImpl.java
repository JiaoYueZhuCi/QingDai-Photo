package com.qingdai.service.impl;

import com.qingdai.entity.dto.GroupPhotoDTO;
import com.qingdai.entity.GroupPhoto;
import com.qingdai.mapper.GroupPhotoMapper;
import com.qingdai.service.GroupPhotoService;
import com.qingdai.service.PhotoService;
import com.qingdai.service.base.BaseCachedServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import com.qingdai.service.GroupPhotoPhotoService;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-03-19
 */
@Service
@CacheConfig(cacheNames = "groupPhoto")
public class GroupPhotoServiceImpl extends BaseCachedServiceImpl<GroupPhotoMapper, GroupPhoto> implements GroupPhotoService {
    @Autowired
    private GroupPhotoPhotoService groupPhotoPhotoService;
    @Autowired
    private PhotoService photoService;

    @Override
    @Cacheable(key = "'dto_' + #id")
    public GroupPhotoDTO getGroupPhotoDTOById(String id) {
        GroupPhoto groupPhoto = getById(id);
        if (groupPhoto == null) {
            return null;
        }

        GroupPhotoDTO dto = new GroupPhotoDTO();
        dto.setGroupPhoto(groupPhoto);

        // 获取关联的照片ID列表
        List<String> photoIds = groupPhotoPhotoService.getPhotoIdsByGroupPhotoId(id);
        dto.setPhotoIds(photoIds);

        return dto;
    }

    @Override
    @Cacheable(key = "'allDTOs'")
    public List<GroupPhotoDTO> getAllGroupPhotoDTOs() {
        List<GroupPhoto> groupPhotos = list();
        return groupPhotos.stream()
                .map(groupPhoto -> {
                    GroupPhotoDTO dto = new GroupPhotoDTO();
                    dto.setGroupPhoto(groupPhoto);

                    List<String> photoIds = groupPhotoPhotoService.getPhotoIdsByGroupPhotoId(groupPhoto.getId());
                    dto.setPhotoIds(photoIds);

                    return dto;
                })
                .collect(Collectors.toList());
    }
}
