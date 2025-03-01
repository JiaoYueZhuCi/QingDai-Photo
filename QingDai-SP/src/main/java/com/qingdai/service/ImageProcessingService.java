package com.qingdai.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.Rational;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.qingdai.entity.Photo;
import com.qingdai.utils.DateUtils;
import com.qingdai.utils.FileUtils;
import com.qingdai.utils.SnowflakeIdGenerator;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ImageProcessingService {
    @Autowired
    PhotoService photoService;
    @Value("${qingdai.defaultAuthor}")
    private String defaultAuthor;

    // 压缩文件到指定目录
    public void processBatch(File srcDir, File destDir, int maxSizeKB, boolean overwrite) {
        Arrays.stream(Objects.requireNonNull(srcDir.listFiles()))
                .parallel()
                .filter(this::isSupportedImage)
                .forEach(file -> processSingleImage(file, destDir, maxSizeKB, overwrite));
    }

    // 判断是否为支持的图片格式
    private boolean isSupportedImage(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg")
                || name.endsWith(".png") || name.endsWith(".webp");
    }

    /*获取文件名、格式和基础名称。
    将PNG格式转换为JPEG格式。
    检查目标文件是否存在且是否允许覆盖。
    计算最大字节数。
    优先调整图片质量以满足大小要求。
    如果调整质量不达标，则尝试缩小图片尺寸。
    如果仍不达标，抛出异常。*/
    private void processSingleImage(File srcFile, File destDir, int maxSizeKB, boolean overwrite) {
        try {
            String fileName = srcFile.getName();
            String formatName = FileUtils.getFileExtension(fileName).toLowerCase();
            String baseName = FileUtils.getBaseName(fileName);

            // 处理PNG转换为JPEG
            if ("png".equals(formatName)) {
                fileName = baseName + ".jpg"; // 修改后缀为jpg
                formatName = "jpg";
            }

            File destFile = new File(destDir, fileName);

            if (destFile.exists() && !overwrite) return;

            long maxSizeBytes = maxSizeKB * 1024L;
            boolean sizeMet = false;

            // 优先调整质量参数
            sizeMet = adjustQuality(srcFile, destFile, formatName, maxSizeBytes);

            // 若未达标，调整尺寸
            if (!sizeMet) {
                sizeMet = scaleDownImage(srcFile, destFile, formatName, maxSizeBytes);
            }

            if (!sizeMet) {
                throw new IOException("无法压缩到指定大小: " + srcFile.getName());
            }
        } catch (IOException e) {
            throw new RuntimeException("文件处理失败: " + srcFile.getName(), e);
        }
    }

    // 循环逐步降低图片质量，直到满足文件大小要求 如果质量低于最小值仍未满足要求，则返回false
    private boolean adjustQuality(File srcFile, File destFile, String formatName, long maxSizeBytes) throws IOException {
        double quality = 0.85;
        double minQuality = 0.2;
        double step = 0.05;

        while (quality >= minQuality) {
            Thumbnails.of(srcFile)
                    .scale(1)
                    .outputFormat(formatName)
                    .outputQuality(quality)
                    // 处理PNG透明背景
                    .imageType(BufferedImage.TYPE_INT_RGB)
                    .toFile(destFile);

            if (destFile.length() <= maxSizeBytes) {
                return true;
            }
            quality = BigDecimal.valueOf(quality).subtract(BigDecimal.valueOf(step)).doubleValue();
        }
        return false;
    }

    //图片按比例缩小
    private boolean scaleDownImage(File srcFile, File destFile, String formatName, long maxSizeBytes) throws IOException {
        double scale = 1.0;
        double step = 0.1;

        while (scale >= 0.1) {
            Thumbnails.of(srcFile)
                    .scale(scale)
                    .outputFormat(formatName)
                    .outputQuality(0.7) // 适当质量值
                    .imageType(BufferedImage.TYPE_INT_RGB)
                    .toFile(destFile);

            if (destFile.length() <= maxSizeBytes) {
                return true;
            }
            scale -= step;
        }
        return false;
    }

    // 雪花算法生成器
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);

    // 处理指定文件夹中的图片文件，返回一个包含处理后照片信息的 Photo 对象列表
    public List<Photo> processImageFolder(File folder) {
        return Arrays.stream(FileUtils.getImageFiles(folder))  // 返回 Stream<File>
                .parallel()  // 并行处理
                .map(this::processSingleImage)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 生成包含元数据的Photo对象
    private Photo processSingleImage(File imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) return null;

            //Id 原图 作者 宽度 高度 压缩图 简介 精选
            Photo photo = buildBasicPhoto(imageFile, image);
            //拍摄时间 拍摄参数
            processExifData(imageFile, photo);

            return photo;
        } catch (IOException | ImageProcessingException e) {
            System.out.println("处理文件失败: " + imageFile.getName() + e);
            return null;
        }
    }

    // 生成Photo对象
    private Photo buildBasicPhoto(File file, BufferedImage image) {
        Photo photo = new Photo();
        photo.setId(idGenerator.nextId());
        photo.setFileName(file.getName());
        photo.setAuthor(defaultAuthor);
        photo.setWidth(image.getWidth());
        photo.setHeight(image.getHeight());
        photo.setTitle(null);
        photo.setIntroduce(null);
        photo.setStart(0);
        return photo;
    }

    // 处理EXIF数据
    private void processExifData(File imageFile, Photo photo) throws ImageProcessingException, IOException {
        Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
        ExifSubIFDDirectory exif = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

        if (exif == null) return;

        processShootingTime(exif, photo);
        processCameraSettings(exif, photo);
        processCameraModel(metadata, photo);
    }

    // 格式化拍摄时间
    private void processShootingTime(ExifSubIFDDirectory exif, Photo photo) {
        Optional.ofNullable(exif.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL))
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

    // 处理相机型号  需要getFirstDirectoryOfType(ExifIFD0Directory.class);
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
            photo.setCamera(model);
        } else {
            System.out.println("相机型号为空");
        }
    }

    //
    public String getFileNameById(Long photoId) {
        Photo photo = photoService.getById(photoId);
        if (photo == null) {
            return null;
        }
        return photo.getFileName();
    }
}

