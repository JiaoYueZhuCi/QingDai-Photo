package com.qingdai.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {
    private static final SimpleDateFormat DATETIME_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String formatDateTime(Date date) {
        return DATETIME_FORMAT.format(date);
    }

    public static String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentTime = LocalDateTime.now().format(formatter);
        return currentTime;
    }

    public static LocalDateTime getLocalDateTime() {
        return LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
    }

}
