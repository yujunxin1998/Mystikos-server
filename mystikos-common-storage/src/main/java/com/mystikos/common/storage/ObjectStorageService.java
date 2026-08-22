package com.mystikos.common.storage;

import java.io.InputStream;
import java.time.Duration;

/**
 * 对象存储端口（Port）。各限界上下文只依赖这个接口，不直接依赖 MinIO SDK——
 * 换存储后端（阿里云 OSS/腾讯云 COS/AWS S3）时只需要新增一个实现类，业务代码不用改。
 */
public interface ObjectStorageService {

    /**
     * 上传对象。
     *
     * @param objectKey   完整对象键（含目录前缀，如 "avatar/2026/08/xxx.png"），调用方自己决定命名策略
     * @param content     文件内容流，方法内部会读完但不负责关闭（调用方负责，如 try-with-resources）
     * @param size        内容字节数，未知时可传 -1（MinIO 会走分片上传，但已知大小时更高效）
     * @param contentType MIME 类型，未知时传 "application/octet-stream"
     */
    void upload(String objectKey, InputStream content, long size, String contentType);

    /**
     * 生成一个限时有效的预签名下载链接，客户端拿这个链接直接访问对象存储，不经过应用服务器中转。
     */
    String presignedDownloadUrl(String objectKey, Duration ttl);

    /**
     * 删除对象。对象不存在时不报错（幂等）。
     */
    void delete(String objectKey);
}
