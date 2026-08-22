-- Role 之前只是 Java 枚举（代码层面的固定集合），identity_user_role/identity_role_permission
-- 两张表的 role 列一直是裸字符串，没有外键约束防止插入野编码。
-- 这里把 Role 枚举镜像成一张表：Java 枚举仍然是行为/展示名的权威来源
-- （见 com.mystikos.identity.domain.model.Role），这张表只用来提供外键完整性，
-- 不是说角色从此可以在运行时任意增删——新增角色仍然要改枚举 + 加一行种子数据，两边保持同步。
CREATE TABLE identity_role (
    code          VARCHAR(32) PRIMARY KEY,
    display_name  VARCHAR(64) NOT NULL,
    sort_order    INT NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO identity_role (code, display_name, sort_order) VALUES
    ('GUEST', '游客', 0),
    ('MEMBER', '会员用户', 1),
    ('COMPANION', '陪玩', 2),
    ('CUSTOMER_SERVICE', '客服', 3),
    ('ASSESSOR', '考核官', 4),
    ('ADMIN', '管理员', 5)
ON CONFLICT (code) DO NOTHING;

-- 现有的两张关联表补外键，防止以后哪里手滑插入一个不存在的角色编码
ALTER TABLE identity_user_role
    ADD CONSTRAINT identity_user_role_role_fk FOREIGN KEY (role) REFERENCES identity_role (code);

ALTER TABLE identity_role_permission
    ADD CONSTRAINT identity_role_permission_role_fk FOREIGN KEY (role) REFERENCES identity_role (code);
