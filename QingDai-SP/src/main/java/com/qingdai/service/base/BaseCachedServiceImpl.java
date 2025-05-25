package com.qingdai.service.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 带缓存功能的ServiceImpl基类
 * 任何需要缓存的Service实现类只需继承此类，并添加@CacheConfig注解指定缓存名称即可
 * 
 * @param <M> Mapper类型
 * @param <T> 实体类型
 */
@CacheConfig(cacheNames = "base")
public class BaseCachedServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    @Override
    // @Cacheable(keyGenerator = "classMethodParamsKeyGenerator")
    public T getById(Serializable id) {
        return super.getById(id);
    }
    
    @Override
    // @Cacheable(keyGenerator = "classMethodKeyGenerator")
    public List<T> list() {
        return super.list();
    }
    
    @Override
    @CacheEvict(allEntries = true)
    public boolean save(T entity) {
        return super.save(entity);
    }
    
    @Override
    @CacheEvict(allEntries = true)
    public boolean updateById(T entity) {
        return super.updateById(entity);
    }
    
    @Override
    @CacheEvict(allEntries = true)
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }
    
    @Override
    @CacheEvict(allEntries = true)
    public boolean saveBatch(Collection<T> entityList) {
        return super.saveBatch(entityList);
    }
    
    @Override
    @CacheEvict(allEntries = true)
    public boolean updateBatchById(Collection<T> entityList) {
        return super.updateBatchById(entityList);
    }
    
    @Override
    @CacheEvict(allEntries = true)
    public boolean removeByIds(Collection<?> idList) {
        return super.removeByIds(idList);
    }
} 