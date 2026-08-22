-- 验证码改存 Redis（见 mystikos-identity 的 RedisVerificationCodeRepositoryImpl），
-- 天生一次性、短生命周期的数据用 TTL 存更合适，不用维护"过期清理"任务。
-- Postgres 里这张表不再写入，直接删掉。
DROP TABLE IF EXISTS identity_verification_code;
