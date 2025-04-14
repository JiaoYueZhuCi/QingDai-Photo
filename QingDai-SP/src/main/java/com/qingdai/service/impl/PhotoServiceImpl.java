package com.qingdai.service.impl;

import com.qingdai.entity.Photo;
import com.qingdai.mapper.PhotoMapper;
import com.qingdai.service.PhotoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

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
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.util.List;

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
public class PhotoServiceImpl extends ServiceImpl<PhotoMapper, Photo> implements PhotoService {

    @Value("${qingdai.defaultAuthor}")
    private String defaultAuthor;

    // 雪花算法生成器
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);

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

    // 处理指定文件夹中的图片文件，返回一个包含处理后照片信息的 Photo 对象列表
    @Override
    public List<Photo> getPhotosByFolder(File folder) {
        return Arrays.stream(FileUtils.getImageFiles(folder)) // 返回 Stream<File>
                .parallel() // 并行处理
                .map(this::getPhotoObjectByFile)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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

    // @Override
    // public void thumbnailPhotoFromMultipartFileToFolder(MultipartFile photo,
    //         File pendingDir,
    //         File thumbnailDir,
    //         int maxSizeKB,
    //         boolean overwrite) throws IOException {
    //     // 创建随机名称的临时目录
    //     File tempDir = FileUtils.createTempDir(pendingDir);
    //     if (!tempDir.exists() && !tempDir.mkdirs()) {
    //         throw new IOException("无法创建临时目录: " + tempDir.getAbsolutePath());
    //     }

    //     // 保存所有图片到临时目录
    //     File thumbnailUrl = new File(tempDir, photo.getOriginalFilename());
    //     try {
    //         FileUtils.saveFile(photo, tempDir);
    //     } catch (IOException e) {
    //         throw new RuntimeException(e);
    //     }

    //     // 调用压缩方法处理临时目录中的文件
    //     compressPhoto(thumbnailUrl, thumbnailDir, maxSizeKB, overwrite);

    //     // 删除临时目录及其内容
    //     FileUtils.deleteFolder(tempDir);
    // }

    // 根据照片ID获取文件名
    @Override
    public String getFileNameById(String photoId) {
        Photo photo = getById(photoId);
        if (photo == null) {
            return null;
        }
        return photo.getFileName();
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
            String fileName = fullSizeUrl.getName();
            String formatName = FileUtils.getFileExtension(fileName).toLowerCase();
            String baseName = FileUtils.getBaseName(fileName);

            // 处理PNG转换为JPEG
            if ("png".equals(formatName)) {
                fileName = baseName + ".jpg"; // 修改后缀为jpg
                formatName = "jpg";
            }

            File thumbnailUrl = new File(thumbnailDir, fileName);

            if (thumbnailUrl.exists() && !overwrite)
                return;

            long maxSizeBytes = maxSizeKB * 1024L;
            boolean sizeMet = false;

            // 优先调整质量参数
            sizeMet = adjustPhotoQuality(fullSizeUrl, thumbnailUrl, formatName, maxSizeBytes);

            // 若未达标，调整尺寸
            if (!sizeMet) {
                sizeMet = scaleDownPhoto(fullSizeUrl, thumbnailUrl, formatName, maxSizeBytes);
            }

            if (!sizeMet) {
                throw new IOException("无法压缩到指定大小: " + fullSizeUrl.getName());
            }
        } catch (IOException e) {
            throw new RuntimeException("文件处理失败: " + fullSizeUrl.getName(), e);
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
            Thumbnails.of(fullSizeUrl)
                    .scale(1)
                    .outputFormat(formatName)
                    .outputQuality(quality)
                    // 处理PNG透明背景
                    .imageType(BufferedImage.TYPE_INT_RGB)
                    .toFile(thumbnailUrl);

            if (thumbnailUrl.length() <= maxSizeBytes) {
                return true;
            }
            quality = BigDecimal.valueOf(quality).subtract(BigDecimal.valueOf(step)).doubleValue();
        }
        return false;
    }

    // 图片按比例缩小
    private boolean scaleDownPhoto(File fullSizeUrl, File thumbnailUrl, String formatName, long maxSizeBytes)
            throws IOException {
        double scale = 1.0;
        double step = 0.1;

        while (scale >= 0.1) {
            Thumbnails.of(fullSizeUrl)
                    .scale(scale)
                    .outputFormat(formatName)
                    .outputQuality(0.7) // 适当质量值
                    .imageType(BufferedImage.TYPE_INT_RGB)
                    .toFile(thumbnailUrl);

            if (thumbnailUrl.length() <= maxSizeBytes) {
                return true;
            }
            scale -= step;
        }
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
        photo.setStart(0);
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
    }

    // 格式化拍摄时间
    private void processShootingTime(ExifSubIFDDirectory exif, Photo photo) {
        Date photoDate = exif.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        // 使 photoDate 向前八个小时 矫正时区错误
        if (photoDate != null) {
            photoDate = new Date(photoDate.getTime() - 8 * 60 * 60 * 1000);
        }
        Optional.ofNullable(photoDate)
                .ifPresent(date -> photo.setTime(DateUtils.formatDateTime(date)));
    }

    // 存储相机拍摄参数
    private void processCameraSettings(ExifSubIFDDirectory exif, Photo photo) {
        // 处理光圈 FNumber
        Rational fNumberRational = exif.getRational(ExifSubIFDDirectory.TAG_FNUMBER);
        if (fNumberRational != null) {
            double fNumber = fNumberRational.doubleValue();
            photo.setAperture(String.format("F%.1f", fNumber));
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
        log.info("完成1000K压缩");
        
        // 基于1000K临时目录的图片压缩到100K目录
        thumbnailPhotosFromFolderToFolder(temp1000KDir, thumbnail100KDir, 100, overwrite);
        log.info("完成100K压缩");
        
        // 将1000K临时目录的图片复制到1000K目录
        FileUtils.copyFiles(temp1000KDir, thumbnail1000KDir);
        log.info("完成1000K图片复制");
        
        // 复制到原图目录
        FileUtils.copyFiles(tempDir, fullSizeDir);
        log.info("完成原图复制");
    }
}
