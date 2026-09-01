CREATE TABLE gifting_tier (
    id               BIGINT PRIMARY KEY,
    code             VARCHAR(32) NOT NULL,
    display_name     VARCHAR(32) NOT NULL,
    display_name_en  VARCHAR(64),
    multiplier       NUMERIC(5, 2) NOT NULL,
    sort_order       INT NOT NULL,
    active           BOOLEAN NOT NULL DEFAULT true,

    CONSTRAINT gifting_tier_code_unique UNIQUE (code)
);

INSERT INTO gifting_tier (id, code, display_name, display_name_en, multiplier, sort_order) VALUES
    (1, 'COMMON', '普通祝福', 'Common Blessing', 1.0, 1),
    (2, 'RARE', '稀有秘藏', 'Rare Arcana', 1.5, 2),
    (3, 'LEGENDARY', '传说神谕', 'Legendary Oracle', 2.0, 3)
ON CONFLICT (code) DO NOTHING;

-- 老的占位礼物目录（FLOWER/CANDLE/LETTER/EARRING/CROWN/STARLIGHT_PENDANT，见 V8）
-- 没有档位归属，且是明确标注为占位值的演示数据，本次连同其流水一并清空，
-- 由 V41 用秘典 v1.0 的真实礼物/价格重新播种。生产环境如果这张表已经有真实流水，
-- 这一步需要先手工评估，不能直接照搬。
DELETE FROM gifting_transaction;
DELETE FROM gifting_catalog_item;

ALTER TABLE gifting_catalog_item ADD COLUMN tier_id BIGINT REFERENCES gifting_tier (id);
ALTER TABLE gifting_catalog_item ALTER COLUMN tier_id SET NOT NULL;

COMMENT ON TABLE gifting_tier IS '礼物档位（Gifting 限界上下文），运营配置数据。multiplier 决定同样星辉石价值的礼物能换到多少亲密度，见 gifting_transaction.intimacy_value';
COMMENT ON COLUMN gifting_tier.multiplier IS '亲密度倍率：intimacy_value = amount x multiplier；调整这个值只影响此后新发生的赠礼，历史流水已快照倍率不受影响';
COMMENT ON COLUMN gifting_catalog_item.tier_id IS '所属档位，引用 gifting_tier(id)';
