package com.mystikos.common.storage.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.common.result.ResponseCode;
import com.mystikos.common.storage.MinioProperties;
import com.mystikos.common.storage.ObjectStorageService;
import com.mystikos.common.storage.web.dto.UploadResult;
import com.mystikos.common.web.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 通用文件上传/下载入口。这里只提供"存对象、给个能取回来的 key、按 key 换预签名链接"这种
 * 最通用的能力，不区分业务语义（头像/凭证/证据……）——需要专属校验规则（文件类型、大小上限、
 * 是否要求人工审核）的业务模块可以自己在 adapter/web 层包一层，内部调用 {@link ObjectStorageService}。
 */
@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "文件与对象存储", description = "通用文件上传/预签名下载链接（基于 MinIO）")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private static final DateTimeFormatter DIR_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final ObjectStorageService objectStorageService;
    private final MinioProperties properties;

    public FileController(ObjectStorageService objectStorageService, MinioProperties properties) {
        this.objectStorageService = objectStorageService;
        this.properties = properties;
    }

    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "返回的 objectKey 用于后续查询预签名下载链接或删除")
    public APIResponse<UploadResult> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ResponseCode.FILE_EMPTY);
        }
        String objectKey = LocalDate.now().format(DIR_FORMAT) + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        try {
            objectStorageService.upload(objectKey, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (IOException e) {
            throw new BusinessException(ResponseCode.FILE_UPLOAD_FAILED);
        }
        String url = objectStorageService.presignedDownloadUrl(
                objectKey, Duration.ofMinutes(properties.getPresignedUrlTtlMinutes()));
        return APIResponse.ok(new UploadResult(objectKey, url));
    }

    @GetMapping("/url")
    @Operation(summary = "按 objectKey 获取预签名下载链接", description = "链接限时有效，配置见 mystikos.oss.minio.presigned-url-ttl-minutes")
    public APIResponse<String> presignedUrl(@Parameter(description = "上传时返回的 objectKey") @RequestParam String objectKey) {
        return APIResponse.ok(objectStorageService.presignedDownloadUrl(
                objectKey, Duration.ofMinutes(properties.getPresignedUrlTtlMinutes())));
    }

    @DeleteMapping
    @Operation(summary = "删除文件")
    public APIResponse<Void> delete(@Parameter(description = "上传时返回的 objectKey") @RequestParam String objectKey) {
        objectStorageService.delete(objectKey);
        return APIResponse.ok();
    }
}
