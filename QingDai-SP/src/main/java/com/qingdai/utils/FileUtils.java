package com.qingdai.utils;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {
    private static final int ZIP_BUFFER_SIZE = 4096;

    public static ResponseEntity<Resource> getFilesResource(List<File> files) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("文件列表不能为空");
        }

        File tempDir = createTempDir(new File(System.getProperty("java.io.tmpdir")));
        File zipFile = new File(tempDir, "package_" + System.currentTimeMillis() + ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (File file : files) {
                if (!file.exists() || !file.canRead())
                    continue;

                ZipEntry entry = new ZipEntry(file.getName());
                zos.putNextEntry(entry);

                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[ZIP_BUFFER_SIZE];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                }
                zos.closeEntry();
            }
        } catch (IOException e) {
            deleteFolder(tempDir);
            throw new IOException("压缩文件创建失败: " + e.getMessage());
        }

        Resource resource = new FileSystemResource(zipFile);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + zipFile.getName() + "\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .contentLength(zipFile.length())
                .body(resource);
    }

    // 判断目录是否存在，不存在直接异常
    public static void validateDirectory(File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("目录不存在: " + dir.getAbsolutePath());
        }
    }

    // 获取文件后缀名
    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    // 获取文件基本名的方法
    public static String getBaseName(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? fileName : fileName.substring(0, lastDotIndex);
    }

    private static final String[] IMAGE_EXTENSIONS = { "jpg", "jpeg", "png" };
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

        return folder.listFiles((dir, name) -> name.toLowerCase().matches(pattern));
    }

    // 获取封装好文件的响应体
    public static ResponseEntity<Resource> getFileResource(String filePath, String fileName) {
        File file = Paths.get(filePath).resolve(fileName).toFile();
        if (!file.exists() || !file.canRead()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        long contentLength = file.length();
        Resource resource = new FileSystemResource(file);

        String fileExtension = getFileExtension(fileName).toLowerCase();
        MediaType mediaType;
        if (fileExtension.equals("jpg") || fileExtension.equals("jpeg")) {
            mediaType = MediaType.IMAGE_JPEG;
        } else if (fileExtension.equals("png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(contentLength)
                .body(resource);
    }

    // 保存MultipartFile到指定目录
    public static void saveFile(MultipartFile file, File destDir) throws IOException {
        System.out.println("进入saveFile方法");
        if (!destDir.exists() && !destDir.mkdirs()) {
            throw new IOException("无法创建目标目录: " + destDir.getAbsolutePath());
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IOException("文件名为空");
        }

        File destFile = new File(destDir, fileName);
        try {
            System.out.println("尝试将文件转移到: " + destFile.getAbsolutePath());
            if (file.isEmpty()) {
                throw new IOException("上传的文件内容为空");
            }

            // 创建父目录（如果不存在）
            Files.createDirectories(destFile.getParentFile().toPath());

            // 执行文件复制操作
            try {
                Files.copy(file.getInputStream(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                System.out.println("Files.copy:" + e);
            }

            System.out.println("文件成功保存到: " + destFile.getAbsolutePath());
        } catch (IOException e) {
            throw new IOException("文件保存失败: " + destFile.getName() + ", 错误原因: " + e.getMessage(), e);
        }
    }

    // 复制文件夹中的所有文件到目标文件夹
    public static void copyFiles(File sourceDir, File destDir) throws IOException {
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new IOException("源目录不存在或不是目录: " + sourceDir.getAbsolutePath());
        }
        if (!destDir.exists() && !destDir.mkdirs()) {
            throw new IOException("无法创建目标目录: " + destDir.getAbsolutePath());
        }

        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    Path sourcePath = Paths.get(file.getAbsolutePath());
                    Path destPath = Paths.get(destDir.getAbsolutePath(), file.getName());
                    Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    // 删除文件夹中的所有文件
    public static void deleteFile(File file) throws IOException {
        if (!file.exists())
            return;
        if (!file.delete()) {
            throw new IOException("无法删除文件: " + file.getAbsolutePath());
        }
    }

    public static void deleteFiles(File dir) throws IOException {
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException("目录不存在或不是目录: " + dir.getAbsolutePath());
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    if (!file.delete()) {
                        throw new IOException("无法删除文件: " + file.getAbsolutePath());
                    }
                }
            }
        }
    }

    // 在Dir下创建随机名称的临时目录
    public static File createTempDir(File Dir) throws IOException {
        // 确保父目录存在
        if (!Dir.exists() && !Dir.mkdirs()) {
            throw new IOException("父目录: " + Dir + "下无法创建临时目录");
        }
        String tempDirName = UUID.randomUUID().toString();
        File tempDir = new File(Dir, tempDirName);
        if (!tempDir.mkdirs()) {
            throw new IOException("无法创建临时目录: " + tempDir);
        }
        return tempDir;
    }

    // 删除文件夹及其所有内容
    public static void deleteFolder(File dir) throws IOException {
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException("目录不存在或不是目录: " + dir.getAbsolutePath());
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file); // 递归删除子目录
                } else {
                    if (!file.delete()) {
                        throw new IOException("无法删除文件: " + file.getAbsolutePath());
                    }
                }
            }
        }

        if (!dir.delete()) {
            throw new IOException("无法删除目录: " + dir.getAbsolutePath());
        }
    }

}