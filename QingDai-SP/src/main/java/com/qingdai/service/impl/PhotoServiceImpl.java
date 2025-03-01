package com.qingdai.service.impl;

import com.qingdai.entity.Photo;
import com.qingdai.mapper.PhotoMapper;
import com.qingdai.service.PhotoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.YearMonth;
import java.time.Year;
import java.time.LocalDate;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-02-28
 */
@Service
public class PhotoServiceImpl extends ServiceImpl<PhotoMapper, Photo> implements PhotoService {

    @Override
    public long countByMonth(YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return count(new LambdaQueryWrapper<Photo>()
                .ge(Photo::getTime, startDate)
                .le(Photo::getTime, endDate));
    }

    @Override
    public long countByYear(Year year) {
        LocalDate startDate = year.atDay(1);
        LocalDate endDate = year.atDay(year.length());
        return count(new LambdaQueryWrapper<Photo>()
                .ge(Photo::getTime, startDate)
                .le(Photo::getTime, endDate));
    }

    @Override
    public long countByMonthAndStart(YearMonth yearMonth, int start) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return count(new LambdaQueryWrapper<Photo>()
                .ge(Photo::getTime, startDate)
                .le(Photo::getTime, endDate)
                .eq(Photo::getStart, start));
    }

    @Override
    public long countByYearAndStart(Year year, int start) {
        LocalDate startDate = year.atDay(1);
        LocalDate endDate = year.atDay(year.length());
        return count(new LambdaQueryWrapper<Photo>()
                .ge(Photo::getTime, startDate)
                .le(Photo::getTime, endDate)
                .eq(Photo::getStart, start));
    }
}
