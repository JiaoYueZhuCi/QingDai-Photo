package com.qingdai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingdai.entity.dto.GroupPhotoDTO;
import com.qingdai.entity.GroupPhotoPhoto;
import com.qingdai.mapper.GroupPhotoPhotoMapper;
import com.qingdai.service.GroupPhotoPhotoService;
import com.qingdai.service.base.BaseCachedServiceImpl;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-03-29
 */
@Service
@CacheConfig(cacheNames = "groupPhotoPhoto")
public class GroupPhotoPhotoServiceImpl extends BaseCachedServiceImpl<GroupPhotoPhotoMapper, GroupPhotoPhoto> implements GroupPhotoPhotoService {

    @Override
    @Cacheable(key = "'count_' + #groupPhotoId")
    public int countByGroupPhotoId(String groupPhotoId) {
        return Math.toIntExact(lambdaQuery().eq(GroupPhotoPhoto::getGroupPhotoId, groupPhotoId).count());
    }

    @Override
    @Cacheable(key = "'photoIds_' + #groupPhotoId")
    public List<String> getPhotoIdsByGroupPhotoId(String groupPhotoId) {
        LambdaQueryWrapper<GroupPhotoPhoto> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupPhotoPhoto::getGroupPhotoId, groupPhotoId)
                   .select(GroupPhotoPhoto::getPhotoId);
        return this.list(queryWrapper)
                 .stream()
                 .map(GroupPhotoPhoto::getPhotoId)
                 .collect(Collectors.toList());
    }
    
    @Override
    @CacheEvict(value = {"groupPhotoPhoto", "groupPhoto"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void deleteByGroupPhotoId(String groupPhotoId) {
        LambdaQueryWrapper<GroupPhotoPhoto> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupPhotoPhoto::getGroupPhotoId, groupPhotoId);
        this.remove(queryWrapper);
    }

    @Override
    @CacheEvict(value = {"groupPhotoPhoto", "groupPhoto"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void updateGroupPhotoPhoto(GroupPhotoDTO groupPhotoDTO) {
        String groupPhotoId = groupPhotoDTO.getGroupPhoto().getId();
        
        // 删除不再需要的关联
        LambdaQueryWrapper<GroupPhotoPhoto> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(GroupPhotoPhoto::getGroupPhotoId, groupPhotoId)
            .notIn(GroupPhotoPhoto::getPhotoId, groupPhotoDTO.getPhotoIds());
        this.remove(deleteWrapper);
        
        // 获取现有的关联
        LambdaQueryWrapper<GroupPhotoPhoto> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupPhotoPhoto::getGroupPhotoId, groupPhotoId);
        List<GroupPhotoPhoto> existingRelations = this.list(queryWrapper);
        
        // 添加新的关联
        for (String photoId : groupPhotoDTO.getPhotoIds()) {
            boolean exists = existingRelations.stream()
                .anyMatch(r -> r.getPhotoId().equals(photoId));
                
            if (!exists) {
                GroupPhotoPhoto newRelation = new GroupPhotoPhoto(groupPhotoId, photoId);
                this.save(newRelation);
            }
        }
    }
}
