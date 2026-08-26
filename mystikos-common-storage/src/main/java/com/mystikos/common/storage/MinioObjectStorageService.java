package com.mystikos.common.storage;

import com.mystikos.common.result.ResponseCode;
import com.mystikos.common.web.exception.BusinessException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Http;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Duration;

@Slf4j
@Service
public class MinioObjectStorageService implements ObjectStorageService {

    private final MinioClient minioClient;
    private final MinioClient minioPresignClient;
    private final MinioProperties properties;

    public MinioObjectStorageService(
            @Qualifier("minioClient") MinioClient minioClient,
            @Qualifier("minioPresignClient") MinioClient minioPresignClient,
            MinioProperties properties) {
        this.minioClient = minioClient;
        this.minioPresignClient = minioPresignClient;
        this.properties = properties;
    }

    @Override
    public void upload(String objectKey, InputStream content, long size, String contentType) {
        long partSize = size > 0 ? -1L : 10485760L;
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .stream(content, size, partSize)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            log.error("MinIO 上传失败，objectKey={}", objectKey, e);
            throw new BusinessException(ResponseCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public String presignedDownloadUrl(String objectKey, Duration ttl) {
        try {
            return minioPresignClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Http.Method.GET)
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .expiry((int) ttl.toSeconds())
                    .build());
        } catch (Exception e) {
            log.error("MinIO 生成预签名链接失败，objectKey={}", objectKey, e);
            throw new BusinessException(ResponseCode.FILE_NOT_FOUND);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            log.error("MinIO 删除失败，objectKey={}", objectKey, e);
            throw new BusinessException(ResponseCode.FILE_UPLOAD_FAILED);
        }
    }
}
