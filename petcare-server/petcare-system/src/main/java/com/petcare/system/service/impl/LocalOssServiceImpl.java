package com.petcare.system.service.impl;

import com.petcare.system.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 本地文件存储服务（开发/单机部署默认实现）。
 * <p>
 * 生产环境建议切换为 OSS（阿里云/腾讯云）实现。
 *
 * @see OssService
 */
@Slf4j
@Service
@ConditionalOnMissingBean(value = OssService.class, ignored = LocalOssServiceImpl.class)
public class LocalOssServiceImpl implements OssService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Override
    public String upload(MultipartFile file, String folder) {
        try {
            Path dir = Paths.get(uploadDir, folder);
            Files.createDirectories(dir);

            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + ext;
            Path targetPath = dir.resolve(filename);

            file.transferTo(targetPath.toFile());
            String url = "/uploads/" + folder + "/" + filename;
            log.info("文件上传成功: {} -> {}", originalFilename, url);
            return url;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String fileUrl) {
        try {
            // 从 URL 中提取相对路径: /uploads/xxx/yyy.png -> ./uploads/xxx/yyy.png
            String relativePath = fileUrl.replaceFirst("^/uploads/", "");
            Path filePath = Paths.get(uploadDir, relativePath);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("文件删除失败: {}", fileUrl, e);
            return false;
        }
    }

    @Override
    public String getPresignedUrl(String fileUrl, long expireSeconds) {
        // 本地存储直接返回 URL（无权限控制）
        return fileUrl;
    }
}
