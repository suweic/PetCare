package com.petcare.system.controller;

import com.petcare.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 文件上传控制器。
 * <p>
 * 支持医生资质证书、用户头像、宠物头像、问诊图片等上传。
 * 文件按日期分目录存储，返回相对 URL。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    /** 允许的图片类型（MIME） */
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    /** 允许的文档类型（资质证书） */
    private static final Set<String> ALLOWED_DOC_TYPES = Set.of(
            "image/jpeg", "image/png", "application/pdf");

    /** 最大文件大小：10MB */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /** 魔数字节数（用于校验文件真实类型） */
    private static final int MAGIC_BYTES_LENGTH = 8;

    /** 已知文件魔数签名映射 */
    private static final Map<String, byte[]> MAGIC_BYTES = Map.of(
            "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "image/png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A},
            "image/gif", new byte[]{0x47, 0x49, 0x46, 0x38},
            "image/webp", new byte[]{0x52, 0x49, 0x46, 0x46},
            "application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46}
    );

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    /**
     * 上传图片（通用：头像、问诊图片等）。
     * <p>Authentication 参数预留给后续权限校验使用（如：仅认证用户可上传）。</p>
     */
    @PostMapping("/image")
    public ResponseEntity<Result<Map<String, String>>> uploadImage(
            @SuppressWarnings("unused") Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        return doUpload(file, ALLOWED_IMAGE_TYPES, "images");
    }

    /**
     * 上传资质证书（医生资格证、执业证）。
     * <p>仅医生和管理员可上传资质证书，普通用户无权限。</p>
     */
    @PostMapping("/certificate")
    public ResponseEntity<Result<Map<String, String>>> uploadCertificate(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        // 权限校验：仅医生和管理员可上传资质证书
        if (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_DOCTOR")
                        || a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403)
                    .body(Result.error("无权上传资质证书，仅医生或管理员可操作"));
        }
        return doUpload(file, ALLOWED_DOC_TYPES, "certificates");
    }

    private ResponseEntity<Result<Map<String, String>>> doUpload(
            MultipartFile file, Set<String> allowedTypes, String subDir) {

        // 1. 文件为空检查
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Result.badRequest("上传文件不能为空"));
        }

        // 2. 文件大小检查
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest()
                    .body(Result.badRequest("文件大小不能超过 10MB"));
        }

        // 3. 文件类型检查（Content-Type）
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            return ResponseEntity.badRequest()
                    .body(Result.badRequest("不支持的文件类型: " + contentType
                            + "，允许: " + String.join(", ", allowedTypes)));
        }

        // 4. 检查文件扩展名（双重校验 + 路径穿越防护）
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.contains("..")) {
            return ResponseEntity.badRequest()
                    .body(Result.badRequest("文件名不合法"));
        }

        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalName.substring(dotIndex).toLowerCase();
        }

        Set<String> allowedExts = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".pdf");
        if (!extension.isEmpty() && !allowedExts.contains(extension)) {
            return ResponseEntity.badRequest()
                    .body(Result.badRequest("不支持的文件扩展名: " + extension));
        }

        // 5. 魔数校验：读取文件头字节，验证真实类型与声明类型一致
        try (InputStream inputStream = file.getInputStream()) {
            byte[] magicBytes = new byte[MAGIC_BYTES_LENGTH];
            int bytesRead = inputStream.read(magicBytes, 0, MAGIC_BYTES_LENGTH);

            if (bytesRead > 0) {
                boolean magicMatch = false;
                String detectedType = null;
                for (Map.Entry<String, byte[]> entry : MAGIC_BYTES.entrySet()) {
                    byte[] signature = entry.getValue();
                    if (bytesRead >= signature.length && startsWith(magicBytes, signature)) {
                        magicMatch = true;
                        detectedType = entry.getKey();
                        break;
                    }
                }
                // 魔数匹配的类型必须与声明的 Content-Type 一致
                if (magicMatch) {
                    if (!contentType.equals(detectedType)) {
                        log.warn("文件类型不匹配: claimed={}, actual={}, originalName={}",
                                contentType, detectedType, originalName);
                        return ResponseEntity.badRequest()
                                .body(Result.badRequest("文件内容与声明的类型不匹配，上传被拒绝"));
                    }
                }
                if (!magicMatch) {
                    log.warn("文件魔数校验失败: claimed={}, originalName={}", contentType, originalName);
                    return ResponseEntity.badRequest()
                            .body(Result.badRequest("文件内容与声明的类型不匹配，上传被拒绝"));
                }
            }
        } catch (IOException e) {
            log.error("读取文件魔数失败", e);
            return ResponseEntity.badRequest()
                    .body(Result.badRequest("无法读取文件内容"));
        }

        // 6. 生成唯一文件名并存储（使用独立的 InputStream）
        try (InputStream fileStream = file.getInputStream()) {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String storedName = uuid + (extension.isEmpty() ? "" : extension);
            String relativePath = "/" + subDir + "/" + dateDir + "/" + storedName;

            Path targetPath = Paths.get(uploadPath, subDir, dateDir);
            Files.createDirectories(targetPath);
            Files.copy(fileStream, targetPath.resolve(storedName),
                    StandardCopyOption.REPLACE_EXISTING);

            Map<String, String> result = new LinkedHashMap<>();
            result.put("url", relativePath);
            result.put("originalName", originalName);
            result.put("size", String.valueOf(file.getSize()));

            log.info("文件上传成功: path={}, originalName={}, size={}",
                    relativePath, originalName, file.getSize());

            return ResponseEntity.ok(Result.success("上传成功", result));
        } catch (IOException e) {
            log.error("文件存储失败", e);
            return ResponseEntity.internalServerError()
                    .body(Result.error("文件上传失败，请重试"));
        }
    }

    /** 检查字节数组是否以指定签名开头 */
    private boolean startsWith(byte[] data, byte[] signature) {
        if (data.length < signature.length) return false;
        for (int i = 0; i < signature.length; i++) {
            if (data[i] != signature[i]) return false;
        }
        return true;
    }
}
