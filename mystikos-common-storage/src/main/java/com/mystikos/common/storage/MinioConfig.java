package com.mystikos.common.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 应用启动时检查目标 bucket 是否存在，不存在就自动建——本地/测试环境免去手动
 * 用 mc 客户端建桶的步骤；生产环境的桶通常由运维预先建好并配置好生命周期/权限策略，
 * 这里的自动建桶逻辑是幂等的（已存在就跳过），不会覆盖已有配置。
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    @Bean("minioClient")
    @Primary
    public MinioClient minioClient(MinioProperties properties) throws Exception {
        MinioClient client = MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();

        boolean exists = client.bucketExists(BucketExistsArgs.builder()
                .bucket(properties.getBucket())
                .build());
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder()
                    .bucket(properties.getBucket())
                    .build());
            log.info("MinIO bucket 不存在，已自动创建：{}", properties.getBucket());
        }
        return client;
    }

    /**
     * 只用于离线计算预签名 URL，不会通过公网地址执行上传或 bucket 检查。
     */
    @Bean("minioPresignClient")
    public MinioClient minioPresignClient(MinioProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.effectivePublicEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
