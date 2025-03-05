package com.qingdai.utils;

import java.io.File;

public class ValidationUtils {
    // 文件大小限制校验
    public static void validateMaxSize(int maxSizeKB) {
        if (maxSizeKB < 100 || maxSizeKB > 10240) {
            throw new IllegalArgumentException("文件大小限制需在100KB-10MB之间");
        }
    }


}