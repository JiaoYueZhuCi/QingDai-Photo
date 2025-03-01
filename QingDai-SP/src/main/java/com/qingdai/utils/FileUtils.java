package com.qingdai.utils;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class FileUtils {
    //   判断目录是否存在，不存在直接异常
    public static void validateDirectory(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("无法创建目录: " + dir.getAbsolutePath());
        }
    }

    //   获取文件后缀名
    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    //获取文件基本名的方法
    public static String getBaseName(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? fileName : fileName.substring(0, lastDotIndex);
    }

    private static final String[] IMAGE_EXTENSIONS = {"jpg", "jpeg", "png"};
    private static final String EXTENSION_PATTERN = ".*\\.(%s)$";

    // 根据路径验证文件是否存在
    public static File validateFolder(String path) {
        File folder = new File(path);
        return (folder.exists() && folder.isDirectory()) ? folder : null;
    }

    // 获取文件夹下所有图片文件
    public static File[] getImageFiles(File folder) {
        String pattern = String.format(EXTENSION_PATTERN,
                String.join("|", IMAGE_EXTENSIONS));

        return folder.listFiles((dir, name) ->
                name.toLowerCase().matches(pattern));
    }

    // 获取封装好文件的响应体
    public static ResponseEntity<Resource> getFileResource(String filePath, String fileName) {
        File file = new File(filePath + fileName);
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        Resource resource = new FileSystemResource(file);
        String fileExtension = getFileExtension(fileName).toLowerCase();
        if (fileExtension.equals("jpg") || fileExtension.equals("jpeg")) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        } else if (fileExtension.equals("png")) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);
        }  else {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(null);
        }
    }
}
