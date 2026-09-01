CREATE TABLE membership_tier_definition (
    id                          BIGINT PRIMARY KEY,
    code                        VARCHAR(32) NOT NULL,
    display_name                VARCHAR(32) NOT NULL,
    display_name_en             VARCHAR(64),
    level                       INT NOT NULL,
    cumulative_spend_threshold  NUMERIC(14, 2) NOT NULL,
    perk_description            VARCHAR(255),
    sort_order                  INT NOT NULL,

    CONSTRAINT membership_tier_definition_code_unique UNIQUE (code)
);

COMMENT ON TABLE membership_tier_definition IS 'VIP 等级定义（Membership 限界上下文），运营配置数据，取代原来的 DefaultMembershipTier 枚举';

-- 老的 5 档 LV1-LV5 枚举编码不再存在，已有账户（如果有）先归到新梯度的最低档，
-- 下一次 accrueSpend/reverseSpend 会按真实累计消费重新解析出正确的等级；
-- 默认值同步换成新梯度的最低档编码，避免新建账户短暂落在一个不存在的 code 上。
UPDATE membership_account SET current_tier_code = 'VISITOR' WHERE current_tier_code IN ('LV1', 'LV2', 'LV3', 'LV4', 'LV5');
ALTER TABLE membership_account ALTER COLUMN current_tier_code SET DEFAULT 'VISITOR';
