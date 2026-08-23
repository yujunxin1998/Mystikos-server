-- 给固定 6 个角色里除 GUEST 外的每一个角色补一个可登录的测试账号，方便联调时
-- 不用每次都手写注册流程。GUEST 故意不建——它是未登录/匿名访问的默认权限集合，
-- 不是落库的角色分配（见 Role 枚举上的注释），没有对应的"账号"概念。
-- ADMIN 已经有 V4 的超级管理员（administrator@mystikos.local），这里不重复建。
--
-- 密码统一是 Test@123456，BCrypt 哈希（strength 10，Spring Security BCryptPasswordEncoder
-- 默认）用项目实际依赖的 spring-security-crypto 生成并验证过 matches() 通过，不是编的字符串。
-- 跟 V4 一样：这是本地开发/联调用的种子数据，上线前必须删除或改密码。
INSERT INTO identity_user (id, email, password_hash, nickname, privacy_anonymous, status, created_at) VALUES
    (2, 'member@mystikos.local', '$2a$10$kORc.uSw0lituVY7XkBu4ez3fFkco2YnGsgCKMtebKi2d34Kt087.', '会员测试账号', false, 'ACTIVE', now()),
    (3, 'companion@mystikos.local', '$2a$10$kORc.uSw0lituVY7XkBu4ez3fFkco2YnGsgCKMtebKi2d34Kt087.', '陪玩测试账号', false, 'ACTIVE', now()),
    (4, 'customerservice@mystikos.local', '$2a$10$kORc.uSw0lituVY7XkBu4ez3fFkco2YnGsgCKMtebKi2d34Kt087.', '客服测试账号', false, 'ACTIVE', now()),
    (5, 'assessor@mystikos.local', '$2a$10$kORc.uSw0lituVY7XkBu4ez3fFkco2YnGsgCKMtebKi2d34Kt087.', '考核官测试账号', false, 'ACTIVE', now())
ON CONFLICT (email) DO NOTHING;

INSERT INTO identity_user_role (user_id, role) VALUES
    (2, 'MEMBER'),
    (3, 'COMPANION'),
    (4, 'CUSTOMER_SERVICE'),
    (5, 'ASSESSOR')
ON CONFLICT DO NOTHING;
