-- 商城收货地址簿：老板可维护多个收货地址，结算时选一个。
-- 区分国内/海外两种字段要求（address_type），country_code/province_code 引用
-- common_region(code)（见 V15/V32），不建 DB 外键——跨模块引用惯例见
-- RegionQueryService#exists 的校验方式，identity_user.region_code 也是同样做法。
CREATE TABLE commerce_patron_address (
    id             BIGINT PRIMARY KEY,
    patron_id      BIGINT NOT NULL,
    address_type   VARCHAR(16) NOT NULL,
    recipient_name VARCHAR(64) NOT NULL,
    phone          VARCHAR(32) NOT NULL,
    country_code   VARCHAR(10) NOT NULL,
    province_code  VARCHAR(10),
    city           VARCHAR(64),
    district       VARCHAR(64),
    address_line1  VARCHAR(255) NOT NULL,
    address_line2  VARCHAR(255),
    state_region   VARCHAR(64),
    postal_code    VARCHAR(32),
    is_default     BOOLEAN NOT NULL DEFAULT false,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_commerce_patron_address_patron ON commerce_patron_address (patron_id);

COMMENT ON TABLE commerce_patron_address IS '老板收货地址簿';
COMMENT ON COLUMN commerce_patron_address.address_type IS 'DOMESTIC=国内（需省/市/区）/ OVERSEAS=海外（需国家/城市，省州可选）';
COMMENT ON COLUMN commerce_patron_address.country_code IS '引用 common_region(code)，国内地址固定为 CN';
COMMENT ON COLUMN commerce_patron_address.province_code IS '引用 common_region(code)，仅国内地址使用';
COMMENT ON COLUMN commerce_patron_address.district IS '区/县，仅国内地址使用，无对应 region 数据，自由文本';
COMMENT ON COLUMN commerce_patron_address.state_region IS '州/大区，仅海外地址可选使用，无对应 region 数据，自由文本';
