package com.mystikos.common.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO 连接参数，对应 application.yml 的 {@code mystikos.oss.minio.*}。
 * 本地开发默认值指向本机常见的 MinIO Docker 起法（端口 9000），生产环境必须
 * 通过环境变量覆盖 access-key/secret-key（不能用默认的 minioadmin/minioadmin）。
 */
@Data
@ConfigurationProperties(prefix = "mystikos.oss.minio")
public class MinioProperties {

    /**
     * MinIO 服务地址，如 http://localhost:9000。
     */
    private String endpoint = "http://localhost:9000";

    private String accessKey = "minioadmin";

    private String secretKey = "minioadmin";

    /**
     * 存储桶名称，启动时如果不存在会自动创建。
     */
    private String bucket = "mystikos";

    /**
     * 预签名下载链接的默认有效期。
     */
    private int presignedUrlTtlMinutes = 15;
}
