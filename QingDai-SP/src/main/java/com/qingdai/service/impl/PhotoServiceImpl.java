package com.qingdai.service.impl;

import com.qingdai.entity.Photo;
import com.qingdai.entity.GroupPhotoPhoto;
import com.qingdai.mapper.PhotoMapper;
import com.qingdai.service.PhotoService;
import com.qingdai.service.GroupPhotoPhotoService;
import com.qingdai.service.base.BaseCachedServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import java.time.YearMonth;
import java.time.Year;
import java.time.LocalDate;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.Rational;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.qingdai.utils.DateUtils;
import com.qingdai.utils.FileUtils;
import com.qingdai.utils.SnowflakeIdGenerator;
import com.qingdai.utils.BloomFilterUtil;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;
import org.springframework.transaction.annotation.Transactional;
import com.qingdai.service.FileProcessService;
import org.springframework.context.annotation.Lazy;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-02-28
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "photo")
public class PhotoServiceImpl extends BaseCachedServiceImpl<PhotoMapper, Photo> implements PhotoService {

    @Value("${qingdai.photo.author}")
    private String author;

    @Value("${qingdai.url.fullSizeUrl}")
    private String fullSizeUrl;

    @Value("${qingdai.url.thumbnail100KUrl}")
    private String thumbnail100KUrl;

    @Value("${qingdai.url.thumbnail1000KUrl}")
    private String thumbnail1000KUrl;

    @Autowired
    private GroupPhotoPhotoService groupPhotoPhotoService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private BloomFilterUtil bloomFilterUtil;

    @Lazy
    @Autowired
    private FileProcessService fileProcessService;

    // Redis中存储上传任务状态的键前缀
    private static final String UPLOAD_STATUS_KEY_PREFIX = "photo:upload:status:";

    // 雪花算法生成器
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);

    @Override
    @Cacheable(key = "'countByMonth_' + #yearMonth")
    public long countByMonth(YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return count(new LambdaQueryWrapper<Photo>()
                .ge(Photo::getShootTime, startDate)
                .le(Photo::getShootTime, endDate));
    }

    @Override
    @Cacheable(key = "'countByYear_' + #year")
    public long countByYear(Year year) {
        LocalDate startDate = year.atDay(1);
        LocalDate endDate = year.atDay(year.length());
        return count(new LambdaQueryWrapper<Photo>()
                .ge(Photo::getShootTime, startDate)
                .le(Photo::getShootTime, endDate));
    }

    @Override
    @Cacheable(key = "'countByMonthAndStart_' + #yearMonth + '_' + #startRating")
    public long countByMonthAndStart(YearMonth yearMonth, int startRating) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return count(new LambdaQueryWrapper<Photo>()
                .ge(Photo::getShootTime, startDate)
                .le(Photo::getShootTime, endDate)
                .eq(Photo::getStartRating, startRating));
    }

    @Override
    @Cacheable(key = "'countByYearAndStart_' + #year + '_' + #startRating")
    public long countByYearAndStart(Year year, int startRating) {
        LocalDate startDate = year.atDay(1);
        LocalDate endDate = year.atDay(year.length());
        return count(new LambdaQueryWrapper<Photo>()
                .ge(Photo::getShootTime, startDate)
                .le(Photo::getShootTime, endDate)
                .eq(Photo::getStartRating, startRating));
    }

    // 处理指定文件夹中的图片文件，返回一个包含处理后照片信息的 Photo 对象列表
    @Override
    @Cacheable(key = "'photosByFolder_' + #folder.getName()")
    public List<Photo> getPhotosByFolder(File folder) {
        return Arrays.stream(FileUtils.getImageFiles(folder)) // 返回 Stream<File>
                .parallel() // 并行处理
                .map(this::getPhotoObjectByFile)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 根据照片ID获取文件名
    @Override
    @Cacheable(key = "'fileName_' + #photoId", unless = "#result == null")
    public String getFileNameById(String photoId) {
        // 先检查布隆过滤器，如果布隆过滤器显示元素不存在，则直接返回null
        // 这样可以避免对不存在的ID进行数据库查询，防止缓存穿透
        // if (!bloomFilterUtil.exists(photoId)) {
        // log.debug("照片ID在布隆过滤器中不存在: {}", photoId);
        // return null;
        // }

        Photo photo = getById(photoId);
        if (photo == null) {
            log.debug("照片ID在数据库中不存在: {}", photoId);
            return null;
        }
        return photo.getFileName();
    }

    // 处理上传的文件并返回Photo对象列表
    @Override
    public List<Photo> getPhotosByMultipartFiles(MultipartFile[] files) {
        return Arrays.stream(files)
                .parallel()
                .map(this::getPhotoObjectByMultipartFile)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 压缩文件夹内所有图片到指定目录
    @Override
    public void thumbnailPhotosFromFolderToFolder(File srcDir, File thumbnailDir, int maxSizeKB, boolean overwrite) {
        fileProcessService.thumbnailPhotosFromFolderToFolder(srcDir, thumbnailDir, maxSizeKB, overwrite);
    }

    // 判断是否为支持的图片格式
    @Override
    public boolean multipartFileIsSupportedPhoto(MultipartFile file) {
        return fileProcessService.multipartFileIsSupportedPhoto(file);
    }

    // 判断文件是否为支持的图片格式
    @Override
    public boolean fileIsSupportedPhoto(File file) {
        return fileProcessService.fileIsSupportedPhoto(file);
    }

    /*
     * 获取文件名、格式和基础名称。
     * 将PNG格式转换为JPEG格式。
     * 检查目标文件是否存在且是否允许覆盖。
     * 计算最大字节数。
     * 优先调整图片质量以满足大小要求。
     * 如果调整质量不达标，则尝试缩小图片尺寸。
     * 如果仍不达标，抛出异常。
     */
    private void compressPhoto(File fullSizeUrl, File thumbnailDir, int maxSizeKB, boolean overwrite) {
        try {
            log.info("开始处理图片: {}", fullSizeUrl.getAbsolutePath());
            String fileName = fullSizeUrl.getName();
            String formatName = FileUtils.getFileExtension(fileName).toLowerCase();
            String baseName = FileUtils.getBaseName(fileName);

            // 处理PNG转换为JPEG
            if ("png".equals(formatName)) {
                fileName = baseName + ".jpg"; // 修改后缀为jpg
                formatName = "jpg";
            }

            File thumbnailUrl = new File(thumbnailDir, fileName);
            log.info("目标文件路径: {}", thumbnailUrl.getAbsolutePath());

            if (thumbnailUrl.exists() && !overwrite) {
                log.info("文件已存在且不允许覆盖，跳过处理: {}", fileName);
                return;
            }

            long maxSizeBytes = maxSizeKB * 1024L;
            boolean sizeMet = false;

            // 优先调整质量参数
            log.info("开始调整图片质量，目标大小: {}KB", maxSizeKB);
            sizeMet = adjustPhotoQuality(fullSizeUrl, thumbnailUrl, formatName, maxSizeBytes);

            // 若未达标，调整尺寸
            if (!sizeMet) {
                log.info("质量调整未达标，开始调整图片尺寸");
                sizeMet = scaleDownPhoto(fullSizeUrl, thumbnailUrl, formatName, maxSizeBytes);
            }

            if (!sizeMet) {
                String errorMsg = String.format("无法压缩到指定大小: %s (当前大小: %dKB, 目标大小: %dKB)",
                        fullSizeUrl.getName(),
                        fullSizeUrl.length() / 1024,
                        maxSizeKB);
                log.error(errorMsg);
                throw new IOException(errorMsg);
            }
            log.info("图片{}压缩成功", fullSizeUrl.getName());
        } catch (IOException e) {
            String errorMsg = String.format("文件处理失败: %s, 原因: %s", fullSizeUrl.getName(), e.getMessage());
            log.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    @Override
    public Photo getPhotoObjectByMultipartFile(MultipartFile file) {
        File tempFile = null;
        try {
            String fileName = file.getOriginalFilename();

            // 创建临时目录和文件
            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            tempFile = new File(tempDir, fileName);

            // 确保目录存在并保存文件
            Files.createDirectories(tempFile.getParentFile().toPath());
            file.transferTo(tempFile); // 添加文件传输操作

            Photo photo = getPhotoObjectByFile(tempFile);
            return photo;
        } catch (Exception e) {
            System.out.println("处理文件失败: " + (tempFile != null ? tempFile.getAbsolutePath() : "unknown") + "\n" + e);
            return null;
        } finally {
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.delete(tempFile.toPath()); // 使用NIO方式删除更可靠
                } catch (IOException e) {
                    System.out.println("删除临时文件失败: " + tempFile.getAbsolutePath());
                }
            }
        }
    }

    // 生成包含元数据的Photo对象
    private Photo getPhotoObjectByFile(File PhotoFile) {
        try {
            BufferedImage image = ImageIO.read(PhotoFile);
            if (image == null)
                return null;

            // Id 原图 作者 宽度 高度 压缩图 简介 精选
            Photo photo = buildBasicPhoto(PhotoFile, image);
            // 拍摄时间 拍摄参数
            processExifData(PhotoFile, photo);

            return photo;
        } catch (IOException | ImageProcessingException e) {
            System.out.println("处理文件失败: " + PhotoFile.getName() + e);
            return null;
        }
    }

    // 循环逐步降低图片质量，直到满足文件大小要求 如果质量低于最小值仍未满足要求，则返回false
    private boolean adjustPhotoQuality(File fullSizeUrl, File thumbnailUrl, String formatName, long maxSizeBytes)
            throws IOException {
        double quality = 0.85;
        double minQuality = 0.2;
        double step = 0.05;

        while (quality >= minQuality) {
            log.debug("尝试压缩质量: {}, 文件: {}", quality, fullSizeUrl.getName());
            try {
                Thumbnails.of(fullSizeUrl)
                        .scale(1)
                        .outputFormat(formatName)
                        .outputQuality(quality)
                        // 处理PNG透明背景
                        .imageType(BufferedImage.TYPE_INT_RGB)
                        .toFile(thumbnailUrl);

                long currentSize = thumbnailUrl.length();
                log.debug("压缩后大小: {}KB, 目标大小: {}KB", currentSize / 1024, maxSizeBytes / 1024);

                if (currentSize <= maxSizeBytes) {
                    log.info("质量调整成功，最终质量: {}, 文件大小: {}KB", quality, currentSize / 1024);
                    return true;
                }
            } catch (Exception e) {
                log.error("调整质量时发生错误，质量: {}, 文件: {}, 错误: {}", quality, fullSizeUrl.getName(), e.getMessage());
                throw e;
            }
            quality = BigDecimal.valueOf(quality).subtract(BigDecimal.valueOf(step)).doubleValue();
        }
        log.warn("质量调整未达到目标大小，最低质量: {}, 文件: {}", minQuality, fullSizeUrl.getName());
        return false;
    }

    // 图片按比例缩小
    private boolean scaleDownPhoto(File fullSizeUrl, File thumbnailUrl, String formatName, long maxSizeBytes)
            throws IOException {
        double scale = 1.0;
        double step = 0.1;

        while (scale >= 0.1) {
            log.debug("尝试缩放比例: {}, 文件: {}", scale, fullSizeUrl.getName());
            try {
                Thumbnails.of(fullSizeUrl)
                        .scale(scale)
                        .outputFormat(formatName)
                        .outputQuality(0.7) // 适当质量值
                        .imageType(BufferedImage.TYPE_INT_RGB)
                        .toFile(thumbnailUrl);

                long currentSize = thumbnailUrl.length();
                log.debug("缩放后大小: {}KB, 目标大小: {}KB", currentSize / 1024, maxSizeBytes / 1024);

                if (currentSize <= maxSizeBytes) {
                    log.info("缩放成功，最终比例: {}, 文件大小: {}KB", scale, currentSize / 1024);
                    return true;
                }
            } catch (Exception e) {
                log.error("缩放图片时发生错误，比例: {}, 文件: {}, 错误: {}", scale, fullSizeUrl.getName(), e.getMessage());
                throw e;
            }
            scale -= step;
        }
        log.warn("缩放未达到目标大小，最小比例: {}, 文件: {}", 0.1, fullSizeUrl.getName());
        return false;
    }

    // 生成Photo对象
    private Photo buildBasicPhoto(File file, BufferedImage Photo) {
        Photo photo = new Photo();
        photo.setId(idGenerator.nextId());
        photo.setFileName(file.getName());
        photo.setAuthor(author);
        photo.setWidth(Photo.getWidth());
        photo.setHeight(Photo.getHeight());
        photo.setTitle(null);
        photo.setIntroduce(null);
        photo.setStartRating(0);
        return photo;
    }

    // 处理EXIF数据
    private void processExifData(File PhotoFile, Photo photo) throws ImageProcessingException, IOException {
        Metadata metadata = ImageMetadataReader.readMetadata(PhotoFile);
        ExifSubIFDDirectory exif = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

        if (exif == null)
            return;

        processShootingTime(exif, photo);
        processCameraSettings(exif, photo);
        processCameraModel(metadata, photo);
        processFocalLength(exif, photo);
    }

    // 格式化拍摄时间
    private void processShootingTime(ExifSubIFDDirectory exif, Photo photo) {
        Date photoDate = exif.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        // 使 photoDate 向前八个小时 矫正时区错误
        if (photoDate != null) {
            photoDate = new Date(photoDate.getTime() - 8 * 60 * 60 * 1000);
            photo.setShootTime(DateUtils.formatDateTime(photoDate));
            return;
        }

        // 如果无法从EXIF获取时间，尝试从文件名解析
        String fileName = photo.getFileName();
        if (fileName != null) {
            // 尝试匹配格式：20250318-095601-DSC_2046.jpg
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^(\\d{8})-(\\d{6})");
            java.util.regex.Matcher matcher = pattern.matcher(fileName);
            if (matcher.find()) {
                String dateStr = matcher.group(1);
                String timeStr = matcher.group(2);
                try {
                    // 解析日期和时间
                    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
                    Date parsedDate = dateFormat.parse(dateStr + timeStr);
                    photo.setShootTime(DateUtils.formatDateTime(parsedDate));
                    return;
                } catch (java.text.ParseException e) {
                    // 解析失败，继续尝试其他格式
                }
            }

            // 尝试匹配格式：20240505-DSC_4929.jpg
            pattern = java.util.regex.Pattern.compile("^(\\d{8})");
            matcher = pattern.matcher(fileName);
            if (matcher.find()) {
                String dateStr = matcher.group(1);
                try {
                    // 解析日期，时间设为00:00:00
                    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyyMMdd");
                    Date parsedDate = dateFormat.parse(dateStr);
                    photo.setShootTime(DateUtils.formatDateTime(parsedDate));
                    return;
                } catch (java.text.ParseException e) {
                    // 解析失败，不设置时间
                }
            }
        }
    }

    // 存储相机拍摄参数
    private void processCameraSettings(ExifSubIFDDirectory exif, Photo photo) {
        // 处理光圈 FNumber
        Rational fNumberRational = exif.getRational(ExifSubIFDDirectory.TAG_FNUMBER);
        if (fNumberRational != null) {
            double fNumber = fNumberRational.doubleValue();
            photo.setAperture(String.format("%.1f", fNumber));
        }

        // 处理快门速度 ExposureTime
        Rational exposureTimeRational = exif.getRational(ExifSubIFDDirectory.TAG_EXPOSURE_TIME);
        if (exposureTimeRational != null) {
            long numerator = exposureTimeRational.getNumerator();
            long denominator = exposureTimeRational.getDenominator();
            String shutter;
            if (denominator == 1) {
                shutter = String.valueOf(numerator); // 整秒，如 2"
            } else if (numerator == 1) {
                shutter = "1/" + denominator; // 分数形式，如 1/500
            } else {
                shutter = String.format("%.1f", exposureTimeRational.doubleValue()); // 其他情况，如 0.3"
            }
            photo.setShutter(shutter);
        }

        // 处理ISO
        Integer isoValue = exif.getInteger(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT);
        if (isoValue != null) {
            photo.setIso(isoValue.toString());
        }

        // 处理镜头
        String lensModel = exif.getString(ExifSubIFDDirectory.TAG_LENS_MODEL);
        if (lensModel != null) {
            photo.setLens(lensModel);
        }
    }

    // 处理相机型号 需要getFirstDirectoryOfType(ExifIFD0Directory.class);
    private void processCameraModel(Metadata metadata, Photo photo) {
        // 从 Metadata 中获取 ExifIFD0 目录（存储相机制造商和型号）
        ExifIFD0Directory exifIFD0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

        String model = null;

        if (exifIFD0 != null) {
            model = exifIFD0.getString(ExifIFD0Directory.TAG_MODEL);
        }

        // 如果未找到，尝试从 ExifSubIFDDirectory 获取（部分相机可能在此存储）
        if (model == null) {
            ExifSubIFDDirectory exifSubIFD = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exifSubIFD != null) {
                model = exifSubIFD.getString(ExifSubIFDDirectory.TAG_MODEL);
            }
        }

        photo.setCamera(model);
    }

    // 处理焦距数据
    private void processFocalLength(ExifSubIFDDirectory exif, Photo photo) {
        // 处理焦距 FocalLength
        Rational focalLengthRational = exif.getRational(ExifSubIFDDirectory.TAG_FOCAL_LENGTH);
        if (focalLengthRational != null) {
            double focalLength = focalLengthRational.doubleValue();
            photo.setFocalLength(String.format("%.0f", focalLength));
        }
    }

    @Override
    public void deletePhotoFiles(String fullSizeUrl, String thumbnail100KUrl, String thumbnail1000KUrl, String fileName)
            throws IOException {
        fileProcessService.deletePhotoFiles(fullSizeUrl, thumbnail100KUrl, thumbnail1000KUrl, fileName);
    }

    @Override
    public void processPhotoCompression(File tempDir, File thumbnail100KDir, File thumbnail1000KDir, File fullSizeDir,
            boolean overwrite) throws IOException {
        fileProcessService.processPhotoCompression(tempDir, thumbnail100KDir, thumbnail1000KDir, fullSizeDir,
                overwrite);
    }

    /**
     * 回滚已处理的文件（删除已复制的文件）
     * 
     * @param processedFiles 需要回滚的文件列表
     */
    private void rollbackProcessedFiles(List<File> processedFiles) {
        log.info("开始回滚已处理的文件，共{}个文件", processedFiles.size());
        for (File file : processedFiles) {
            try {
                if (file.exists() && file.isFile()) {
                    boolean deleted = file.delete();
                    if (deleted) {
                        log.debug("成功删除文件: {}", file.getAbsolutePath());
                    } else {
                        log.warn("无法删除文件: {}", file.getAbsolutePath());
                    }
                }
            } catch (Exception e) {
                log.error("删除文件时发生错误: {}, 错误: {}", file.getAbsolutePath(), e.getMessage());
            }
        }
        log.info("文件回滚完成");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessResult processPhotosFromFrontend(MultipartFile[] files, Integer startRating, boolean overwrite) {
        List<Photo> existingPhotos = new ArrayList<>();
        List<Photo> newPhotos = new ArrayList<>();
        List<File> processedFiles = new ArrayList<>(); // 用于记录已处理的文件，便于回滚

        try {
            if (files == null || files.length == 0) {
                log.warn("没有收到文件");
                return new ProcessResult(Collections.emptyList(), Collections.emptyList(), false);
            }

            // 验证文件类型
            List<MultipartFile> validFiles = Arrays.stream(files)
                    .filter(this::multipartFileIsSupportedPhoto)
                    .collect(Collectors.toList());

            if (validFiles.isEmpty()) {
                log.warn("没有有效的照片文件");
                return new ProcessResult(Collections.emptyList(), Collections.emptyList(), false);
            }

            // 遍历并处理每个文件
            for (MultipartFile file : validFiles) {
                // 获取照片对象
                Photo photo = getPhotoObjectByMultipartFile(file);
                if (photo == null) {
                    log.warn("无法从文件中获取照片信息: {}", file.getOriginalFilename());
                    continue;
                }

                // 设置初始星级
                photo.setStartRating(startRating);

                // 检查是否存在同名文件
                Photo existingPhoto = getOne(
                        new LambdaQueryWrapper<Photo>()
                                .eq(Photo::getFileName, photo.getFileName()));

                if (existingPhoto != null) {
                    // 已经存在同名文件
                    if (!overwrite) {
                        log.warn("照片已存在且不覆盖: {}", photo.getFileName());
                        continue;
                    }

                    log.info("覆盖已存在的照片: {}", photo.getFileName());
                    // 保留ID和创建时间
                    photo.setId(existingPhoto.getId());
                    photo.setCreatedTime(existingPhoto.getCreatedTime());

                    // 保留UI相关字段
                    photo.setFileName(existingPhoto.getFileName());
                    photo.setAuthor(existingPhoto.getAuthor());
                    photo.setIntroduce(existingPhoto.getIntroduce());
                    photo.setStartRating(existingPhoto.getStartRating());

                    // 删除原有文件
                    try {
                        deletePhotoFiles(fullSizeUrl, thumbnail100KUrl, thumbnail1000KUrl, photo.getFileName());
                        existingPhotos.add(photo);
                    } catch (IOException e) {
                        log.error("删除原有文件失败: {}, 错误: {}", photo.getFileName(), e.getMessage(), e);
                        throw new RuntimeException("删除原有文件失败: " + e.getMessage(), e);
                    }
                } else {
                    // 新照片，生成ID
                    photo.setId(String.valueOf(idGenerator.nextId()));
                    newPhotos.add(photo);
                }

                // 将文件保存到目标目录
                try {
                    String fileName = file.getOriginalFilename();
                    // 保存原图
                    File destFullSize = new File(fullSizeUrl, fileName);
                    file.transferTo(destFullSize);
                    processedFiles.add(destFullSize);

                    // 压缩到1000K目录
                    File destThumbnail1000K = new File(thumbnail1000KUrl, fileName);
                    Thumbnails.of(destFullSize)
                            .scale(1.0)
                            .outputQuality(0.8)
                            .toFile(destThumbnail1000K);
                    processedFiles.add(destThumbnail1000K);

                    // 压缩到100K目录
                    File destThumbnail100K = new File(thumbnail100KUrl, fileName);
                    Thumbnails.of(destThumbnail1000K)
                            .scale(1.0)
                            .outputQuality(0.5)
                            .toFile(destThumbnail100K);
                    processedFiles.add(destThumbnail100K);
                } catch (IOException e) {
                    log.error("保存文件失败: {}, 错误: {}", file.getOriginalFilename(), e.getMessage(), e);
                    fileProcessService.rollbackProcessedFiles(processedFiles);
                    throw new RuntimeException("保存文件失败: " + e.getMessage(), e);
                }
            }

            // 保存到数据库
            if (!existingPhotos.isEmpty()) {
                updateBatchById(existingPhotos);
            }

            if (!newPhotos.isEmpty()) {
                saveBatch(newPhotos);
            }

            return new ProcessResult(existingPhotos, newPhotos, true);
        } catch (Exception e) {
            log.error("处理照片时发生错误: {}", e.getMessage(), e);
            // 回滚文件操作
            fileProcessService.rollbackProcessedFiles(processedFiles);
            // 事务会自动回滚数据库操作
            throw new RuntimeException("处理照片失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessResult processPhotoFromMQ(String[] fileNames, String tempDirPath, Integer startRating,
            boolean overwrite) {
        List<Photo> existingPhotos = new ArrayList<>();
        List<Photo> newPhotos = new ArrayList<>();

        try {
            if (fileNames == null || fileNames.length == 0) {
                log.warn("没有文件名");
                return new ProcessResult(Collections.emptyList(), Collections.emptyList(), false);
            }

            File tempDir = new File(tempDirPath);
            if (!tempDir.exists() || !tempDir.isDirectory()) {
                log.warn("临时目录不存在或不是目录: {}", tempDirPath);
                return new ProcessResult(Collections.emptyList(), Collections.emptyList(), false);
            }

            // 处理照片元数据
            List<Photo> photos = new ArrayList<>();
            for (String fileName : fileNames) {
                File photoFile = new File(tempDir, fileName);
                if (photoFile.exists() && fileIsSupportedPhoto(photoFile)) {
                    Photo photo = getPhotoObjectByFile(photoFile);
                    if (photo != null) {
                        photos.add(photo);
                    }
                }
            }

            if (photos.isEmpty()) {
                log.warn("没有有效的图片");
                return new ProcessResult(Collections.emptyList(), Collections.emptyList(), false);
            }

            // 检查重复文件名

            for (Photo photo : photos) {
                Photo existingPhoto = getOne(
                        new LambdaQueryWrapper<Photo>()
                                .eq(Photo::getFileName, photo.getFileName()));

                if (existingPhoto != null) {
                    // 已经存在同名文件
                    if (!overwrite) {
                        log.warn("照片已存在且不覆盖: {}", photo.getFileName());
                        continue;
                    }

                    log.info("覆盖已存在的照片: {}", photo.getFileName());
                    // 保留ID和创建时间
                    photo.setId(existingPhoto.getId());
                    photo.setCreatedTime(existingPhoto.getCreatedTime());
                    existingPhotos.add(photo);
                } else {
                    // 新照片，生成ID
                    photo.setId(String.valueOf(idGenerator.nextId()));
                    photo.setStartRating(startRating);
                    newPhotos.add(photo);
                }
            }

            // 保存到数据库
            if (!existingPhotos.isEmpty()) {
                updateBatchById(existingPhotos);
            }

            if (!newPhotos.isEmpty()) {
                saveBatch(newPhotos);
            }

            return new ProcessResult(existingPhotos, newPhotos, true);
        } catch (Exception e) {
            log.error("处理照片元数据时发生错误: {}", e.getMessage(), e);
            throw new RuntimeException("处理照片元数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> validatePhotoExistence() {
        log.info("开始验证数据库照片在文件系统中的存在性");
        List<Photo> allPhotos = list();

        Map<String, Object> result = new HashMap<>();
        List<String> missingFullSizeFiles = new ArrayList<>();
        List<String> missingThumbnail100KFiles = new ArrayList<>();
        List<String> missingThumbnail1000KFiles = new ArrayList<>();
        List<String> missingAllFiles = new ArrayList<>();
        List<String> missingDetails = new ArrayList<>();

        if (allPhotos.isEmpty()) {
            log.warn("数据库中没有照片记录");
            result.put("message", "数据库中没有照片记录");
            return result;
        }

        int totalCount = allPhotos.size();

        for (Photo photo : allPhotos) {
            String fileName = photo.getFileName();
            boolean fullSizeMissing = !new File(fullSizeUrl, fileName).exists();
            boolean thumbnail100KMissing = !new File(thumbnail100KUrl, fileName).exists();
            boolean thumbnail1000KMissing = !new File(thumbnail1000KUrl, fileName).exists();

            if (fullSizeMissing) {
                missingFullSizeFiles.add(fileName);
            }

            if (thumbnail100KMissing) {
                missingThumbnail100KFiles.add(fileName);
            }

            if (thumbnail1000KMissing) {
                missingThumbnail1000KFiles.add(fileName);
            }

            if (fullSizeMissing && thumbnail100KMissing && thumbnail1000KMissing) {
                missingAllFiles.add(fileName);
                missingDetails.add(String.format("ID:%s,文件名:%s,原图缺失:%s,100K缺失:%s,1000K缺失:%s",
                        photo.getId(), fileName, 
                        fullSizeMissing ? "true" : "false",
                        thumbnail100KMissing ? "true" : "false",
                        thumbnail1000KMissing ? "true" : "false"));
            }
        }

        int missingFullSize = missingFullSizeFiles.size();
        int missingThumbnail100K = missingThumbnail100KFiles.size();
        int missingThumbnail1000K = missingThumbnail1000KFiles.size();
        int missingAllCount = missingAllFiles.size();

        log.info("数据库中共有{}张照片，其中{}张丢失原图，{}张丢失100K缩略图，{}张丢失1000K缩略图，{}张三种图片都丢失",
                totalCount, missingFullSize, missingThumbnail100K, missingThumbnail1000K,
                missingAllCount);

        // 使用与前端匹配的键名
        result.put("totalCount", totalCount);
        result.put("missingFullSize", missingFullSize);
        result.put("missing100K", missingThumbnail100K);
        result.put("missing1000K", missingThumbnail1000K);
        result.put("missingAll", missingAllCount);
        result.put("missingFullSizeFiles", missingFullSizeFiles);
        result.put("missing100KFiles", missingThumbnail100KFiles);
        result.put("missing1000KFiles", missingThumbnail1000KFiles);
        result.put("missingAllFiles", missingAllFiles);
        result.put("missingDetails", missingDetails);
        result.put("message", String.format("数据库中共有%d张照片，其中%d张丢失原图，%d张丢失100K缩略图，%d张丢失1000K缩略图，%d张三种图片都丢失",
                totalCount, missingFullSize, missingThumbnail100K, missingThumbnail1000K,
                missingAllCount));

        return result;
    }

    @Override
    public Map<String, Object> validateFileSystemPhotos(String fullSizeUrl, String thumbnail100KUrl,
            String thumbnail1000KUrl) {
        return fileProcessService.validateFileSystemPhotos(fullSizeUrl, thumbnail100KUrl, thumbnail1000KUrl);
    }

    @Override
    public Map<String, Object> deletePhotosNotInDatabase(String fullSizeUrl, String thumbnail100KUrl,
            String thumbnail1000KUrl) {
        return fileProcessService.deletePhotosNotInDatabase(fullSizeUrl, thumbnail100KUrl, thumbnail1000KUrl);
    }

    @Override
    public Map<String, Object> deleteMissingPhotoRecords() {
        log.info("开始删除丢失了全部三种图片的数据库记录");
        Map<String, Object> result = new HashMap<>();

        // 先验证数据库照片在文件系统中的存在性
        Map<String, Object> validateResult = validatePhotoExistence();

        @SuppressWarnings("unchecked")
        List<String> missingDetails = (List<String>) validateResult.get("missingDetails");
        
        if (missingDetails == null || missingDetails.isEmpty()) {
            log.info("没有需要删除的记录");
            result.put("message", "没有需要删除的记录");
            result.put("totalDeleted", 0);
            result.put("deletedRecords", Collections.emptyList());
            result.put("errorRecords", Collections.emptyList());
            return result;
        }

        List<String> deletedRecords = new ArrayList<>();
        List<String> errorRecords = new ArrayList<>();
        int totalDeleted = 0;

        // 遍历所有缺失记录
        for (String detail : missingDetails) {
            // 解析缺失记录详情
            // 格式是: "ID:123,文件名:xxx.jpg,原图缺失:true,100K缺失:true,1000K缺失:true"
            try {
                String idStr = detail.substring(detail.indexOf("ID:") + 3, detail.indexOf(","));
                boolean fullSizeMissing = detail.contains("原图缺失:true");
                boolean thumbnail100KMissing = detail.contains("100K缺失:true");
                boolean thumbnail1000KMissing = detail.contains("1000K缺失:true");

                // 只有当三种图片都缺失时才删除记录
                if (fullSizeMissing && thumbnail100KMissing && thumbnail1000KMissing) {
                    if (removeById(idStr)) {
                        deletedRecords.add(detail);
                        totalDeleted++;
                        log.info("已删除缺失全部三种图片的数据库记录，ID: {}", idStr);
                    } else {
                        errorRecords.add(detail + " - 删除失败");
                        log.error("删除数据库记录失败，ID: {}", idStr);
                    }
                }
            } catch (Exception e) {
                errorRecords.add(detail + " - 异常: " + e.getMessage());
                log.error("处理缺失记录时发生异常: {}, 详情: {}", e.getMessage(), detail, e);
            }
        }

        result.put("totalDeleted", totalDeleted);
        result.put("deletedRecords", deletedRecords);
        result.put("errorRecords", errorRecords);

        log.info("删除操作完成，总共删除{}条记录", totalDeleted);

        return result;
    }

    @Override
    @Cacheable(key = "'dashboardStats'", unless = "#result == null")
    public Map<String, Object> getPhotoDashboardStats() {
        log.warn("此次照片数据分析查询未使用缓存");
        Map<String, Object> stats = new HashMap<>();

        stats.put("typeStats", getPhotoTypeCounts());
        stats.put("changeStats", getPhotoChangeStats());
        stats.put("subjectStats", getPhotoSubjectStats());
        stats.put("cameraStats", getCameraStats());
        stats.put("lensStats", getLensStats());
        stats.put("isoStats", getIsoStats());
        stats.put("shutterStats", getShutterStats());
        stats.put("apertureStats", getApertureStats());
        stats.put("focalLengthStats", getFocalLengthStats());
        stats.put("monthStats", getMonthStats());
        stats.put("yearStats", getYearStats());

        return stats;
    }

    @Override
    @Cacheable(key = "'photoTypeCounts'")
    public Map<String, Object> getPhotoTypeCounts() {
        Map<String, Object> photoTypeCounts = new HashMap<>();
        photoTypeCounts.put("starred", count(new LambdaQueryWrapper<Photo>().eq(Photo::getStartRating, 1)));
        photoTypeCounts.put("normal", count(new LambdaQueryWrapper<Photo>().eq(Photo::getStartRating, 0)));
        photoTypeCounts.put("meteorology", count(new LambdaQueryWrapper<Photo>().eq(Photo::getStartRating, 2)));
        photoTypeCounts.put("hidden", count(new LambdaQueryWrapper<Photo>().eq(Photo::getStartRating, -1)));
        return photoTypeCounts;
    }

    @Override
    @Cacheable(key = "'photoChangeStats'")
    public Map<String, Object> getPhotoChangeStats() {
        Map<String, Object> changeStats = new HashMap<>();

        // 普通照片变化
        changeStats.put("monthlyChange", countByMonth(YearMonth.now())
                - countByMonth(YearMonth.now().minusMonths(1)));
        changeStats.put("yearlyChange",
                countByYear(Year.now()) - countByYear(Year.now().minusYears(1)));

        // 精选照片变化
        changeStats.put("monthlyStarredChange", countByMonthAndStart(YearMonth.now(), 1)
                - countByMonthAndStart(YearMonth.now().minusMonths(1), 1));
        changeStats.put("yearlyStarredChange", countByYearAndStart(Year.now(), 1)
                - countByYearAndStart(Year.now().minusYears(1), 1));

        // 气象照片变化
        changeStats.put("monthlyMeteorologyChange", countByMonthAndStart(YearMonth.now(), 2)
                - countByMonthAndStart(YearMonth.now().minusMonths(1), 2));
        changeStats.put("yearlyMeteorologyChange", countByYearAndStart(Year.now(), 2)
                - countByYearAndStart(Year.now().minusYears(1), 2));

        return changeStats;
    }

    @Override
    @Cacheable(key = "'photoSubjectStats'")
    public Map<String, Object> getPhotoSubjectStats() {
        Map<String, Object> subjectCounts = new HashMap<>();
        // 查询ID为1的组（朝霞）的照片数量
        subjectCounts.put("morningGlow",
                groupPhotoPhotoService.count(new LambdaQueryWrapper<GroupPhotoPhoto>()
                        .eq(GroupPhotoPhoto::getGroupPhotoId, "1")));

        // 查询ID为2的组（晚霞）的照片数量
        subjectCounts.put("eveningGlow",
                groupPhotoPhotoService.count(new LambdaQueryWrapper<GroupPhotoPhoto>()
                        .eq(GroupPhotoPhoto::getGroupPhotoId, "2")));

        // 查询ID为3的组（日出）的照片数量
        subjectCounts.put("sunrise",
                groupPhotoPhotoService.count(new LambdaQueryWrapper<GroupPhotoPhoto>()
                        .eq(GroupPhotoPhoto::getGroupPhotoId, "3")));

        // 查询ID为4的组（日落）的照片数量
        subjectCounts.put("sunset",
                groupPhotoPhotoService.count(new LambdaQueryWrapper<GroupPhotoPhoto>()
                        .eq(GroupPhotoPhoto::getGroupPhotoId, "4")));

        return subjectCounts;
    }

    @Override
    @Cacheable(key = "'cameraStats'")
    public List<Map<String, Object>> getCameraStats() {
        final List<Map<String, Object>> cameraCounts = new ArrayList<>();

        Map<String, Long> cameraCountMap = list(new LambdaQueryWrapper<Photo>()
                .isNotNull(Photo::getCamera))
                .stream()
                .map(Photo::getCamera)
                .filter(camera -> camera != null && !camera.isEmpty())
                .collect(Collectors.groupingBy(
                        camera -> camera,
                        Collectors.counting()));

        cameraCountMap.forEach((cameraName, count) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", cameraName);
            map.put("value", count);
            cameraCounts.add(map);
        });

        return cameraCounts;
    }

    @Override
    @Cacheable(key = "'lensStats'")
    public List<Map<String, Object>> getLensStats() {
        final List<Map<String, Object>> lensCounts = new ArrayList<>();

        Map<String, Long> lensCountMap = list(new LambdaQueryWrapper<Photo>()
                .isNotNull(Photo::getLens))
                .stream()
                .map(Photo::getLens)
                .filter(lens -> lens != null && !lens.isEmpty())
                .collect(Collectors.groupingBy(
                        lens -> lens,
                        Collectors.counting()));

        lensCountMap.forEach((lensName, count) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", lensName);
            map.put("value", count);
            lensCounts.add(map);
        });

        return lensCounts;
    }

    @Override
    @Cacheable(key = "'isoStats'")
    public List<Map<String, Object>> getIsoStats() {
        final List<Map<String, Object>> isoCounts = new ArrayList<>();

        Map<String, Long> isoCountMap = list(new LambdaQueryWrapper<Photo>()
                .isNotNull(Photo::getIso))
                .stream()
                .map(Photo::getIso)
                .filter(iso -> iso != null && !iso.isEmpty())
                .collect(Collectors.groupingBy(
                        iso -> iso,
                        Collectors.counting()));

        isoCountMap.forEach((isoName, count) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", isoName);
            map.put("value", count);
            isoCounts.add(map);
        });

        return isoCounts;
    }

    @Override
    @Cacheable(key = "'shutterStats'")
    public List<Map<String, Object>> getShutterStats() {
        final List<Map<String, Object>> shutterCounts = new ArrayList<>();

        Map<String, Long> shutterCountMap = list(new LambdaQueryWrapper<Photo>()
                .isNotNull(Photo::getShutter))
                .stream()
                .map(Photo::getShutter)
                .filter(shutter -> shutter != null && !shutter.isEmpty())
                .collect(Collectors.groupingBy(
                        shutter -> shutter,
                        Collectors.counting()));

        shutterCountMap.forEach((shutterName, count) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", shutterName);
            map.put("value", count);
            shutterCounts.add(map);
        });

        return shutterCounts;
    }

    @Override
    @Cacheable(key = "'apertureStats'")
    public List<Map<String, Object>> getApertureStats() {
        final List<Map<String, Object>> apertureCounts = new ArrayList<>();

        Map<String, Long> apertureCountMap = list(new LambdaQueryWrapper<Photo>()
                .isNotNull(Photo::getAperture))
                .stream()
                .map(Photo::getAperture)
                .filter(aperture -> aperture != null && !aperture.isEmpty())
                .collect(Collectors.groupingBy(
                        aperture -> aperture,
                        Collectors.counting()));

        apertureCountMap.forEach((apertureName, count) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", apertureName);
            map.put("value", count);
            apertureCounts.add(map);
        });

        return apertureCounts;
    }

    @Override
    @Cacheable(key = "'focalLengthStats'")
    public List<Map<String, Object>> getFocalLengthStats() {
        List<Map<String, Object>> stats = new ArrayList<>();
        try {
            // 获取所有不重复的焦距值
            List<String> focalLengths = baseMapper.selectList(
                    new LambdaQueryWrapper<Photo>()
                            .select(Photo::getFocalLength)
                            .isNotNull(Photo::getFocalLength)
                            .ne(Photo::getFocalLength, ""))
                    .stream()
                    .map(Photo::getFocalLength)
                    .distinct()
                    .collect(Collectors.toList());

            // 统计每个焦距的使用次数
            for (String focalLength : focalLengths) {
                long count = baseMapper.selectCount(
                        new LambdaQueryWrapper<Photo>()
                                .eq(Photo::getFocalLength, focalLength));

                Map<String, Object> stat = new HashMap<>();
                stat.put("name", focalLength);
                stat.put("value", count);
                stats.add(stat);
            }
        } catch (Exception e) {
            log.error("获取焦距统计数据时发生错误: {}", e.getMessage(), e);
        }
        return stats;
    }

    @Override
    @Cacheable(key = "'monthStats'")
    public Map<String, Long> getMonthStats() {
        Map<String, Long> monthStats = new HashMap<>();

        try {
            for (int i = 1; i <= 12; i++) {
                String monthStr = String.format("%02d", i);
                long count = count(new LambdaQueryWrapper<Photo>()
                        .isNotNull(Photo::getShootTime)
                        .apply("DATE_FORMAT(shoot_time, '%m') = {0}", monthStr));
                monthStats.put(String.valueOf(i), count);
            }
        } catch (Exception e) {
            log.error("获取月度统计数据时发生错误: {}", e.getMessage(), e);
            // 确保即使出错也返回12个月的数据
            for (int i = 1; i <= 12; i++) {
                if (!monthStats.containsKey(String.valueOf(i))) {
                    monthStats.put(String.valueOf(i), 0L);
                }
            }
        }

        return monthStats;
    }

    @Override
    @Cacheable(key = "'yearStats'")
    public Map<String, Long> getYearStats() {
        Map<String, Long> yearStats = new HashMap<>();

        try {
            // 查询数据库中最早的年份和最晚的年份
            Photo earliestPhoto = getOne(new LambdaQueryWrapper<Photo>()
                    .isNotNull(Photo::getShootTime)
                    .orderByAsc(Photo::getShootTime)
                    .last("LIMIT 1"));

            Photo latestPhoto = getOne(new LambdaQueryWrapper<Photo>()
                    .isNotNull(Photo::getShootTime)
                    .orderByDesc(Photo::getShootTime)
                    .last("LIMIT 1"));

            if (earliestPhoto != null && latestPhoto != null &&
                    earliestPhoto.getShootTime() != null && latestPhoto.getShootTime() != null) {

                int startYear = Integer.parseInt(earliestPhoto.getShootTime().substring(0, 4));
                int endYear = Integer.parseInt(latestPhoto.getShootTime().substring(0, 4));

                // 统计每一年的照片数量
                for (int year = startYear; year <= endYear; year++) {
                    long count = count(new LambdaQueryWrapper<Photo>()
                            .isNotNull(Photo::getShootTime)
                            .apply("DATE_FORMAT(shoot_time, '%Y') = {0}", String.valueOf(year)));
                    yearStats.put(String.valueOf(year), count);
                }
            } else {
                // 如果没有照片或时间为空，返回当前年份的空数据
                yearStats.put(String.valueOf(Year.now().getValue()), 0L);
            }
        } catch (Exception e) {
            log.error("获取年度统计数据时发生错误: {}", e.getMessage(), e);
            // 确保至少返回当前年份的数据
            yearStats.put(String.valueOf(Year.now().getValue()), 0L);
        }

        return yearStats;
    }

    @Override
    @Cacheable(key = "'allCameras'")
    public List<String> getAllCameras() {
        log.info("获取所有相机型号");
        List<String> cameras = list()
                .stream()
                .map(Photo::getCamera)
                .filter(camera -> camera != null && !camera.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        log.info("成功获取所有相机型号，共{}种", cameras.size());
        return cameras;
    }

    @Override
    @Cacheable(key = "'allLenses'")
    public List<String> getAllLenses() {
        log.info("获取所有镜头型号");
        List<String> lenses = list()
                .stream()
                .map(Photo::getLens)
                .filter(lens -> lens != null && !lens.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        log.info("成功获取所有镜头型号，共{}种", lenses.size());
        return lenses;
    }

    @Override
    @CacheEvict(value = { "photo" }, allEntries = true)
    public boolean updateCameraName(String oldCamera, String newCamera) {
        log.info("更新相机型号: {} -> {}", oldCamera, newCamera);
        try {
            // 检查是否有使用该相机型号的照片
            long count = count(new LambdaQueryWrapper<Photo>()
                    .eq(Photo::getCamera, oldCamera));

            if (count == 0) {
                log.warn("没有找到使用该相机型号的照片: {}", oldCamera);
                return false;
            }

            // 更新相机型号
            boolean updated = update()
                    .eq("camera", oldCamera)
                    .set("camera", newCamera)
                    .update();

            if (updated) {
                log.info("成功将相机型号从 {} 更新为 {}, 更新了 {} 条记录", oldCamera, newCamera, count);
            } else {
                log.error("更新相机型号失败: {} -> {}", oldCamera, newCamera);
            }

            return updated;
        } catch (Exception e) {
            log.error("更新相机型号时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @CacheEvict(value = { "photo" }, allEntries = true)
    public boolean updateLensName(String oldLens, String newLens) {
        log.info("更新镜头型号: {} -> {}", oldLens, newLens);
        try {
            // 检查是否有使用该镜头型号的照片
            long count = count(new LambdaQueryWrapper<Photo>()
                    .eq(Photo::getLens, oldLens));

            if (count == 0) {
                log.warn("没有找到使用该镜头型号的照片: {}", oldLens);
                return false;
            }

            // 更新镜头型号
            boolean updated = update()
                    .eq("lens", oldLens)
                    .set("lens", newLens)
                    .update();

            if (updated) {
                log.info("成功将镜头型号从 {} 更新为 {}, 更新了 {} 条记录", oldLens, newLens, count);
            } else {
                log.error("更新镜头型号失败: {} -> {}", oldLens, newLens);
            }

            return updated;
        } catch (Exception e) {
            log.error("更新镜头型号时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Cacheable(key = "'photoCountByCamera_' + #camera")
    public long getPhotoCountByCamera(String camera) {
        log.info("获取相机 {} 的照片数量", camera);
        try {
            long count = count(new LambdaQueryWrapper<Photo>()
                    .eq(Photo::getCamera, camera));

            log.info("成功获取相机 {} 的照片数量: {}", camera, count);
            return count;
        } catch (Exception e) {
            log.error("获取相机照片数量时发生错误: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    @Cacheable(key = "'photoCountByLens_' + #lens")
    public long getPhotoCountByLens(String lens) {
        return baseMapper.selectCount(
                new LambdaQueryWrapper<Photo>()
                        .eq(Photo::getLens, lens));
    }

    @Override
    @Cacheable(key = "'allFocalLengths'")
    public List<String> getAllFocalLengths() {
        return baseMapper.selectList(
                new LambdaQueryWrapper<Photo>()
                        .select(Photo::getFocalLength)
                        .isNotNull(Photo::getFocalLength)
                        .ne(Photo::getFocalLength, ""))
                .stream()
                .map(Photo::getFocalLength)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(key = "'photoCountByFocalLength_' + #focalLength")
    public long getPhotoCountByFocalLength(String focalLength) {
        return baseMapper.selectCount(
                new LambdaQueryWrapper<Photo>()
                        .eq(Photo::getFocalLength, focalLength));
    }

    @Override
    @CacheEvict(value = { "photo" }, allEntries = true)
    public boolean updateFocalLength(String oldFocalLength, String newFocalLength) {
        return baseMapper.update(
                new LambdaUpdateWrapper<Photo>()
                        .eq(Photo::getFocalLength, oldFocalLength)
                        .set(Photo::getFocalLength, newFocalLength)) > 0;
    }

    /**
     * 重命名照片文件
     * 
     * @param oldFileName 原文件名
     * @param newFileName 新文件名
     * @return 是否所有文件都重命名成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean renamePhotoFiles(String oldFileName, String newFileName) {
        try {
            return fileProcessService.renamePhotoFiles(oldFileName, newFileName, fullSizeUrl, thumbnail100KUrl,
                    thumbnail1000KUrl);
        } catch (IOException e) {
            log.error("重命名照片文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("重命名照片文件失败", e);
        }
    }

    @Override
    @Cacheable(key = "'noMetadataPhotos_' + #page + '_' + #pageSize")
    public Page<Photo> getNoMetadataPhotosByPage(int page, int pageSize) {
        Page<Photo> photoPage = new Page<>(page, pageSize);

        // 构建查询条件：任何一个元数据字段为NULL或空字符串
        LambdaQueryWrapper<Photo> queryWrapper = new LambdaQueryWrapper<Photo>()
                .orderByDesc(Photo::getShootTime)
                .and(wrapper -> wrapper
                        .isNull(Photo::getFileName).or().eq(Photo::getFileName, "")
                        .or()
                        .isNull(Photo::getAuthor).or().eq(Photo::getAuthor, "")
                        .or()
                        .isNull(Photo::getWidth)
                        .or()
                        .isNull(Photo::getHeight)
                        .or()
                        .isNull(Photo::getShootTime).or().eq(Photo::getShootTime, "")
                        .or()
                        .isNull(Photo::getAperture).or().eq(Photo::getAperture, "")
                        .or()
                        .isNull(Photo::getShutter).or().eq(Photo::getShutter, "")
                        .or()
                        .isNull(Photo::getIso).or().eq(Photo::getIso, "")
                        .or()
                        .isNull(Photo::getCamera).or().eq(Photo::getCamera, "")
                        .or()
                        .isNull(Photo::getLens).or().eq(Photo::getLens, "")
                        .or()
                        .isNull(Photo::getFocalLength).or().eq(Photo::getFocalLength, "")
                        .or()
                        .isNull(Photo::getStartRating));

        return page(photoPage, queryWrapper);
    }

    /**
     * 更新消息状态
     * 
     * @param messageId 消息ID
     * @param status    状态 (PROCESSING/COMPLETED/FAILED)
     * @param progress  进度 (0-100)
     * @param message   消息内容
     */
    @Override
    public void updatePhotoUploadStatus(String messageId, String status, int progress, String message) {
        Map<String, Object> statusInfo = new HashMap<>();
        statusInfo.put("messageId", messageId);
        statusInfo.put("status", status);
        statusInfo.put("progress", progress);
        statusInfo.put("message", message);
        statusInfo.put("updateTime", System.currentTimeMillis());

        // 存储到Redis，设置24小时过期
        String key = UPLOAD_STATUS_KEY_PREFIX + messageId;
        redisTemplate.opsForValue().set(key, statusInfo, 24, TimeUnit.HOURS);
        log.debug("已更新消息ID: {} 的处理状态为: {}, 进度: {}%", messageId, status, progress);
    }

    @Override
    public Map<String, Object> getPhotoUploadStatus(String messageId) {
        String key = UPLOAD_STATUS_KEY_PREFIX + messageId;
        Object statusObj = redisTemplate.opsForValue().get(key);

        if (statusObj != null && statusObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> statusInfo = (Map<String, Object>) statusObj;
            log.debug("获取到消息ID: {} 的处理状态: {}, 进度: {}%",
                    messageId, statusInfo.get("status"), statusInfo.get("progress"));
            return statusInfo;
        }

        // 如果没有找到状态信息，返回默认完成状态
        // 在实际应用中，可能需要返回"未找到"或"过期"等状态
        Map<String, Object> defaultStatus = new HashMap<>();
        defaultStatus.put("messageId", messageId);
        defaultStatus.put("status", "COMPLETED");
        defaultStatus.put("progress", 100);
        defaultStatus.put("message", "处理已完成或状态信息已过期");
        defaultStatus.put("updateTime", System.currentTimeMillis());

        log.warn("未找到消息ID: {} 的处理状态，返回默认完成状态", messageId);
        return defaultStatus;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePhotoById(String id, String fullSizeUrl, String thumbnail100KUrl, String thumbnail1000KUrl) {
        try {
            Photo photo = getById(id);
            if (photo == null) {
                log.warn("尝试删除不存在的照片，ID: {}", id);
                return false;
            }

            // 删除关联的组图照片记录
            LambdaQueryWrapper<GroupPhotoPhoto> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(GroupPhotoPhoto::getPhotoId, id);
            groupPhotoPhotoService.remove(queryWrapper);

            // 删除照片文件
            try {
                fileProcessService.deletePhotoFiles(fullSizeUrl, thumbnail100KUrl, thumbnail1000KUrl,
                        photo.getFileName());
            } catch (IOException e) {
                log.error("删除照片文件失败，ID: {}, 文件名: {}, 错误: {}", id, photo.getFileName(), e.getMessage(), e);
                throw new RuntimeException("删除照片文件失败: " + e.getMessage(), e);
            }

            // 删除照片记录
            boolean removed = removeById(id);
            if (removed) {
                log.info("成功删除照片及其文件，ID: {}, 文件名: {}", id, photo.getFileName());
                return true;
            } else {
                log.error("删除照片记录失败，ID: {}", id);
                throw new RuntimeException("删除照片记录失败");
            }
        } catch (RuntimeException e) {
            log.error("删除照片时发生错误，ID: {}, 错误: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean save(Photo entity) {
        boolean result = super.save(entity);
        if (result) {
            // 保存成功后，将ID添加到布隆过滤器
            bloomFilterUtil.add(entity.getId());
            log.debug("照片ID已添加到布隆过滤器: {}", entity.getId());
        }
        return result;
    }

    @Override
    public boolean saveBatch(Collection<Photo> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result) {
            // 批量保存成功后，将所有ID添加到布隆过滤器
            bloomFilterUtil.addBatch(entityList.stream()
                    .map(Photo::getId)
                    .collect(Collectors.toList()));
            log.debug("已将{}个照片ID添加到布隆过滤器", entityList.size());
        }
        return result;
    }

    /**
     * 更新照片信息并处理文件名的修改
     * 
     * @param photo       要更新的照片信息
     * @param oldFileName 原文件名
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePhotoWithFileName(Photo photo, String oldFileName) {
        // 记录需要回滚的信息
        boolean needRenameRollback = false;
        String newFileName = null;

        try {
            // 检查文件名是否发生变化
            if (photo.getFileName() != null && !photo.getFileName().equals(oldFileName)) {
                newFileName = photo.getFileName();
                // 1. 重命名文件
                boolean renameSuccess = false;
                try {
                    renameSuccess = fileProcessService.renamePhotoFiles(oldFileName, newFileName, fullSizeUrl,
                            thumbnail100KUrl, thumbnail1000KUrl);
                    if (!renameSuccess) {
                        log.error("重命名照片文件失败: {} -> {}", oldFileName, newFileName);
                        return false;
                    }
                    needRenameRollback = true;
                } catch (IOException e) {
                    log.error("重命名照片文件失败: {} -> {}, 错误: {}", oldFileName, newFileName, e.getMessage(), e);
                    return false;
                }
            }

            // 2. 更新数据库记录
            boolean updateSuccess = updateById(photo);
            if (!updateSuccess) {
                log.error("更新照片记录失败: {}", photo.getId());
                // 如果更新数据库失败，需要回滚文件重命名操作
                if (needRenameRollback) {
                    try {
                        fileProcessService.renamePhotoFiles(newFileName, oldFileName, fullSizeUrl, thumbnail100KUrl,
                                thumbnail1000KUrl);
                    } catch (IOException e) {
                        log.error("回滚重命名文件失败: {} -> {}, 错误: {}", newFileName, oldFileName, e.getMessage(), e);
                    }
                }
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("更新照片时发生错误: {}", e.getMessage(), e);
            // 如果发生异常，需要回滚文件重命名操作
            if (needRenameRollback) {
                try {
                    fileProcessService.renamePhotoFiles(newFileName, oldFileName, fullSizeUrl, thumbnail100KUrl,
                            thumbnail1000KUrl);
                } catch (IOException ex) {
                    log.error("回滚重命名文件失败: {} -> {}, 错误: {}", newFileName, oldFileName, ex.getMessage(), ex);
                }
            }
            throw new RuntimeException("更新照片失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessResult processPendingPhotosWithCompression(File pendingDir, File thumbnail100KDir,
            File thumbnail1000KDir, File fullSizeDir, boolean overwrite) {
        List<Photo> photos = new ArrayList<>();
        List<File> processedFiles = new ArrayList<>(); // 用于记录已处理的文件，便于回滚

        try {
            // 1. 处理照片元数据
            photos = getPhotosByFolder(pendingDir);
            if (photos.isEmpty()) {
                log.warn("没有找到有效的待处理照片");
                return new ProcessResult(Collections.emptyList(), Collections.emptyList(), false);
            }

            // 为每个照片分配ID
            for (Photo photo : photos) {
                photo.setId(String.valueOf(idGenerator.nextId()));
            }

            // 2. 处理图片压缩
            fileProcessService.processPhotoCompression(pendingDir, thumbnail100KDir, thumbnail1000KDir, fullSizeDir,
                    overwrite);

            // 3. 保存照片到数据库
            saveBatch(photos);

            // 4. 清理pending目录
            try {
                FileUtils.clearFolder(pendingDir, true);
            } catch (Exception e) {
                log.warn("清理pending目录时出错: {}, 但不影响主要流程", e.getMessage());
                // 清理目录失败不影响主流程，所以不抛出异常
            }

            return new ProcessResult(Collections.emptyList(), photos, true);
        } catch (Exception e) {
            log.error("处理待处理图片时发生错误: {}", e.getMessage(), e);
            // 事务会自动回滚数据库操作，这里主动回滚文件操作
            fileProcessService.rollbackProcessedFiles(processedFiles);
            throw new RuntimeException("处理待处理图片失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByFileName(String fileName) {
        return count(new LambdaQueryWrapper<Photo>()
                .eq(Photo::getFileName, fileName)) > 0;
    }

    @Override
    public Map<String, Object> validateMeteorologyGroups() {
        log.info("开始验证气象组图冲突");
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> conflicts = new ArrayList<>();
        
        // 气象组ID对应关系（朝霞-日落，晚霞-日出是冲突的）
        // 使用常量来定义冲突组，而不是硬编码数字
        // 朝霞(1)和日落(4)冲突，晚霞(2)和日出(3)冲突
        final String SUNRISE_GLOW = "1"; // 朝霞
        final String SUNSET_GLOW = "2";  // 晚霞
        final String SUNRISE = "3";      // 日出
        final String SUNSET = "4";       // 日落
        
        List<List<String>> conflictGroups = new ArrayList<>();
        conflictGroups.add(Arrays.asList(SUNRISE_GLOW, SUNSET)); // 朝霞和日落冲突
        conflictGroups.add(Arrays.asList(SUNSET_GLOW, SUNRISE)); // 晚霞和日出冲突
        
        // 获取所有气象组图ID
        List<String> meteorologyGroupIds = new ArrayList<>();
        for (List<String> group : conflictGroups) {
            meteorologyGroupIds.addAll(group);
        }
        
        // 获取所有属于气象组的照片ID列表
        Set<String> allMeteorologyPhotoIds = new HashSet<>();
        
        // 为每对冲突组图构建照片ID映射
        for (List<String> conflictPair : conflictGroups) {
            String groupId1 = conflictPair.get(0);
            String groupId2 = conflictPair.get(1);
            
            // 获取第一个组的所有照片
            List<String> photosInGroup1 = groupPhotoPhotoService.getPhotoIdsByGroupPhotoId(groupId1);
            Set<String> photosInGroup1Set = new HashSet<>(photosInGroup1);
            allMeteorologyPhotoIds.addAll(photosInGroup1);
            log.debug("组图 {} 包含 {} 张照片", groupId1, photosInGroup1.size());
            
            // 获取第二个组的所有照片
            List<String> photosInGroup2 = groupPhotoPhotoService.getPhotoIdsByGroupPhotoId(groupId2);
            Set<String> photosInGroup2Set = new HashSet<>(photosInGroup2);
            allMeteorologyPhotoIds.addAll(photosInGroup2);
            log.debug("组图 {} 包含 {} 张照片", groupId2, photosInGroup2.size());
            
            // 找出同时属于两个组的照片（冲突）
            Set<String> conflictingPhotoIds = new HashSet<>(photosInGroup1Set);
            conflictingPhotoIds.retainAll(photosInGroup2Set); // 取交集
            
            if (!conflictingPhotoIds.isEmpty()) {
                log.debug("发现 {} 张照片同时属于组图 {} 和 {}", conflictingPhotoIds.size(), groupId1, groupId2);
                
                // 查询冲突照片的详细信息
                for (String photoId : conflictingPhotoIds) {
                    Photo photo = getById(photoId);
                    if (photo != null) {
                        Map<String, Object> conflict = new HashMap<>();
                        conflict.put("photoId", photoId);
                        conflict.put("fileName", photo.getFileName());
                        conflict.put("groupIds", Arrays.asList(
                            Integer.parseInt(groupId1),
                            Integer.parseInt(groupId2)
                        ));
                        conflicts.add(conflict);
                    }
                }
            }
        }
        
        int totalMeteorologyPhotos = allMeteorologyPhotoIds.size();
        int conflictCount = conflicts.size();
        
        log.info("气象组图总照片数: {}, 发现 {} 张照片存在气象组图冲突", totalMeteorologyPhotos, conflictCount);
        
        // 组装结果
        result.put("totalPhotos", totalMeteorologyPhotos);
        result.put("conflictCount", conflictCount);
        result.put("conflicts", conflicts);
        
        return result;
    }
}
