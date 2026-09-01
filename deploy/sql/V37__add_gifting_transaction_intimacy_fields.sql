ALTER TABLE gifting_transaction ADD COLUMN tier_multiplier_snapshot NUMERIC(5, 2) NOT NULL DEFAULT 1.0;
ALTER TABLE gifting_transaction ADD COLUMN intimacy_value NUMERIC(14, 2) NOT NULL DEFAULT 0;
ALTER TABLE gifting_transaction ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'COMPLETED';
ALTER TABLE gifting_transaction ALTER COLUMN tier_multiplier_snapshot DROP DEFAULT;
ALTER TABLE gifting_transaction ALTER COLUMN intimacy_value DROP DEFAULT;
ALTER TABLE gifting_transaction ALTER COLUMN status DROP DEFAULT;

COMMENT ON COLUMN gifting_transaction.tier_multiplier_snapshot IS '发送时的档位倍率快照，档位后续调整不改写这笔历史流水';
COMMENT ON COLUMN gifting_transaction.intimacy_value IS '本次获得的亲密度值 = amount x tier_multiplier_snapshot，只驱动 Relationship，不影响 VIP/排行榜';
COMMENT ON COLUMN gifting_transaction.status IS '状态：COMPLETED/REFUNDED，退款见 GiftApplicationService#refundGiftTransaction';
