package com.qingdai.mq.producer;

import com.qingdai.config.RocketMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

/**
 * 图片处理消息生产者
 */
@Slf4j
@Component
public class PhotoProcessor {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private RocketMQConfig rocketMQConfig;
    
    @Autowired
    @Qualifier("rocketMQObjectMapper")  
    private ObjectMapper objectMapper;

    /**
     * 异步发送图片处理消息
     *
     * @param message 图片处理消息
     */
    public void sendPhotoProcessMessage(PhotoMessage message) {
        try {
            rocketMQTemplate.asyncSend(rocketMQConfig.getPhotoTopic(), 
                MessageBuilder.withPayload(message).build(), 
                new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("发送图片处理消息成功: {}, message: {}", sendResult.getSendStatus(), message);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        log.error("发送图片处理消息失败: {}", throwable.getMessage(), throwable);
                    }
                });
        } catch (Exception e) {
            log.error("发送消息失败: {}", e.getMessage(), e);
            throw new RuntimeException("发送消息失败", e);
        }
    }

    /**
     * 图片处理消息
     */
    public static class PhotoMessage implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String messageId;
        private Integer startRating;
        private boolean overwrite;
        private String[] fileNames;
        private String tempDir;

        public PhotoMessage() {
        }

        public PhotoMessage(Integer startRating, boolean overwrite, String[] fileNames, String tempDir) {
            this.messageId = String.valueOf(System.currentTimeMillis()); // 使用时间戳作为默认ID
            this.startRating = startRating;
            this.overwrite = overwrite;
            this.fileNames = fileNames;
            this.tempDir = tempDir;
        }

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public Integer getStartRating() {
            return startRating;
        }

        public void setStartRating(Integer startRating) {
            this.startRating = startRating;
        }

        public boolean isOverwrite() {
            return overwrite;
        }

        public void setOverwrite(boolean overwrite) {
            this.overwrite = overwrite;
        }

        public String[] getFileNames() {
            return fileNames;
        }

        public void setFileNames(String[] fileNames) {
            this.fileNames = fileNames;
        }

        public String getTempDir() {
            return tempDir;
        }

        public void setTempDir(String tempDir) {
            this.tempDir = tempDir;
        }

        @Override
        public String toString() {
            return "PhotoMessage{" +
                    "messageId='" + messageId + '\'' +
                    ", startRating=" + startRating +
                    ", overwrite=" + overwrite +
                    ", fileNames.length=" + (fileNames != null ? fileNames.length : 0) +
                    ", tempDir='" + tempDir + '\'' +
                    '}';
        }
    }
} 