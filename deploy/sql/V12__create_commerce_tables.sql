CREATE TABLE commerce_product (
    id            BIGINT PRIMARY KEY,
    category_id   BIGINT NOT NULL,
    name          VARCHAR(128) NOT NULL,
    description   TEXT,
    price         NUMERIC(12, 2) NOT NULL,
    images        TEXT,
    status        VARCHAR(16) NOT NULL DEFAULT 'ON_SHELF',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_commerce_product_category ON commerce_product (category_id);
CREATE INDEX idx_commerce_product_status ON commerce_product (status);

CREATE TABLE commerce_cart_item (
    id            BIGINT PRIMARY KEY,
    patron_id     BIGINT NOT NULL,
    product_id    BIGINT NOT NULL REFERENCES commerce_product (id),
    quantity      INT NOT NULL,

    CONSTRAINT commerce_cart_item_patron_product_unique UNIQUE (patron_id, product_id)
);

CREATE TABLE commerce_wishlist_item (
    id            BIGINT PRIMARY KEY,
    patron_id     BIGINT NOT NULL,
    product_id    BIGINT NOT NULL REFERENCES commerce_product (id),
    added_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT commerce_wishlist_item_patron_product_unique UNIQUE (patron_id, product_id)
);

CREATE TABLE commerce_inventory_stock (
    product_id    BIGINT PRIMARY KEY REFERENCES commerce_product (id),
    available_qty INT NOT NULL DEFAULT 0,
    reserved_qty  INT NOT NULL DEFAULT 0
);

CREATE TABLE commerce_order (
    id                  BIGINT PRIMARY KEY,
    patron_id           BIGINT NOT NULL,
    total_amount        NUMERIC(14, 2) NOT NULL,
    shipping_address    TEXT NOT NULL,
    status              VARCHAR(16) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_commerce_order_patron ON commerce_order (patron_id);

CREATE TABLE commerce_order_item (
    id                      BIGINT PRIMARY KEY,
    order_id                BIGINT NOT NULL REFERENCES commerce_order (id),
    product_id              BIGINT NOT NULL,
    product_name_snapshot   VARCHAR(128) NOT NULL,
    unit_price_snapshot     NUMERIC(12, 2) NOT NULL,
    quantity                INT NOT NULL
);

CREATE INDEX idx_commerce_order_item_order ON commerce_order_item (order_id);

-- 占位商品种子数据（对应 UI 原型的商城页），价格/库存都是占位值，业务确认后直接改表。
INSERT INTO commerce_product (id, category_id, name, description, price, images, status) VALUES
    (1, 1, '公会周边T恤', '公会限定款纯棉T恤', 89.00, '', 'ON_SHELF'),
    (2, 1, '公会周边马克杯', '公会限定款马克杯', 39.00, '', 'ON_SHELF'),
    (3, 2, '传奇搭档限定钥匙扣', '仅亲密度顶级老板可购买（本期未接入准入校验，见 docs）', 199.00, '', 'ON_SHELF')
ON CONFLICT (id) DO NOTHING;

INSERT INTO commerce_inventory_stock (product_id, available_qty, reserved_qty) VALUES
    (1, 100, 0),
    (2, 100, 0),
    (3, 20, 0)
ON CONFLICT (product_id) DO NOTHING;

COMMENT ON TABLE commerce_product IS '商品（Commerce 限界上下文）。陪玩推荐关联（recommendedBy/eligibilityRule）这次未建，见 docs/architecture/prd-alignment.md 的缺口清单';
COMMENT ON COLUMN commerce_product.id IS '主键ID';
COMMENT ON COLUMN commerce_product.category_id IS '分类ID，暂无独立分类表，业务确认分类体系后再建';
COMMENT ON COLUMN commerce_product.name IS '商品名';
COMMENT ON COLUMN commerce_product.description IS '商品描述';
COMMENT ON COLUMN commerce_product.price IS '价格';
COMMENT ON COLUMN commerce_product.images IS '图片URL列表，逗号分隔（数量少，未建独立表）';
COMMENT ON COLUMN commerce_product.status IS '上下架状态：ON_SHELF/OFF_SHELF';
COMMENT ON COLUMN commerce_product.created_at IS '创建时间';

COMMENT ON TABLE commerce_cart_item IS '购物车行（Commerce 限界上下文），按(patron_id, product_id)唯一，不建独立"购物车"聚合表';
COMMENT ON COLUMN commerce_cart_item.id IS '主键ID';
COMMENT ON COLUMN commerce_cart_item.patron_id IS '老板用户ID';
COMMENT ON COLUMN commerce_cart_item.product_id IS '商品ID';
COMMENT ON COLUMN commerce_cart_item.quantity IS '数量';

COMMENT ON TABLE commerce_wishlist_item IS '心愿单行（Commerce 限界上下文）';
COMMENT ON COLUMN commerce_wishlist_item.id IS '主键ID';
COMMENT ON COLUMN commerce_wishlist_item.patron_id IS '老板用户ID';
COMMENT ON COLUMN commerce_wishlist_item.product_id IS '商品ID';
COMMENT ON COLUMN commerce_wishlist_item.added_at IS '加入时间';

COMMENT ON TABLE commerce_inventory_stock IS '库存（Commerce 限界上下文），一个商品一行，下单预占/取消释放，不做超卖';
COMMENT ON COLUMN commerce_inventory_stock.product_id IS '商品ID，主键';
COMMENT ON COLUMN commerce_inventory_stock.available_qty IS '可售数量';
COMMENT ON COLUMN commerce_inventory_stock.reserved_qty IS '已预占数量（下单未支付/未发货期间占用）';

COMMENT ON TABLE commerce_order IS '商城订单（Commerce 限界上下文）。下单后停在 DRAFT，尚未接支付，和 booking_order 同一个阶段';
COMMENT ON COLUMN commerce_order.id IS '主键ID';
COMMENT ON COLUMN commerce_order.patron_id IS '下单老板用户ID';
COMMENT ON COLUMN commerce_order.total_amount IS '订单总金额';
COMMENT ON COLUMN commerce_order.shipping_address IS '收货地址';
COMMENT ON COLUMN commerce_order.status IS '订单状态：DRAFT/PENDING_PAYMENT/PAID/FULFILLING/SHIPPED/COMPLETED/CANCELLED/REFUNDED';
COMMENT ON COLUMN commerce_order.created_at IS '创建时间';

COMMENT ON TABLE commerce_order_item IS '订单行快照（Commerce 限界上下文），下单时复制商品名/单价，后续商品改价改名不影响历史订单';
COMMENT ON COLUMN commerce_order_item.id IS '主键ID';
COMMENT ON COLUMN commerce_order_item.order_id IS '订单ID';
COMMENT ON COLUMN commerce_order_item.product_id IS '商品ID';
COMMENT ON COLUMN commerce_order_item.product_name_snapshot IS '下单时的商品名快照';
COMMENT ON COLUMN commerce_order_item.unit_price_snapshot IS '下单时的单价快照';
COMMENT ON COLUMN commerce_order_item.quantity IS '数量';
