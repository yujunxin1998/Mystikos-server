-- EXCLUDE USING gist 对整数做等值判断需要 btree_gist 提供的操作符类支持
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE booking_order (
    id                BIGINT PRIMARY KEY,
    patron_id         BIGINT NOT NULL,
    companion_id      BIGINT NOT NULL,
    sku_id            BIGINT NOT NULL,
    time_range_start  TIMESTAMPTZ NOT NULL,
    time_range_end    TIMESTAMPTZ NOT NULL,
    price_snapshot    NUMERIC(12, 2) NOT NULL,
    status            VARCHAR(32) NOT NULL,
    version           BIGINT NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT booking_order_time_range_valid CHECK (time_range_end > time_range_start)
);

CREATE INDEX idx_booking_order_patron ON booking_order (patron_id);
CREATE INDEX idx_booking_order_companion ON booking_order (companion_id);

-- 同一陪玩的同一时段，只要处于"占用中"的状态，不允许重叠。
-- 这是防止"同一时段被抢"的数据库层保证，比纯应用层加锁更硬，
-- 见 docs/architecture/domain-model.md 的 Booking 设计要点。
ALTER TABLE booking_order
    ADD CONSTRAINT booking_order_no_overlap
    EXCLUDE USING gist (
        companion_id WITH =,
        tstzrange(time_range_start, time_range_end, '[)') WITH &&
    )
    WHERE (status IN ('PENDING_PAYMENT', 'PAID', 'MATCHING', 'ACCEPTED', 'IN_SERVICE'));
