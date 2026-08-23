-- 打手（陪玩）后台管理扩展资料，一对一挂在 identity_user 上，不是独立账号体系——
-- "陪玩"就是拥有 COMPANION 角色的 identity_user，这张表只补运营后台需要的字段
-- （级别/技能标签/时薪/银行信息），账号本身（登录/角色/启停）继续归 identity_user 管。
-- 级别、技能标签业务暂无固定枚举/目录，先用自由文本承接；身份证号/银行卡号本阶段明文存储，
-- 项目还没有字段级加密基础设施，先跑通功能，见 CompanionProfile 类注释。
CREATE TABLE identity_companion_profile (
    user_id             BIGINT PRIMARY KEY REFERENCES identity_user (id),
    level               VARCHAR(32),
    skill_tags          VARCHAR(255),
    hourly_rate         NUMERIC(10,2),
    companion_status    VARCHAR(16) NOT NULL DEFAULT 'OFFLINE',
    id_card_no          VARCHAR(32),
    bank_account_name   VARCHAR(64),
    bank_account_no     VARCHAR(64),
    bank_name           VARCHAR(64),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE identity_companion_profile IS '打手（陪玩）后台管理扩展资料，一对一挂在 identity_user 上';
COMMENT ON COLUMN identity_companion_profile.skill_tags IS '技能标签，逗号分隔的自由文本，暂未接入 identity_tag_definition 目录体系';
COMMENT ON COLUMN identity_companion_profile.companion_status IS '接单状态：AVAILABLE/BUSY/OFFLINE，与账号启用状态（identity_user.status）是两回事';
COMMENT ON COLUMN identity_companion_profile.id_card_no IS '身份证号，明文存储（项目暂无字段级加密基础设施）';
COMMENT ON COLUMN identity_companion_profile.bank_account_no IS '银行卡号，明文存储（项目暂无字段级加密基础设施）';
