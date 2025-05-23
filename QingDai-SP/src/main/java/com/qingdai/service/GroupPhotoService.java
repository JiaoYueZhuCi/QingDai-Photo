package com.qingdai.service;

import com.qingdai.entity.dto.GroupPhotoDTO;
import com.qingdai.entity.GroupPhoto;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-03-19
 */
public interface GroupPhotoService extends IService<GroupPhoto> {
    GroupPhotoDTO getGroupPhotoDTOById(String id);
    
    List<GroupPhotoDTO> getAllGroupPhotoDTOs();

    Page<GroupPhotoDTO> getGroupPhotoDTOsByPage(int page, int pageSize);
    
    /**
     * 删除组图和相关联记录
     * @param id 组图ID
     * @return 是否成功删除
     */
    boolean deleteGroupPhotoWithRelations(String id);
}
