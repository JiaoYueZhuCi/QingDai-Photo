package com.qingdai.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.dynamic.RedisCommandFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.qingdai.redis.RedisBloomCommands;

@Configuration
public class BloomFilterConfig {
    
    @Value("${spring.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.redis.port:6379}")
    private int redisPort;
    
    @Value("${spring.redis.password:}")
    private String redisPassword;
    
    @Value("${spring.redis.database:0}")
    private int redisDatabase;
    
    @Bean(destroyMethod = "close")
    public RedisClient redisClient() {
        RedisURI redisUri = RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort)
                .withDatabase(redisDatabase)
                .build();
        
        if (redisPassword != null && !redisPassword.isEmpty()) {
            redisUri.setPassword(redisPassword);
        }
        
        return RedisClient.create(redisUri);
    }
    
    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, String> connection(RedisClient redisClient) {
        return redisClient.connect();
    }
    
    @Bean
    public RedisBloomCommands redisBloomCommands(StatefulRedisConnection<String, String> connection) {
        RedisCommandFactory factory = new RedisCommandFactory(connection);
        return factory.getCommands(RedisBloomCommands.class);
    }
} 