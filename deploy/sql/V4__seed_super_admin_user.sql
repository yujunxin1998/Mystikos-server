-- 内置超级管理员，用于系统首次部署时有一个可登录的入口。
--
-- 登录标识用邮箱渠道：我们的登录模型只支持手机号/邮箱二选一（没有裸用户名概念，
-- 见 V3 迁移），administrator@mystikos.local 是内部占位地址，不是真实可达邮箱，
-- 只用来登录，不会真的收邮件。
--
-- 密码 Mystikos@123 的 BCrypt 哈希（strength 10，Spring Security BCryptPasswordEncoder 默认），
-- 用项目实际依赖的 spring-security-crypto 生成并验证过 matches() 通过，不是随手编的字符串。
-- 上线前必须改密码/直接换成从密钥管理服务下发的初始密码，这里只是本地开发/联调用的种子数据。
INSERT INTO identity_user (id, email, password_hash, nickname, privacy_anonymous, status, created_at)
VALUES (
    1,
    'administrator@mystikos.local',
    '$2a$10$.sIFVYoV58.JuSrgv5b2ueFFjMsZ8cl5VsrhYouHp/9DW5T1sdG/.',
    '超级管理员',
    false,
    'ACTIVE',
    now()
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO identity_user_role (user_id, role)
VALUES (1, 'ADMIN')
ON CONFLICT DO NOTHING;
