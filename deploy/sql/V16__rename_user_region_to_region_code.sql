-- V13 上线时用的是自由文本 region；后来决定改成引用 mystikos-common-region 的
-- 行政区划编码（见 domain-model.md），但 V13 已经在本地开发库跑过一次了——
-- Flyway 不允许悄悄改写已应用的迁移文件（会话内已经因为改了 V13 内容导致 checksum
-- 校验失败），只能像这样另开一个新迁移做重命名，不能直接回去改 V13。
ALTER TABLE identity_user RENAME COLUMN region TO region_code;

ALTER TABLE identity_user ALTER COLUMN region_code TYPE VARCHAR(10);

COMMENT ON COLUMN identity_user.region_code IS '所在地区，引用 common_region.code（国家或一级行政区编码）；不建 DB 级外键——common_region 表在 V15 才建，且这是跨模块引用，合法性由 mystikos-identity 应用层调用 RegionQueryService 校验，不是数据库约束';
