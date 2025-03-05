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
import org.springframework.web.multipart.MultipartFile;

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
public class PhotoProcessingService {

    
    @Autowired
    PhotoService photoService;
    @Value("${qingdai.defaultAuthor}")
    private String defaultAuthor;

    // 雪花算法生成器
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);

    // 处理指定文件夹中的图片文件，返回一个包含处理后照片信息的 Photo 对象列表
    public List<Photo> getPhotosByFolder(File folder) {
        return Arrays.stream(FileUtils.getImageFiles(folder))  // 返回 Stream<File>
                .parallel()  // 并行处理
                .map(this::getPhotoObjectByFile)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 处理上传的文件并返回Photo对象列表
    public List<Photo> getPhotosByMultipartFiles(MultipartFile[] files) {
        return Arrays.stream(files)
                .parallel()
                .map(this::getPhotoObjectByMultipartFile)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 压缩文件夹内所有图片到指定目录
    public void compressPhotosFromFolderToFolder(File srcDir, File thumbnailDir, int maxSizeKB, boolean overwrite) {
        Arrays.stream(Objects.requireNonNull(srcDir.listFiles()))
                .parallel()
                .filter(this::isSupportedPhoto)
                .forEach(file -> compressPhoto(file, thumbnailDir, maxSizeKB, overwrite));
    }

    public void compressPhotosFromMultipartFileToFolder(MultipartFile[] photos,
                                                        File pendingDir,
                                                        File thumbnailDir,
                                                        int maxSizeKB,
                                                        boolean overwrite) throws IOException {
        // 创建随机名称的临时目录
        File tempDir = FileUtils.createTempDir(pendingDir);

        //保存所有图片到临时目录
        Arrays.stream(photos)
                .parallel()
                .forEach(photo -> {
                    File thumbnailFile = new File(tempDir, photo.getOriginalFilename());
                    try {
                        photo.transferTo(thumbnailFile);
                    } catch (IOException e) {
                        throw new RuntimeException("无法保存文件到临时目录: " + thumbnailFile.getAbsolutePath(), e);
                    }
                });

        // 调用压缩方法处理临时目录中的文件
        compressPhoto(tempDir, thumbnailDir, maxSizeKB, overwrite);

        // 删除临时目录及其内容
        FileUtils.deleteFolder(tempDir);
    }


    // 根据照片ID获取文件名
    public String getFileNameById(Long photoId) {
        Photo photo = photoService.getById(photoId);
        if (photo == null) {
            return null;
        }
        return photo.getFileName();
    }

    // 判断是否为支持的图片格式
    public boolean isSupportedPhoto(File file) {
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
    private void compressPhoto(File fullSizeDir, File thumbnailDir, int maxSizeKB, boolean overwrite) {
        try {
            String fileName = fullSizeDir.getName();
            String formatName = FileUtils.getFileExtension(fileName).toLowerCase();
            String baseName = FileUtils.getBaseName(fileName);

            // 处理PNG转换为JPEG
            if ("png".equals(formatName)) {
                fileName = baseName + ".jpg"; // 修改后缀为jpg
                formatName = "jpg";
            }

            File thumbnailFile = new File(thumbnailDir, fileName);

            if (thumbnailFile.exists() && !overwrite) return;

            long maxSizeBytes = maxSizeKB * 1024L;
            boolean sizeMet = false;

            // 优先调整质量参数
            sizeMet = adjustPhotoQuality(fullSizeDir, thumbnailDir, formatName, maxSizeBytes);

            // 若未达标，调整尺寸
            if (!sizeMet) {
                sizeMet = scaleDownPhoto(fullSizeDir, thumbnailDir, formatName, maxSizeBytes);
            }

            if (!sizeMet) {
                throw new IOException("无法压缩到指定大小: " + fullSizeDir.getName());
            }
        } catch (IOException e) {
            throw new RuntimeException("文件处理失败: " + fullSizeDir.getName(), e);
        }
    }

    public Photo getPhotoObjectByMultipartFile(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();

            String formatName = FileUtils.getFileExtension(fileName).toLowerCase();
            String baseName = FileUtils.getBaseName(fileName);

            // 保存文件到临时目录
            File tempFile = File.createTempFile(baseName, "." + formatName);
            file.transferTo(tempFile);

            Photo photo = getPhotoObjectByFile(tempFile);

            // 删除临时文件
            if (!tempFile.delete()) {
                System.out.println("无法删除临时文件: " + tempFile.getAbsolutePath());
            }

            return photo;
        } catch (IOException e) {
            System.out.println("处理文件失败: " + file.getOriginalFilename() + e);
            return null;
        }
    }

    // 生成包含元数据的Photo对象
    private Photo getPhotoObjectByFile(File PhotoFile) {
        try {
            BufferedImage image = ImageIO.read(PhotoFile);
            if (image == null) return null;

            //Id 原图 作者 宽度 高度 压缩图 简介 精选
            Photo photo = buildBasicPhoto(PhotoFile, image);
            //拍摄时间 拍摄参数
            processExifData(PhotoFile, photo);

            return photo;
        } catch (IOException | ImageProcessingException e) {
            System.out.println("处理文件失败: " + PhotoFile.getName() + e);
            return null;
        }
    }

    // 循环逐步降低图片质量，直到满足文件大小要求 如果质量低于最小值仍未满足要求，则返回false
    private boolean adjustPhotoQuality(File fullSizeDir, File thumbnailDir, String formatName, long maxSizeBytes) throws IOException {
        double quality = 0.85;
        double minQuality = 0.2;
        double step = 0.05;

        while (quality >= minQuality) {
            Thumbnails.of(fullSizeDir)
                    .scale(1)
                    .outputFormat(formatName)
                    .outputQuality(quality)
                    // 处理PNG透明背景
                    .imageType(BufferedImage.TYPE_INT_RGB)
                    .toFile(thumbnailDir);

            if (thumbnailDir.length() <= maxSizeBytes) {
                return true;
            }
            quality = BigDecimal.valueOf(quality).subtract(BigDecimal.valueOf(step)).doubleValue();
        }
        return false;
    }

    //图片按比例缩小
    private boolean scaleDownPhoto(File fullSizeDir, File thumbnailDir, String formatName, long maxSizeBytes) throws IOException {
        double scale = 1.0;
        double step = 0.1;

        while (scale >= 0.1) {
            Thumbnails.of(fullSizeDir)
                    .scale(scale)
                    .outputFormat(formatName)
                    .outputQuality(0.7) // 适当质量值
                    .imageType(BufferedImage.TYPE_INT_RGB)
                    .toFile(thumbnailDir);

            if (thumbnailDir.length() <= maxSizeBytes) {
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
        }

        photo.setCamera(model);
    }

    public void deletePhotoFiles(String fullSizeUrl,String thumbnailSizeUrl,String fileName) throws IOException {
        FileUtils.deleteFile(new File(fullSizeUrl, fileName));
        FileUtils.deleteFile(new File(thumbnailSizeUrl, fileName));
    }
}