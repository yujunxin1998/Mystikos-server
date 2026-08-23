-- Spring Security 免鉴权（permitAll）路径原来硬编码在 SecurityConfig 里，
-- 改成从这张表读，应用启动组装 SecurityFilterChain 时查一次库（不是运行时热更新）。
-- 见 com.mystikos.common.security.SecurityConfig / SecurityWhitelistMapper。
-- 种子数据是原来硬编码在 SecurityConfig 里的 5 个 /api/v1/auth/** 公开接口，迁移后行为不变。
CREATE TABLE security_whitelist_path (
    id            BIGINT PRIMARY KEY,
    path_pattern  VARCHAR(255) NOT NULL,
    http_method   VARCHAR(10),
    description   VARCHAR(255),
    enabled       BOOLEAN NOT NULL DEFAULT true,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO security_whitelist_path (id, path_pattern, http_method, description, enabled) VALUES
    (1, '/api/v1/auth/verification-codes', NULL, '发送验证码', true),
    (2, '/api/v1/auth/register', NULL, '注册', true),
    (3, '/api/v1/auth/login', NULL, '登录', true),
    (4, '/api/v1/auth/refresh-token', NULL, '刷新令牌', true),
    (5, '/api/v1/auth/oauth/*/login', NULL, '第三方登录', true);
