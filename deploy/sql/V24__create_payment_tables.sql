-- 支付账本限界上下文：PaymentIntent/LedgerEntry/Wallet/WithdrawRequest。
-- 金额一律 NUMERIC，不用浮点类型；不与其他上下文的表建外键（跨上下文只能经事件/Port，
-- 不能联表），见 docs/architecture/module-structure.md 的数据库拆分约定。

CREATE TABLE payment_intent (
    id                BIGINT PRIMARY KEY,
    source_type       VARCHAR(32) NOT NULL,
    source_id         BIGINT NOT NULL,
    patron_id         BIGINT NOT NULL,
    amount            NUMERIC(12, 2) NOT NULL,
    currency          VARCHAR(8) NOT NULL,
    status            VARCHAR(32) NOT NULL,
    gateway_provider  VARCHAR(32),
    gateway_ref       VARCHAR(128),
    client_secret     VARCHAR(256),
    idempotency_key   VARCHAR(64) NOT NULL,
    failure_reason    VARCHAR(500),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 同一笔业务来源允许有多条历史意图（首次失败后重新发起支付），但幂等键本身必须唯一
-- ——它既是我们自己"同一次建单请求不重复调网关"的护栏，也是网关侧 idempotency key 的来源。
CREATE UNIQUE INDEX uk_payment_intent_idempotency_key ON payment_intent (idempotency_key);
CREATE INDEX idx_payment_intent_source ON payment_intent (source_type, source_id);
CREATE INDEX idx_payment_intent_gateway_ref ON payment_intent (gateway_ref);
CREATE INDEX idx_payment_intent_patron ON payment_intent (patron_id);

COMMENT ON TABLE payment_intent IS '支付意图，通过 source_type+source_id 回指业务订单，不持有业务细节';
COMMENT ON COLUMN payment_intent.gateway_provider IS '网关标识，如 stripe；内部钱包扣款记 INTERNAL_WALLET，没有真正调用外部网关';
COMMENT ON COLUMN payment_intent.status IS 'CREATED/REQUIRES_ACTION/CAPTURED/FAILED/REFUNDED';

-- 不可变账本行，append-only：任何资金移动的更正都应该是新增一条反向记录，不是改这一行
-- （应用层不提供 update/delete 方法，这里不额外加数据库触发器强制，靠代码约定）。
CREATE TABLE payment_ledger_entry (
    id           BIGINT PRIMARY KEY,
    intent_id    BIGINT,
    wallet_id    BIGINT,
    direction    VARCHAR(8) NOT NULL,
    amount       NUMERIC(12, 2) NOT NULL,
    currency     VARCHAR(8) NOT NULL,
    occurred_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_payment_ledger_entry_intent ON payment_ledger_entry (intent_id);
CREATE INDEX idx_payment_ledger_entry_wallet ON payment_ledger_entry (wallet_id);

COMMENT ON TABLE payment_ledger_entry IS '不可变账本行，append-only';
COMMENT ON COLUMN payment_ledger_entry.intent_id IS '关联的支付意图；提现冻结/退回这类没有对应 PaymentIntent 的内部调整为空';
COMMENT ON COLUMN payment_ledger_entry.wallet_id IS '关联的钱包；平台和外部网关之间的资金移动（非钱包内部转账）为空';

-- 用户在平台内的记账余额——不是托管客户资金的持牌电子钱包，见 Wallet 类注释。
CREATE TABLE payment_wallet (
    id          BIGINT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    balance     NUMERIC(12, 2) NOT NULL DEFAULT 0,
    currency    VARCHAR(8) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT payment_wallet_balance_non_negative CHECK (balance >= 0)
);

CREATE UNIQUE INDEX uk_payment_wallet_user_id ON payment_wallet (user_id);

COMMENT ON TABLE payment_wallet IS '用户内部记账余额，真实资金留在 Stripe 平台余额里，这里只是账本';

-- 陪玩提现申请。打款只发生在人工审批（APPROVED）之后，见 WithdrawRequest 类注释。
CREATE TABLE payment_withdraw_request (
    id                   BIGINT PRIMARY KEY,
    companion_id         BIGINT NOT NULL,
    amount               NUMERIC(12, 2) NOT NULL,
    currency             VARCHAR(8) NOT NULL,
    status               VARCHAR(32) NOT NULL DEFAULT 'PENDING_REVIEW',
    stripe_transfer_ref  VARCHAR(128),
    decided_by           BIGINT,
    decided_at           TIMESTAMPTZ,
    reject_reason        VARCHAR(500),
    requested_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_payment_withdraw_request_companion ON payment_withdraw_request (companion_id, requested_at DESC);

COMMENT ON TABLE payment_withdraw_request IS '陪玩提现申请：PENDING_REVIEW -> APPROVED -> PAID，旁路 REJECTED';

-- 陪玩收款账户和 Stripe Connect 账户的映射，Payment 自己的支付基础设施数据。
CREATE TABLE payment_companion_payout_account (
    id                        BIGINT PRIMARY KEY,
    user_id                   BIGINT NOT NULL,
    stripe_connect_account_id VARCHAR(128) NOT NULL
);

CREATE UNIQUE INDEX uk_payment_companion_payout_account_user_id ON payment_companion_payout_account (user_id);

COMMENT ON TABLE payment_companion_payout_account IS '陪玩收款账户与 Stripe Connect 账户的映射；是否可打款不缓存在这张表，审批提现时实时问 Stripe';
