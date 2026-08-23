-- 头像持久化只保存对象存储键；临时预签名 URL 不属于持久化数据。
-- 按产品决定不兼容旧头像数据，升级后用户需要重新上传头像。
ALTER TABLE identity_user
    DROP COLUMN avatar_url,
    ADD COLUMN avatar_object_key TEXT;

COMMENT ON COLUMN identity_user.avatar_object_key IS '头像对象存储键，展示 URL 在查询资料时动态生成';
