-- 老板资料细化：性别/头像/生日/个性签名/地区。
-- gender 三态（MALE/FEMALE/UNDISCLOSED），自报，不做强制二选一。
-- birth_date 只是资料展示字段，不作为年龄/未成年人判定依据——
-- 那属于独立的实名认证流程（PRD 缺口，见 docs/architecture/prd-alignment.md 第3节），
-- 两者故意不互相赋值，避免自报生日被误当成年龄证明。
ALTER TABLE identity_user
    ADD COLUMN gender      VARCHAR(16) NOT NULL DEFAULT 'UNDISCLOSED',
    ADD COLUMN avatar_url  VARCHAR(255),
    ADD COLUMN birth_date  DATE,
    ADD COLUMN bio         VARCHAR(255),
    ADD COLUMN region      VARCHAR(64);

COMMENT ON COLUMN identity_user.gender IS '性别：MALE/FEMALE/UNDISCLOSED，自报展示用，默认 UNDISCLOSED';
COMMENT ON COLUMN identity_user.avatar_url IS '头像地址（对象存储 URL），先经 /api/v1/files/upload 上传拿到 URL';
COMMENT ON COLUMN identity_user.birth_date IS '生日，仅作资料展示，不作为年龄/实名认证依据';
COMMENT ON COLUMN identity_user.bio IS '个性签名';
COMMENT ON COLUMN identity_user.region IS '所在地区，自由文本';
