package com.qingdai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.qingdai.entity.Photo;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.YearMonth;
import java.time.Year;
import java.util.List;
import java.util.Map;

public interface PhotoService extends IService<Photo> {

    long countByMonth(YearMonth yearMonth);

    long countByYear(Year year);

    long countByMonthAndStart(YearMonth yearMonth, int start);

    long countByYearAndStart(Year year, int start);

    // 从PhotoProcessingService迁移的方法
    List<Photo> getPhotosByFolder(File folder);

    List<Photo> getPhotosByMultipartFiles(MultipartFile[] files);

    void thumbnailPhotosFromFolderToFolder(File srcDir, File thumbnailDir, int maxSizeKB, boolean overwrite);

    // void thumbnailPhotoFromMultipartFileToFolder(MultipartFile photo,
    // File pendingDir,
    // File thumbnailDir,
    // int maxSizeKB,
    // boolean overwrite) throws IOException;

    String getFileNameById(String photoId);

    boolean multipartFileIsSupportedPhoto(MultipartFile file);

    boolean fileIsSupportedPhoto(File file);

    void deletePhotoFiles(String fullSizeUrl, String thumbnail100KUrl, String thumbnail1000KUrl, String fileName)
            throws IOException;

    Photo getPhotoObjectByMultipartFile(MultipartFile file);

    /**
     * 处理图片压缩流程
     * 
     * @param tempDir           临时目录
     * @param thumbnail100KDir  100K压缩图片目录
     * @param thumbnail1000KDir 1000K压缩图片目录
     * @param fullSizeDir       原图目录
     * @param overwrite         是否覆盖已存在的文件
     * @throws IOException 处理过程中可能发生的IO异常
     */
    void processPhotoCompression(File tempDir, File thumbnail100KDir, File thumbnail1000KDir, File fullSizeDir,
            boolean overwrite) throws IOException;

    /**
     * 处理前端上传的图片
     * @param files 上传的图片文件数组
     * @param start 图片的start值
     * @param overwrite 是否覆盖已存在的文件
     * @return 处理结果，包含更新和新增的图片列表
     */
    ProcessResult processPhotosFromFrontend(MultipartFile[] files, Integer start, boolean overwrite);

    /**
     * 验证数据库中的照片文件是否都存在于文件系统中
     * @return 包含验证结果的Map，包括总照片数、缺失原图数、缺失100K压缩图数、缺失1000K压缩图数及详细信息
     */
    Map<String, Object> validatePhotoExistence();

    /**
     * 验证文件系统中的照片是否都存在于数据库中
     * @param fullSizeUrl 原图目录路径
     * @param thumbnail100KUrl 100K压缩图目录路径
     * @param thumbnail1000KUrl 1000K压缩图目录路径
     * @return 包含验证结果的Map，包括各目录文件数、数据库照片数及不在数据库中的文件列表
     */
    Map<String, Object> validateFileSystemPhotos(String fullSizeUrl, String thumbnail100KUrl, String thumbnail1000KUrl);

    /**
     * 删除文件系统中存在但数据库中没有记录的图片
     * @param fullSizeUrl 原图目录路径
     * @param thumbnail100KUrl 100K压缩图目录路径
     * @param thumbnail1000KUrl 1000K压缩图目录路径
     * @return 包含删除结果的Map，包括删除的文件数量及详情
     */
    Map<String, Object> deletePhotosNotInDatabase(String fullSizeUrl, String thumbnail100KUrl, String thumbnail1000KUrl);

    /**
     * 删除丢失了全部三种图片文件(原图、100K和1000K)的数据库记录
     * @return 包含删除结果的Map，包括删除的记录数量及详情
     */
    Map<String, Object> deleteMissingPhotoRecords();

    /**
     * 获取照片仪表盘统计数据
     * @return 包含各种统计信息的Map
     */
    Map<String, Object> getPhotoDashboardStats();
    
    /**
     * 获取照片类型统计（精选/普通/气象/隐藏）
     * @return 包含各类型照片数量的Map
     */
    Map<String, Object> getPhotoTypeCounts();
    
    /**
     * 获取照片数量年月变化统计
     * @return 包含年月变化统计的Map
     */
    Map<String, Object> getPhotoChangeStats();
    
    /**
     * 获取拍摄主题统计（朝霞/晚霞/日出/日落）
     * @return 包含拍摄主题统计的Map
     */
    Map<String, Object> getPhotoSubjectStats();
    
    /**
     * 获取相机统计
     * @return 包含相机使用频率的列表
     */
    List<Map<String, Object>> getCameraStats();
    
    /**
     * 获取镜头统计
     * @return 包含镜头使用频率的列表
     */
    List<Map<String, Object>> getLensStats();
    
    /**
     * 获取ISO统计
     * @return 包含ISO值使用频率的列表
     */
    List<Map<String, Object>> getIsoStats();
    
    /**
     * 获取快门速度统计
     * @return 包含快门速度使用频率的列表
     */
    List<Map<String, Object>> getShutterStats();
    
    /**
     * 获取光圈值统计
     * @return 包含光圈值使用频率的列表
     */
    List<Map<String, Object>> getApertureStats();
    
    /**
     * 获取焦距统计
     * @return 包含焦距使用频率的列表
     */
    List<Map<String, Object>> getFocalLengthStats();
    
    /**
     * 获取按月份统计的拍摄时间统计
     * @return 包含每月拍摄数量的Map
     */
    Map<String, Long> getMonthStats();
    
    /**
     * 获取按年份统计的拍摄数量
     * @return 包含每年拍摄数量的Map
     */
    Map<String, Long> getYearStats();

    /**
     * 获取所有相机型号
     * @return 所有不重复的相机型号列表
     */
    List<String> getAllCameras();

    /**
     * 获取所有镜头型号
     * @return 所有不重复的镜头型号列表
     */
    List<String> getAllLenses();

    /**
     * 更新相机型号名称
     * @param oldCamera 旧相机型号
     * @param newCamera 新相机型号
     * @return 更新是否成功
     */
    boolean updateCameraName(String oldCamera, String newCamera);

    /**
     * 更新镜头型号名称
     * @param oldLens 旧镜头型号
     * @param newLens 新镜头型号
     * @return 更新是否成功
     */
    boolean updateLensName(String oldLens, String newLens);

    /**
     * 获取指定相机型号的照片数量
     * @param camera 相机型号
     * @return 照片数量
     */
    long getPhotoCountByCamera(String camera);

    /**
     * 获取指定镜头型号的照片数量
     * @param lens 镜头型号
     * @return 照片数量
     */
    long getPhotoCountByLens(String lens);

    /**
     * 获取所有焦距值
     * @return 所有不重复的焦距值列表
     */
    List<String> getAllFocalLengths();

    /**
     * 获取指定焦距的照片数量
     * @param focalLength 焦距值
     * @return 照片数量
     */
    long getPhotoCountByFocalLength(String focalLength);

    /**
     * 更新焦距值
     * @param oldFocalLength 旧焦距值
     * @param newFocalLength 新焦距值
     * @return 更新是否成功
     */
    boolean updateFocalLength(String oldFocalLength, String newFocalLength);

    /**
     * 获取无元数据照片分页信息
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页照片信息
     */
    Page<Photo> getNoMetadataPhotosByPage(int page, int pageSize);

    /**
     * 重命名照片文件
     * @param oldFileName 原文件名
     * @param newFileName 新文件名
     * @return 是否所有文件都重命名成功
     */
    boolean renamePhotoFiles(String oldFileName, String newFileName);

    /**
     * MQ消息消费者处理图片
     * @param fileNames 文件名数组
     * @param tempDirPath 临时目录路径
     * @param start 图片的start值
     * @param overwrite 是否覆盖已存在的文件
     * @return 处理结果
     */
    ProcessResult processPhotoFromMQ(String[] fileNames, String tempDirPath, Integer start, boolean overwrite);

    /**
     * 获取照片上传处理状态
     * @param messageId 消息ID
     * @return 包含处理状态的Map
     */
    Map<String, Object> getPhotoUploadStatus(String messageId);

    /**
     * 更新照片上传处理状态
     * @param messageId 消息ID
     * @param status 状态 (PROCESSING/COMPLETED/FAILED)
     * @param progress 进度 (0-100)
     * @param message 消息内容
     */
    void updatePhotoUploadStatus(String messageId, String status, int progress, String message);

    /**
     * 处理结果类
     */
    class ProcessResult {
        private final List<Photo> existingPhotos;
        private final List<Photo> newPhotos;
        private final boolean success;

        public ProcessResult(List<Photo> existingPhotos, List<Photo> newPhotos, boolean success) {
            this.existingPhotos = existingPhotos;
            this.newPhotos = newPhotos;
            this.success = success;
        }

        public List<Photo> getExistingPhotos() {
            return existingPhotos;
        }

        public List<Photo> getNewPhotos() {
            return newPhotos;
        }

        public boolean isSuccess() {
            return success;
        }
    }

    // 添加删除照片的方法
    boolean deletePhotoById(String id, String fullSizeUrl, String thumbnail100KUrl, String thumbnail1000KUrl);
}