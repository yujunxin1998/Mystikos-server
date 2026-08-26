-- 陪玩没有独立 SKU 定价（只有 identity_companion_profile.hourly_rate 一份单价），
-- 下单改为"选陪玩 + 选时长（小时）"，价格由服务端按 hourlyRate * duration_hours 计算，
-- 不再信任客户端传入的 sku_id / price_snapshot。
ALTER TABLE booking_order DROP COLUMN sku_id;
ALTER TABLE booking_order ADD COLUMN duration_hours NUMERIC(5, 2) NOT NULL;

COMMENT ON COLUMN booking_order.duration_hours IS '下单时长（小时），按小时计费';
