package com.qingdai.service;

import com.baomidou.mybatisplus.extension.service.IService;
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
}