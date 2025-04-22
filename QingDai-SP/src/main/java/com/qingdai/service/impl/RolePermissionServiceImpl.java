package com.qingdai.service.impl;

import com.qingdai.entity.RolePermission;
import com.qingdai.mapper.RolePermissionMapper;
import com.qingdai.service.RolePermissionService;
import com.qingdai.service.base.BaseCachedServiceImpl;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-03-08
 */
@Service
@CacheConfig(cacheNames = "rolePermission")
public class RolePermissionServiceImpl extends BaseCachedServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {
}
