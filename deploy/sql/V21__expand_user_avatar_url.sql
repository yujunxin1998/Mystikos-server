-- MinIO 预签名 URL 包含较长的查询参数，VARCHAR(255) 无法可靠保存。
ALTER TABLE identity_user
    ALTER COLUMN avatar_url TYPE TEXT;

COMMENT ON COLUMN identity_user.avatar_url IS '头像地址（对象存储预签名 URL），先经 /api/v1/files/upload 上传拿到 URL';
