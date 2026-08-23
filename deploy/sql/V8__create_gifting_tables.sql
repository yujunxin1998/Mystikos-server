CREATE TABLE gifting_catalog_item (
    id                      BIGINT PRIMARY KEY,
    code                    VARCHAR(64) NOT NULL,
    name                    VARCHAR(64) NOT NULL,
    icon                    VARCHAR(255),
    price                   NUMERIC(12, 2) NOT NULL,
    unlock_rule_type        VARCHAR(32) NOT NULL DEFAULT 'NONE',
    unlock_rule_threshold   NUMERIC(14, 2),
    active                  BOOLEAN NOT NULL DEFAULT true,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT gifting_catalog_item_code_unique UNIQUE (code)
);

CREATE TABLE gifting_transaction (
    id            BIGINT PRIMARY KEY,
    patron_id     BIGINT NOT NULL,
    companion_id  BIGINT NOT NULL,
    gift_id       BIGINT NOT NULL REFERENCES gifting_catalog_item (id),
    quantity      INT NOT NULL,
    amount        NUMERIC(14, 2) NOT NULL,
    sent_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_gifting_transaction_patron ON gifting_transaction (patron_id);
CREATE INDEX idx_gifting_transaction_patron_gift ON gifting_transaction (patron_id, gift_id);
CREATE INDEX idx_gifting_transaction_companion ON gifting_transaction (companion_id);

-- 占位种子数据，对应 UI 原型的礼物墙（鲜花/蜡烛/信笺/耳环/皇冠/星辰吊坠），
-- 价格和解锁阈值都是占位值，业务确认后直接改这张表即可，不用改代码。
INSERT INTO gifting_catalog_item (id, code, name, icon, price, unlock_rule_type, unlock_rule_threshold) VALUES
    (1, 'FLOWER', '鲜花', 'flower', 6.00, 'NONE', NULL),
    (2, 'CANDLE', '蜡烛', 'candle', 12.00, 'NONE', NULL),
    (3, 'LETTER', '信笺', 'letter', 20.00, 'CUMULATIVE_COUNT', 5),
    (4, 'EARRING', '耳环', 'earring', 68.00, 'CUMULATIVE_SPEND', 200),
    (5, 'CROWN', '皇冠', 'crown', 288.00, 'CUMULATIVE_SPEND', 1000),
    (6, 'STARLIGHT_PENDANT', '星辰吊坠', 'pendant', 688.00, 'CUMULATIVE_SPEND', 5000)
ON CONFLICT (code) DO NOTHING;

COMMENT ON TABLE gifting_catalog_item IS '礼物目录（Gifting 限界上下文），运营配置数据';
COMMENT ON COLUMN gifting_catalog_item.id IS '主键ID';
COMMENT ON COLUMN gifting_catalog_item.code IS '礼物编码，唯一';
COMMENT ON COLUMN gifting_catalog_item.name IS '礼物展示名';
COMMENT ON COLUMN gifting_catalog_item.icon IS '图标标识/URL';
COMMENT ON COLUMN gifting_catalog_item.price IS '单价';
COMMENT ON COLUMN gifting_catalog_item.unlock_rule_type IS '解锁规则类型：NONE/CUMULATIVE_COUNT/CUMULATIVE_SPEND 已实现评估逻辑；CONSECUTIVE_DAYS/LEADERBOARD_RANK/INTIMACY_STAGE 目前只存储配置，不做解锁判定（跨上下文查询会形成循环模块依赖，见 docs/architecture/domain-model.md）';
COMMENT ON COLUMN gifting_catalog_item.unlock_rule_threshold IS '解锁规则阈值，NONE 类型时为空，占位数值，业务确认后可直接改表';
COMMENT ON COLUMN gifting_catalog_item.active IS '是否上架';
COMMENT ON COLUMN gifting_catalog_item.created_at IS '创建时间';

COMMENT ON TABLE gifting_transaction IS '赠礼流水（Gifting 限界上下文），不可变，撤销需另建补偿记录而非改这张表';
COMMENT ON COLUMN gifting_transaction.id IS '主键ID';
COMMENT ON COLUMN gifting_transaction.patron_id IS '赠送方（老板）用户ID';
COMMENT ON COLUMN gifting_transaction.companion_id IS '接收方（陪玩）用户ID';
COMMENT ON COLUMN gifting_transaction.gift_id IS '礼物ID，关联 gifting_catalog_item';
COMMENT ON COLUMN gifting_transaction.quantity IS '赠送数量';
COMMENT ON COLUMN gifting_transaction.amount IS '本次交易总金额（= 单价 x 数量，下单时快照，目录改价不影响历史记录）';
COMMENT ON COLUMN gifting_transaction.sent_at IS '赠送时间';
