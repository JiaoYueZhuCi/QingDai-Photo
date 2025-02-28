package com.qingdai.controller;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.qingdai.entities.CommonResult;
import com.qingdai.entity.Photo;
import com.qingdai.service.PhotoService;
import com.qingdai.utils.SnowflakeIdGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

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
    @Value("${qingdai.fullSizeUrl}")
    private String fullSizeUrl;
    @Value("${qingdai.thumbnailSizeUrl}")
    private String thumbnailSizeUrl;


    @GetMapping("/getAllPhotos")
    @Operation(summary = "获取全部照片信息", description = "从数据库获取所有照片的详细信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取照片列表"),
                    @ApiResponse(responseCode = "404", description = "未找到照片数据"),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误")
            })
    public CommonResult<List<Photo>> getAllPhotos() {
        try {
            // 1. 使用MyBatis Plus的list方法获取所有记录
            List<Photo> photos = photoService.list();

            // 2. 处理空数据集情况
            if (photos == null || photos.isEmpty()) {
                return new CommonResult<>(404, "照片库为空", Collections.emptyList());
            }

            // 3. 返回成功结果（自动包装为JSON格式）
            return new CommonResult<>(200, "成功获取" + photos.size() + "张照片信息", photos);

        } catch (Exception e) {
            // 4. 异常处理（建议记录日志）
            e.printStackTrace();
            return new CommonResult<>(500, "服务器繁忙，请稍后重试", null);
        }
    }

    @GetMapping("/toMysql")
    @Operation(summary = "所有图片信息自动入数据库", description = "所有图片信息自动入数据库")
    public CommonResult toMysql() {

        File folder = new File(fullSizeUrl);

        // 2. 校验文件夹有效性
        if (!folder.exists() || !folder.isDirectory()) {
            return new CommonResult(500, "照片文件夹路径无效");
        }

        // 3. 获取所有图片文件（支持jpg/jpeg/png）
        File[] imageFiles = folder.listFiles((dir, name) ->
                name.toLowerCase().matches(".*\\.(jpg|jpeg|png)$"));

        if (imageFiles == null || imageFiles.length == 0) {
            return new CommonResult(404, "未找到图片文件");
        }

        List<Photo> photos = new ArrayList<>();

//        雪花算法
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);

        // 4. 遍历处理每个图片文件
        for (File imageFile : imageFiles) {
            try {
                // 5. 读取基础图片信息
                BufferedImage image = ImageIO.read(imageFile);
                if (image == null) continue;

                // 6. 创建实体对象并填充基础字段
                Photo photo = new Photo();
                photo.setId(generator.nextId());
                photo.setThumbnail("");          // 缩略图留空
                photo.setFullSize(imageFile.getName());// 原图文件名
                photo.setAuthor("皎月祝辞");         // 默认作者
                photo.setWidth(image.getWidth());    // 图片宽度
                photo.setHeight(image.getHeight());    // 图片高度
                photo.setIntroduce("");             // 介绍留空
                photo.setStart(0);


                // 7. 读取EXIF元数据
                Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
                ExifSubIFDDirectory exif = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

                if (exif != null) {
                    // 处理拍摄时间
                    Date shootDate = exif.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                    if (shootDate != null) {
                        photo.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(shootDate));
                    }

                    // 构建拍摄数据字符串
                    List<String> exifData = new ArrayList<>();
                    addExifInfo(exif, ExifSubIFDDirectory.TAG_FNUMBER, "光圈", exifData);
                    addExifInfo(exif, ExifSubIFDDirectory.TAG_EXPOSURE_TIME, "快门", exifData);
                    addExifInfo(exif, ExifSubIFDDirectory.TAG_ISO_EQUIVALENT, "ISO", exifData);

                    if (!exifData.isEmpty()) {
                        photo.setData(String.join(" ", exifData));
                    }
                }

                photos.add(photo);
            } catch (Exception e) {
                // 8. 异常处理（建议记录日志）
                System.err.println("处理文件失败: " + imageFile.getName());
                e.printStackTrace();
            }
        }

        // 9. 批量插入数据库
        try {
            boolean result = photoService.saveBatch(photos);
            return result ?
                    new CommonResult(200, "成功插入" + photos.size() + "条记录") :
                    new CommonResult(500, "数据库操作失败");
        } catch (Exception e) {
            return new CommonResult(500, "数据库异常: " + e.getMessage());
        }
    }

    // 辅助方法：添加EXIF信息到列表
    private void addExifInfo(ExifSubIFDDirectory exif, int tagType, String prefix, List<String> list) {
        String value = exif.getDescription(tagType);
        if (value != null && !value.trim().isEmpty()) {
            list.add(prefix + ":" + value);
        }
    }

    @Operation(summary = "批量图片压缩",
            description = "将原图目录中的图片压缩到缩略图目录",
            parameters = {
                    @Parameter(name = "maxSizeKB", in = ParameterIn.QUERY,
                            description = "最大文件大小(KB)",
                            schema = @Schema(minimum = "100", maximum = "10240")),
                    @Parameter(name = "overwrite", in = ParameterIn.QUERY,
                            description = "覆盖已存在文件")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "压缩任务已启动"),
                    @ApiResponse(responseCode = "400", description = "参数校验失败"),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误")
            })
    @PostMapping("/compress")
    public ResponseEntity<String> compressImages(
            @RequestParam(defaultValue = "1024") int maxSizeKB,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        // 参数校验
        if (maxSizeKB < 100 || maxSizeKB > 10240) {
            return ResponseEntity.badRequest().body("文件大小限制需在100KB-10MB之间");
        }

        File srcDir = new File(fullSizeUrl);
        File destDir = new File(thumbnailSizeUrl);

        // 验证原图目录
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            return ResponseEntity.badRequest().body("原图目录不存在");
        }

        // 创建目标目录
        if (!destDir.exists() && !destDir.mkdirs()) {
            return ResponseEntity.internalServerError().body("无法创建目标目录");
        }

        // 并行处理图片文件
        Arrays.stream(Objects.requireNonNull(srcDir.listFiles()))
                .parallel()
                .filter(this::isSupportedImage)
                .forEach(file -> processSingleImage(file, destDir, maxSizeKB, overwrite));

        return ResponseEntity.ok("压缩任务已提交");
    }

    private boolean isSupportedImage(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg")
                || name.endsWith(".png") || name.endsWith(".webp");
    }

    private void processSingleImage(File srcFile, File destDir, int maxSizeKB, boolean overwrite) {
        try {
            File destFile = new File(destDir, srcFile.getName());

            // 跳过已存在文件
            if (destFile.exists() && !overwrite) return;

            // 动态压缩逻辑
            double quality = 0.85;
            do {
                Thumbnails.of(srcFile)
                        .scale(1)
                        .outputQuality(quality)
                        .toFile(destFile);

                quality -= 0.1;
            } while (destFile.length() > maxSizeKB * 1024L && quality > 0.2);

        } catch (IOException e) {
            throw new RuntimeException("文件处理失败: " + srcFile.getName(), e);
        }
    }

}
