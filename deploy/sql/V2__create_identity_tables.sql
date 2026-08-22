CREATE TABLE identity_user (
    id                      BIGINT PRIMARY KEY,
    username                VARCHAR(64) NOT NULL,
    phone                   VARCHAR(32),
    email                   VARCHAR(128),
    password_hash           VARCHAR(255),
    status                  VARCHAR(16) NOT NULL,
    membership_tier_level   INT,
    membership_tier_code    VARCHAR(32),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT identity_user_username_unique UNIQUE (username)
);

-- 一个用户可以同时拥有多个角色（比如既是会员又是陪玩），用关联表而不是单列。
CREATE TABLE identity_user_role (
    user_id BIGINT NOT NULL REFERENCES identity_user (id),
    role    VARCHAR(32) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- 权限编码由业务后续定义，这里只建结构，不预置任何角色-权限映射
-- （跟会员等级"留空"是同一个思路：不在框架里编业务规则）。
CREATE TABLE identity_role_permission (
    role            VARCHAR(32) NOT NULL,
    permission_code VARCHAR(64) NOT NULL,
    PRIMARY KEY (role, permission_code)
);
