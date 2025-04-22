package com.qingdai.service.impl;

import com.qingdai.entity.Permission;
import com.qingdai.mapper.PermissionMapper;
import com.qingdai.service.PermissionService;
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
@CacheConfig(cacheNames = "permission")
public class PermissionServiceImpl extends BaseCachedServiceImpl<PermissionMapper, Permission> implements PermissionService {
}
