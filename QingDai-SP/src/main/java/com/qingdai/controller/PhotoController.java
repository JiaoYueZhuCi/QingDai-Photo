package com.qingdai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingdai.entity.dto.PhotoStartStatusDTO;
import com.qingdai.entity.Photo;
import com.qingdai.service.PhotoService;
import com.qingdai.utils.FileUtils;
import com.qingdai.utils.ValidationUtils;
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

import java.io.File;
import java.io.IOException;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
@RequestMapping("/photo")
public class PhotoController {

    @Autowired
    PhotoService photoService;
    @Value("${qingdai.fullSizeUrl}")
    private String fullSizeUrl;
    @Value("${qingdai.pendingUrl}")
    private String pendingUrl;
    @Value("${qingdai.thumbnailSizeUrl}")
    private String thumbnailSizeUrl;
    @Value("${qingdai.thumbnail1000KUrl}")
    private String thumbnail1000KUrl;
    @Value("${qingdai.thumbnail100KUrl}")
    private String thumbnail100KUrl;


    @GetMapping("/getAllPhotos")
    @Operation(summary = "获取全部照片信息(时间倒叙)", description = "从数据库获取所有照片的详细信息(时间倒叙)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Photo>> getAllPhotos() {
        try {
            List<Photo> photos = photoService.list(
                    new LambdaQueryWrapper<Photo>()
                            .orderByDesc(Photo::getTime)
            );

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

    @GetMapping("/getStartPhotos")
    @Operation(summary = "获取代表作照片信息(时间倒叙)", description = "从数据库获取所有代表作的详细信息(时间倒叙)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Photo>> getStartPhotos() {
        try {
            List<Photo> photos = photoService.list(
                    new LambdaQueryWrapper<Photo>()
                            .orderByDesc(Photo::getTime)
                            .eq(Photo::getStart, 1));

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

    @GetMapping("/toMysql")
    @Operation(summary = "所有图片信息自动入数据库", description = "所有图片信息自动入数据库")
    // @PreAuthorize("hasRole('ADMIN')")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> FullSizePhototoMysql() {
        log.info("开始执行图片信息入库操作，源目录: {}", fullSizeUrl);
        
        File folder = FileUtils.validateFolder(fullSizeUrl);
        if (folder == null) {
            log.error("照片文件夹路径无效: {}", fullSizeUrl);
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
    @GetMapping("/thumbnail")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> thumbnailImages(
            @RequestParam(defaultValue = "1024") int maxSizeKB,
            @RequestParam(defaultValue = "false") boolean overwrite) {

        try {
            log.info("开始执行图片压缩任务，最大大小: {}KB, 是否覆盖: {}", maxSizeKB, overwrite);
            ValidationUtils.validateMaxSize(maxSizeKB);

            File pendingDir = new File(pendingUrl);
            File thumbnailSizeDir = new File(thumbnailSizeUrl);

            FileUtils.validateDirectory(pendingDir);
            FileUtils.validateDirectory(thumbnailSizeDir);

            photoService.thumbnailPhotosFromFolderToFolder(pendingDir, thumbnailSizeDir, maxSizeKB, overwrite);

            if (thumbnailSizeDir.listFiles() == null || thumbnailSizeDir.listFiles().length == 0) {
                log.error("压缩任务失败，目标文件夹未生成任何文件");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("压缩任务失败，目标文件夹未生成任何文件");
            }

            log.info("图片压缩任务完成，目标目录: {}", thumbnailSizeDir.getPath());
            return ResponseEntity.ok("压缩任务已完成");
        } catch (IllegalArgumentException e) {
            log.error("图片压缩参数验证失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getThumbnail100KPhoto")
    @Operation(summary = "获取指定100K压缩照片文件", description = "根据接收到的的照片ID获取100K压缩照片文件并返回")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Resource> getThumbnail100KPhotoById(@RequestParam String id) {
        try {
            log.info("获取100K压缩照片，ID: {}", id);
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

    @GetMapping("/getThumbnail1000KPhoto")
    @Operation(summary = "获取指定1000K压缩照片文件", description = "根据接收到的的照片ID获取1000K压缩照片文件并返回")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Resource> getThumbnail1000KPhotoById(@RequestParam String id) {
        try {
            log.info("获取1000K压缩照片，ID: {}", id);
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

    @GetMapping("/getThumbnail100KPhotos")
    @PreAuthorize("permitAll()")
    @Operation(summary = "批量获取压缩照片文件", description = "根据ID列表获取多个压缩照片文件")
    public ResponseEntity<Resource> getThumbnail100KPhotosByIds(@RequestParam List<String> ids) {
        try {
            log.info("开始批量获取100K压缩照片，ID列表: {}", ids);
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

    @GetMapping("/getFullSizePhoto")
    @PreAuthorize("hasRole('VIEWER')")
    @Operation(summary = "获取指定原图照片文件", description = "根据接收到的照片ID获取原图照片文件并返回")
    public ResponseEntity<Resource> getFullSizePhotoById(@RequestParam String id) {
        try {
            log.info("获取原图照片，ID: {}", id);
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

    @GetMapping("/getPhotoInfo")
    @PreAuthorize("permitAll()")
    @Operation(summary = "获取照片元数据", description = "根据ID获取照片的完整元数据信息")
    public ResponseEntity<Photo> getPhotoById(@RequestParam String id) {
        try {
            log.info("开始获取照片元数据，ID: {}", id);
            Photo photo = photoService.getById(Long.valueOf(id));
            if (photo == null) {
                log.warn("未找到ID为{}的照片记录", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            log.info("成功获取照片元数据，ID: {}", id);
            return ResponseEntity.ok(photo);
        } catch (Exception e) {
            log.error("获取照片元数据时发生错误，ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getPhotoCount")
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

    @GetMapping("/getStartPhotoCount")
    @PreAuthorize("permitAll()")
    @Operation(summary = "获取代表作照片总数", description = "从数据库获取start字段为1的照片总数")
    public ResponseEntity<Long> getStartPhotoCount() {
        try {
            long count = photoService.count(new LambdaQueryWrapper<Photo>().eq(Photo::getStart, 1));
            log.info("成功获取代表作照片总数: {}", count);
            return ResponseEntity.ok().body(count);
        } catch (Exception e) {
            log.error("获取代表作照片总数时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getMonthlyPhotoCountChange")
    @PreAuthorize("permitAll()")
    @Operation(summary = "获取本月照片数量相比上个月照片数量的变化", description = "根据time字段计算本月照片数量相比上个月照片数量的变化")
    public ResponseEntity<Long> getMonthlyPhotoCountChange() {
        try {
            log.info("开始计算月度照片数量变化");
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

    @GetMapping("/getYearlyPhotoCountChange")
    @PreAuthorize("permitAll()")
    @Operation(summary = "获取今年照片数量相比去年照片数量的变化", description = "根据time字段计算今年照片数量相比去年照片数量的变化")
    public ResponseEntity<Long> getYearlyPhotoCountChange() {
        try {
            log.info("开始计算年度照片数量变化");
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

    @GetMapping("/getMonthlyStartPhotoCountChange")
    @Operation(summary = "获取本月精选照片数量相比上个月照片数量的变化", description = "根据time字段计算本月代表作照片数量相比上个月照片数量的变化")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Long> getMonthlyStartPhotoCountChange() {
        try {
            log.info("开始计算月度精选照片数量变化");
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

    @GetMapping("/getYearlyStartPhotoCountChange")
    @Operation(summary = "获取今年精选照片数量相比去年照片数量的变化", description = "根据time字段计算今年代表作照片数量相比去年照片数量的变化")
    public ResponseEntity<Long> getYearlyStartPhotoCountChange() {
        try {
            log.info("开始计算年度精选照片数量变化");
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

    @GetMapping("/getVisiblePhotosByPage")
    @Operation(summary = "获取可见图片分页信息", description = "从数据库获取start=0或1的分页照片信息(时间倒叙)")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<Photo>> getVisiblePhotosByPage(
            @RequestParam int page,
            @RequestParam int pageSize) {
        try {
            log.info("开始获取可见照片分页信息，页码: {}, 每页大小: {}", page, pageSize);
            if (page < 1) {
                page = 1;
                log.warn("页码小于1，已自动调整为1");
            }
            Page<Photo> photoPage = new Page<>(page, pageSize);
            photoService.page(photoPage, new LambdaQueryWrapper<Photo>()
                    .orderByDesc(Photo::getTime)
                    .in(Photo::getStart, Arrays.asList(0, 1)));
            log.info("成功获取可见照片分页信息，总记录数: {}, 总页数: {}", photoPage.getTotal(), photoPage.getPages());
            return ResponseEntity.ok().body(photoPage);
        } catch (Exception e) {
            log.error("获取可见照片分页信息时发生错误，页码: {}, 每页大小: {}, 错误: {}", page, pageSize, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getPhotosByPage")
    @Operation(summary = "获取分页照片信息(时间倒叙)", description = "从数据库获取分页照片的信息(时间倒叙)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Photo>> getPhotosByPage(
            @RequestParam int page,
            @RequestParam int pageSize) {
        try {
            log.info("开始获取照片分页信息，页码: {}, 每页大小: {}", page, pageSize);
            if (page < 1) {
                page = 1;
                log.warn("页码小于1，已自动调整为1");
            }
            Page<Photo> photoPage = new Page<>(page, pageSize);
            photoService.page(photoPage, new LambdaQueryWrapper<Photo>()
                    .orderByDesc(Photo::getTime));
            log.info("成功获取照片分页信息，总记录数: {}, 总页数: {}", photoPage.getTotal(), photoPage.getPages());
            return ResponseEntity.ok().body(photoPage);
        } catch (Exception e) {
            log.error("获取照片分页信息时发生错误，页码: {}, 每页大小: {}, 错误: {}", page, pageSize, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getStartPhotosByPage")
    @Operation(summary = "获取分页代表作照片信息(时间倒叙)", description = "从数据库获取分页代表作的详细信息(时间倒叙)")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<Photo>> getStartPhotosByPage(
            @RequestParam int page,
            @RequestParam int pageSize) {
        try {
            log.info("开始获取代表作照片分页信息，页码: {}, 每页大小: {}", page, pageSize);
            Page<Photo> photoPage = new Page<>(page, pageSize);
            photoService.page(photoPage, new LambdaQueryWrapper<Photo>()
                    .orderByDesc(Photo::getTime)
                    .eq(Photo::getStart, 1));
            log.info("成功获取代表作照片分页信息，总记录数: {}, 总页数: {}", photoPage.getTotal(), photoPage.getPages());
            return ResponseEntity.ok().body(photoPage);
        } catch (Exception e) {
            log.error("获取代表作照片分页信息时发生错误，页码: {}, 每页大小: {}, 错误: {}", page, pageSize, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Transactional
    @Operation(summary = "处理待处理图片", description = "将pendingUrl内的图片添加到数据库，压缩到thumbnailSizeUrl，复制到fullSizeUrl，删除pendingUrl图片")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/processPendingPhotos")
    public ResponseEntity<String> processPendingPhotos(
            @RequestParam(defaultValue = "false") boolean overwrite) {
        try {
            log.info("开始处理待处理图片，是否覆盖: {}", overwrite);
            
            File pendingDir = FileUtils.validateFolder(pendingUrl);
            File fullSizeDir = FileUtils.validateFolder(fullSizeUrl);
            File thumbnail100KDir = FileUtils.validateFolder(thumbnail100KUrl);
            File thumbnail1000KDir = FileUtils.validateFolder(thumbnail1000KUrl);

            if (pendingDir == null || fullSizeDir == null || thumbnail100KDir == null || thumbnail1000KDir == null) {
                log.error("目录验证失败，pendingDir: {}, fullSizeDir: {}, thumbnail100KDir: {}, thumbnail1000KDir: {}", 
                    pendingDir, fullSizeDir, thumbnail100KDir, thumbnail1000KDir);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("目录验证失败");
            }

            log.info("开始获取待处理图片列表");
            List<Photo> photos = photoService.getPhotosByFolder(pendingDir);

            if (photos.isEmpty()) {
                log.warn("没有找到有效的待处理照片");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("无有效照片待处理");
            }
            log.info("找到{}张待处理照片", photos.size());

            // 压缩图片到100K目录
            log.info("开始压缩图片到100K目录");
            photoService.thumbnailPhotosFromFolderToFolder(pendingDir, thumbnail100KDir, 100, overwrite);
            log.info("完成100K压缩");

            // 压缩图片到1000K目录
            log.info("开始压缩图片到1000K目录");
            photoService.thumbnailPhotosFromFolderToFolder(pendingDir, thumbnail1000KDir, 1000, overwrite);
            log.info("完成1000K压缩");

            // 复制原图到fullSizeDir
            log.info("开始复制原图到目标目录");
            File[] imageFiles = FileUtils.getImageFiles(pendingDir);
            log.info("待复制的原图文件数量: {}", imageFiles.length);
            
            Arrays.stream(imageFiles)
                    .forEach(file -> {
                        try {
                            log.debug("正在复制文件: {}", file.getName());
                            FileUtils.copyFile(file, new File(fullSizeDir, file.getName()), overwrite);
                        } catch (IOException e) {
                            log.error("复制文件时出错: {}, 错误: {}", file.getName(), e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    });
            log.info("完成原图复制");

            // 存入数据库
            log.info("开始保存照片信息到数据库");
            boolean result = photoService.saveBatch(photos);

            if (result) {
                // 清理pending目录
                log.info("开始清理临时目录");
                FileUtils.clearFolder(pendingDir, true);
                log.info("成功处理所有照片，共处理{}张", photos.size());
                return ResponseEntity.ok("处理成功，已处理 " + photos.size() + " 张照片");
            } else {
                log.error("数据库批量保存失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("数据库保存失败");
            }
        } catch (Exception e) {
            log.error("处理待处理图片时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("处理出错: " + e.getMessage());
        }
    }

    @Transactional
    @Operation(summary = "处理前端传入的待处理图片", description = "将前端传入的图片添加到数据库，压缩到thumbnailSizeUrl，复制到fullSizeUrl")
    @PostMapping("/processPhotosFromFrontend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> processPhotosFromFrontend(
            @RequestParam(defaultValue = "false") boolean overwrite,
            @Parameter(description = "上传的图片文件数组", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    array = @ArraySchema(arraySchema = @Schema(type = "array", implementation = File.class),
                            schema = @Schema(type = "string", format = "binary")))) @RequestParam("files") MultipartFile[] files) {
        try {
            log.info("开始处理前端上传的图片文件，是否覆盖: {}", overwrite);
            
            if (files == null || files.length == 0) {
                log.warn("没有收到文件");
                return ResponseEntity.badRequest().body("没有收到文件");
            }

            // 验证文件目录
            File pendingDir = FileUtils.validateFolder(pendingUrl);
            File fullSizeDir = FileUtils.validateFolder(fullSizeUrl);
            File thumbnail100KDir = FileUtils.validateFolder(thumbnail100KUrl);
            File thumbnail1000KDir = FileUtils.validateFolder(thumbnail1000KUrl);

            if (pendingDir == null || fullSizeDir == null || thumbnail100KDir == null || thumbnail1000KDir == null) {
                log.error("目录验证失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("目录验证失败");
            }

            // 验证文件类型
            List<MultipartFile> validFiles = Arrays.stream(files)
                    .filter(photoService::multipartFileIsSupportedPhoto)
                    .collect(Collectors.toList());

            if (validFiles.isEmpty()) {
                log.warn("没有有效的图片文件");
                return ResponseEntity.badRequest().body("没有有效的图片文件");
            }

            log.info("开始处理{}个有效图片文件", validFiles.size());

            // 保存到临时目录
            for (MultipartFile file : validFiles) {
                FileUtils.saveFile(file, pendingDir);
                log.debug("已保存文件到临时目录: {}", file.getOriginalFilename());
            }

            // 处理照片元数据
            List<Photo> photos = photoService.getPhotosByMultipartFiles(validFiles.toArray(new MultipartFile[0]));

            // 单个文件处理遍历
            for (MultipartFile file : validFiles) {
                try {
                    log.debug("开始处理文件: {}", file.getOriginalFilename());
                    // 压缩到100K目录
                    photoService.thumbnailPhotoFromMultipartFileToFolder(file, pendingDir, thumbnail100KDir, 100, overwrite);
                    // 压缩到1000K目录
                    photoService.thumbnailPhotoFromMultipartFileToFolder(file, pendingDir, thumbnail1000KDir, 1000, overwrite);
                    // 复制到原图目录
                    File destFile = new File(fullSizeDir, file.getOriginalFilename());
                    if (!destFile.exists() || overwrite) {
                        FileUtils.saveFile(file, fullSizeDir);
                    }
                    log.debug("成功处理文件: {}", file.getOriginalFilename());
                } catch (IOException e) {
                    log.error("处理文件时出错: {}, 错误: {}", file.getOriginalFilename(), e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("处理文件时出错: " + e.getMessage());
                }
            }

            // 保存到数据库
            boolean result = photoService.saveBatch(photos);

            // 清理临时目录
            FileUtils.clearFolder(pendingDir, true);
            log.info("已清理临时目录");

            if (result) {
                log.info("成功处理{}张图片", photos.size());
                return ResponseEntity.ok("成功处理 " + photos.size() + " 张图片");
            } else {
                log.error("数据库保存失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("数据库保存失败");
            }
        } catch (Exception e) {
            log.error("处理图片时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("处理出错: " + e.getMessage());
        }
    }

    @PutMapping("/updatePhotoInfo")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新照片信息", description = "根据ID更新照片元数据信息")
    public ResponseEntity<String> updatePhotoInfo(@RequestBody Photo photo) {
        try {
            log.info("开始更新照片信息，ID: {}", photo.getId());
            if (photo.getId() == null) {
                log.error("更新照片信息失败：ID为空");
                return ResponseEntity.badRequest().body("ID不能为空");
            }

            boolean updated = photoService.updateById(photo);
            if (!updated) {
                log.warn("未找到ID为{}的照片记录", photo.getId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到对应照片记录");
            }

            log.info("成功更新照片信息，ID: {}", photo.getId());
            return ResponseEntity.ok("照片信息更新成功");
        } catch (Exception e) {
            log.error("更新照片信息时发生错误，ID: {}, 错误: {}", photo.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新过程中发生错误: " + e.getMessage());
        }
    }

    @DeleteMapping("/deletePhotoById")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "根据ID删除照片", description = "根据ID删除数据库记录及对应的原图和压缩图文件")
    public ResponseEntity<String> deletePhotoById(@RequestParam String id) {
        try {
            log.info("开始删除照片，ID: {}", id);
            Long photoId = Long.valueOf(id);
            Photo photo = photoService.getById(photoId);
            if (photo == null) {
                log.warn("未找到ID为{}的照片记录", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("照片不存在");
            }

            photoService.deletePhotoFiles(fullSizeUrl, thumbnail100KUrl, thumbnail1000KUrl, photo.getFileName());
            boolean result = photoService.removeById(photoId);
            
            if (result) {
                log.info("成功删除照片，ID: {}, 文件名: {}", id, photo.getFileName());
                return ResponseEntity.ok("照片删除成功");
            } else {
                log.error("删除照片记录失败，ID: {}", id);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("照片删除失败");
            }
        } catch (Exception e) {
            log.error("删除照片时发生错误，ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("删除照片出错: " + e.getMessage());
        }
    }

    @PutMapping("/updatePhotoStartStatus")
    @Operation(summary = "更新照片星标状态", description = "根据ID更新照片的星标状态")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updatePhotoStartStatus(
            @RequestBody PhotoStartStatusDTO photoStartStatusDTO) {
        try {
            log.info("开始更新照片星标状态，ID: {}, 状态: {}", photoStartStatusDTO.getId(), photoStartStatusDTO.getStart());
            String id = photoStartStatusDTO.getId();
            Integer start = photoStartStatusDTO.getStart();

            if (id == null) {
                log.error("更新星标状态失败：ID为空");
                return ResponseEntity.badRequest().body("ID不能为空");
            }
            if (start == null || (start != 0 && start != 1 && start != -1)) {
                log.error("更新星标状态失败：无效的start值: {}", start);
                return ResponseEntity.badRequest().body("start参数必须为0或1");
            }

            boolean updated = photoService.update(new LambdaUpdateWrapper<Photo>()
                    .eq(Photo::getId, Long.parseLong(id))
                    .set(Photo::getStart, start));

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

    @GetMapping("/getPhotosByIds")
    @PreAuthorize("permitAll()")
    @Operation(summary = "根据ID列表获取多个照片对象", description = "从数据库获取对应ID列表的多个照片信息")
    public ResponseEntity<List<Photo>> getPhotosByIds(@RequestParam List<String> ids) {
        try {
            log.info("开始批量获取照片对象，ID列表: {}", ids);

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
}
