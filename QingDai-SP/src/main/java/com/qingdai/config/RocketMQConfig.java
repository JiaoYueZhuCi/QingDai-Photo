package com.qingdai.config;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.beans.factory.config.BeanDefinition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * RocketMQ配置类
 */
@Configuration
public class RocketMQConfig {

    @Value("${rocketmq.topic.photo}")
    private String photoTopic;

    /**
     * 获取Photo处理Topic名称
     */
    public String getPhotoTopic() {
        return photoTopic;
    }

    /**
     * 为RocketMQ配置专用的ObjectMapper
     */
    @Bean(name = "rocketMQObjectMapper")
    public ObjectMapper rocketMQObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    /**
     * 配置Jackson消息转换器
     */
    @Bean
    public MappingJackson2MessageConverter mappingJackson2MessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(rocketMQObjectMapper());
        converter.setSerializedPayloadClass(String.class);
        converter.setStrictContentTypeMatch(false);
        return converter;
    }
    
    /**
     * 配置RocketMQ消息转换器
     */
    @Bean
    @Primary
    public RocketMQMessageConverter rocketMQMessageConverter() {
        return new RocketMQMessageConverter();
    }
}