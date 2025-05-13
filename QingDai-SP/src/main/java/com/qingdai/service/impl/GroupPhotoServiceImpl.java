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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.qingdai.service.GroupPhotoPhotoService;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-03-19
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "groupPhoto")
public class GroupPhotoServiceImpl extends BaseCachedServiceImpl<GroupPhotoMapper, GroupPhoto>
        implements GroupPhotoService {
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

    @Override
    public Page<GroupPhotoDTO> getGroupPhotoDTOsByPage(int page, int pageSize) {
        // 先分页查询组图基本信息
        Page<GroupPhoto> groupPhotoPage = new Page<>(page, pageSize);
        page(groupPhotoPage);

        // 创建返回的DTO分页对象
        Page<GroupPhotoDTO> dtoPage = new Page<>(page, pageSize, groupPhotoPage.getTotal());

        // 转换组图数据为DTO
        List<GroupPhotoDTO> records = groupPhotoPage.getRecords().stream()
                .map(groupPhoto -> {
                    GroupPhotoDTO dto = new GroupPhotoDTO();
                    dto.setGroupPhoto(groupPhoto);

                    // 获取关联的照片ID列表
                    List<String> photoIds = groupPhotoPhotoService.getPhotoIdsByGroupPhotoId(groupPhoto.getId());
                    dto.setPhotoIds(photoIds);

                    return dto;
                })
                .collect(Collectors.toList());

        dtoPage.setRecords(records);
        return dtoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteGroupPhotoWithRelations(String id) {
        try {
            GroupPhoto groupPhoto = getById(id);
            if (groupPhoto == null) {
                log.warn("尝试删除不存在的组图，ID: {}", id);
                return false;
            }

            // 先删除关联的GroupPhotoPhoto记录
            groupPhotoPhotoService.deleteByGroupPhotoId(id);

            // 再删除组图记录
            boolean removed = removeById(id);
            if (removed) {
                log.info("成功删除组图及其关联照片，ID: {}", id);
                return true;
            } else {
                log.error("删除组图失败，ID: {}", id);
                throw new RuntimeException("删除组图失败");
            }
        } catch (Exception e) {
            log.error("删除组图时发生异常，ID: {}，错误: {}", id, e.getMessage(), e);
            throw new RuntimeException("删除组图失败: " + e.getMessage(), e);
        }
    }
}
