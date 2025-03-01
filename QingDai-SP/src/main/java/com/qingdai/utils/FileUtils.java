package com.qingdai.utils;

import java.io.File;
import java.io.IOException;

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

    public static File validateFolder(String path) {
        File folder = new File(path);
        return (folder.exists() && folder.isDirectory()) ? folder : null;
    }

    public static File[] getImageFiles(File folder) {
        String pattern = String.format(EXTENSION_PATTERN,
                String.join("|", IMAGE_EXTENSIONS));

        return folder.listFiles((dir, name) ->
                name.toLowerCase().matches(pattern));
    }
}
