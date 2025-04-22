package com.qingdai.service.impl;

import com.qingdai.entity.Timeline;
import com.qingdai.mapper.TimelineMapper;
import com.qingdai.service.TimelineService;
import com.qingdai.service.base.BaseCachedServiceImpl;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-04-12
 */
@Service
@CacheConfig(cacheNames = "timeline")
public class TimelineServiceImpl extends BaseCachedServiceImpl<TimelineMapper, Timeline> implements TimelineService {
}
