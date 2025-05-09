package com.qingdai.redis;

import io.lettuce.core.dynamic.Commands;
import io.lettuce.core.dynamic.annotation.Command;
import io.lettuce.core.dynamic.annotation.CommandNaming;

/**
 * RedisBloom 布隆过滤器命令接口
 */
@CommandNaming(strategy = CommandNaming.Strategy.DOT)
public interface RedisBloomCommands extends Commands {
    
    /**
     * 将元素添加到布隆过滤器
     * @param key 过滤器的key
     * @param item 要添加的元素
     * @return 如果元素已存在返回0，如果是新元素返回1
     */
    @Command("BF.ADD :key :item")
    Boolean add(String key, String item);
    
    /**
     * 检查元素是否存在于布隆过滤器中
     * @param key 过滤器的key
     * @param item 要检查的元素
     * @return 如果元素可能存在返回true，否则返回false
     */
    @Command("BF.EXISTS :key :item")
    Boolean exists(String key, String item);
    
    /**
     * 创建一个新的布隆过滤器
     * @param key 过滤器的key
     * @param errorRate 期望的误判率
     * @param capacity 期望的容量
     * @return 是否成功创建
     */
    @Command("BF.RESERVE :key :errorRate :capacity")
    Boolean reserve(String key, double errorRate, long capacity);
} 