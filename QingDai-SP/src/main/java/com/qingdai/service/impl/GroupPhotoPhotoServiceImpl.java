package com.qingdai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingdai.entity.dto.GroupPhotoDTO;
import com.qingdai.entity.GroupPhotoPhoto;
import com.qingdai.mapper.GroupPhotoPhotoMapper;
import com.qingdai.service.GroupPhotoPhotoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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
public class GroupPhotoPhotoServiceImpl extends ServiceImpl<GroupPhotoPhotoMapper, GroupPhotoPhoto> implements GroupPhotoPhotoService {

    @Override
    public int countByGroupPhotoId(String groupPhotoId) {
        return Math.toIntExact(lambdaQuery().eq(GroupPhotoPhoto::getGroupPhotoId, groupPhotoId).count());
    }

    @Override
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
    public void deleteByGroupPhotoId(String groupPhotoId) {
        LambdaQueryWrapper<GroupPhotoPhoto> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupPhotoPhoto::getGroupPhotoId, groupPhotoId);
        this.remove(queryWrapper);
    }



    @Override
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
                GroupPhotoPhoto newRelation = new GroupPhotoPhoto();
                newRelation.setGroupPhotoId(groupPhotoId);
                newRelation.setPhotoId(photoId);
                this.save(newRelation);
            }
        }
    }
}
