-- 陪玩身份申请：用户提交申请，管理员线下考核后在后台修改状态、录入考核结果。
-- 考核过程本身不进系统，只有 SUBMITTED -> IN_ASSESSMENT -> APPROVED/REJECTED 这几个状态流转
-- 和最终考核结果需要落库，见 CompanionIdentityApplication 类注释。
-- 同一用户允许有多条历史申请（被拒后可以重新申请），所以主键是独立的代理ID，不是 user_id。
CREATE TABLE identity_companion_application (
    id                              BIGINT PRIMARY KEY,
    user_id                         BIGINT NOT NULL REFERENCES identity_user (id),
    real_name                       VARCHAR(64),
    gender                          VARCHAR(16),
    birth_date                      DATE,
    self_intro                      VARCHAR(500),
    game_nickname                   VARCHAR(64),
    game_rank_proof_object_key      TEXT,
    contact_country_code            VARCHAR(8),
    contact_phone                   VARCHAR(32),
    contact_email                   VARCHAR(128),
    status                          VARCHAR(16) NOT NULL DEFAULT 'SUBMITTED',
    reviewer_id                     BIGINT REFERENCES identity_user (id),
    review_result                   VARCHAR(16),
    review_comment                  VARCHAR(500),
    reviewed_at                     TIMESTAMPTZ,
    created_at                      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_identity_companion_application_user_id ON identity_companion_application (user_id, created_at DESC);

COMMENT ON TABLE identity_companion_application IS '陪玩身份申请，同一用户可有多条历史记录（被拒后允许重新申请）';
COMMENT ON COLUMN identity_companion_application.contact_phone IS '申请专用的联系手机号，与账号绑定的 identity_user.phone 是两回事';
COMMENT ON COLUMN identity_companion_application.status IS '申请状态：SUBMITTED/IN_ASSESSMENT/APPROVED/REJECTED';
COMMENT ON COLUMN identity_companion_application.reviewer_id IS '考核人，引用 identity_user.id（从系统用户里查询关联，不是独立角色表）';
COMMENT ON COLUMN identity_companion_application.review_result IS '考核结果：PASS/FAIL，必须与考核人同时录入；决定最终状态是 APPROVED 还是 REJECTED';

-- 申请时选中的擅长游戏类型，多对多，跟 identity_user_tag 一样是简单联合主键关联表。
CREATE TABLE identity_companion_application_tag (
    application_id BIGINT NOT NULL REFERENCES identity_companion_application (id),
    tag_id         BIGINT NOT NULL REFERENCES identity_tag_definition (id),
    PRIMARY KEY (application_id, tag_id)
);

COMMENT ON TABLE identity_companion_application_tag IS '陪玩身份申请里选中的擅长游戏类型，多对多关联表';

-- identity_companion_profile 正式接入标签目录：擅长游戏类型不再用逗号拼接的自由文本存储。
ALTER TABLE identity_companion_profile DROP COLUMN skill_tags;

CREATE TABLE identity_companion_profile_tag (
    user_id BIGINT NOT NULL REFERENCES identity_companion_profile (user_id),
    tag_id  BIGINT NOT NULL REFERENCES identity_tag_definition (id),
    PRIMARY KEY (user_id, tag_id)
);

COMMENT ON TABLE identity_companion_profile_tag IS '陪玩台账的擅长游戏类型，多对多关联表，替代原先的 skill_tags 自由文本字段';

-- identity_companion_profile 加身份状态：ACTIVE=当前拥有陪玩身份，REVOKED=身份已被收回（软删除，
-- 资料保留方便重新开通）。跟 companion_status（接单中/忙碌/离线）是两回事，见 CompanionProfile 类注释。
ALTER TABLE identity_companion_profile ADD COLUMN identity_status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE';

COMMENT ON COLUMN identity_companion_profile.identity_status IS '身份状态：ACTIVE/REVOKED，与接单状态（companion_status）是两回事';
