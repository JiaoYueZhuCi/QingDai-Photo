package com.qingdai.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.qingdai.entity.Photo;
import com.qingdai.utils.DateUtils;
import com.qingdai.utils.FileUtils;
import com.qingdai.utils.SnowflakeIdGenerator;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.velocity.shaded.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImageProcessingService {
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
            String baseName = FilenameUtils.getBaseName(fileName);

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

    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);

    public List<Photo> processImageFolder(File folder) {
        return Arrays.stream(FileUtils.getImageFiles(folder))  // 返回 Stream<File>
                .parallel()  // 并行处理
                .map(this::processSingleImage)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    private Photo processSingleImage(File imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) return null;

            Photo photo = buildBasicPhoto(imageFile, image);
            processExifData(imageFile, photo);
            return photo;
        } catch (IOException | ImageProcessingException e) {
            System.out.println("处理文件失败: " + imageFile.getName() + e);
            return null;
        }
    }

    private Photo buildBasicPhoto(File file, BufferedImage image) {
        Photo photo = new Photo();
        photo.setId(idGenerator.nextId());
        photo.setFullSize(file.getName());
        photo.setAuthor(defaultAuthor);
        photo.setWidth(image.getWidth());
        photo.setHeight(image.getHeight());
        photo.setThumbnail(null);
        photo.setIntroduce(null);
        photo.setStart(0);
        return photo;
    }

    private void processExifData(File imageFile, Photo photo) throws ImageProcessingException, IOException {
        Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
        ExifSubIFDDirectory exif = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

        if (exif == null) return;

        processShootingTime(exif, photo);
        processCameraSettings(exif, photo);
    }

    private void processShootingTime(ExifSubIFDDirectory exif, Photo photo) {
        Optional.ofNullable(exif.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL))
                .ifPresent(date -> photo.setTime(DateUtils.formatDateTime(date)));
    }

    private void processCameraSettings(ExifSubIFDDirectory exif, Photo photo) {
        List<String> params = new ArrayList<>();
        addExifParam(exif, ExifSubIFDDirectory.TAG_FNUMBER, "光圈", params);
        addExifParam(exif, ExifSubIFDDirectory.TAG_EXPOSURE_TIME, "快门", params);
        addExifParam(exif, ExifSubIFDDirectory.TAG_ISO_EQUIVALENT, "ISO", params);

        if (!params.isEmpty()) {
            photo.setData(String.join(" ", params));
        }
    }

    private void addExifParam(ExifSubIFDDirectory exif, int tag, String prefix, List<String> collector) {
        Optional.ofNullable(exif.getDescription(tag))
                .filter(StringUtils::isNotBlank)
                .ifPresent(value -> collector.add(prefix + ":" + value));
    }
}

