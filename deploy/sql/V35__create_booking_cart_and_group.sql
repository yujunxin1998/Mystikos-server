-- 陪玩预约购物车 + 组合支付。多条预约草稿（不同陪玩/不同时段）可以合并结算成一次支付，
-- 见 BookingOrderGroup 类注释。
CREATE TABLE booking_cart_line (
    id               BIGINT PRIMARY KEY,
    patron_id        BIGINT NOT NULL,
    companion_id     BIGINT NOT NULL,
    time_range_start TIMESTAMPTZ NOT NULL,
    time_range_end   TIMESTAMPTZ NOT NULL,
    duration_hours   NUMERIC(5,2) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT booking_cart_line_time_range_valid CHECK (time_range_end > time_range_start)
);

CREATE INDEX idx_booking_cart_line_patron ON booking_cart_line (patron_id);

COMMENT ON TABLE booking_cart_line IS '预约购物车草稿行：陪玩+时段+时长，还没算权威价格、也没有防重占位';

-- 故意不加 EXCLUDE 约束：购物车里的行只是草稿，不是真实占用；
-- 真正的防重仍由 booking_order 的 EXCLUDE 约束在结算落单那一刻把关。

CREATE TABLE booking_order_group (
    id           BIGINT PRIMARY KEY,
    patron_id    BIGINT NOT NULL,
    status       VARCHAR(32) NOT NULL,
    total_amount NUMERIC(12,2) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    version      BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_booking_order_group_patron ON booking_order_group (patron_id);

COMMENT ON TABLE booking_order_group IS '多条预约合并支付的组，状态机只管到 PAID 为止（DRAFT/PENDING_PAYMENT/PAID/EXPIRED/CANCELLED）';

ALTER TABLE booking_order ADD COLUMN group_id BIGINT REFERENCES booking_order_group (id);
CREATE INDEX idx_booking_order_group ON booking_order (group_id);
COMMENT ON COLUMN booking_order.group_id IS 'NULL=独立预约（含立即预约路径）；非空=归属某个组合支付的预约组';
