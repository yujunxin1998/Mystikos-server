-- 商城订单接入 15 分钟支付时限（同 booking_order 的 PAYMENT_VALIDITY，EXPIRED 只是新增的
-- Java 枚举值，status 列本来就是 VARCHAR，不用改列；created_at 已存在，直接复用做 TTL 起点）
-- 以及下单时选择的地址追溯字段。
ALTER TABLE commerce_order ADD COLUMN shipping_address_id BIGINT REFERENCES commerce_patron_address (id) ON DELETE SET NULL;

COMMENT ON COLUMN commerce_order.shipping_address_id IS '下单时选择的地址，仅用于追溯；地址被删除不影响历史订单（权威信息在 shipping_address 快照字符串里）';
