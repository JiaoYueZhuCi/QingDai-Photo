package com.qingdai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.qingdai.entity.Photo;
import org.springframework.core.io.Resource;

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
}