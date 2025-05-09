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

// 导入从PhotoProcessingService所需的类
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
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;
import org.springframework.transaction.annotation.Transactional;

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

    @Value("${qingdai.defaultAuthor}")
    private String defaultAuthor;

    @Value("${qingdai.fullSizeUrl}")
    private String fullSizeUrl;
    
    @Value("${qingdai.thumbnail100KUrl}")
    private String thumbnail100KUrl;
    
    @Value("${qingdai.thumbnail1000KUrl}")
    private String thumbnail1000KUrl;

    @Autowired
    private GroupPhotoPhotoService groupPhotoPhotoService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private BloomFilterUtil bloomFilterUtil;
    
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
    @Cacheable(key = "'fileName_' + #photoId")
    public String getFileNameById(String photoId) {
        // 先检查布隆过滤器，如果布隆过滤器显示元素不存在，则直接返回null
        // 这样可以避免对不存在的ID进行数据库查询，防止缓存穿透
        if (!bloomFilterUtil.exists(photoId)) {
            log.debug("照片ID在布隆过滤器中不存在: {}", photoId);
            return null;
        }
        
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
        Arrays.stream(Objects.requireNonNull(srcDir.listFiles()))
                .parallel()
                .filter(this::fileIsSupportedPhoto)
                .forEach(file -> compressPhoto(file, thumbnailDir, maxSizeKB, overwrite));
    }

    // 判断是否为支持的图片格式
    @Override
    public boolean multipartFileIsSupportedPhoto(MultipartFile file) {
        String fileName = file.getOriginalFilename().toLowerCase();
        String contentType = file.getContentType();

        // 验证文件扩展名
        boolean validExtension = fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")
                || fileName.endsWith(".png") || fileName.endsWith(".webp");

        // 验证MIME类型
        boolean validMime = contentType != null && (contentType.startsWith("image/")
                && (contentType.equals("image/jpeg")
                        || contentType.equals("image/png")
                        || contentType.equals("image/webp")));

        return validExtension && validMime;
    }

    // 判断文件是否为支持的图片格式
    @Override
    public boolean fileIsSupportedPhoto(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return false;
        }

        String fileName = file.getName().toLowerCase();

        // 验证文件扩展名
        boolean validExtension = fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")
                || fileName.endsWith(".png") || fileName.endsWith(".webp");

        try {
            // 探测真实MIME类型
            String mimeType = Files.probeContentType(file.toPath());
            boolean validMime = mimeType != null && (mimeType.startsWith("image/")
                    && (mimeType.equals("image/jpeg")
                            || mimeType.equals("image/png")
                            || mimeType.equals("image/webp")));

            return validExtension && validMime;
        } catch (IOException e) {
            System.out.println("文件类型检测失败: " + file.getName() + " - " + e.getMessage());
            return false;
        }
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
        photo.setAuthor(defaultAuthor);
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
        FileUtils.deleteFile(new File(fullSizeUrl, fileName));
        FileUtils.deleteFile(new File(thumbnail100KUrl, fileName));
        FileUtils.deleteFile(new File(thumbnail1000KUrl, fileName));
    }

    @Override
    public void processPhotoCompression(File tempDir, File thumbnail100KDir, File thumbnail1000KDir, File fullSizeDir, boolean overwrite) throws IOException {
        // 在tempDir下创建1000K临时目录
        File temp1000KDir = new File(tempDir, "1000K");
        if (!temp1000KDir.exists() && !temp1000KDir.mkdirs()) {
            throw new IOException("无法创建1000K临时目录: " + temp1000KDir.getAbsolutePath());
        }
        
        // 压缩到1000K临时目录
        thumbnailPhotosFromFolderToFolder(tempDir, temp1000KDir, 1000, overwrite);
        log.info("目录{}完成1000K压缩",tempDir.getName());
        
        // 基于1000K临时目录的图片压缩到100K目录
        thumbnailPhotosFromFolderToFolder(temp1000KDir, thumbnail100KDir, 100, overwrite);
        log.info("目录{}完成100K压缩",tempDir.getName());
        
        // 将1000K临时目录的图片复制到1000K目录
        FileUtils.copyFiles(temp1000KDir, thumbnail1000KDir);
        log.info("目录{}完成1000K图片复制",tempDir.getName());
        
        // 复制到原图目录
        FileUtils.copyFiles(tempDir, fullSizeDir);
        log.info("目录{}完成原图复制",tempDir.getName());
    }

    @Override
    public ProcessResult processPhotosFromFrontend(MultipartFile[] files, Integer startRating, boolean overwrite) {
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
                log.warn("没有有效的图片文件");
                return new ProcessResult(Collections.emptyList(), Collections.emptyList(), false);
            }

            // 处理照片元数据
            List<Photo> photos = getPhotosByMultipartFiles(validFiles.toArray(new MultipartFile[0]));

            // 检查重复文件名
            List<Photo> existingPhotos = new ArrayList<>();
            List<Photo> newPhotos = new ArrayList<>();
            
            for (Photo photo : photos) {
                Photo existingPhoto = getOne(
                    new LambdaQueryWrapper<Photo>()
                        .eq(Photo::getFileName, photo.getFileName())
                );
                
                if (existingPhoto != null) {
                    // 保留原有字段
                    photo.setId(existingPhoto.getId());
                    photo.setTitle(existingPhoto.getTitle());
                    photo.setFileName(existingPhoto.getFileName());
                    photo.setAuthor(existingPhoto.getAuthor());
                    photo.setIntroduce(existingPhoto.getIntroduce());
                    photo.setStartRating(existingPhoto.getStartRating());
                    
                    // 删除原有文件
                    deletePhotoFiles(fullSizeUrl, thumbnail100KUrl, thumbnail1000KUrl, photo.getFileName());
                    
                    existingPhotos.add(photo);
                } else {
                    // 设置新的start值
                    photo.setStartRating(startRating);
                    newPhotos.add(photo);
                }
            }

            // 保存到数据库
            boolean result = true;
            if (!existingPhotos.isEmpty()) {
                result = result && updateBatchById(existingPhotos);
                log.info("图片信息更新数据库成功");
            }
            if (!newPhotos.isEmpty()) {
                result = result && saveBatch(newPhotos);
                log.info("新图片信息保存数据库成功");
            }

            return new ProcessResult(existingPhotos, newPhotos, result);
        } catch (Exception e) {
            log.error("处理图片时发生错误: {}", e.getMessage(), e);
            return new ProcessResult(Collections.emptyList(), Collections.emptyList(), false);
        }
    }

    @Override
    public ProcessResult processPhotoFromMQ(String[] fileNames, String tempDirPath, Integer startRating, boolean overwrite) {
        try {
            if (fileNames == null || fileNames.length == 0) {
                log.warn("没有接收到文件名");
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
            List<Photo> existingPhotos = new ArrayList<>();
            List<Photo> newPhotos = new ArrayList<>();
            
            for (Photo photo : photos) {
                Photo existingPhoto = getOne(
                    new LambdaQueryWrapper<Photo>()
                        .eq(Photo::getFileName, photo.getFileName())
                );
                
                if (existingPhoto != null) {
                    // 保留原有字段
                    photo.setId(existingPhoto.getId());
                    photo.setTitle(existingPhoto.getTitle());
                    photo.setFileName(existingPhoto.getFileName());
                    photo.setAuthor(existingPhoto.getAuthor());
                    photo.setIntroduce(existingPhoto.getIntroduce());
                    photo.setStartRating(existingPhoto.getStartRating());
                    
                    // 在MQ消费场景下不需要删除原有文件，因为文件已经被处理和替换
                    
                    existingPhotos.add(photo);
                } else {
                    // 设置新的startRating值
                    photo.setStartRating(startRating);
                    newPhotos.add(photo);
                }
            }

            // 保存到数据库
            boolean result = true;
            if (!existingPhotos.isEmpty()) {
                result = result && updateBatchById(existingPhotos);
                log.info("图片信息更新数据库成功");
            }
            if (!newPhotos.isEmpty()) {
                result = result && saveBatch(newPhotos);
                log.info("新图片信息保存数据库成功");
            }

            return new ProcessResult(existingPhotos, newPhotos, result);
        } catch (Exception e) {
            log.error("处理来自MQ的图片时发生错误: {}", e.getMessage(), e);
            return new ProcessResult(Collections.emptyList(), Collections.emptyList(), false);
        }
    }

    @Override
    public Map<String, Object> validatePhotoExistence() {
        log.info("开始验证数据库照片在文件系统中的存在性");
        List<Photo> allPhotos = list();
        
        Map<String, Object> result = new HashMap<>();
        
        if (allPhotos.isEmpty()) {
            log.warn("数据库中没有照片记录");
            result.put("message", "数据库中没有照片记录");
            return result;
        }
        
        int totalCount = allPhotos.size();
        int missingFullSize = 0;
        int missing100K = 0;
        int missing1000K = 0;
        List<String> missingDetails = new ArrayList<>();
        
        for (Photo photo : allPhotos) {
            String fileName = photo.getFileName();
            boolean fullSizeMissing = false;
            boolean thumbnail100KMissing = false;
            boolean thumbnail1000KMissing = false;
            
            // 检查原图
            File fullSizeFile = new File(fullSizeUrl, fileName);
            if (!fullSizeFile.exists()) {
                missingFullSize++;
                fullSizeMissing = true;
                log.debug("照片ID:{}的原图文件缺失，文件名:{}", photo.getId(), fileName);
            }
            
            // 检查100K压缩图
            File thumbnail100KFile = new File(thumbnail100KUrl, fileName);
            if (!thumbnail100KFile.exists()) {
                missing100K++;
                thumbnail100KMissing = true;
                log.debug("照片ID:{}的100K压缩图文件缺失，文件名:{}", photo.getId(), fileName);
            }
            
            // 检查1000K压缩图
            File thumbnail1000KFile = new File(thumbnail1000KUrl, fileName);
            if (!thumbnail1000KFile.exists()) {
                missing1000K++;
                thumbnail1000KMissing = true;
                log.debug("照片ID:{}的1000K压缩图文件缺失，文件名:{}", photo.getId(), fileName);
            }
            
            // 如果有任何一个文件缺失，添加到详情列表
            if (fullSizeMissing || thumbnail100KMissing || thumbnail1000KMissing) {
                missingDetails.add(String.format("ID:%s,文件名:%s,原图缺失:%b,100K缺失:%b,1000K缺失:%b", 
                    photo.getId(), fileName, fullSizeMissing, thumbnail100KMissing, thumbnail1000KMissing));
            }
        }
        
        result.put("totalCount", totalCount);
        result.put("missingFullSize", missingFullSize);
        result.put("missing100K", missing100K);
        result.put("missing1000K", missing1000K);
        result.put("missingDetails", missingDetails);
        
        log.info("验证完成，总照片数:{}, 缺失原图:{}, 缺失100K压缩图:{}, 缺失1000K压缩图:{}", 
            totalCount, missingFullSize, missing100K, missing1000K);
        
        return result;
    }
    
    @Override
    public Map<String, Object> validateFileSystemPhotos(String fullSizeUrl, String thumbnail100KUrl, String thumbnail1000KUrl) {
        log.info("开始验证文件系统中照片在数据库中的存在性");
        Map<String, Object> result = new HashMap<>();
        
        // 获取所有目录下的文件
        File fullSizeDir = new File(fullSizeUrl);
        File thumbnail100KDir = new File(thumbnail100KUrl);
        File thumbnail1000KDir = new File(thumbnail1000KUrl);
        
        // 验证目录
        if (!fullSizeDir.exists() || !fullSizeDir.isDirectory() ||
            !thumbnail100KDir.exists() || !thumbnail100KDir.isDirectory() ||
            !thumbnail1000KDir.exists() || !thumbnail1000KDir.isDirectory()) {
            log.error("无法访问一个或多个照片目录");
            result.put("error", "无法访问照片目录");
            return result;
        }
        
        File[] fullSizeFiles = fullSizeDir.listFiles();
        File[] thumbnail100KFiles = thumbnail100KDir.listFiles();
        File[] thumbnail1000KFiles = thumbnail1000KDir.listFiles();
        
        if (fullSizeFiles == null || thumbnail100KFiles == null || thumbnail1000KFiles == null) {
            log.error("无法列出一个或多个照片目录中的文件");
            result.put("error", "无法列出目录文件");
            return result;
        }
        
        // 获取数据库中所有照片文件名
        List<Photo> allPhotos = list();
        Set<String> dbPhotoNames = allPhotos.stream()
                .map(Photo::getFileName)
                .collect(Collectors.toSet());
        
        // 检查原图目录
        List<String> fullSizeNotInDb = Arrays.stream(fullSizeFiles)
                .filter(f -> !f.isDirectory() && fileIsSupportedPhoto(f))
                .map(File::getName)
                .filter(name -> !dbPhotoNames.contains(name))
                .peek(name -> log.debug("原图文件:{}不在数据库中", name))
                .collect(Collectors.toList());
        
        // 检查100K压缩图目录
        List<String> thumbnail100KNotInDb = Arrays.stream(thumbnail100KFiles)
                .filter(f -> !f.isDirectory() && fileIsSupportedPhoto(f))
                .map(File::getName)
                .filter(name -> !dbPhotoNames.contains(name))
                .peek(name -> log.debug("100K压缩文件:{}不在数据库中", name))
                .collect(Collectors.toList());
        
        // 检查1000K压缩图目录
        List<String> thumbnail1000KNotInDb = Arrays.stream(thumbnail1000KFiles)
                .filter(f -> !f.isDirectory() && fileIsSupportedPhoto(f))
                .map(File::getName)
                .filter(name -> !dbPhotoNames.contains(name))
                .peek(name -> log.debug("1000K压缩文件:{}不在数据库中", name))
                .collect(Collectors.toList());
        
        result.put("fullSizeCount", fullSizeFiles.length);
        result.put("thumbnail100KCount", thumbnail100KFiles.length);
        result.put("thumbnail1000KCount", thumbnail1000KFiles.length);
        result.put("dbPhotoCount", dbPhotoNames.size());
        result.put("fullSizeNotInDb", fullSizeNotInDb);
        result.put("thumbnail100KNotInDb", thumbnail100KNotInDb);
        result.put("thumbnail1000KNotInDb", thumbnail1000KNotInDb);
        
        log.info("验证完成，原图目录文件数:{}, 100K压缩图目录文件数:{}, 1000K压缩图目录文件数:{}, 数据库照片数:{}, " +
                "原图不在数据库中:{}, 100K压缩图不在数据库中:{}, 1000K压缩图不在数据库中:{}",
                fullSizeFiles.length, thumbnail100KFiles.length, thumbnail1000KFiles.length, dbPhotoNames.size(),
                fullSizeNotInDb.size(), thumbnail100KNotInDb.size(), thumbnail1000KNotInDb.size());
        
        return result;
    }

    @Override
    public Map<String, Object> deletePhotosNotInDatabase(String fullSizeUrl, String thumbnail100KUrl, String thumbnail1000KUrl) {
        log.info("开始删除文件系统中数据库没有记录的照片");
        Map<String, Object> result = new HashMap<>();
        
        // 先验证文件系统中的文件
        Map<String, Object> validateResult = validateFileSystemPhotos(fullSizeUrl, thumbnail100KUrl, thumbnail1000KUrl);
        
        // 如果验证过程中出现错误，直接返回错误信息
        if (validateResult.containsKey("error")) {
            log.error("验证文件系统照片失败，无法执行删除操作");
            result.put("error", "验证失败，无法执行删除操作");
            return result;
        }
        
        // 从验证结果中获取不在数据库中的文件列表
        @SuppressWarnings("unchecked")
        List<String> fullSizeNotInDb = (List<String>) validateResult.getOrDefault("fullSizeNotInDb", new ArrayList<String>());
        
        @SuppressWarnings("unchecked")
        List<String> thumbnail100KNotInDb = (List<String>) validateResult.getOrDefault("thumbnail100KNotInDb", new ArrayList<String>());
        
        @SuppressWarnings("unchecked")
        List<String> thumbnail1000KNotInDb = (List<String>) validateResult.getOrDefault("thumbnail1000KNotInDb", new ArrayList<String>());
        
        int deletedFullSize = 0;
        int deleted100K = 0;
        int deleted1000K = 0;
        List<String> deletedFiles = new ArrayList<>();
        List<String> errorFiles = new ArrayList<>();
        
        // 删除原图目录中的文件
        for (String fileName : fullSizeNotInDb) {
            try {
                File file = new File(fullSizeUrl, fileName);
                if (file.delete()) {
                    deletedFullSize++;
                    deletedFiles.add("原图: " + fileName);
                    log.debug("已删除原图文件: {}", fileName);
                } else {
                    errorFiles.add("原图删除失败: " + fileName);
                    log.warn("删除原图文件失败: {}", fileName);
                }
            } catch (Exception e) {
                errorFiles.add("原图删除异常: " + fileName);
                log.error("删除原图文件时发生异常: {}, 错误: {}", fileName, e.getMessage(), e);
            }
        }
        
        // 删除100K压缩图目录中的文件
        for (String fileName : thumbnail100KNotInDb) {
            try {
                File file = new File(thumbnail100KUrl, fileName);
                if (file.delete()) {
                    deleted100K++;
                    deletedFiles.add("100K压缩图: " + fileName);
                    log.debug("已删除100K压缩图文件: {}", fileName);
                } else {
                    errorFiles.add("100K压缩图删除失败: " + fileName);
                    log.warn("删除100K压缩图文件失败: {}", fileName);
                }
            } catch (Exception e) {
                errorFiles.add("100K压缩图删除异常: " + fileName);
                log.error("删除100K压缩图文件时发生异常: {}, 错误: {}", fileName, e.getMessage(), e);
            }
        }
        
        // 删除1000K压缩图目录中的文件
        for (String fileName : thumbnail1000KNotInDb) {
            try {
                File file = new File(thumbnail1000KUrl, fileName);
                if (file.delete()) {
                    deleted1000K++;
                    deletedFiles.add("1000K压缩图: " + fileName);
                    log.debug("已删除1000K压缩图文件: {}", fileName);
                } else {
                    errorFiles.add("1000K压缩图删除失败: " + fileName);
                    log.warn("删除1000K压缩图文件失败: {}", fileName);
                }
            } catch (Exception e) {
                errorFiles.add("1000K压缩图删除异常: " + fileName);
                log.error("删除1000K压缩图文件时发生异常: {}, 错误: {}", fileName, e.getMessage(), e);
            }
        }
        
        // 整理删除结果
        result.put("deletedFullSize", deletedFullSize);
        result.put("deleted100K", deleted100K);
        result.put("deleted1000K", deleted1000K);
        result.put("totalDeleted", deletedFullSize + deleted100K + deleted1000K);
        result.put("deletedFiles", deletedFiles);
        result.put("errorFiles", errorFiles);
        
        log.info("删除完成，删除原图: {}, 删除100K压缩图: {}, 删除1000K压缩图: {}, 总删除文件: {}, 错误文件: {}", 
            deletedFullSize, deleted100K, deleted1000K, 
            deletedFullSize + deleted100K + deleted1000K, errorFiles.size());
        
        return result;
    }

    @Override
    public Map<String, Object> deleteMissingPhotoRecords() {
        log.info("开始删除丢失了全部三种图片的数据库记录");
        Map<String, Object> result = new HashMap<>();
        
        // 先验证数据库照片在文件系统中的存在性
        Map<String, Object> validateResult = validatePhotoExistence();
        
        if (!validateResult.containsKey("missingDetails")) {
            log.error("验证数据库照片失败，无法执行删除操作");
            result.put("error", "验证失败，无法执行删除操作");
            return result;
        }
        
        @SuppressWarnings("unchecked")
        List<String> missingDetails = (List<String>) validateResult.get("missingDetails");
        
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
                    long id = Long.parseLong(idStr);
                    if (removeById(id)) {
                        deletedRecords.add(detail);
                        totalDeleted++;
                        log.info("已删除缺失全部三种图片的数据库记录，ID: {}", id);
                    } else {
                        errorRecords.add(detail + " - 删除失败");
                        log.error("删除数据库记录失败，ID: {}", id);
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
        try {
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
        } catch (Exception e) {
            log.error("获取照片统计数据时发生错误: {}", e.getMessage(), e);
        }
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
                    .ne(Photo::getFocalLength, "")
            ).stream()
            .map(Photo::getFocalLength)
            .distinct()
            .collect(Collectors.toList());

            // 统计每个焦距的使用次数
            for (String focalLength : focalLengths) {
                long count = baseMapper.selectCount(
                    new LambdaQueryWrapper<Photo>()
                        .eq(Photo::getFocalLength, focalLength)
                );
                
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
        
        for (int i = 1; i <= 12; i++) {
            final int month = i;
            long count = count(new LambdaQueryWrapper<Photo>()
                    .apply("DATE_FORMAT(time, '%m') = {0}", String.format("%02d", month)));
            monthStats.put(String.valueOf(month), count);
        }
        
        return monthStats;
    }
    
    @Override
    @Cacheable(key = "'yearStats'")
    public Map<String, Long> getYearStats() {
        Map<String, Long> yearStats = new HashMap<>();
        
        // 获取最早的照片年份和当前年份
        int currentYear = Year.now().getValue();
        
        // 尝试获取最早照片的年份
        Integer earliestYear = getOne(new LambdaQueryWrapper<Photo>()
                .orderByAsc(Photo::getShootTime)
                .last("LIMIT 1"))
                .getShootTime() != null ? Integer.parseInt(
                        getOne(new LambdaQueryWrapper<Photo>()
                                .orderByAsc(Photo::getShootTime)
                                .last("LIMIT 1"))
                                .getShootTime().substring(0, 4))
                        : currentYear - 5; // 如果没有照片，默认显示近5年
        
        // 确保至少有5年的数据显示
        earliestYear = Math.min(earliestYear, currentYear - 4);
        
        // 统计每年的照片数量
        for (int year = earliestYear; year <= currentYear; year++) {
            final int queryYear = year;
            long count = count(new LambdaQueryWrapper<Photo>()
                    .apply("DATE_FORMAT(time, '%Y') = {0}", String.valueOf(queryYear)));
            yearStats.put(String.valueOf(queryYear), count);
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
    @CacheEvict(value = {"photo"}, allEntries = true)
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
    @CacheEvict(value = {"photo"}, allEntries = true)
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
                .eq(Photo::getLens, lens)
        );
    }

    @Override
    @Cacheable(key = "'allFocalLengths'")
    public List<String> getAllFocalLengths() {
        return baseMapper.selectList(
            new LambdaQueryWrapper<Photo>()
                .select(Photo::getFocalLength)
                .isNotNull(Photo::getFocalLength)
                .ne(Photo::getFocalLength, "")
        ).stream()
        .map(Photo::getFocalLength)
        .distinct()
        .collect(Collectors.toList());
    }

    @Override
    @Cacheable(key = "'photoCountByFocalLength_' + #focalLength")
    public long getPhotoCountByFocalLength(String focalLength) {
        return baseMapper.selectCount(
            new LambdaQueryWrapper<Photo>()
                .eq(Photo::getFocalLength, focalLength)
        );
    }

    @Override
    @CacheEvict(value = {"photo"}, allEntries = true)
    public boolean updateFocalLength(String oldFocalLength, String newFocalLength) {
        return baseMapper.update(
            new LambdaUpdateWrapper<Photo>()
                .eq(Photo::getFocalLength, oldFocalLength)
                .set(Photo::getFocalLength, newFocalLength)
        ) > 0;
    }

    /**
     * 重命名照片文件
     * @param oldFileName 原文件名
     * @param newFileName 新文件名
     * @return 是否所有文件都重命名成功
     */
    @Override
    public boolean renamePhotoFiles(String oldFileName, String newFileName) {
        if (oldFileName == null || newFileName == null || oldFileName.equals(newFileName)) {
            return true;
        }

        boolean allSuccess = true;
        List<String> paths = Arrays.asList(fullSizeUrl, thumbnail100KUrl, thumbnail1000KUrl);
        List<String> failedPaths = new ArrayList<>();

        for (String path : paths) {
            File oldFile = new File(path, oldFileName);
            File newFile = new File(path, newFileName);

            if (oldFile.exists()) {
                try {
                    if (!oldFile.renameTo(newFile)) {
                        log.error("重命名文件失败: {} -> {}", oldFile.getAbsolutePath(), newFile.getAbsolutePath());
                        allSuccess = false;
                        failedPaths.add(path);
                    } else {
                        log.info("成功重命名文件: {} -> {}", oldFile.getAbsolutePath(), newFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    log.error("重命名文件时发生异常: {} -> {}, 错误: {}", 
                        oldFile.getAbsolutePath(), newFile.getAbsolutePath(), e.getMessage(), e);
                    allSuccess = false;
                    failedPaths.add(path);
                }
            } else {
                log.warn("文件不存在，无需重命名: {}", oldFile.getAbsolutePath());
            }
        }

        if (!allSuccess) {
            log.error("部分文件重命名失败，失败的路径: {}", String.join(", ", failedPaths));
        }

        return allSuccess;
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
                    .isNull(Photo::getStartRating)
                );
        
        return page(photoPage, queryWrapper);
    }

    /**
     * 更新消息状态
     * @param messageId 消息ID
     * @param status 状态 (PROCESSING/COMPLETED/FAILED)
     * @param progress 进度 (0-100)
     * @param message 消息内容
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
    @Transactional
    public boolean deletePhotoById(String id, String fullSizeUrl, String thumbnail100KUrl, String thumbnail1000KUrl) {
        try {
            Long photoId = Long.valueOf(id);
            Photo photo = this.getById(photoId);
            if (photo == null) {
                log.warn("未找到ID为{}的照片记录", id);
                return false;
            }

            // 先删除照片在group_photo_photo表中的所有关联记录
            LambdaQueryWrapper<GroupPhotoPhoto> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(GroupPhotoPhoto::getPhotoId, id);
            groupPhotoPhotoService.remove(queryWrapper);

            // 删除照片文件
            this.deletePhotoFiles(fullSizeUrl, thumbnail100KUrl, thumbnail1000KUrl, photo.getFileName());
            
            // 删除照片记录
            boolean result = this.removeById(photoId);

            if (result) {
                log.info("成功删除照片，ID: {}, 文件名: {}", id, photo.getFileName());
                // 注意：这里不需要从布隆过滤器中删除元素
                // 布隆过滤器没有删除操作，对误判率的影响很小
                return true;
            } else {
                log.error("删除照片记录失败，ID: {}", id);
                return false;
            }
        } catch (Exception e) {
            log.error("删除照片时发生错误，ID: {}, 错误: {}", id, e.getMessage(), e);
            throw new RuntimeException("删除照片失败: " + e.getMessage(), e);
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
}
