package com.petcare.system.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 对象存储服务接口。
 * <p>
 * 当前使用本地文件存储（{@code LocalOssServiceImpl}）。
 * 接入云OSS时，创建 {@code AliyunOssService} / {@code TencentCosService} 实现此接口即可切换。
 * <p>
 * 配置示例（阿里云 OSS）：
 * <pre>
 * oss:
 *   provider: aliyun
 *   endpoint: ${OSS_ENDPOINT}
 *   access-key-id: ${OSS_ACCESS_KEY_ID}
 *   access-key-secret: ${OSS_ACCESS_KEY_SECRET}
 *   bucket: petcare-uploads
 * </pre>
 */
public interface OssService {

    /**
     * 上传文件。
     *
     * @param file   上传文件
     * @param folder 存储目录（如 avatar / certificate / consultation）
     * @return 可访问的URL
     */
    String upload(MultipartFile file, String folder);

    /**
     * 删除文件。
     *
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    boolean delete(String fileUrl);

    /**
     * 生成预签名URL（私有Bucket场景）。
     *
     * @param fileUrl       文件URL
     * @param expireSeconds 过期秒数
     * @return 预签名URL
     */
    String getPresignedUrl(String fileUrl, long expireSeconds);
}
