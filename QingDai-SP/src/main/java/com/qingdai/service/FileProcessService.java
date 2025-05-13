package com.qingdai.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 文件处理服务接口，专注于文件操作相关的逻辑
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-04-15
 */
public interface FileProcessService {
    
    /**
     * 判断是否为支持的图片格式
     * @param file 文件对象
     * @return 是否为支持的图片格式
     */
    boolean fileIsSupportedPhoto(File file);
    
    /**
     * 判断是否为支持的图片格式
     * @param file 上传的文件对象 
     * @return 是否为支持的图片格式
     */
    boolean multipartFileIsSupportedPhoto(MultipartFile file);
    
    /**
     * 压缩文件夹内所有图片到指定目录
     * @param srcDir 源目录
     * @param thumbnailDir 目标目录
     * @param maxSizeKB 最大大小(KB)
     * @param overwrite 是否覆盖已存在的文件
     */
    void thumbnailPhotosFromFolderToFolder(File srcDir, File thumbnailDir, int maxSizeKB, boolean overwrite);
    
    /**
     * 处理照片压缩
     * @param tempDir 临时目录
     * @param thumbnail100KDir 100K缩略图目录
     * @param thumbnail1000KDir 1000K缩略图目录
     * @param fullSizeDir 原图目录
     * @param overwrite 是否覆盖已存在的文件
     * @throws IOException 处理文件时的IO异常
     */
    void processPhotoCompression(File tempDir, File thumbnail100KDir, File thumbnail1000KDir, File fullSizeDir, boolean overwrite) throws IOException;
    
    /**
     * 删除照片文件
     * @param fullSizeUrl 原图目录
     * @param thumbnail100KUrl 100K缩略图目录
     * @param thumbnail1000KUrl 1000K缩略图目录
     * @param fileName 文件名
     * @throws IOException 删除文件时的IO异常
     */
    void deletePhotoFiles(String fullSizeUrl, String thumbnail100KUrl, String thumbnail1000KUrl, String fileName) throws IOException;
    
    /**
     * 重命名照片文件
     * @param oldFileName 原文件名
     * @param newFileName 新文件名
     * @param fullSizeUrl 原图目录
     * @param thumbnail100KUrl 100K缩略图目录
     * @param thumbnail1000KUrl 1000K缩略图目录
     * @return 是否所有文件都重命名成功
     * @throws IOException 重命名文件时的IO异常
     */
    boolean renamePhotoFiles(String oldFileName, String newFileName, String fullSizeUrl, String thumbnail100KUrl, String thumbnail1000KUrl) throws IOException;
    
    /**
     * 回滚已处理的文件（删除已复制的文件）
     * @param processedFiles 需要回滚的文件列表
     */
    void rollbackProcessedFiles(List<File> processedFiles);
    
    /**
     * 验证文件系统中的照片是否在数据库中有记录
     * @param fullSizeUrl 原图目录
     * @param thumbnail100KUrl 100K缩略图目录
     * @param thumbnail1000KUrl 1000K缩略图目录
     * @return 验证结果
     */
    Map<String, Object> validateFileSystemPhotos(String fullSizeUrl, String thumbnail100KUrl, String thumbnail1000KUrl);
    
    /**
     * 删除文件系统中数据库没有记录的照片
     * @param fullSizeUrl 原图目录
     * @param thumbnail100KUrl 100K缩略图目录
     * @param thumbnail1000KUrl 1000K缩略图目录
     * @return 删除结果
     */
    Map<String, Object> deletePhotosNotInDatabase(String fullSizeUrl, String thumbnail100KUrl, String thumbnail1000KUrl);
} 