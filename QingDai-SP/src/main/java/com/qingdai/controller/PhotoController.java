package com.qingdai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingdai.entity.dto.PhotoStartStatusDTO;
import com.qingdai.entity.Photo;
import com.qingdai.entity.GroupPhotoPhoto;
import com.qingdai.service.PhotoService;
import com.qingdai.service.GroupPhotoPhotoService;
import com.qingdai.service.GroupPhotoService;
import com.qingdai.utils.FileUtils;
import com.qingdai.utils.ValidationUtils;
import com.qingdai.mq.producer.PhotoProcessor;
import com.qingdai.service.FileProcessService;
import com.qingdai.service.PhotoViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.io.IOException;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-02-28
 */
@Slf4j
@RestController
@Tag(name = "图片管理", description = "图片相关操作接口")
@SecurityRequirement(name = "BearerAuth")
@RequestMapping("/photos")
public class PhotoController {

    @Autowired
    PhotoService photoService;
    
    @Autowired
    private PhotoProcessor photoProcessor;

    @Autowired
    private FileProcessService fileProcessService;

    @Autowired
    private PhotoViewService photoViewService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${qingdai.url.fullSizeUrl}")
    private String fullSizeUrl;
    @Value("${qingdai.url.pendingUrl}")
    private String pendingUrl;
    @Value("${qingdai.url.thumbnailSizeUrl}")
    private String thumbnailSizeUrl;
    @Value("${qingdai.url.thumbnail1000KUrl}")
    private String thumbnail1000KUrl;
    @Value("${qingdai.url.thumbnail100KUrl}")
    private String thumbnail100KUrl;

    @Value("${qingdai.redis.key.photo-view}")
    private String viewKeyPrefix;

    @GetMapping("/cdn/thumbnails/small")
    @PreAuthorize("permitAll()")
    @Operation(summary = "批量获取压缩照片文件", description = "根据ID列表获取多个压缩照片文件")
    public ResponseEntity<Resource> getThumbnail100KPhotosByIds(@RequestParam List<String> ids) {
        try {
            List<File> validFiles = new ArrayList<>();

            for (String id : ids) {
                try {
                    String fileName = photoService.getFileNameById(id);
                    if (fileName != null) {
                        File file = new File(thumbnail100KUrl, fileName);
                        if (file.exists()) {
                            validFiles.add(file);
                            log.debug("成功添加照片文件到列表，ID: {}, 文件名: {}", id, fileName);
                        } else {
                            log.warn("照片文件不存在，ID: {}, 文件名: {}", id, fileName);
                        }
                    } else {
                        log.warn("未找到ID为{}的照片记录", id);
                    }
                } catch (NumberFormatException e) {
                    log.warn("无效的ID格式: {}", id);
                }
            }

            if (validFiles.isEmpty()) {
                log.warn("未找到任何有效的照片文件");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            log.info("成功获取批量照片文件，共{}个文件", validFiles.size());
            return FileUtils.getFilesResource(validFiles);
        } catch (Exception e) {
            log.error("批量获取照片文件时发生错误，ID列表: {}, 错误: {}", ids, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/cdn/thumbnail/small")
    @Operation(summary = "获取指定100K压缩照片文件", description = "根据接收到的的照片ID获取100K压缩照片文件并返回")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Resource> getThumbnail100KPhotoById(@RequestParam String id) {
        try {
            String fileName = photoService.getFileNameById(id);
            if (fileName == null) {
                log.warn("未找到ID为{}的照片记录", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            log.info("成功获取100K压缩照片，文件名: {}", fileName);
            return FileUtils.getFileResource(thumbnail100KUrl, fileName);
        } catch (Exception e) {
            log.error("获取100K压缩照片时发生错误，ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/cdn/thumbnail/medium")
    @Operation(summary = "获取指定1000K压缩照片文件", description = "根据接收到的的照片ID获取1000K压缩照片文件并返回")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Resource> getThumbnail1000KPhotoById(@RequestParam String id) {
        try {
            String fileName = photoService.getFileNameById(id);
            if (fileName == null) {
                log.warn("未找到ID为{}的照片记录", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            log.info("成功获取1000K压缩照片，文件名: {}", fileName);
            return FileUtils.getFileResource(thumbnail1000KUrl, fileName);
        } catch (Exception e) {
            log.error("获取1000K压缩照片时发生错误，ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/cdn/fullsize")
    @PreAuthorize("hasRole('VIEWER')")
    @Operation(summary = "获取指定原图照片文件", description = "根据接收到的照片ID获取原图照片文件并返回")
    public ResponseEntity<Resource> getFullSizePhotoById(@RequestParam String id) {
        try {
            String fileName = photoService.getFileNameById(id);
            if (fileName == null) {
                log.warn("未找到ID为{}的照片记录", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            log.info("成功获取原图照片，文件名: {}", fileName);
            return FileUtils.getFileResource(fullSizeUrl, fileName);
        } catch (Exception e) {
            log.error("获取原图照片时发生错误，ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    @Operation(summary = "获取全部照片信息(时间倒叙)", description = "从数据库获取所有照片的详细信息(时间倒叙)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Photo>> getAllPhotos() {
        try {
            List<Photo> photos = photoService.list(
                    new LambdaQueryWrapper<Photo>()
                            .orderByDesc(Photo::getShootTime));

            if (photos == null || photos.isEmpty()) {
                log.warn("未找到任何照片记录");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }

            log.info("成功获取所有照片信息，共{}条记录", photos.size());
            return ResponseEntity.ok().body(photos);

        } catch (Exception e) {
            log.error("获取所有照片信息时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/starred")
    @Operation(summary = "获取代表作照片信息(时间倒叙)", description = "从数据库获取所有代表作的详细信息(时间倒叙)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Photo>> getStartRatingPhotos() {
        try {
            List<Photo> photos = photoService.list(
                    new LambdaQueryWrapper<Photo>()
                            .orderByDesc(Photo::getShootTime)
                            .eq(Photo::getShootTime, 1));

            if (photos == null || photos.isEmpty()) {
                log.warn("未找到任何代表作照片记录");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }

            log.info("成功获取所有代表作照片信息，共{}条记录", photos.size());
            return ResponseEntity.ok().body(photos);

        } catch (Exception e) {
            log.error("获取代表作照片信息时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/import-pending")
    @Operation(summary = "pending目录所有图片信息自动入数据库", description = "pending目录所有图片信息自动入数据库")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> FullSizePhotoToMysql() {
        File folder = FileUtils.validateFolder(pendingUrl);
        if (folder == null) {
            log.error("照片文件夹路径无效: {}", pendingUrl);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("照片文件夹路径无效");
        }

        List<Photo> photos = photoService.getPhotosByFolder(folder);
        if (photos.isEmpty()) {
            log.warn("没有找到有效的照片文件");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("没有有效照片可保存");
        }

        try {
            boolean result = photoService.saveBatch(photos);
            if (result) {
                log.info("成功将{}张照片信息保存到数据库", photos.size());
                return ResponseEntity.status(HttpStatus.OK)
                        .body("成功插入" + photos.size() + "条记录");
            } else {
                log.error("数据库批量插入操作失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("数据库操作失败");
            }
        } catch (Exception e) {
            log.error("保存照片信息到数据库时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("数据库异常: " + e.getMessage());
        }
    }

    @Operation(summary = "批量图片压缩", description = "将pendingUrl目录中的图片压缩到缩略图目录", parameters = {
            @Parameter(name = "maxSizeKB", in = ParameterIn.QUERY, description = "最大文件大小(KB)", schema = @Schema(type = "integer", minimum = "10", maximum = "10240", example = "1024")),
            @Parameter(name = "overwrite", in = ParameterIn.QUERY, description = "覆盖已存在文件", schema = @Schema(type = "boolean"))
    })
    @PostMapping("/thumbnail-pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> thumbnailImages(
            @RequestParam(defaultValue = "1024") int maxSizeKB,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        try {
            File sourceDir = FileUtils.validateFolder(pendingUrl);
            File targetDir = FileUtils.validateFolder(thumbnailSizeUrl);
            if (sourceDir == null || targetDir == null) {
                log.error("目录验证失败：{}或{}不是有效的目录", pendingUrl, thumbnailSizeUrl);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("目录验证失败，请检查配置");
            }

            fileProcessService.thumbnailPhotosFromFolderToFolder(sourceDir, targetDir, maxSizeKB, overwrite);
            log.info("缩略图处理完成");
            return ResponseEntity.ok("缩略图处理完成");
        } catch (Exception e) {
            log.error("处理缩略图时出错：{}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("处理缩略图时出错：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "获取照片元数据", description = "根据ID获取照片的完整元数据信息")
    public ResponseEntity<Photo> getPhotoById(@PathVariable String id) {
        try {
            Photo photo = photoService.getById(id);
            if (photo == null) {
                log.warn("未找到ID为{}的照片记录", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Photo());
            }
            log.info("成功获取照片元数据，ID: {}", id);
            return ResponseEntity.ok(photo);
        } catch (Exception e) {
            log.error("获取照片元数据时发生错误，ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Photo());
        }
    }

    @GetMapping("/count")
    @PreAuthorize("permitAll()")
    @Operation(summary = "获取照片总数", description = "从数据库获取所有照片的总数")
    public ResponseEntity<Long> getPhotoCount() {
        try {
            long count = photoService.count();
            log.info("成功获取照片总数: {}", count);
            return ResponseEntity.ok().body(count);
        } catch (Exception e) {
            log.error("获取照片总数时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/count/starred")
    @PreAuthorize("permitAll()")
    @Operation(summary = "获取代表作照片总数", description = "从数据库获取start字段为1的照片总数")
    public ResponseEntity<Long> getStartPhotoCount() {
        try {
            long count = photoService.count(new LambdaQueryWrapper<Photo>().eq(Photo::getStartRating, 1));
            log.info("成功获取代表作照片总数: {}", count);
            return ResponseEntity.ok().body(count);
        } catch (Exception e) {
            log.error("获取代表作照片总数时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/count/by-types")
    @PreAuthorize("permitAll()")
    @Operation(summary = "根据照片类型获取照片总数", description = "从数据库获取指定类型的照片总数，支持多个类型")
    public ResponseEntity<Long> getPhotoCountByTypes(@RequestParam String types) {
        try {
            // 解析类型参数，支持多个类型，如 "0,1,2"
            String[] typeArray = types.split(",");
            List<Integer> typeList = new ArrayList<>();
            for (String type : typeArray) {
                typeList.add(Integer.parseInt(type.trim()));
            }

            // 构建查询条件
            LambdaQueryWrapper<Photo> queryWrapper = new LambdaQueryWrapper<Photo>()
                    .in(Photo::getStartRating, typeList);

            long count = photoService.count(queryWrapper);
            log.info("成功获取照片类型{}的照片总数: {}", types, count);
            return ResponseEntity.ok().body(count);
        } catch (Exception e) {
            log.error("获取照片类型{}的照片总数时发生错误: {}", types, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/count/monthly-change")
    @PreAuthorize("permitAll()")
    @Operation(summary = "获取本月照片数量相比上个月照片数量的变化", description = "根据time字段计算本月照片数量相比上个月照片数量的变化")
    public ResponseEntity<Long> getMonthlyPhotoCountChange() {
        try {
            long currentMonthCount = photoService.countByMonth(YearMonth.now());
            long previousMonthCount = photoService.countByMonth(YearMonth.now().minusMonths(1));
            long change = currentMonthCount - previousMonthCount;
            log.info("成功计算月度照片数量变化，本月: {}, 上月: {}, 变化: {}", currentMonthCount, previousMonthCount, change);
            return ResponseEntity.ok().body(change);
        } catch (Exception e) {
            log.error("计算月度照片数量变化时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/count/yearly-change")
    @PreAuthorize("permitAll()")
    @Operation(summary = "获取今年照片数量相比去年照片数量的变化", description = "根据time字段计算今年照片数量相比去年照片数量的变化")
    public ResponseEntity<Long> getYearlyPhotoCountChange() {
        try {
            long currentYearCount = photoService.countByYear(Year.now());
            long previousYearCount = photoService.countByYear(Year.now().minusYears(1));
            long change = currentYearCount - previousYearCount;
            log.info("成功计算年度照片数量变化，今年: {}, 去年: {}, 变化: {}", currentYearCount, previousYearCount, change);
            return ResponseEntity.ok().body(change);
        } catch (Exception e) {
            log.error("计算年度照片数量变化时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/count/starred/monthly-change")
    @Operation(summary = "获取本月精选照片数量相比上个月照片数量的变化", description = "根据time字段计算本月代表作照片数量相比上个月照片数量的变化")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Long> getMonthlyStartPhotoCountChange() {
        try {
            long currentMonthCount = photoService.countByMonthAndStart(YearMonth.now(), 1);
            long previousMonthCount = photoService.countByMonthAndStart(YearMonth.now().minusMonths(1), 1);
            long change = currentMonthCount - previousMonthCount;
            log.info("成功计算月度精选照片数量变化，本月: {}, 上月: {}, 变化: {}", currentMonthCount, previousMonthCount, change);
            return ResponseEntity.ok().body(change);
        } catch (Exception e) {
            log.error("计算月度精选照片数量变化时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/count/starred/yearly-change")
    @Operation(summary = "获取今年精选照片数量相比去年照片数量的变化", description = "根据time字段计算今年代表作照片数量相比去年照片数量的变化")
    public ResponseEntity<Long> getYearlyStartPhotoCountChange() {
        try {
            long currentYearCount = photoService.countByYearAndStart(Year.now(), 1);
            long previousYearCount = photoService.countByYearAndStart(Year.now().minusYears(1), 1);
            long change = currentYearCount - previousYearCount;
            log.info("成功计算年度精选照片数量变化，今年: {}, 去年: {}, 变化: {}", currentYearCount, previousYearCount, change);
            return ResponseEntity.ok().body(change);
        } catch (Exception e) {
            log.error("计算年度精选照片数量变化时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/visible")
    @Operation(summary = "获取可见图片分页信息", description = "从数据库获取start=0或1的分页照片信息(时间倒叙)")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<Photo>> getVisiblePhotosByPage(
            @RequestParam int page,
            @RequestParam int pageSize) {
        try {
            if (page < 1) {
                page = 1;
                log.warn("页码小于1，已自动调整为1");
            }
            Page<Photo> photoPage = new Page<>(page, pageSize);
            photoService.page(photoPage, new LambdaQueryWrapper<Photo>()
                    .orderByDesc(Photo::getShootTime)
                    .in(Photo::getStartRating, Arrays.asList(0, 1)));
            log.info("成功获取可见照片分页信息，总记录数: {}, 总页数: {}", photoPage.getTotal(), photoPage.getPages());
            return ResponseEntity.ok().body(photoPage);
        } catch (Exception e) {
            log.error("获取可见照片分页信息时发生错误，页码: {}, 每页大小: {}, 错误: {}", page, pageSize, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/page")
    @Operation(summary = "获取分页照片信息(时间倒叙)", description = "从数据库获取分页照片的信息(时间倒叙)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Photo>> getPhotosByPage(
            @RequestParam int page,
            @RequestParam int pageSize) {
        try {
            if (page < 1) {
                page = 1;
                log.warn("页码小于1，已自动调整为1");
            }
            Page<Photo> photoPage = new Page<>(page, pageSize);
            photoService.page(photoPage, new LambdaQueryWrapper<Photo>()
                    .orderByDesc(Photo::getShootTime));
            log.info("成功获取照片分页信息，总记录数: {}, 总页数: {}", photoPage.getTotal(), photoPage.getPages());
            return ResponseEntity.ok().body(photoPage);
        } catch (Exception e) {
            log.error("获取照片分页信息时发生错误，页码: {}, 每页大小: {}, 错误: {}", page, pageSize, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/starred/page")
    @Operation(summary = "获取分页代表作照片信息(时间倒叙)", description = "从数据库获取分页代表作的详细信息(时间倒叙)")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<Photo>> getStartPhotosByPage(
            @RequestParam int page,
            @RequestParam int pageSize) {
        try {
            Page<Photo> photoPage = new Page<>(page, pageSize);
            photoService.page(photoPage, new LambdaQueryWrapper<Photo>()
                    .orderByDesc(Photo::getShootTime)
                    .eq(Photo::getStartRating, 1));
            log.info("成功获取代表作照片分页信息，总记录数: {}, 总页数: {}", photoPage.getTotal(), photoPage.getPages());
            return ResponseEntity.ok().body(photoPage);
        } catch (Exception e) {
            log.error("获取代表作照片分页信息时发生错误，页码: {}, 每页大小: {}, 错误: {}", page, pageSize, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "处理待处理图片", description = "将pendingUrl内的图片添加到数据库，压缩到thumbnailSizeUrl，复制到fullSizeUrl，删除pendingUrl图片")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/process-pending")
    public ResponseEntity<String> processPendingPhotos(
            @RequestParam(defaultValue = "false") boolean overwrite) {
        try {
            File pendingDir = FileUtils.validateFolder(pendingUrl);
            File fullSizeDir = FileUtils.validateFolder(fullSizeUrl);
            File thumbnail100KDir = FileUtils.validateFolder(thumbnail100KUrl);
            File thumbnail1000KDir = FileUtils.validateFolder(thumbnail1000KUrl);

            if (pendingDir == null || fullSizeDir == null || thumbnail100KDir == null || thumbnail1000KDir == null) {
                log.error("目录验证失败，pendingDir: {}, fullSizeDir: {}, thumbnail100KDir: {}, thumbnail1000KDir: {}",
                        pendingDir, fullSizeDir, thumbnail100KDir, thumbnail1000KDir);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("目录验证失败");
            }

            PhotoService.ProcessResult result = photoService.processPendingPhotosWithCompression(
                pendingDir, thumbnail100KDir, thumbnail1000KDir, fullSizeDir, overwrite);

            if (result.isSuccess()) {
                log.info("成功处理所有照片，共处理{}张", result.getNewPhotos().size());
                return ResponseEntity.ok("处理成功，已处理 " + result.getNewPhotos().size() + " 张照片");
            } else {
                log.error("处理照片失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("处理照片失败");
            }
        } catch (Exception e) {
            log.error("处理待处理图片时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("处理出错: " + e.getMessage());
        }
    }

    @Operation(summary = "上传图片", description = "将前端传入的图片添加到数据库，压缩到thumbnailSizeUrl，复制到fullSizeUrl")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> processPhotosFromFrontend(
            @RequestParam(defaultValue = "true") boolean overwrite,
            @RequestParam(defaultValue = "0") Integer start,
            @Parameter(description = "上传的图片文件数组", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, array = @ArraySchema(arraySchema = @Schema(type = "array", implementation = File.class), schema = @Schema(type = "string", format = "binary")))) @RequestParam("files") MultipartFile[] files) {
        long startTime = System.currentTimeMillis();
        try {
            if (files == null || files.length == 0) {
                log.warn("没有收到文件");
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "没有收到文件"));
            }

            // 验证文件目录
            File pendingDir = FileUtils.validateFolder(pendingUrl);
            File fullSizeDir = FileUtils.validateFolder(fullSizeUrl);
            File thumbnail100KDir = FileUtils.validateFolder(thumbnail100KUrl);
            File thumbnail1000KDir = FileUtils.validateFolder(thumbnail1000KUrl);

            if (pendingDir == null || fullSizeDir == null || thumbnail100KDir == null || thumbnail1000KDir == null) {
                log.error("目录验证失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "目录验证失败"));
            }

            // 创建随机名称的临时目录
            File tempDir = FileUtils.createTempDir(pendingDir);
            if (!tempDir.exists() && !tempDir.mkdirs()) {
                throw new IOException("无法创建临时目录: " + tempDir.getAbsolutePath());
            }
            log.info("创建临时目录: {}", tempDir.getAbsolutePath());

            // 保存到临时目录
            String[] fileNames = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                FileUtils.saveFile(file, tempDir);
                fileNames[i] = file.getOriginalFilename();
                log.debug("图片：{},已保存文件到临时目录", file.getOriginalFilename());
            }

            // 使用RocketMQ进行异步处理
            try {
                // 创建消息ID
                String messageId = String.valueOf(System.currentTimeMillis());
                
                // 创建图片处理消息
                PhotoProcessor.PhotoMessage photoMessage = new PhotoProcessor.PhotoMessage(
                        start, overwrite, fileNames, tempDir.getAbsolutePath());
                // 设置消息ID
                photoMessage.setMessageId(messageId);
                
                // 初始化任务状态
                photoService.updatePhotoUploadStatus(messageId, "PROCESSING", 0, "已提交到处理队列");
                
                // 使用MQ发送消息
                photoProcessor.sendPhotoProcessMessage(photoMessage);
                
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                log.info("图片上传成功，已提交到队列进行异步处理，耗时{}毫秒", duration);
                
                // 返回消息ID，以便前端查询进度
                Map<String, Object> response = new HashMap<>();
                response.put("messageId", messageId);
                response.put("message", "图片上传成功，已提交到队列进行异步处理，将在后台处理 " + files.length + " 张图片");
                response.put("files", files.length);
                
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                log.error("提交到消息队列时出错, 错误: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.singletonMap("error", "提交到消息队列时出错: " + e.getMessage()));
            }
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.error("处理图片时发生错误: {}, 耗时{}毫秒", e.getMessage(), duration, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "处理出错: " + e.getMessage() + "，耗时" + duration + "毫秒"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新照片信息", description = "根据ID更新照片元数据信息")
    public ResponseEntity<String> updatePhotoInfo(@RequestBody Photo photo, @PathVariable String id) {
        try {
            if (photo.getId() == null) {
                log.error("更新照片信息失败:ID为空");
                return ResponseEntity.badRequest().body("ID不能为空");
            }

            // 获取原照片信息
            Photo oldPhoto = photoService.getById(photo.getId());
            if (oldPhoto == null) {
                log.warn("未找到ID为{}的照片记录", photo.getId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到对应照片记录");
            }

            // 使用Service层方法处理文件重命名和数据库更新的一致性
            boolean result = photoService.updatePhotoWithFileName(photo, oldPhoto.getFileName());

            if (result) {
                log.info("成功更新照片信息,ID: {}", photo.getId());
                return ResponseEntity.ok("照片信息更新成功");
            } else {
                log.warn("更新照片信息失败,ID: {}", photo.getId());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新照片信息失败");
            }
        } catch (Exception e) {
            log.error("更新照片信息时发生错误,ID: {}, 错误: {}", photo.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新过程中发生错误: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "根据ID删除照片", description = "根据ID删除数据库记录及对应的原图和压缩图文件")
    public ResponseEntity<String> deletePhotoById(@PathVariable String id) {
        try {
            boolean result = photoService.deletePhotoById(id, fullSizeUrl, thumbnail100KUrl, thumbnail1000KUrl);
            
            if (result) {
                return ResponseEntity.ok("照片删除成功");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("照片不存在");
            }
        } catch (Exception e) {
            log.error("删除照片时发生错误，ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("删除照片出错: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/starred")
    @Operation(summary = "更新照片星标状态", description = "根据ID更新照片的星标状态")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updatePhotoStartStartRating(
            @RequestBody PhotoStartStatusDTO photoStartStatusDTO, @PathVariable String id) {
        try {
            Integer start = photoStartStatusDTO.getStartRating();

            if (start == null || (start != 0 && start != 1 && start != -1)) {
                log.error("更新星标状态失败：无效的start值: {}", start);
                return ResponseEntity.badRequest().body("start参数必须为0或1");
            }

            boolean updated = photoService.update(new LambdaUpdateWrapper<Photo>()
                    .eq(Photo::getId, Long.parseLong(id))
                    .set(Photo::getStartRating, start));

            if (!updated) {
                log.warn("未找到ID为{}的照片记录", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到对应照片记录");
            }

            log.info("成功更新照片星标状态，ID: {}, 新状态: {}", id, start);
            return ResponseEntity.ok("星标状态更新成功");
        } catch (Exception e) {
            log.error("更新照片星标状态时发生错误，DTO: {}, 错误: {}", photoStartStatusDTO, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("更新过程中发生错误: " + e.getMessage());
        }
    }

    @GetMapping("/ids")
    @PreAuthorize("permitAll()")
    @Operation(summary = "根据ID列表获取多个照片对象", description = "从数据库获取对应ID列表的多个照片信息")
    public ResponseEntity<List<Photo>> getPhotosByIds(@RequestParam List<String> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                log.warn("请求的ID列表为空");
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }

            List<Long> photoIds = new ArrayList<>();
            for (String id : ids) {
                try {
                    photoIds.add(Long.parseLong(id));
                    log.debug("添加有效ID到查询列表: {}", id);
                } catch (NumberFormatException e) {
                    log.warn("无效的ID格式: {}", id);
                }
            }

            if (photoIds.isEmpty()) {
                log.warn("没有有效的照片ID");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }

            List<Photo> photos = photoService.listByIds(photoIds);

            if (photos.isEmpty()) {
                log.warn("未找到任何照片记录，ID列表: {}", ids);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }

            log.info("成功获取批量照片对象，共{}个照片", photos.size());
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            log.error("批量获取照片对象时发生错误，ID列表: {}, 错误: {}", ids, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/hidden/page")
    @Operation(summary = "获取分页隐藏照片信息(时间倒叙)", description = "从数据库获取分页隐藏照片的详细信息(start=-1)(时间倒叙)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Photo>> getHiddenPhotosByPage(
            @RequestParam int page,
            @RequestParam int pageSize) {
        try {
            if (page < 1) {
                page = 1;
                log.warn("页码小于1，已自动调整为1");
            }
            Page<Photo> photoPage = new Page<>(page, pageSize);
            photoService.page(photoPage, new LambdaQueryWrapper<Photo>()
                    .orderByDesc(Photo::getShootTime)
                    .eq(Photo::getStartRating,-1));
            log.info("成功获取隐藏照片分页信息，总记录数: {}, 总页数: {}", photoPage.getTotal(), photoPage.getPages());
            return ResponseEntity.ok().body(photoPage);
        } catch (Exception e) {
            log.error("获取隐藏照片分页信息时发生错误，页码: {}, 每页大小: {}, 错误: {}", page, pageSize, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/meteorology/page")
    @Operation(summary = "获取分页气象照片信息(时间倒叙)", description = "从数据库获取分页气象照片的详细信息(start=2)(时间倒叙)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Photo>> getMeteorologyPhotosByPage(
            @RequestParam int page,
            @RequestParam int pageSize) {
        try {
            if (page < 1) {
                page = 1;
                log.warn("页码小于1，已自动调整为1");
            }
            Page<Photo> photoPage = new Page<>(page, pageSize);
            photoService.page(photoPage, new LambdaQueryWrapper<Photo>()
                    .orderByDesc(Photo::getShootTime)
                    .eq(Photo::getStartRating, 2));
            log.info("成功获取气象照片分页信息，总记录数: {}, 总页数: {}", photoPage.getTotal(), photoPage.getPages());
            return ResponseEntity.ok().body(photoPage);
        } catch (Exception e) {
            log.error("获取气象照片分页信息时发生错误，页码: {}, 每页大小: {}, 错误: {}", page, pageSize, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/validate-existence")
    @Operation(summary = "验证数据库照片文件是否存在", description = "验证数据库记录中的照片是否在原图、100K压缩图和1000K压缩图目录中存在")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> validatePhotoExistence() {
        try {
            log.info("控制器调用: 验证数据库照片在文件系统中的存在性");
            Map<String, Object> result = photoService.validatePhotoExistence();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("验证照片文件存在性时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "验证过程中发生错误: " + e.getMessage()));
        }
    }

    @GetMapping("/validate-filesystem")
    @Operation(summary = "验证文件系统照片是否存在于数据库", description = "验证文件系统中的照片是否在数据库中有记录")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> validateFileSystemPhotos() {
        try {
            log.info("控制器调用: 验证文件系统中照片在数据库中的存在性");
            Map<String, Object> result = photoService.validateFileSystemPhotos(fullSizeUrl, thumbnail100KUrl,
                    thumbnail1000KUrl);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("验证文件系统照片时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "验证过程中发生错误: " + e.getMessage()));
        }
    }

    @GetMapping("/validate-meteorology-groups")
    @Operation(summary = "验证气象组图冲突", description = "检查照片是否同时属于冲突的气象组图类型")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> validateMeteorologyGroups() {
        try {
            log.info("控制器调用: 验证气象组图冲突");
            Map<String, Object> result = photoService.validateMeteorologyGroups();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("验证气象组图冲突时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "验证过程中发生错误: " + e.getMessage()));
        }
    }

    @DeleteMapping("/not-in-database")
    @Operation(summary = "删除数据库中没有记录的照片", description = "删除文件系统中存在但数据库中没有记录的照片文件")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deletePhotosNotInDatabase() {
        try {
            log.info("控制器调用: 删除文件系统中数据库没有记录的照片");
            Map<String, Object> result = photoService.deletePhotosNotInDatabase(fullSizeUrl, thumbnail100KUrl,
                    thumbnail1000KUrl);

            if (result.containsKey("error")) {
                log.error("删除操作失败: {}", result.get("error"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(result);
            }

            log.info("成功删除数据库中没有记录的照片");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("删除数据库中没有记录的照片时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "删除过程中发生错误: " + e.getMessage()));
        }
    }

    @DeleteMapping("/missing-records")
    @Operation(summary = "删除丢失了全部三种图片的数据库记录", description = "删除数据库中丢失了原图、100K和1000K全部三种图片文件的记录")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteMissingPhotoRecords() {
        try {
            log.info("控制器调用: 删除丢失了全部三种图片的数据库记录");
            Map<String, Object> result = photoService.deleteMissingPhotoRecords();

            if (result.containsKey("error")) {
                log.error("删除操作失败: {}", result.get("error"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(result);
            }

            log.info("成功删除丢失了全部三种图片的数据库记录");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("删除丢失照片记录时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "删除过程中发生错误: " + e.getMessage()));
        }
    }

    @GetMapping("/stats/dashboard")
    @Operation(summary = "获取所有照片统计数据", description = "获取照片类型、年月变化、拍摄主题、相机、镜头、参数等全面统计数据")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> getPhotoDashboardStats() {
        try { 
            Map<String, Object> stats = photoService.getPhotoDashboardStats();
            log.info("成功获取照片统计数据");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取照片统计数据时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/cameras")
    @Operation(summary = "获取所有相机型号", description = "获取数据库中所有不重复的相机型号列表")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<String>> getAllCameras() {
        log.info("获取所有相机型号");
        List<String> cameras = photoService.getAllCameras();
        return ResponseEntity.ok(cameras);
    }

    @GetMapping("/lenses")
    @Operation(summary = "获取所有镜头型号", description = "获取数据库中所有不重复的镜头型号列表")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<String>> getAllLenses() {
        log.info("获取所有镜头型号");
        List<String> lenses = photoService.getAllLenses();
        return ResponseEntity.ok(lenses);
    }

    @PutMapping("/cameras/{oldCamera}")
    @Operation(summary = "批量更新相机型号", description = "将所有使用指定相机型号的照片更新为新的相机型号")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateCameraName(
            @PathVariable String oldCamera,
            @RequestParam String newCamera) {
        log.info("更新相机型号: {} -> {}", oldCamera, newCamera);
        
        if (photoService.updateCameraName(oldCamera, newCamera)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/lenses")
    @Operation(summary = "批量更新镜头型号", description = "将所有使用指定镜头型号的照片更新为新的镜头型号")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateLensName(
            @RequestParam String oldLens,
            @RequestParam String newLens) {
        log.info("更新镜头型号: {} -> {}", oldLens, newLens);
        
        if (photoService.updateLensName(oldLens, newLens)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/count/by-camera/{camera}")
    @Operation(summary = "获取指定相机型号的照片数量", description = "获取使用指定相机型号拍摄的照片数量")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Long> getPhotoCountByCamera(@PathVariable String camera) {
        log.info("获取相机 {} 的照片数量", camera);
        long count = photoService.getPhotoCountByCamera(camera);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/by-lens")
    @Operation(summary = "获取指定镜头型号的照片数量", description = "获取使用指定镜头型号拍摄的照片数量")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Long> getPhotoCountByLens(@RequestParam String lens) {
        log.info("获取镜头 {} 的照片数量", lens);
        long count = photoService.getPhotoCountByLens(lens);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/focal-lengths")
    @Operation(summary = "获取所有焦距值", description = "获取数据库中所有不重复的焦距值列表")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<String>> getAllFocalLengths() {
        log.info("获取所有焦距值");
        List<String> focalLengths = photoService.getAllFocalLengths();
        return ResponseEntity.ok(focalLengths);
    }

    @GetMapping("/count/by-focal-length/{focalLength}")
    @Operation(summary = "获取指定焦距的照片数量", description = "获取使用指定焦距拍摄的照片数量")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Long> getPhotoCountByFocalLength(@PathVariable String focalLength) {
        log.info("获取焦距 {} 的照片数量", focalLength);
        long count = photoService.getPhotoCountByFocalLength(focalLength);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/focal-lengths/{oldFocalLength}")
    @Operation(summary = "批量更新焦距值", description = "将所有使用指定焦距的照片更新为新的焦距值")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateFocalLength(
            @PathVariable String oldFocalLength,
            @RequestParam String newFocalLength) {
        log.info("更新焦距值: {} -> {}", oldFocalLength, newFocalLength);
        
        if (photoService.updateFocalLength(oldFocalLength, newFocalLength)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/no-metadata/page")
    @Operation(summary = "获取无元数据照片分页信息", description = "获取除title、introduce、created_time、updated_time外其他字段为空的分页照片信息")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Photo>> getNoMetadataPhotosByPage(
            @RequestParam int page,
            @RequestParam int pageSize) {
        try {
            if (page < 1) {
                page = 1;
                log.warn("页码小于1，已自动调整为1");
            }
            
            Page<Photo> photoPage = photoService.getNoMetadataPhotosByPage(page, pageSize);
            log.info("成功获取无元数据照片分页信息，总记录数: {}, 总页数: {}", photoPage.getTotal(), photoPage.getPages());
            return ResponseEntity.ok().body(photoPage);
        } catch (Exception e) {
            log.error("获取无元数据照片分页信息时发生错误，页码: {}, 每页大小: {}, 错误: {}", page, pageSize, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{messageId}/upload/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询照片上传状态", description = "根据消息ID查询异步上传照片的处理状态")
    public ResponseEntity<Map<String, Object>> checkPhotoUploadStatus(@PathVariable String messageId) {
        try {
            Map<String, Object> statusInfo = photoService.getPhotoUploadStatus(messageId);
            log.info("查询照片上传状态，消息ID: {}, 状态: {}", messageId, statusInfo.get("status"));
            return ResponseEntity.ok(statusInfo);
        } catch (Exception e) {
            log.error("查询照片上传状态时发生错误，消息ID: {}, 错误: {}", messageId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}/view-count")
    @PreAuthorize("permitAll()")
    @Operation(summary = "获取照片浏览量", description = "获取指定照片的浏览量")
    public ResponseEntity<Long> getPhotoViewCount(@PathVariable String id) {
        try {
            long count = photoViewService.getViewCount(id);
            log.info("成功获取照片浏览量，ID: {}, 浏览量: {}", id, count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("获取照片浏览量时发生错误，ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats/views")
    @Operation(summary = "获取所有照片浏览量统计", description = "获取所有照片的浏览量统计信息")
    public ResponseEntity<List<Map<String, Object>>> getPhotoViewStats() {
        try {
            List<Map<String, Object>> stats = photoViewService.getAllPhotoViewStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取照片浏览量统计失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/view-count")
    @PreAuthorize("permitAll()")
    @Operation(summary = "增加照片浏览量", description = "增加指定照片的浏览量")
    public ResponseEntity<Long> incrementPhotoViewCount(
            @PathVariable String id,
            HttpServletRequest request) {
        try {
            String ip = getClientIp(request);
            long count = photoViewService.incrementViewCount(id, ip);
            log.info("成功增加照片浏览量，ID: {}, IP: {}, 当前浏览量: {}", id, ip, count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("增加照片浏览量时发生错误，ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP，多个IP按照','分割
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
