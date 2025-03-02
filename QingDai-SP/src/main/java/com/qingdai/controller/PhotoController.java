package com.qingdai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingdai.entity.Photo;
import com.qingdai.service.ImageProcessingService;
import com.qingdai.service.PhotoService;
import com.qingdai.utils.FileUtils;
import com.qingdai.utils.ValidationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.Year;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-02-28
 */
@Tag(name = "图片管理", description = "图片相关操作接口")
@RestController
@RequestMapping("/photo")
public class PhotoController {
    @Autowired
    PhotoService photoService;
    @Autowired
    private ImageProcessingService imageService;
    @Value("${qingdai.fullSizeUrl}")
    private String fullSizeUrl;
    @Value("${qingdai.thumbnailSizeUrl}")
    private String thumbnailSizeUrl;


    @GetMapping("/getAllPhotos")
    @Operation(summary = "获取全部照片信息(时间倒叙)", description = "从数据库获取所有照片的详细信息(时间倒叙)")
    public ResponseEntity<List<Photo>> getAllPhotos() {
        try {
            // 1. 使用MyBatis Plus的list方法获取所有记录
            List<Photo> photos = photoService.list(
                    new LambdaQueryWrapper<Photo>()
                            .orderByDesc(Photo::getTime) // 按时间倒序
            );


            // 2. 处理空数据集情况
            if (photos == null || photos.isEmpty()) {
                // 返回空数据的响应，自动使用404状态码
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }

            // 3. 返回成功结果，自动使用200状态码
            return ResponseEntity.ok().body(photos);

        } catch (Exception e) {
            // 4. 异常处理（建议记录日志）
            e.printStackTrace();
            // 返回500状态码并自动使用错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getStartPhotos")
    @Operation(summary = "获取代表作照片信息(时间倒叙)", description = "从数据库获取所有代表作的详细信息(时间倒叙)")
    public ResponseEntity<List<Photo>> getStartPhotos() {
        try {
            // 1. 使用MyBatis Plus的list方法获取所有记录
            List<Photo> photos = photoService.list(
                    new LambdaQueryWrapper<Photo>()
                            .orderByDesc(Photo::getTime) // 按时间倒序
                            .eq(Photo::getStart, 1)
            );

            // 2. 处理空数据集情况
            if (photos == null || photos.isEmpty()) {
                // 返回空数据的响应，自动使用404状态码
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }

            // 3. 返回成功结果，自动使用200状态码
            return ResponseEntity.ok().body(photos);

        } catch (Exception e) {
            // 4. 异常处理（建议记录日志）
            e.printStackTrace();
            // 返回500状态码并自动使用错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/toMysql")
    @Operation(summary = "所有图片信息自动入数据库", description = "所有图片信息自动入数据库")
    public ResponseEntity<String> toMysql() {
        // 1. 获取验证后的文件夹
        File folder = FileUtils.validateFolder(fullSizeUrl);
        if (folder == null) {
            // 文件夹路径无效，返回500错误
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("照片文件夹路径无效");
        }

        // 2. 获取并处理图片
        List<Photo> photos = imageService.processImageFolder(folder);
        if (photos.isEmpty()) {
            // 没有有效照片，返回404错误
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("没有有效照片可保存");
        }

        // 3. 保存到数据库
        try {
            boolean result = photoService.saveBatch(photos);
            if (result) {
                // 成功插入数据，返回成功消息和插入的记录数
                return ResponseEntity.status(HttpStatus.OK)
                        .body("成功插入" + photos.size() + "条记录");
            } else {
                // 数据库操作失败，返回500错误
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("数据库操作失败");
            }
        } catch (Exception e) {
            // 数据库异常，返回500错误和异常信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("数据库异常: " + e.getMessage());
        }
    }


    @Operation(summary = "批量图片压缩",
            description = "将原图目录中的图片压缩到缩略图目录",
            parameters = {
                    @Parameter(name = "maxSizeKB", in = ParameterIn.QUERY,
                            description = "最大文件大小(KB)",
                            schema = @Schema(
                                    type = "integer",
                                    minimum = "100",
                                    maximum = "10240",
                                    example = "1024")),
                    @Parameter(name = "overwrite", in = ParameterIn.QUERY,
                            description = "覆盖已存在文件",
                            schema = @Schema(type = "boolean"))
            })
    @GetMapping("/compress")
    public ResponseEntity<String> compressImages(
            @RequestParam(defaultValue = "1024") int maxSizeKB,
            @RequestParam(defaultValue = "false") boolean overwrite) {

        try {
            // 参数校验
            ValidationUtils.validateMaxSize(maxSizeKB);

            File srcDir = new File(fullSizeUrl);
            File destDir = new File(thumbnailSizeUrl);

            // 目录验证
            ValidationUtils.validateDirectory(srcDir);
            FileUtils.validateDirectory(destDir);

            // 执行处理
            imageService.processBatch(srcDir, destDir, maxSizeKB, overwrite);

            return ResponseEntity.ok("压缩任务已完成");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/getCompressedPhoto")
    @Operation(summary = "获取指定压缩照片文件", description = "根据接收到的照片ID获取压缩照片文件并返回")
    public ResponseEntity<Resource> getThumbnailPhoto(@RequestParam String id) {
        try {
            String fileName = imageService.getFileNameById(Long.valueOf(id));
            if (fileName == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return FileUtils.getFileResource(thumbnailSizeUrl, fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getFullSizePhoto")
    @Operation(summary = "获取指定原图照片文件", description = "根据接收到的照片ID获取原图照片文件并返回")
    public ResponseEntity<Resource> getFullSizePhoto(@RequestParam String id) {
        try {
            String fileName = imageService.getFileNameById(Long.valueOf(id));
            if (fileName == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return FileUtils.getFileResource(fullSizeUrl, fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getPhotoCount")
    @Operation(summary = "获取照片总数", description = "从数据库获取所有照片的总数")
    public ResponseEntity<Long> getPhotoCount() {
        try {
            long count = photoService.count();
            return ResponseEntity.ok().body(count);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getStartPhotoCount")
    @Operation(summary = "获取代表作照片总数", description = "从数据库获取start字段为1的照片总数")
    public ResponseEntity<Long> getStartPhotoCount() {
        try {
            long count = photoService.count(new LambdaQueryWrapper<Photo>().eq(Photo::getStart, 1));
            return ResponseEntity.ok().body(count);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getMonthlyPhotoCountChange")
    @Operation(summary = "获取本月照片数量相比上个月照片数量的变化", description = "根据time字段计算本月照片数量相比上个月照片数量的变化")
    public ResponseEntity<Long> getMonthlyPhotoCountChange() {
        try {
            long currentMonthCount = photoService.countByMonth(YearMonth.now());
            long previousMonthCount = photoService.countByMonth(YearMonth.now().minusMonths(1));
            return ResponseEntity.ok().body(currentMonthCount - previousMonthCount);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getYearlyPhotoCountChange")
    @Operation(summary = "获取今年照片数量相比去年照片数量的变化", description = "根据time字段计算今年照片数量相比去年照片数量的变化")
    public ResponseEntity<Long> getYearlyPhotoCountChange() {
        try {
            long currentYearCount = photoService.countByYear(Year.now());
            long previousYearCount = photoService.countByYear(Year.now().minusYears(1));
            return ResponseEntity.ok().body(currentYearCount - previousYearCount);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getMonthlyStartPhotoCountChange")
    @Operation(summary = "获取本月start=1照片数量相比上个月照片数量的变化", description = "根据time字段计算本月代表作照片数量相比上个月照片数量的变化")
    public ResponseEntity<Long> getMonthlyStartPhotoCountChange() {
        try {
            long currentMonthCount = photoService.countByMonthAndStart(YearMonth.now(), 1);
            long previousMonthCount = photoService.countByMonthAndStart(YearMonth.now().minusMonths(1), 1);
            return ResponseEntity.ok().body(currentMonthCount - previousMonthCount);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getYearlyStartPhotoCountChange")
    @Operation(summary = "获取今年start=1照片数量相比去年照片数量的变化", description = "根据time字段计算今年代表作照片数量相比去年照片数量的变化")
    public ResponseEntity<Long> getYearlyStartPhotoCountChange() {
        try {
            long currentYearCount = photoService.countByYearAndStart(Year.now(), 1);
            long previousYearCount = photoService.countByYearAndStart(Year.now().minusYears(1), 1);
            return ResponseEntity.ok().body(currentYearCount - previousYearCount);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getPhotosByPage")
    @Operation(summary = "获取分页照片信息(时间倒叙)", description = "从数据库获取分页照片的详细信息(时间倒叙)")
//    @Cacheable(value = "photos", key = "#page + '_' + #pageSize")
    public ResponseEntity<Page<Photo>> getPhotosByPage(
            @RequestParam int page,
            @RequestParam int pageSize) {
        try {
            System.out.println("page: " + page);
            System.out.println("pageSize: " + pageSize);

            // 确保page参数是从1开始的
            if (page < 1) {
                page = 1;
            }
            // 1. 使用MyBatis Plus的page方法获取分页记录
            Page<Photo> photoPage = new Page<>(page, pageSize);
            photoService.page(photoPage, new LambdaQueryWrapper<Photo>()
                    .orderByDesc(Photo::getTime) // 按时间倒序
            );

            // 2. 返回成功结果，自动使用200状态码
            return ResponseEntity.ok().body(photoPage);

        } catch (Exception e) {
            // 3. 异常处理（建议记录日志）
            e.printStackTrace();
            // 返回500状态码并自动使用错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getStartPhotosByPage")
    @Operation(summary = "获取分页代表作照片信息(时间倒叙)", description = "从数据库获取分页代表作的详细信息(时间倒叙)")
//    @Cacheable(value = "startPhotos", key = "#page + '_' + #pageSize")
    public ResponseEntity<Page<Photo>> getStartPhotosByPage(
            @RequestParam int page,
            @RequestParam int pageSize) {
        try {
            // 1. 使用MyBatis Plus的page方法获取分页记录
            Page<Photo> photoPage = new Page<>(page, pageSize);
            photoService.page(photoPage, new LambdaQueryWrapper<Photo>()
                    .orderByDesc(Photo::getTime) // 按时间倒序
                    .eq(Photo::getStart, 1)
            );

            // 2. 返回成功结果，自动使用200状态码
            return ResponseEntity.ok().body(photoPage);

        } catch (Exception e) {
            // 3. 异常处理（建议记录日志）
            e.printStackTrace();
            // 返回500状态码并自动使用错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}


