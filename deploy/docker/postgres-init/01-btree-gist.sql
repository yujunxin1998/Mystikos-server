-- 只在数据目录第一次初始化（initdb）时由官方镜像自动执行一次。
-- booking_order 的 EXCLUDE USING gist 防重叠约束依赖这个扩展，见 deploy/sql/V1。
CREATE EXTENSION IF NOT EXISTS btree_gist;
