package com.qingdai.service.impl;

import com.qingdai.service.FileProcessService;
import com.qingdai.service.PhotoService;
import com.qingdai.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 * 文件处理服务实现类
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-04-15
 */
@Service
@Slf4j
public class FileProcessServiceImpl implements FileProcessService {

    @Lazy
    @Autowired
    private PhotoService photoService;

    @Override
    public boolean fileIsSupportedPhoto(File file) {
        if (!file.isFile()) {
            return false;
        }
        String fileName = file.getName().toLowerCase();
        // 仅支持jpg、jpeg、png、gif格式
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || 
               fileName.endsWith(".png") || fileName.endsWith(".gif");
    }

    @Override
    public boolean multipartFileIsSupportedPhoto(MultipartFile file) {
        String fileName = file.getOriginalFilename().toLowerCase();
        String contentType = file.getContentType();

        // 验证文件扩展名
        boolean validExtension = fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") 
            || fileName.endsWith(".png") || fileName.endsWith(".gif");

        // 验证Content-Type
        boolean validContentType = contentType != null && 
            (contentType.equals("image/jpeg") || contentType.equals("image/png") || 
             contentType.equals("image/gif"));

        return validExtension && validContentType;
    }

    @Override
    public void thumbnailPhotosFromFolderToFolder(File srcDir, File thumbnailDir, int maxSizeKB, boolean overwrite) {
        Arrays.stream(Objects.requireNonNull(srcDir.listFiles()))
                .parallel()
                .filter(this::fileIsSupportedPhoto)
                .forEach(file -> compressPhoto(file, thumbnailDir, maxSizeKB, overwrite));
    }

    private void compressPhoto(File fullSizeFile, File thumbnailDir, int maxSizeKB, boolean overwrite) {
        String fileName = fullSizeFile.getName();
        File thumbnailFile = new File(thumbnailDir, fileName);

        // 如果目标文件已存在且不覆盖，则跳过
        if (thumbnailFile.exists() && !overwrite) {
            log.debug("压缩图片跳过，目标文件已存在: {}", thumbnailFile.getAbsolutePath());
            return;
        }

        try {
            // 确保目标目录存在
            if (!thumbnailDir.exists() && !thumbnailDir.mkdirs()) {
                log.error("无法创建目标目录: {}", thumbnailDir.getAbsolutePath());
                return;
            }

            BufferedImage image = ImageIO.read(fullSizeFile);
            if (image == null) {
                log.warn("无法读取图片: {}", fullSizeFile.getAbsolutePath());
                return;
            }

            String formatName = getFormatName(fullSizeFile.getName());
            if (formatName == null) {
                log.warn("无法确定图片格式: {}", fullSizeFile.getAbsolutePath());
                return;
            }

            // 计算最大字节数
            long maxSizeBytes = maxSizeKB * 1024L;

            // 先尝试通过调整质量压缩图片
            boolean success = adjustPhotoQuality(fullSizeFile, thumbnailFile, formatName, maxSizeBytes);

            // 如果质量调整不足以达到目标大小，则通过缩小尺寸来压缩图片
            if (!success) {
                scaleDownPhoto(fullSizeFile, thumbnailFile, formatName, maxSizeBytes);
            }
        } catch (Exception e) {
            log.error("压缩图片时出错: {}, 错误: {}", fullSizeFile.getAbsolutePath(), e.getMessage(), e);
        }
    }

    private String getFormatName(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if ("jpg".equals(extension)) {
            return "jpeg";
        }
        return extension;
    }

    private boolean adjustPhotoQuality(File fullSizeFile, File thumbnailFile, String formatName, long maxSizeBytes)
            throws IOException {
        double quality = 0.9; // 起始质量
        double minQuality = 0.1; // 最低质量

        while (quality >= minQuality) {
            Thumbnails.of(fullSizeFile)
                    .scale(1.0) // 保持原始大小
                    .outputQuality(quality)
                    .outputFormat(formatName)
                    .toFile(thumbnailFile);

            if (thumbnailFile.length() <= maxSizeBytes) {
                log.debug("通过质量调整成功压缩图片: {}, 质量: {}, 大小: {}KB", 
                        thumbnailFile.getName(), quality, thumbnailFile.length() / 1024);
                return true;
            }

            quality -= 0.1; // 逐步降低质量
        }

        return false;
    }

    private boolean scaleDownPhoto(File fullSizeFile, File thumbnailFile, String formatName, long maxSizeBytes)
            throws IOException {
        double scale = 0.9; // 起始缩放比例
        double minScale = 0.1; // 最小缩放比例

        while (scale >= minScale) {
            Thumbnails.of(fullSizeFile)
                    .scale(scale)
                    .outputQuality(0.8) // 使用固定的质量
                    .outputFormat(formatName)
                    .toFile(thumbnailFile);

            if (thumbnailFile.length() <= maxSizeBytes) {
                log.debug("通过缩小尺寸成功压缩图片: {}, 缩放比例: {}, 大小: {}KB", 
                        thumbnailFile.getName(), scale, thumbnailFile.length() / 1024);
                return true;
            }

            scale -= 0.1; // 逐步缩小尺寸
        }

        log.warn("无法将图片压缩到目标大小: {}, 最终大小: {}KB", 
                thumbnailFile.getName(), thumbnailFile.length() / 1024);
        return false;
    }

    @Override
    public void processPhotoCompression(File tempDir, File thumbnail100KDir, File thumbnail1000KDir, File fullSizeDir, boolean overwrite) throws IOException {
        // 记录已处理的文件，便于回滚
        List<File> processedFiles = new ArrayList<>();
        
        try {
            // 在tempDir下创建1000K临时目录
            File temp1000KDir = new File(tempDir, "1000K");
            if (!temp1000KDir.exists() && !temp1000KDir.mkdirs()) {
                throw new IOException("无法创建1000K临时目录: " + temp1000KDir.getAbsolutePath());
            }
            
            // 压缩到1000K临时目录
            thumbnailPhotosFromFolderToFolder(tempDir, temp1000KDir, 1000, overwrite);
            log.info("目录{}完成1000K压缩", tempDir.getName());
            
            // 基于1000K临时目录的图片压缩到100K目录
            thumbnailPhotosFromFolderToFolder(temp1000KDir, thumbnail100KDir, 100, overwrite);
            log.info("目录{}完成100K压缩", tempDir.getName());
            
            // 将tempDir/1000K目录下的图片复制到thumbnail1000KDir
            for (File file : Objects.requireNonNull(temp1000KDir.listFiles())) {
                if (file.isFile() && fileIsSupportedPhoto(file)) {
                    File destFile = new File(thumbnail1000KDir, file.getName());
                    FileUtils.copyFile(file, destFile, overwrite);
                    processedFiles.add(destFile);
                }
            }
            
            // 将tempDir下的图片复制到fullSizeDir
            for (File file : Objects.requireNonNull(tempDir.listFiles())) {
                if (file.isFile() && fileIsSupportedPhoto(file) && !file.getName().equals("1000K")) {
                    File destFile = new File(fullSizeDir, file.getName());
                    FileUtils.copyFile(file, destFile, overwrite);
                    processedFiles.add(destFile);
                }
            }
            
            log.info("目录{}完成图片处理", tempDir.getName());
        } catch (Exception e) {
            // 出现异常，回滚已处理的文件
            rollbackProcessedFiles(processedFiles);
            log.error("处理图片压缩时发生错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void deletePhotoFiles(String fullSizeUrl, String thumbnail100KUrl, String thumbnail1000KUrl, String fileName) throws IOException {
        boolean allDeleted = true;
        StringBuilder errorMessages = new StringBuilder();

        // 删除原图
        File fullSizeFile = new File(fullSizeUrl, fileName);
        if (fullSizeFile.exists() && !fullSizeFile.delete()) {
            allDeleted = false;
            errorMessages.append("删除原图失败: ").append(fullSizeFile.getAbsolutePath()).append("; ");
        }

        // 删除100K缩略图
        File thumbnail100KFile = new File(thumbnail100KUrl, fileName);
        if (thumbnail100KFile.exists() && !thumbnail100KFile.delete()) {
            allDeleted = false;
            errorMessages.append("删除100K缩略图失败: ").append(thumbnail100KFile.getAbsolutePath()).append("; ");
        }

        // 删除1000K缩略图
        File thumbnail1000KFile = new File(thumbnail1000KUrl, fileName);
        if (thumbnail1000KFile.exists() && !thumbnail1000KFile.delete()) {
            allDeleted = false;
            errorMessages.append("删除1000K缩略图失败: ").append(thumbnail1000KFile.getAbsolutePath());
        }

        if (!allDeleted) {
            throw new IOException(errorMessages.toString());
        }
    }

    @Override
    public boolean renamePhotoFiles(String oldFileName, String newFileName, String fullSizeUrl, String thumbnail100KUrl, String thumbnail1000KUrl) throws IOException {
        if (oldFileName == null || newFileName == null || oldFileName.equals(newFileName)) {
            return true;
        }

        boolean allSuccess = true;
        List<String> paths = Arrays.asList(fullSizeUrl, thumbnail100KUrl, thumbnail1000KUrl);
        List<String> failedPaths = new ArrayList<>();
        List<File> renamedFiles = new ArrayList<>(); // 用于记录已重命名的文件，便于回滚

        try {
            for (String path : paths) {
                File oldFile = new File(path, oldFileName);
                File newFile = new File(path, newFileName);

                if (oldFile.exists()) {
                    // 如果目标文件已存在，先删除
                    if (newFile.exists() && !newFile.delete()) {
                        allSuccess = false;
                        failedPaths.add(path);
                        log.error("无法删除已存在的目标文件: {}", newFile.getAbsolutePath());
                        continue;
                    }

                    // 重命名文件
                    if (!oldFile.renameTo(newFile)) {
                        allSuccess = false;
                        failedPaths.add(path);
                        log.error("无法重命名文件: {} -> {}", oldFile.getAbsolutePath(), newFile.getAbsolutePath());
                    } else {
                        renamedFiles.add(newFile); // 记录成功重命名的文件
                    }
                }
            }

            // 如果有任何一个目录的重命名失败，回滚所有已重命名的文件
            if (!allSuccess) {
                // 回滚已重命名的文件
                for (File newFile : renamedFiles) {
                    String path = newFile.getParent();
                    File oldFile = new File(path, oldFileName);
                    if (!newFile.renameTo(oldFile)) {
                        log.error("回滚重命名文件失败: {} -> {}", newFile.getAbsolutePath(), oldFile.getAbsolutePath());
                    }
                }
                throw new IOException("重命名照片文件失败，失败的路径: " + failedPaths);
            }

            return true;
        } catch (Exception e) {
            // 回滚已重命名的文件
            for (File newFile : renamedFiles) {
                String path = newFile.getParent();
                File oldFile = new File(path, oldFileName);
                if (!newFile.renameTo(oldFile)) {
                    log.error("回滚重命名文件失败: {} -> {}", newFile.getAbsolutePath(), oldFile.getAbsolutePath());
                }
            }
            throw e;
        }
    }

    @Override
    public void rollbackProcessedFiles(List<File> processedFiles) {
        if (processedFiles == null || processedFiles.isEmpty()) {
            return;
        }

        for (File file : processedFiles) {
            try {
                if (file.exists() && !file.delete()) {
                    log.warn("无法删除已处理的文件进行回滚: {}", file.getAbsolutePath());
                }
            } catch (Exception e) {
                log.error("回滚文件时出错: {}, 错误: {}", file.getAbsolutePath(), e.getMessage(), e);
            }
        }
    }

    @Override
    public Map<String, Object> validateFileSystemPhotos(String fullSizeUrl, String thumbnail100KUrl, String thumbnail1000KUrl) {
        log.info("开始验证文件系统照片在数据库中的存在性");
        
        Map<String, Object> result = new HashMap<>();
        List<String> missingInDatabase = new ArrayList<>();
        
        // 获取所有照片文件名
        Set<String> allFileNames = new HashSet<>();
        
        // 获取原图目录中的所有照片
        File fullSizeDir = new File(fullSizeUrl);
        if (fullSizeDir.exists() && fullSizeDir.isDirectory()) {
            for (File file : Objects.requireNonNull(fullSizeDir.listFiles())) {
                if (fileIsSupportedPhoto(file)) {
                    allFileNames.add(file.getName());
                }
            }
        }
        
        // 获取100K缩略图目录中的所有照片
        File thumbnail100KDir = new File(thumbnail100KUrl);
        if (thumbnail100KDir.exists() && thumbnail100KDir.isDirectory()) {
            for (File file : Objects.requireNonNull(thumbnail100KDir.listFiles())) {
                if (fileIsSupportedPhoto(file)) {
                    allFileNames.add(file.getName());
                }
            }
        }
        
        // 获取1000K缩略图目录中的所有照片
        File thumbnail1000KDir = new File(thumbnail1000KUrl);
        if (thumbnail1000KDir.exists() && thumbnail1000KDir.isDirectory()) {
            for (File file : Objects.requireNonNull(thumbnail1000KDir.listFiles())) {
                if (fileIsSupportedPhoto(file)) {
                    allFileNames.add(file.getName());
                }
            }
        }
        
        int totalFiles = allFileNames.size();
        if (totalFiles == 0) {
            log.warn("文件系统中没有照片");
            result.put("message", "文件系统中没有照片");
            return result;
        }
        
        // 检查每个文件是否在数据库中有记录
        for (String fileName : allFileNames) {
            boolean existsInDatabase = photoService.existsByFileName(fileName);
            if (!existsInDatabase) {
                missingInDatabase.add(fileName);
            }
        }
        
        int missingCount = missingInDatabase.size();
        log.info("文件系统中有{}个照片文件，其中{}个在数据库中没有记录", totalFiles, missingCount);
        
        result.put("totalFiles", totalFiles);
        result.put("missingInDatabase", missingCount);
        result.put("missingFileNames", missingInDatabase);
        result.put("message", String.format("文件系统中有%d个照片文件，其中%d个在数据库中没有记录", totalFiles, missingCount));
        
        return result;
    }

    @Override
    public Map<String, Object> deletePhotosNotInDatabase(String fullSizeUrl, String thumbnail100KUrl, String thumbnail1000KUrl) {
        log.info("开始删除数据库中没有记录的照片文件");
        
        Map<String, Object> result = new HashMap<>();
        List<String> deletedFiles = new ArrayList<>();
        
        // 验证文件系统照片
        Map<String, Object> validateResult = validateFileSystemPhotos(fullSizeUrl, thumbnail100KUrl, thumbnail1000KUrl);
        
        @SuppressWarnings("unchecked")
        List<String> missingFileNames = (List<String>) validateResult.get("missingFileNames");
        
        if (missingFileNames == null || missingFileNames.isEmpty()) {
            log.info("没有需要删除的照片文件");
            result.put("message", "没有需要删除的照片文件");
            return result;
        }
        
        int totalToDelete = missingFileNames.size();
        int deletedCount = 0;
        
        // 删除每个在数据库中没有记录的文件
        for (String fileName : missingFileNames) {
            boolean allDeleted = true;
            
            // 删除原图
            File fullSizeFile = new File(fullSizeUrl, fileName);
            if (fullSizeFile.exists() && !fullSizeFile.delete()) {
                log.warn("无法删除原图文件: {}", fullSizeFile.getAbsolutePath());
                allDeleted = false;
            }
            
            // 删除100K缩略图
            File thumbnail100KFile = new File(thumbnail100KUrl, fileName);
            if (thumbnail100KFile.exists() && !thumbnail100KFile.delete()) {
                log.warn("无法删除100K缩略图文件: {}", thumbnail100KFile.getAbsolutePath());
                allDeleted = false;
            }
            
            // 删除1000K缩略图
            File thumbnail1000KFile = new File(thumbnail1000KUrl, fileName);
            if (thumbnail1000KFile.exists() && !thumbnail1000KFile.delete()) {
                log.warn("无法删除1000K缩略图文件: {}", thumbnail1000KFile.getAbsolutePath());
                allDeleted = false;
            }
            
            if (allDeleted) {
                deletedCount++;
                deletedFiles.add(fileName);
            }
        }
        
        log.info("成功删除了{}个数据库中没有记录的照片文件，共{}个需要删除", deletedCount, totalToDelete);
        
        result.put("totalToDelete", totalToDelete);
        result.put("deletedCount", deletedCount);
        result.put("deletedFiles", deletedFiles);
        result.put("message", String.format("成功删除了%d个数据库中没有记录的照片文件，共%d个需要删除", deletedCount, totalToDelete));
        
        return result;
    }
} 