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
    void thumbnailPhotoFromMultipartFileToFolder(MultipartFile photo, File pendingDir, File thumbnailDir, int maxSizeKB, boolean overwrite) throws IOException;
    String getFileNameById(String photoId);
    boolean multipartFileIsSupportedPhoto(MultipartFile file);
    boolean fileIsSupportedPhoto(File file);
    void deletePhotoFiles(String fullSizeUrl, String thumbnail100KUrl, String thumbnail1000KUrl, String fileName) throws IOException;
    Photo getPhotoObjectByMultipartFile(MultipartFile file);
}