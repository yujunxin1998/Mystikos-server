CREATE TABLE membership_account (
    id                  BIGINT PRIMARY KEY,
    patron_id           BIGINT NOT NULL,
    current_tier_code   VARCHAR(32) NOT NULL DEFAULT 'LV1',
    cumulative_spend    NUMERIC(14, 2) NOT NULL DEFAULT 0,
    tier_upgraded_at    TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT membership_account_patron_unique UNIQUE (patron_id)
);

COMMENT ON TABLE membership_account IS '会员成长账户（Membership 限界上下文），与老板用户 1:1';
COMMENT ON COLUMN membership_account.id IS '主键ID';
COMMENT ON COLUMN membership_account.patron_id IS '老板用户ID';
COMMENT ON COLUMN membership_account.current_tier_code IS '当前等级编码，对应 Java 枚举 DefaultMembershipTier（LV1-LV5，门槛为占位值，业务确认后改枚举即可）';
COMMENT ON COLUMN membership_account.cumulative_spend IS '累计消费金额，只增不减；当前只由 Gifting 的赠礼事件驱动累加（临时顶替 Payment 的 PaymentCaptured，见代码注释）';
COMMENT ON COLUMN membership_account.tier_upgraded_at IS '最近一次升级时间';
