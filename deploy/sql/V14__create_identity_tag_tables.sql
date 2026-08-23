-- 标签目录（游戏类型等），后台配置、前台多选。category 是自由字符串，
-- 不预置枚举，跟 identity_role_permission 的 permission_code 同一个思路。
CREATE TABLE identity_tag_definition (
    id          BIGINT PRIMARY KEY,
    category    VARCHAR(32)  NOT NULL,
    label       VARCHAR(32)  NOT NULL,
    sort_order  INT          NOT NULL DEFAULT 0,
    enabled     BOOLEAN      NOT NULL DEFAULT true,

    CONSTRAINT identity_tag_definition_category_label_unique UNIQUE (category, label)
);

CREATE INDEX idx_identity_tag_definition_category ON identity_tag_definition (category, sort_order);

COMMENT ON TABLE identity_tag_definition IS '标签目录（游戏类型等），后台配置项';
COMMENT ON COLUMN identity_tag_definition.id IS '主键ID';
COMMENT ON COLUMN identity_tag_definition.category IS '标签分类，如 GAME_TYPE，自由字符串';
COMMENT ON COLUMN identity_tag_definition.label IS '标签展示名';
COMMENT ON COLUMN identity_tag_definition.sort_order IS '排序权重，越小越靠前';
COMMENT ON COLUMN identity_tag_definition.enabled IS '是否启用，停用后不出现在前台多选列表';

-- 用户选中的标签，多对多。跟 identity_user_role 一样是简单联合主键关联表。
CREATE TABLE identity_user_tag (
    user_id BIGINT NOT NULL REFERENCES identity_user (id),
    tag_id  BIGINT NOT NULL REFERENCES identity_tag_definition (id),
    PRIMARY KEY (user_id, tag_id)
);

COMMENT ON TABLE identity_user_tag IS '用户已选中的标签，多对多关联表';
COMMENT ON COLUMN identity_user_tag.user_id IS '用户ID';
COMMENT ON COLUMN identity_user_tag.tag_id IS '标签ID';

-- 占位种子数据：游戏类型标签示例，业务确定真实清单后自行通过 POST /api/v1/tags 增补/调整。
INSERT INTO identity_tag_definition (id, category, label, sort_order, enabled) VALUES
    (1, 'GAME_TYPE', '王者荣耀', 1, true),
    (2, 'GAME_TYPE', '英雄联盟', 2, true),
    (3, 'GAME_TYPE', '和平精英', 3, true),
    (4, 'GAME_TYPE', '原神', 4, true),
    (5, 'GAME_TYPE', 'CS2', 5, true),
    (6, 'GAME_TYPE', '其他', 99, true)
ON CONFLICT (category, label) DO NOTHING;
