-- 注册渠道从"用户名"改成"手机号或邮箱二选一"，username 不再需要
ALTER TABLE identity_user
    DROP CONSTRAINT identity_user_username_unique,
    DROP COLUMN username,
    ADD COLUMN nickname VARCHAR(64),
    ADD COLUMN privacy_anonymous BOOLEAN NOT NULL DEFAULT false;

-- phone/email 允许为空（二选一），但非空值必须全局唯一。
-- PostgreSQL 的 UNIQUE 约束天然允许多个 NULL 共存，不会互相冲突。
ALTER TABLE identity_user
    ADD CONSTRAINT identity_user_phone_unique UNIQUE (phone),
    ADD CONSTRAINT identity_user_email_unique UNIQUE (email);

-- 第三方登录绑定：一个用户可以绑多个 provider，同一 provider 的同一账号只能绑一个用户
CREATE TABLE identity_oauth_binding (
    id                BIGINT PRIMARY KEY,
    user_id           BIGINT NOT NULL REFERENCES identity_user (id),
    provider          VARCHAR(32) NOT NULL,
    provider_user_id  VARCHAR(128) NOT NULL,
    bound_at          TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT identity_oauth_binding_unique UNIQUE (provider, provider_user_id)
);

-- 验证码：一次性，5 分钟有效期（应用层控制），consumed_at 非空即失效
CREATE TABLE identity_verification_code (
    id           BIGINT PRIMARY KEY,
    channel      VARCHAR(16) NOT NULL,
    identifier   VARCHAR(128) NOT NULL,
    purpose      VARCHAR(32) NOT NULL,
    code         VARCHAR(8) NOT NULL,
    expires_at   TIMESTAMPTZ NOT NULL,
    consumed_at  TIMESTAMPTZ
);

CREATE INDEX idx_identity_verification_code_lookup
    ON identity_verification_code (channel, identifier, purpose, id DESC);

-- Refresh Token：只存哈希，可主动吊销（登出/换设备），不是无状态 JWT
CREATE TABLE identity_refresh_token (
    id          BIGINT PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES identity_user (id),
    token_hash  VARCHAR(128) NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked_at  TIMESTAMPTZ,

    CONSTRAINT identity_refresh_token_hash_unique UNIQUE (token_hash)
);

CREATE INDEX idx_identity_refresh_token_user ON identity_refresh_token (user_id);
