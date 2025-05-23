package com.qingdai.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.qingdai.redis.RedisBloomCommands;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.dynamic.RedisCommandFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

        @Value("${spring.data.redis.host:localhost}")
        private String redisHost;

        @Value("${spring.data.redis.port:6379}")
        private int redisPort;

        @Value("${spring.data.redis.password:}")
        private String redisPassword;

        @Value("${spring.data.redis.database:0}")
        private int redisDatabase;

        @Value("${qingdai.redis.ttl.default-hours:24}")
        private int redisDefaultTtlHours;

        @Value("${qingdai.redis.ttl.cache.cache-hours:24}")
        private int redisCacheTtlHours;

        @Value("${qingdai.redis.ttl.cache.photo-hours:0}")
        private int redisPhotoTtlHours;

        @Value("${qingdai.redis.ttl.uv-hours:0}")
        private int redisUvTtlHours;

        @Value("${qingdai.redis.ttl.photo-view-hours:0}")
        private int redisPhotoViewTtlHours;

        /**
         * 创建一个专门用于Redis的ObjectMapper，需要包含类型信息
         */
        @Bean(name = "redisObjectMapper")
        public ObjectMapper redisObjectMapper() {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
                objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                                ObjectMapper.DefaultTyping.NON_FINAL);

                // 注册JavaTimeModule，使Jackson支持Java 8日期时间API
                objectMapper.registerModule(new JavaTimeModule());
                // 解决Jackson序列化日期为时间戳的问题
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                return objectMapper;
        }

        @Bean
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory,
                        @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
                RedisTemplate<String, Object> template = new RedisTemplate<>();
                template.setConnectionFactory(factory);

                // 使用StringRedisSerializer来序列化和反序列化redis的key值
                template.setKeySerializer(new StringRedisSerializer());

                // 使用定制的Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
                Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(redisObjectMapper,
                                Object.class);
                template.setValueSerializer(serializer);

                // Hash的key也采用StringRedisSerializer的序列化方式
                template.setHashKeySerializer(new StringRedisSerializer());
                // Hash的value采用Jackson2JsonRedisSerializer的序列化方式
                template.setHashValueSerializer(serializer);

                template.afterPropertiesSet();
                return template;
        }

        @Bean
        public CacheManager cacheManager(RedisConnectionFactory factory,
                        @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
                // 创建支持Java 8日期时间API的序列化器
                Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(redisObjectMapper,
                                Object.class);

                // 配置不同cacheName的过期时间
                Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
                configMap.put("photo", getCacheConfig(redisPhotoTtlHours, serializer));

                return RedisCacheManager.builder(factory)
                                .cacheDefaults(getCacheConfig(redisCacheTtlHours, serializer))
                                .withInitialCacheConfigurations(configMap)
                                .build();
        }

        public RedisCacheConfiguration getCacheConfig(int configHours, Jackson2JsonRedisSerializer<Object> serializer) {
                return RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(getTtl(configHours))
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                                .disableCachingNullValues();
        }
        
        /**
         * 根据配置参数名直接获取对应的小时数
         * 
         * @param configHours 配置的小时数
         * @return Duration对象
         */
        public Duration getTtl(int configHours) {
                return configHours <= 0 ? Duration.ZERO : Duration.ofHours(configHours);
        }

        /**
         * 根据传入的小时数设置key的过期时间
         * 如果小时数为零或负数则设置为永不过期
         * 
         * @param redisTemplate Redis操作模板
         * @param key           Redis键
         * @param hours         过期时间（小时）
         */
        public <K> void setKeyTTL(RedisTemplate<K, ?> redisTemplate, K key, int hours) {
                Duration ttl = getTtl(hours);
                if (key != null && redisTemplate.hasKey(key)) {
                        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
                                redisTemplate.persist(key);
                        } else {
                                redisTemplate.expire(key, ttl);
                        }
                }
        }

        /**
         * 为布隆过滤器提供RedisBloomCommands
         */
        @Bean
        public RedisBloomCommands redisBloomCommands(RedisConnectionFactory factory) {
                // 使用Spring提供的Redis连接工厂，避免创建额外的连接
                RedisClient redisClient = RedisClient.create();
                StatefulRedisConnection<String, String> connection = redisClient.connect(
                                RedisURI.builder()
                                                .withHost(redisHost)
                                                .withPort(redisPort)
                                                .withDatabase(redisDatabase)
                                                .withPassword(redisPassword)
                                                .build());

                RedisCommandFactory commandFactory = new RedisCommandFactory(connection);
                return commandFactory.getCommands(RedisBloomCommands.class);
        }
}