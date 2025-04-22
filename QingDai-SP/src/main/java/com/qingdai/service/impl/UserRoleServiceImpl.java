package com.qingdai.service.impl;

import com.qingdai.entity.UserRole;
import com.qingdai.mapper.UserRoleMapper;
import com.qingdai.service.UserRoleService;
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
@CacheConfig(cacheNames = "userRole")
public class UserRoleServiceImpl extends BaseCachedServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {
}
