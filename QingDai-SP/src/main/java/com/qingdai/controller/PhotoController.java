package com.qingdai.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
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
@RequestMapping("/qingdai/photo")
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
    @Operation(summary = "获取全部照片信息", description = "从数据库获取所有照片的详细信息")
    public ResponseEntity<List<Photo>> getAllPhotos() {
        try {
            // 1. 使用MyBatis Plus的list方法获取所有记录
            List<Photo> photos = photoService.list();

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

            return ResponseEntity.ok("压缩任务已提交");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

}


