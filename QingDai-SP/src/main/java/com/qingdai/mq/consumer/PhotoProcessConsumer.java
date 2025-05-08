package com.qingdai.mq.consumer;

import com.qingdai.mq.producer.PhotoProcessor.PhotoMessage;
import com.qingdai.service.PhotoService;
import com.qingdai.service.PhotoService.ProcessResult;
import com.qingdai.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * 图片处理消息消费者
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "${rocketmq.topic.photo}",
        consumerGroup = "${rocketmq.consumer.group}")
public class PhotoProcessConsumer implements RocketMQListener<PhotoMessage> {

    @Autowired
    private PhotoService photoService;
    
    @Value("${qingdai.fullSizeUrl}")
    private String fullSizeUrl;
    
    @Value("${qingdai.thumbnail100KUrl}")
    private String thumbnail100KUrl;
    
    @Value("${qingdai.thumbnail1000KUrl}")
    private String thumbnail1000KUrl;

    @Override
    public void onMessage(PhotoMessage message) {
        long startTime = System.currentTimeMillis();
        String messageId = message.getMessageId(); // 获取消息ID
        
        try {
            log.info("接收到图片处理消息: {}", message);
            
            // 验证参数
            if (message.getFileNames() == null || message.getFileNames().length == 0) {
                log.warn("消息中没有文件名");
                photoService.updatePhotoUploadStatus(messageId, "FAILED", 0, "消息中没有文件名");
                return;
            }

            String tempDirPath = message.getTempDir();
            if (tempDirPath == null || tempDirPath.isEmpty()) {
                log.warn("临时目录路径为空");
                photoService.updatePhotoUploadStatus(messageId, "FAILED", 0, "临时目录路径为空");
                return;
            }

            // 验证文件目录
            File tempDir = new File(tempDirPath);
            File fullSizeDir = FileUtils.validateFolder(fullSizeUrl);
            File thumbnail100KDir = FileUtils.validateFolder(thumbnail100KUrl);
            File thumbnail1000KDir = FileUtils.validateFolder(thumbnail1000KUrl);

            if (!tempDir.exists() || !tempDir.isDirectory() || 
                fullSizeDir == null || thumbnail100KDir == null || thumbnail1000KDir == null) {
                log.error("目录验证失败");
                photoService.updatePhotoUploadStatus(messageId, "FAILED", 0, "目录验证失败");
                return;
            }

            // 更新状态为处理中
            photoService.updatePhotoUploadStatus(messageId, "PROCESSING", 10, "开始处理图片");
            
            // 处理图片元数据和压缩
            try {
                // 处理图片压缩流程
                photoService.updatePhotoUploadStatus(messageId, "PROCESSING", 30, "正在压缩图片");
                photoService.processPhotoCompression(tempDir, thumbnail100KDir, thumbnail1000KDir, fullSizeDir, message.isOverwrite());
                log.info("完成图片压缩处理");
                
                // 处理元数据并保存到数据库
                photoService.updatePhotoUploadStatus(messageId, "PROCESSING", 60, "正在处理图片元数据");
                ProcessResult result = photoService.processPhotoFromMQ(
                        message.getFileNames(), 
                        tempDirPath, 
                        message.getStartRating(), 
                        message.isOverwrite());
                
                if (result.isSuccess()) {
                    photoService.updatePhotoUploadStatus(messageId, "PROCESSING", 80, 
                            String.format("数据处理完成，更新：%d张，新增：%d张", 
                                    result.getExistingPhotos().size(), result.getNewPhotos().size()));
                    log.info("成功处理图片数据，更新：{}张，新增：{}张", 
                        result.getExistingPhotos().size(), result.getNewPhotos().size());
                } else {
                    photoService.updatePhotoUploadStatus(messageId, "PROCESSING", 80, "图片数据处理未完全成功");
                    log.warn("图片数据处理未完全成功");
                }
                
                // 清理临时目录
                try {
                    photoService.updatePhotoUploadStatus(messageId, "PROCESSING", 90, "正在清理临时文件");
                    FileUtils.clearFolder(tempDir, true);
                    log.info("已清理临时目录: {}", tempDirPath);
                } catch (IOException e) {
                    log.warn("清理临时目录时出错: {}", e.getMessage(), e);
                    // 清理临时目录失败不影响整体处理结果
                }
                
                // 更新状态为完成
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                photoService.updatePhotoUploadStatus(messageId, "COMPLETED", 100, 
                        String.format("图片处理完成，耗时%d毫秒", duration));
                log.info("图片处理完成，耗时{}毫秒", duration);
            } catch (Exception e) {
                log.error("处理图片时发生错误: {}", e.getMessage(), e);
                photoService.updatePhotoUploadStatus(messageId, "FAILED", 0, "处理图片时发生错误: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("消费图片处理消息时发生错误: {}", e.getMessage(), e);
            try {
                photoService.updatePhotoUploadStatus(messageId, "FAILED", 0, "系统错误: " + e.getMessage());
            } catch (Exception ex) {
                log.error("更新处理状态失败: {}", ex.getMessage(), ex);
            }
        }
    }
} 