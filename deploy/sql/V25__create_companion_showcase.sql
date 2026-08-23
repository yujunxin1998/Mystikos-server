-- 陪玩名片/展示页：陪玩自己维护、老板可浏览的展示页（照片/游戏tag/精彩视频/语音/自我介绍）。
-- 编辑一次生成一条 revision 记录（保留历史），提交后进入待审核，管理员审核通过才会更新老板端
-- 看到的已发布内容，见 CompanionShowcaseRevision/CompanionShowcase 类注释。
CREATE TABLE identity_companion_showcase_revision (
    id            BIGINT PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES identity_user (id),
    bio           VARCHAR(300),
    status        VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    reviewer_id   BIGINT REFERENCES identity_user (id),
    review_comment VARCHAR(500),
    reviewed_at   TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_identity_companion_showcase_revision_user_id
    ON identity_companion_showcase_revision (user_id, created_at DESC);
CREATE INDEX idx_identity_companion_showcase_revision_status
    ON identity_companion_showcase_revision (status);

COMMENT ON TABLE identity_companion_showcase_revision IS '陪玩名片草稿/历史提交记录，同一用户可有多条历史（每次编辑/提交生成一条）';
COMMENT ON COLUMN identity_companion_showcase_revision.status IS '状态：DRAFT/PENDING_REVIEW/APPROVED/REJECTED';
COMMENT ON COLUMN identity_companion_showcase_revision.reviewer_id IS '审核人，引用 identity_user.id';

-- 名片选中的游戏类型标签，多对多，跟 identity_companion_application_tag 一样是简单联合主键关联表。
CREATE TABLE identity_companion_showcase_revision_tag (
    revision_id BIGINT NOT NULL REFERENCES identity_companion_showcase_revision (id),
    tag_id      BIGINT NOT NULL REFERENCES identity_tag_definition (id),
    PRIMARY KEY (revision_id, tag_id)
);

COMMENT ON TABLE identity_companion_showcase_revision_tag IS '名片选中的游戏类型标签，多对多关联表';

-- 名片媒体（照片/视频/语音），一个 revision 下可以有多条，用 sort_order 保持陪玩排的展示顺序。
CREATE TABLE identity_companion_showcase_revision_media (
    id          BIGINT PRIMARY KEY,
    revision_id BIGINT NOT NULL REFERENCES identity_companion_showcase_revision (id),
    media_type  VARCHAR(16) NOT NULL,
    object_key  TEXT NOT NULL,
    sort_order  INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_identity_companion_showcase_revision_media_revision_id
    ON identity_companion_showcase_revision_media (revision_id);

COMMENT ON TABLE identity_companion_showcase_revision_media IS '名片媒体文件，只存对象存储键；media_type：PHOTO/VIDEO/AUDIO';

-- 已发布台账：只存指向当前生效 revision 的指针，不复制内容，避免和历史记录重复维护。
-- 老板端读取名片时永远只看这张表指向的 revision，草稿/待审内容不会被提前看到。
CREATE TABLE identity_companion_showcase (
    user_id              BIGINT PRIMARY KEY REFERENCES identity_user (id),
    published_revision_id BIGINT REFERENCES identity_companion_showcase_revision (id),
    published_at         TIMESTAMPTZ
);

COMMENT ON TABLE identity_companion_showcase IS '陪玩名片已发布台账，一对一挂在 user_id 上，只存当前生效 revision 的指针';
