-- ============================================================================
-- 一次性初始化脚本（结构 + 种子数据），供线上/测试环境部署使用。
--
-- 跟 deploy/sql/V1..V16 的关系：本地开发继续用 Flyway 按版本号增量迁移
-- （方便，改一次表加一个 V 文件），但线上/测试环境不接 Flyway——
-- mystikos-app 打包时用 `deploy` Maven profile（`mvn package -Pdeploy`）
-- 排除 flyway-core 依赖，启动时不会自动跑迁移、也不会去读 deploy/sql/ 下的
-- 版本化脚本。这个文件是把 V1..V16 应用之后的最终结构 + 种子数据一次性
-- 导出（`pg_dump`）拼成的完整快照，线上部署时手动执行一次即可：
--
--   psql -h <host> -U <user> -d <db> -f deploy/init.sql
--
-- 之后这套环境的表结构变更不再走 Flyway/版本化脚本，由人工手动执行
-- ALTER 语句维护（跟本地开发环境彻底分开两条路径）。
--
-- 重新生成这个文件（比如本地又加了新迁移、需要同步）：在跑完全部 Flyway
-- 迁移、清理掉测试脏数据之后，用
--   pg_dump --no-owner --no-privileges --exclude-table=flyway_schema_history --schema-only
--   pg_dump --no-owner --no-privileges --exclude-table=flyway_schema_history --column-inserts --data-only
-- 重新导出、手动拼接（common_region 表因为是自引用外键，--data-only 单独导不出来，
-- 直接复用 deploy/sql/V15 里现成的种子 INSERT 语句，不用重新导）。
-- ============================================================================

-- ============================================================================
-- 结构（表/约束/索引/COMMENT），从本地已跑完 V1..V16 的开发库导出
-- ============================================================================

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: btree_gist; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS btree_gist WITH SCHEMA public;


--
-- Name: EXTENSION btree_gist; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION btree_gist IS 'support for indexing common datatypes in GiST';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: booking_order; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.booking_order (
    id bigint NOT NULL,
    patron_id bigint NOT NULL,
    companion_id bigint NOT NULL,
    sku_id bigint NOT NULL,
    time_range_start timestamp with time zone NOT NULL,
    time_range_end timestamp with time zone NOT NULL,
    price_snapshot numeric(12,2) NOT NULL,
    status character varying(32) NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT booking_order_time_range_valid CHECK ((time_range_end > time_range_start))
);


--
-- Name: TABLE booking_order; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.booking_order IS '预约撮合订单（Booking 限界上下文）';


--
-- Name: COLUMN booking_order.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.booking_order.id IS '主键ID';


--
-- Name: COLUMN booking_order.patron_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.booking_order.patron_id IS '下单老板用户ID';


--
-- Name: COLUMN booking_order.companion_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.booking_order.companion_id IS '陪玩用户ID';


--
-- Name: COLUMN booking_order.sku_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.booking_order.sku_id IS '服务SKU ID';


--
-- Name: COLUMN booking_order.time_range_start; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.booking_order.time_range_start IS '预约时段开始时间';


--
-- Name: COLUMN booking_order.time_range_end; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.booking_order.time_range_end IS '预约时段结束时间';


--
-- Name: COLUMN booking_order.price_snapshot; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.booking_order.price_snapshot IS '下单时的价格快照';


--
-- Name: COLUMN booking_order.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.booking_order.status IS '订单状态：DRAFT/PENDING_PAYMENT/PAID/MATCHING/ACCEPTED/IN_SERVICE/COMPLETED/CANCELLED/EXPIRED/DISPUTED/REFUNDED';


--
-- Name: COLUMN booking_order.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.booking_order.version IS '乐观锁版本号';


--
-- Name: COLUMN booking_order.created_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.booking_order.created_at IS '创建时间';


--
-- Name: COLUMN booking_order.updated_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.booking_order.updated_at IS '更新时间';


--
-- Name: commerce_cart_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.commerce_cart_item (
    id bigint NOT NULL,
    patron_id bigint NOT NULL,
    product_id bigint NOT NULL,
    quantity integer NOT NULL
);


--
-- Name: TABLE commerce_cart_item; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.commerce_cart_item IS '购物车行（Commerce 限界上下文），按(patron_id, product_id)唯一，不建独立"购物车"聚合表';


--
-- Name: COLUMN commerce_cart_item.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_cart_item.id IS '主键ID';


--
-- Name: COLUMN commerce_cart_item.patron_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_cart_item.patron_id IS '老板用户ID';


--
-- Name: COLUMN commerce_cart_item.product_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_cart_item.product_id IS '商品ID';


--
-- Name: COLUMN commerce_cart_item.quantity; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_cart_item.quantity IS '数量';


--
-- Name: commerce_inventory_stock; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.commerce_inventory_stock (
    product_id bigint NOT NULL,
    available_qty integer DEFAULT 0 NOT NULL,
    reserved_qty integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE commerce_inventory_stock; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.commerce_inventory_stock IS '库存（Commerce 限界上下文），一个商品一行，下单预占/取消释放，不做超卖';


--
-- Name: COLUMN commerce_inventory_stock.product_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_inventory_stock.product_id IS '商品ID，主键';


--
-- Name: COLUMN commerce_inventory_stock.available_qty; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_inventory_stock.available_qty IS '可售数量';


--
-- Name: COLUMN commerce_inventory_stock.reserved_qty; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_inventory_stock.reserved_qty IS '已预占数量（下单未支付/未发货期间占用）';


--
-- Name: commerce_order; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.commerce_order (
    id bigint NOT NULL,
    patron_id bigint NOT NULL,
    total_amount numeric(14,2) NOT NULL,
    shipping_address text NOT NULL,
    status character varying(16) NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: TABLE commerce_order; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.commerce_order IS '商城订单（Commerce 限界上下文）。下单后停在 DRAFT，尚未接支付，和 booking_order 同一个阶段';


--
-- Name: COLUMN commerce_order.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_order.id IS '主键ID';


--
-- Name: COLUMN commerce_order.patron_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_order.patron_id IS '下单老板用户ID';


--
-- Name: COLUMN commerce_order.total_amount; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_order.total_amount IS '订单总金额';


--
-- Name: COLUMN commerce_order.shipping_address; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_order.shipping_address IS '收货地址';


--
-- Name: COLUMN commerce_order.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_order.status IS '订单状态：DRAFT/PENDING_PAYMENT/PAID/FULFILLING/SHIPPED/COMPLETED/CANCELLED/REFUNDED';


--
-- Name: COLUMN commerce_order.created_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_order.created_at IS '创建时间';


--
-- Name: commerce_order_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.commerce_order_item (
    id bigint NOT NULL,
    order_id bigint NOT NULL,
    product_id bigint NOT NULL,
    product_name_snapshot character varying(128) NOT NULL,
    unit_price_snapshot numeric(12,2) NOT NULL,
    quantity integer NOT NULL
);


--
-- Name: TABLE commerce_order_item; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.commerce_order_item IS '订单行快照（Commerce 限界上下文），下单时复制商品名/单价，后续商品改价改名不影响历史订单';


--
-- Name: COLUMN commerce_order_item.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_order_item.id IS '主键ID';


--
-- Name: COLUMN commerce_order_item.order_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_order_item.order_id IS '订单ID';


--
-- Name: COLUMN commerce_order_item.product_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_order_item.product_id IS '商品ID';


--
-- Name: COLUMN commerce_order_item.product_name_snapshot; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_order_item.product_name_snapshot IS '下单时的商品名快照';


--
-- Name: COLUMN commerce_order_item.unit_price_snapshot; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_order_item.unit_price_snapshot IS '下单时的单价快照';


--
-- Name: COLUMN commerce_order_item.quantity; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_order_item.quantity IS '数量';


--
-- Name: commerce_product; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.commerce_product (
    id bigint NOT NULL,
    category_id bigint NOT NULL,
    name character varying(128) NOT NULL,
    description text,
    price numeric(12,2) NOT NULL,
    images text,
    status character varying(16) DEFAULT 'ON_SHELF'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: TABLE commerce_product; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.commerce_product IS '商品（Commerce 限界上下文）。陪玩推荐关联（recommendedBy/eligibilityRule）这次未建，见 docs/architecture/prd-alignment.md 的缺口清单';


--
-- Name: COLUMN commerce_product.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_product.id IS '主键ID';


--
-- Name: COLUMN commerce_product.category_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_product.category_id IS '分类ID，暂无独立分类表，业务确认分类体系后再建';


--
-- Name: COLUMN commerce_product.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_product.name IS '商品名';


--
-- Name: COLUMN commerce_product.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_product.description IS '商品描述';


--
-- Name: COLUMN commerce_product.price; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_product.price IS '价格';


--
-- Name: COLUMN commerce_product.images; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_product.images IS '图片URL列表，逗号分隔（数量少，未建独立表）';


--
-- Name: COLUMN commerce_product.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_product.status IS '上下架状态：ON_SHELF/OFF_SHELF';


--
-- Name: COLUMN commerce_product.created_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_product.created_at IS '创建时间';


--
-- Name: commerce_wishlist_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.commerce_wishlist_item (
    id bigint NOT NULL,
    patron_id bigint NOT NULL,
    product_id bigint NOT NULL,
    added_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: TABLE commerce_wishlist_item; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.commerce_wishlist_item IS '心愿单行（Commerce 限界上下文）';


--
-- Name: COLUMN commerce_wishlist_item.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_wishlist_item.id IS '主键ID';


--
-- Name: COLUMN commerce_wishlist_item.patron_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_wishlist_item.patron_id IS '老板用户ID';


--
-- Name: COLUMN commerce_wishlist_item.product_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_wishlist_item.product_id IS '商品ID';


--
-- Name: COLUMN commerce_wishlist_item.added_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.commerce_wishlist_item.added_at IS '加入时间';


--
-- Name: common_region; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.common_region (
    code character varying(10) NOT NULL,
    parent_code character varying(10),
    level character varying(16) NOT NULL,
    name_zh character varying(64) NOT NULL,
    name_en character varying(64) NOT NULL,
    sort_order integer DEFAULT 0 NOT NULL
);


--
-- Name: TABLE common_region; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.common_region IS '行政区划参考数据（国家 + 一级行政区），只读技术能力表，不属于任何业务限界上下文';


--
-- Name: COLUMN common_region.code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.common_region.code IS 'ISO 3166-1 alpha-2（国家级）或 ISO 3166-2（一级行政区级）编码，主键';


--
-- Name: COLUMN common_region.parent_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.common_region.parent_code IS '上级编码，国家级为空，一级行政区为所属国家的 code';


--
-- Name: COLUMN common_region.level; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.common_region.level IS '层级：COUNTRY / SUBDIVISION';


--
-- Name: COLUMN common_region.name_zh; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.common_region.name_zh IS '中文展示名';


--
-- Name: COLUMN common_region.name_en; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.common_region.name_en IS '英文展示名';


--
-- Name: COLUMN common_region.sort_order; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.common_region.sort_order IS '排序权重，越小越靠前';


--
-- Name: gifting_catalog_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gifting_catalog_item (
    id bigint NOT NULL,
    code character varying(64) NOT NULL,
    name character varying(64) NOT NULL,
    icon character varying(255),
    price numeric(12,2) NOT NULL,
    unlock_rule_type character varying(32) DEFAULT 'NONE'::character varying NOT NULL,
    unlock_rule_threshold numeric(14,2),
    active boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: TABLE gifting_catalog_item; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.gifting_catalog_item IS '礼物目录（Gifting 限界上下文），运营配置数据';


--
-- Name: COLUMN gifting_catalog_item.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gifting_catalog_item.id IS '主键ID';


--
-- Name: COLUMN gifting_catalog_item.code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gifting_catalog_item.code IS '礼物编码，唯一';


--
-- Name: COLUMN gifting_catalog_item.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gifting_catalog_item.name IS '礼物展示名';


--
-- Name: COLUMN gifting_catalog_item.icon; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gifting_catalog_item.icon IS '图标标识/URL';


--
-- Name: COLUMN gifting_catalog_item.price; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gifting_catalog_item.price IS '单价';


--
-- Name: COLUMN gifting_catalog_item.unlock_rule_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gifting_catalog_item.unlock_rule_type IS '解锁规则类型：NONE/CUMULATIVE_COUNT/CUMULATIVE_SPEND 已实现评估逻辑；CONSECUTIVE_DAYS/LEADERBOARD_RANK/INTIMACY_STAGE 目前只存储配置，不做解锁判定（跨上下文查询会形成循环模块依赖，见 docs/architecture/domain-model.md）';


--
-- Name: COLUMN gifting_catalog_item.unlock_rule_threshold; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gifting_catalog_item.unlock_rule_threshold IS '解锁规则阈值，NONE 类型时为空，占位数值，业务确认后可直接改表';


--
-- Name: COLUMN gifting_catalog_item.active; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gifting_catalog_item.active IS '是否上架';


--
-- Name: COLUMN gifting_catalog_item.created_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gifting_catalog_item.created_at IS '创建时间';


--
-- Name: gifting_transaction; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gifting_transaction (
    id bigint NOT NULL,
    patron_id bigint NOT NULL,
    companion_id bigint NOT NULL,
    gift_id bigint NOT NULL,
    quantity integer NOT NULL,
    amount numeric(14,2) NOT NULL,
    sent_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: TABLE gifting_transaction; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.gifting_transaction IS '赠礼流水（Gifting 限界上下文），不可变，撤销需另建补偿记录而非改这张表';


--
-- Name: COLUMN gifting_transaction.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gifting_transaction.id IS '主键ID';


--
-- Name: COLUMN gifting_transaction.patron_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gifting_transaction.patron_id IS '赠送方（老板）用户ID';


--
-- Name: COLUMN gifting_transaction.companion_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gifting_transaction.companion_id IS '接收方（陪玩）用户ID';


--
-- Name: COLUMN gifting_transaction.gift_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gifting_transaction.gift_id IS '礼物ID，关联 gifting_catalog_item';


--
-- Name: COLUMN gifting_transaction.quantity; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gifting_transaction.quantity IS '赠送数量';


--
-- Name: COLUMN gifting_transaction.amount; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gifting_transaction.amount IS '本次交易总金额（= 单价 x 数量，下单时快照，目录改价不影响历史记录）';


--
-- Name: COLUMN gifting_transaction.sent_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gifting_transaction.sent_at IS '赠送时间';


--
-- Name: identity_oauth_binding; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.identity_oauth_binding (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    provider character varying(32) NOT NULL,
    provider_user_id character varying(128) NOT NULL,
    bound_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: TABLE identity_oauth_binding; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.identity_oauth_binding IS '第三方登录绑定（一个用户可绑多个 provider，同一 provider 账号只能绑一个用户）';


--
-- Name: COLUMN identity_oauth_binding.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_oauth_binding.id IS '主键ID';


--
-- Name: COLUMN identity_oauth_binding.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_oauth_binding.user_id IS '绑定到的用户ID';


--
-- Name: COLUMN identity_oauth_binding.provider; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_oauth_binding.provider IS '第三方登录提供商编码，如 discord';


--
-- Name: COLUMN identity_oauth_binding.provider_user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_oauth_binding.provider_user_id IS '第三方平台上的用户ID';


--
-- Name: COLUMN identity_oauth_binding.bound_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_oauth_binding.bound_at IS '绑定时间';


--
-- Name: identity_refresh_token; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.identity_refresh_token (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    token_hash character varying(128) NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    revoked_at timestamp with time zone
);


--
-- Name: TABLE identity_refresh_token; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.identity_refresh_token IS 'Refresh Token（只存哈希，可主动吊销，不是无状态 JWT）';


--
-- Name: COLUMN identity_refresh_token.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_refresh_token.id IS '主键ID';


--
-- Name: COLUMN identity_refresh_token.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_refresh_token.user_id IS '所属用户ID';


--
-- Name: COLUMN identity_refresh_token.token_hash; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_refresh_token.token_hash IS 'Refresh Token 的 SHA-256 哈希，不存明文';


--
-- Name: COLUMN identity_refresh_token.expires_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_refresh_token.expires_at IS '过期时间';


--
-- Name: COLUMN identity_refresh_token.revoked_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_refresh_token.revoked_at IS '吊销时间，非空即已失效（登出/换设备触发）';


--
-- Name: identity_role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.identity_role (
    code character varying(32) NOT NULL,
    display_name character varying(64) NOT NULL,
    sort_order integer NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: TABLE identity_role; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.identity_role IS 'Role 枚举的数据库镜像，只用于给 identity_user_role/identity_role_permission 提供外键完整性；展示名/排序仍以 Java 枚举为权威来源';


--
-- Name: COLUMN identity_role.code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_role.code IS '角色编码，主键，对应 Java 枚举 Role';


--
-- Name: COLUMN identity_role.display_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_role.display_name IS '角色展示名';


--
-- Name: COLUMN identity_role.sort_order; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_role.sort_order IS '展示排序';


--
-- Name: COLUMN identity_role.created_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_role.created_at IS '创建时间';


--
-- Name: identity_role_permission; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.identity_role_permission (
    role character varying(32) NOT NULL,
    permission_code character varying(64) NOT NULL
);


--
-- Name: TABLE identity_role_permission; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.identity_role_permission IS '角色-权限编码关联；权限编码由业务后续定义，框架不预置';


--
-- Name: COLUMN identity_role_permission.role; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_role_permission.role IS '角色编码';


--
-- Name: COLUMN identity_role_permission.permission_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_role_permission.permission_code IS '权限编码';


--
-- Name: identity_tag_definition; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.identity_tag_definition (
    id bigint NOT NULL,
    category character varying(32) NOT NULL,
    label character varying(32) NOT NULL,
    sort_order integer DEFAULT 0 NOT NULL,
    enabled boolean DEFAULT true NOT NULL
);


--
-- Name: TABLE identity_tag_definition; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.identity_tag_definition IS '标签目录（游戏类型等），后台配置项';


--
-- Name: COLUMN identity_tag_definition.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_tag_definition.id IS '主键ID';


--
-- Name: COLUMN identity_tag_definition.category; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_tag_definition.category IS '标签分类，如 GAME_TYPE，自由字符串';


--
-- Name: COLUMN identity_tag_definition.label; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_tag_definition.label IS '标签展示名';


--
-- Name: COLUMN identity_tag_definition.sort_order; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_tag_definition.sort_order IS '排序权重，越小越靠前';


--
-- Name: COLUMN identity_tag_definition.enabled; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_tag_definition.enabled IS '是否启用，停用后不出现在前台多选列表';


--
-- Name: identity_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.identity_user (
    id bigint NOT NULL,
    phone character varying(32),
    email character varying(128),
    password_hash character varying(255),
    status character varying(16) NOT NULL,
    membership_tier_level integer,
    membership_tier_code character varying(32),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    nickname character varying(64),
    privacy_anonymous boolean DEFAULT false NOT NULL,
    gender character varying(16) DEFAULT 'UNDISCLOSED'::character varying NOT NULL,
    avatar_object_key text,
    birth_date date,
    bio character varying(255),
    region_code character varying(10)
);


--
-- Name: TABLE identity_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.identity_user IS '用户账号（Identity & Access 限界上下文），手机号/邮箱二选一注册';


--
-- Name: COLUMN identity_user.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_user.id IS '主键ID';


--
-- Name: COLUMN identity_user.phone; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_user.phone IS '手机号，唯一（可为空，与 email 二选一）';


--
-- Name: COLUMN identity_user.email; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_user.email IS '邮箱，唯一（可为空，与 phone 二选一）';


--
-- Name: COLUMN identity_user.password_hash; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_user.password_hash IS '密码哈希（BCrypt），为空表示只能用验证码登录，不接受密码登录';


--
-- Name: COLUMN identity_user.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_user.status IS '账号状态：ACTIVE/DISABLED/BANNED';


--
-- Name: COLUMN identity_user.membership_tier_level; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_user.membership_tier_level IS '会员等级数值（挂点字段，梯度由 MembershipTier 接口实现决定，见 mystikos-common）';


--
-- Name: COLUMN identity_user.membership_tier_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_user.membership_tier_code IS '会员等级编码（挂点字段，同上）';


--
-- Name: COLUMN identity_user.created_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_user.created_at IS '创建时间';


--
-- Name: COLUMN identity_user.nickname; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_user.nickname IS '昵称（老板侧资料，S2）';


--
-- Name: COLUMN identity_user.privacy_anonymous; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_user.privacy_anonymous IS '是否匿名上榜（老板侧隐私设置，S2）';


--
-- Name: COLUMN identity_user.gender; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_user.gender IS '性别：MALE/FEMALE/UNDISCLOSED，自报展示用，默认 UNDISCLOSED';


--
-- Name: COLUMN identity_user.avatar_object_key; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_user.avatar_object_key IS '头像对象存储键，展示 URL 在查询资料时动态生成';


--
-- Name: COLUMN identity_user.birth_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_user.birth_date IS '生日，仅作资料展示，不作为年龄/实名认证依据';


--
-- Name: COLUMN identity_user.bio; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_user.bio IS '个性签名';


--
-- Name: COLUMN identity_user.region_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_user.region_code IS '所在地区，引用 common_region.code（国家或一级行政区编码）；不建 DB 级外键——common_region 表在 V15 才建，且这是跨模块引用，合法性由 mystikos-identity 应用层调用 RegionQueryService 校验，不是数据库约束';


--
-- Name: identity_user_role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.identity_user_role (
    user_id bigint NOT NULL,
    role character varying(32) NOT NULL
);


--
-- Name: TABLE identity_user_role; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.identity_user_role IS '用户-角色关联（一个用户可同时拥有多个角色）';


--
-- Name: COLUMN identity_user_role.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_user_role.user_id IS '用户ID';


--
-- Name: COLUMN identity_user_role.role; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_user_role.role IS '角色编码，对应 identity_role.code / Java 枚举 Role';


--
-- Name: identity_user_tag; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.identity_user_tag (
    user_id bigint NOT NULL,
    tag_id bigint NOT NULL
);


--
-- Name: TABLE identity_user_tag; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.identity_user_tag IS '用户已选中的标签，多对多关联表';


--
-- Name: COLUMN identity_user_tag.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_user_tag.user_id IS '用户ID';


--
-- Name: COLUMN identity_user_tag.tag_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.identity_user_tag.tag_id IS '标签ID';


--
-- Name: leaderboard_companion_stat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.leaderboard_companion_stat (
    id bigint NOT NULL,
    companion_id bigint NOT NULL,
    charm_value numeric(14,2) DEFAULT 0 NOT NULL
);


--
-- Name: TABLE leaderboard_companion_stat; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.leaderboard_companion_stat IS '陪玩魅力值累计（Leaderboard 限界上下文），纯读侧投影，排名查询时实时排序，不落快照';


--
-- Name: COLUMN leaderboard_companion_stat.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.leaderboard_companion_stat.id IS '代理主键ID';


--
-- Name: COLUMN leaderboard_companion_stat.companion_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.leaderboard_companion_stat.companion_id IS '陪玩用户ID';


--
-- Name: COLUMN leaderboard_companion_stat.charm_value; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.leaderboard_companion_stat.charm_value IS '累计魅力值，目前只由 Gifting 的赠礼金额累加驱动';


--
-- Name: leaderboard_patron_stat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.leaderboard_patron_stat (
    id bigint NOT NULL,
    patron_id bigint NOT NULL,
    guard_value numeric(14,2) DEFAULT 0 NOT NULL
);


--
-- Name: TABLE leaderboard_patron_stat; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.leaderboard_patron_stat IS '老板守护值累计（Leaderboard 限界上下文），结构对称，说明同上';


--
-- Name: COLUMN leaderboard_patron_stat.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.leaderboard_patron_stat.id IS '代理主键ID';


--
-- Name: COLUMN leaderboard_patron_stat.patron_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.leaderboard_patron_stat.patron_id IS '老板用户ID';


--
-- Name: COLUMN leaderboard_patron_stat.guard_value; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.leaderboard_patron_stat.guard_value IS '累计守护值，目前只由 Gifting 的赠礼金额累加驱动';


--
-- Name: membership_account; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.membership_account (
    id bigint NOT NULL,
    patron_id bigint NOT NULL,
    current_tier_code character varying(32) DEFAULT 'LV1'::character varying NOT NULL,
    cumulative_spend numeric(14,2) DEFAULT 0 NOT NULL,
    tier_upgraded_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: TABLE membership_account; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.membership_account IS '会员成长账户（Membership 限界上下文），与老板用户 1:1';


--
-- Name: COLUMN membership_account.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.membership_account.id IS '主键ID';


--
-- Name: COLUMN membership_account.patron_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.membership_account.patron_id IS '老板用户ID';


--
-- Name: COLUMN membership_account.current_tier_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.membership_account.current_tier_code IS '当前等级编码，对应 Java 枚举 DefaultMembershipTier（LV1-LV5，门槛为占位值，业务确认后改枚举即可）';


--
-- Name: COLUMN membership_account.cumulative_spend; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.membership_account.cumulative_spend IS '累计消费金额，只增不减；当前只由 Gifting 的赠礼事件驱动累加（临时顶替 Payment 的 PaymentCaptured，见代码注释）';


--
-- Name: COLUMN membership_account.tier_upgraded_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.membership_account.tier_upgraded_at IS '最近一次升级时间';


--
-- Name: relationship_intimacy_record; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.relationship_intimacy_record (
    id bigint NOT NULL,
    patron_id bigint NOT NULL,
    companion_id bigint NOT NULL,
    stage integer DEFAULT 0 NOT NULL,
    progress_value numeric(14,2) DEFAULT 0 NOT NULL,
    last_interaction_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: TABLE relationship_intimacy_record; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.relationship_intimacy_record IS '老板×陪玩亲密度记录（Relationship 限界上下文），代理主键 + (patron_id, companion_id) 唯一约束模拟复合业务键';


--
-- Name: COLUMN relationship_intimacy_record.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.relationship_intimacy_record.id IS '代理主键ID';


--
-- Name: COLUMN relationship_intimacy_record.patron_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.relationship_intimacy_record.patron_id IS '老板用户ID';


--
-- Name: COLUMN relationship_intimacy_record.companion_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.relationship_intimacy_record.companion_id IS '陪玩用户ID';


--
-- Name: COLUMN relationship_intimacy_record.stage; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.relationship_intimacy_record.stage IS '亲密度阶段：0-4，阈值定义见 IntimacyStagePolicy（占位值，业务确认后可改）';


--
-- Name: COLUMN relationship_intimacy_record.progress_value; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.relationship_intimacy_record.progress_value IS '累计互动进度值，目前只由 Gifting 的赠礼金额累加驱动';


--
-- Name: COLUMN relationship_intimacy_record.last_interaction_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.relationship_intimacy_record.last_interaction_at IS '最后互动时间';


--
-- Name: booking_order booking_order_no_overlap; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking_order
    ADD CONSTRAINT booking_order_no_overlap EXCLUDE USING gist (companion_id WITH =, tstzrange(time_range_start, time_range_end, '[)'::text) WITH &&) WHERE (((status)::text = ANY ((ARRAY['PENDING_PAYMENT'::character varying, 'PAID'::character varying, 'MATCHING'::character varying, 'ACCEPTED'::character varying, 'IN_SERVICE'::character varying])::text[])));


--
-- Name: booking_order booking_order_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking_order
    ADD CONSTRAINT booking_order_pkey PRIMARY KEY (id);


--
-- Name: commerce_cart_item commerce_cart_item_patron_product_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commerce_cart_item
    ADD CONSTRAINT commerce_cart_item_patron_product_unique UNIQUE (patron_id, product_id);


--
-- Name: commerce_cart_item commerce_cart_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commerce_cart_item
    ADD CONSTRAINT commerce_cart_item_pkey PRIMARY KEY (id);


--
-- Name: commerce_inventory_stock commerce_inventory_stock_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commerce_inventory_stock
    ADD CONSTRAINT commerce_inventory_stock_pkey PRIMARY KEY (product_id);


--
-- Name: commerce_order_item commerce_order_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commerce_order_item
    ADD CONSTRAINT commerce_order_item_pkey PRIMARY KEY (id);


--
-- Name: commerce_order commerce_order_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commerce_order
    ADD CONSTRAINT commerce_order_pkey PRIMARY KEY (id);


--
-- Name: commerce_product commerce_product_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commerce_product
    ADD CONSTRAINT commerce_product_pkey PRIMARY KEY (id);


--
-- Name: commerce_wishlist_item commerce_wishlist_item_patron_product_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commerce_wishlist_item
    ADD CONSTRAINT commerce_wishlist_item_patron_product_unique UNIQUE (patron_id, product_id);


--
-- Name: commerce_wishlist_item commerce_wishlist_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commerce_wishlist_item
    ADD CONSTRAINT commerce_wishlist_item_pkey PRIMARY KEY (id);


--
-- Name: common_region common_region_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.common_region
    ADD CONSTRAINT common_region_pkey PRIMARY KEY (code);


--
-- Name: gifting_catalog_item gifting_catalog_item_code_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gifting_catalog_item
    ADD CONSTRAINT gifting_catalog_item_code_unique UNIQUE (code);


--
-- Name: gifting_catalog_item gifting_catalog_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gifting_catalog_item
    ADD CONSTRAINT gifting_catalog_item_pkey PRIMARY KEY (id);


--
-- Name: gifting_transaction gifting_transaction_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gifting_transaction
    ADD CONSTRAINT gifting_transaction_pkey PRIMARY KEY (id);


--
-- Name: identity_oauth_binding identity_oauth_binding_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_oauth_binding
    ADD CONSTRAINT identity_oauth_binding_pkey PRIMARY KEY (id);


--
-- Name: identity_oauth_binding identity_oauth_binding_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_oauth_binding
    ADD CONSTRAINT identity_oauth_binding_unique UNIQUE (provider, provider_user_id);


--
-- Name: identity_refresh_token identity_refresh_token_hash_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_refresh_token
    ADD CONSTRAINT identity_refresh_token_hash_unique UNIQUE (token_hash);


--
-- Name: identity_refresh_token identity_refresh_token_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_refresh_token
    ADD CONSTRAINT identity_refresh_token_pkey PRIMARY KEY (id);


--
-- Name: identity_role_permission identity_role_permission_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_role_permission
    ADD CONSTRAINT identity_role_permission_pkey PRIMARY KEY (role, permission_code);


--
-- Name: identity_role identity_role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_role
    ADD CONSTRAINT identity_role_pkey PRIMARY KEY (code);


--
-- Name: identity_tag_definition identity_tag_definition_category_label_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_tag_definition
    ADD CONSTRAINT identity_tag_definition_category_label_unique UNIQUE (category, label);


--
-- Name: identity_tag_definition identity_tag_definition_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_tag_definition
    ADD CONSTRAINT identity_tag_definition_pkey PRIMARY KEY (id);


--
-- Name: identity_user identity_user_email_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_user
    ADD CONSTRAINT identity_user_email_unique UNIQUE (email);


--
-- Name: identity_user identity_user_phone_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_user
    ADD CONSTRAINT identity_user_phone_unique UNIQUE (phone);


--
-- Name: identity_user identity_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_user
    ADD CONSTRAINT identity_user_pkey PRIMARY KEY (id);


--
-- Name: identity_user_role identity_user_role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_user_role
    ADD CONSTRAINT identity_user_role_pkey PRIMARY KEY (user_id, role);


--
-- Name: identity_user_tag identity_user_tag_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_user_tag
    ADD CONSTRAINT identity_user_tag_pkey PRIMARY KEY (user_id, tag_id);


--
-- Name: leaderboard_companion_stat leaderboard_companion_stat_companion_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.leaderboard_companion_stat
    ADD CONSTRAINT leaderboard_companion_stat_companion_unique UNIQUE (companion_id);


--
-- Name: leaderboard_companion_stat leaderboard_companion_stat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.leaderboard_companion_stat
    ADD CONSTRAINT leaderboard_companion_stat_pkey PRIMARY KEY (id);


--
-- Name: leaderboard_patron_stat leaderboard_patron_stat_patron_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.leaderboard_patron_stat
    ADD CONSTRAINT leaderboard_patron_stat_patron_unique UNIQUE (patron_id);


--
-- Name: leaderboard_patron_stat leaderboard_patron_stat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.leaderboard_patron_stat
    ADD CONSTRAINT leaderboard_patron_stat_pkey PRIMARY KEY (id);


--
-- Name: membership_account membership_account_patron_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.membership_account
    ADD CONSTRAINT membership_account_patron_unique UNIQUE (patron_id);


--
-- Name: membership_account membership_account_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.membership_account
    ADD CONSTRAINT membership_account_pkey PRIMARY KEY (id);


--
-- Name: relationship_intimacy_record relationship_intimacy_record_pair_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.relationship_intimacy_record
    ADD CONSTRAINT relationship_intimacy_record_pair_unique UNIQUE (patron_id, companion_id);


--
-- Name: relationship_intimacy_record relationship_intimacy_record_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.relationship_intimacy_record
    ADD CONSTRAINT relationship_intimacy_record_pkey PRIMARY KEY (id);


--
-- Name: idx_booking_order_companion; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_booking_order_companion ON public.booking_order USING btree (companion_id);


--
-- Name: idx_booking_order_patron; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_booking_order_patron ON public.booking_order USING btree (patron_id);


--
-- Name: idx_commerce_order_item_order; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_commerce_order_item_order ON public.commerce_order_item USING btree (order_id);


--
-- Name: idx_commerce_order_patron; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_commerce_order_patron ON public.commerce_order USING btree (patron_id);


--
-- Name: idx_commerce_product_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_commerce_product_category ON public.commerce_product USING btree (category_id);


--
-- Name: idx_commerce_product_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_commerce_product_status ON public.commerce_product USING btree (status);


--
-- Name: idx_common_region_parent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_common_region_parent ON public.common_region USING btree (parent_code, sort_order);


--
-- Name: idx_gifting_transaction_companion; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gifting_transaction_companion ON public.gifting_transaction USING btree (companion_id);


--
-- Name: idx_gifting_transaction_patron; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gifting_transaction_patron ON public.gifting_transaction USING btree (patron_id);


--
-- Name: idx_gifting_transaction_patron_gift; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gifting_transaction_patron_gift ON public.gifting_transaction USING btree (patron_id, gift_id);


--
-- Name: idx_identity_refresh_token_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_identity_refresh_token_user ON public.identity_refresh_token USING btree (user_id);


--
-- Name: idx_identity_tag_definition_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_identity_tag_definition_category ON public.identity_tag_definition USING btree (category, sort_order);


--
-- Name: idx_leaderboard_companion_stat_charm; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_leaderboard_companion_stat_charm ON public.leaderboard_companion_stat USING btree (charm_value DESC);


--
-- Name: idx_leaderboard_patron_stat_guard; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_leaderboard_patron_stat_guard ON public.leaderboard_patron_stat USING btree (guard_value DESC);


--
-- Name: idx_relationship_intimacy_record_companion; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_relationship_intimacy_record_companion ON public.relationship_intimacy_record USING btree (companion_id);


--
-- Name: idx_relationship_intimacy_record_patron; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_relationship_intimacy_record_patron ON public.relationship_intimacy_record USING btree (patron_id);


--
-- Name: commerce_cart_item commerce_cart_item_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commerce_cart_item
    ADD CONSTRAINT commerce_cart_item_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.commerce_product(id);


--
-- Name: commerce_inventory_stock commerce_inventory_stock_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commerce_inventory_stock
    ADD CONSTRAINT commerce_inventory_stock_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.commerce_product(id);


--
-- Name: commerce_order_item commerce_order_item_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commerce_order_item
    ADD CONSTRAINT commerce_order_item_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.commerce_order(id);


--
-- Name: commerce_wishlist_item commerce_wishlist_item_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commerce_wishlist_item
    ADD CONSTRAINT commerce_wishlist_item_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.commerce_product(id);


--
-- Name: common_region common_region_parent_code_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.common_region
    ADD CONSTRAINT common_region_parent_code_fkey FOREIGN KEY (parent_code) REFERENCES public.common_region(code);


--
-- Name: gifting_transaction gifting_transaction_gift_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gifting_transaction
    ADD CONSTRAINT gifting_transaction_gift_id_fkey FOREIGN KEY (gift_id) REFERENCES public.gifting_catalog_item(id);


--
-- Name: identity_oauth_binding identity_oauth_binding_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_oauth_binding
    ADD CONSTRAINT identity_oauth_binding_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.identity_user(id);


--
-- Name: identity_refresh_token identity_refresh_token_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_refresh_token
    ADD CONSTRAINT identity_refresh_token_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.identity_user(id);


--
-- Name: identity_role_permission identity_role_permission_role_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_role_permission
    ADD CONSTRAINT identity_role_permission_role_fk FOREIGN KEY (role) REFERENCES public.identity_role(code);


--
-- Name: identity_user_role identity_user_role_role_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_user_role
    ADD CONSTRAINT identity_user_role_role_fk FOREIGN KEY (role) REFERENCES public.identity_role(code);


--
-- Name: identity_user_role identity_user_role_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_user_role
    ADD CONSTRAINT identity_user_role_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.identity_user(id);


--
-- Name: identity_user_tag identity_user_tag_tag_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_user_tag
    ADD CONSTRAINT identity_user_tag_tag_id_fkey FOREIGN KEY (tag_id) REFERENCES public.identity_tag_definition(id);


--
-- Name: identity_user_tag identity_user_tag_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.identity_user_tag
    ADD CONSTRAINT identity_user_tag_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.identity_user(id);


--
-- PostgreSQL database dump complete
--


-- ============================================================================
-- 种子数据：管理员账号 / 角色 / 礼物目录 / 标签目录 / 商城商品与库存
-- ============================================================================

--
-- Data for Name: booking_order; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: commerce_product; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.commerce_product (id, category_id, name, description, price, images, status, created_at) VALUES (1, 1, '公会周边T恤', '公会限定款纯棉T恤', 89.00, '', 'ON_SHELF', '2026-08-22 14:01:28.896534+08');
INSERT INTO public.commerce_product (id, category_id, name, description, price, images, status, created_at) VALUES (2, 1, '公会周边马克杯', '公会限定款马克杯', 39.00, '', 'ON_SHELF', '2026-08-22 14:01:28.896534+08');
INSERT INTO public.commerce_product (id, category_id, name, description, price, images, status, created_at) VALUES (3, 2, '传奇搭档限定钥匙扣', '仅亲密度顶级老板可购买（本期未接入准入校验，见 docs）', 199.00, '', 'ON_SHELF', '2026-08-22 14:01:28.896534+08');


--
-- Data for Name: commerce_cart_item; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: commerce_inventory_stock; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.commerce_inventory_stock (product_id, available_qty, reserved_qty) VALUES (3, 20, 0);
INSERT INTO public.commerce_inventory_stock (product_id, available_qty, reserved_qty) VALUES (1, 100, 0);
INSERT INTO public.commerce_inventory_stock (product_id, available_qty, reserved_qty) VALUES (2, 100, 0);


--
-- Data for Name: commerce_order; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: commerce_order_item; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: commerce_wishlist_item; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: gifting_catalog_item; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.gifting_catalog_item (id, code, name, icon, price, unlock_rule_type, unlock_rule_threshold, active, created_at) VALUES (1, 'FLOWER', '鲜花', 'flower', 6.00, 'NONE', NULL, true, '2026-08-22 14:01:28.8013+08');
INSERT INTO public.gifting_catalog_item (id, code, name, icon, price, unlock_rule_type, unlock_rule_threshold, active, created_at) VALUES (2, 'CANDLE', '蜡烛', 'candle', 12.00, 'NONE', NULL, true, '2026-08-22 14:01:28.8013+08');
INSERT INTO public.gifting_catalog_item (id, code, name, icon, price, unlock_rule_type, unlock_rule_threshold, active, created_at) VALUES (3, 'LETTER', '信笺', 'letter', 20.00, 'CUMULATIVE_COUNT', 5.00, true, '2026-08-22 14:01:28.8013+08');
INSERT INTO public.gifting_catalog_item (id, code, name, icon, price, unlock_rule_type, unlock_rule_threshold, active, created_at) VALUES (4, 'EARRING', '耳环', 'earring', 68.00, 'CUMULATIVE_SPEND', 200.00, true, '2026-08-22 14:01:28.8013+08');
INSERT INTO public.gifting_catalog_item (id, code, name, icon, price, unlock_rule_type, unlock_rule_threshold, active, created_at) VALUES (5, 'CROWN', '皇冠', 'crown', 288.00, 'CUMULATIVE_SPEND', 1000.00, true, '2026-08-22 14:01:28.8013+08');
INSERT INTO public.gifting_catalog_item (id, code, name, icon, price, unlock_rule_type, unlock_rule_threshold, active, created_at) VALUES (6, 'STARLIGHT_PENDANT', '星辰吊坠', 'pendant', 688.00, 'CUMULATIVE_SPEND', 5000.00, true, '2026-08-22 14:01:28.8013+08');


--
-- Data for Name: gifting_transaction; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: identity_user; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.identity_user (id, phone, email, password_hash, status, membership_tier_level, membership_tier_code, created_at, nickname, privacy_anonymous, gender, avatar_object_key, birth_date, bio, region_code) VALUES (1, NULL, 'administrator@mystikos.local', '$2a$10$.sIFVYoV58.JuSrgv5b2ueFFjMsZ8cl5VsrhYouHp/9DW5T1sdG/.', 'ACTIVE', NULL, NULL, '2026-08-22 00:54:43.426514+08', '超级管理员', false, 'UNDISCLOSED', NULL, NULL, NULL, NULL);
INSERT INTO public.identity_user (id, phone, email, password_hash, status, membership_tier_level, membership_tier_code, created_at, nickname, privacy_anonymous, gender, avatar_object_key, birth_date, bio, region_code) VALUES (2, NULL, 'member@mystikos.local', '$2a$10$kORc.uSw0lituVY7XkBu4ez3fFkco2YnGsgCKMtebKi2d34Kt087.', 'ACTIVE', NULL, NULL, now(), '会员测试账号', false, 'UNDISCLOSED', NULL, NULL, NULL, NULL);
INSERT INTO public.identity_user (id, phone, email, password_hash, status, membership_tier_level, membership_tier_code, created_at, nickname, privacy_anonymous, gender, avatar_object_key, birth_date, bio, region_code) VALUES (3, NULL, 'companion@mystikos.local', '$2a$10$kORc.uSw0lituVY7XkBu4ez3fFkco2YnGsgCKMtebKi2d34Kt087.', 'ACTIVE', NULL, NULL, now(), '陪玩测试账号', false, 'UNDISCLOSED', NULL, NULL, NULL, NULL);
INSERT INTO public.identity_user (id, phone, email, password_hash, status, membership_tier_level, membership_tier_code, created_at, nickname, privacy_anonymous, gender, avatar_object_key, birth_date, bio, region_code) VALUES (4, NULL, 'customerservice@mystikos.local', '$2a$10$kORc.uSw0lituVY7XkBu4ez3fFkco2YnGsgCKMtebKi2d34Kt087.', 'ACTIVE', NULL, NULL, now(), '客服测试账号', false, 'UNDISCLOSED', NULL, NULL, NULL, NULL);
INSERT INTO public.identity_user (id, phone, email, password_hash, status, membership_tier_level, membership_tier_code, created_at, nickname, privacy_anonymous, gender, avatar_object_key, birth_date, bio, region_code) VALUES (5, NULL, 'assessor@mystikos.local', '$2a$10$kORc.uSw0lituVY7XkBu4ez3fFkco2YnGsgCKMtebKi2d34Kt087.', 'ACTIVE', NULL, NULL, now(), '考核官测试账号', false, 'UNDISCLOSED', NULL, NULL, NULL, NULL);


--
-- Data for Name: identity_oauth_binding; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: identity_refresh_token; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: identity_role; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.identity_role (code, display_name, sort_order, created_at) VALUES ('GUEST', '游客', 0, '2026-08-22 01:00:53.799391+08');
INSERT INTO public.identity_role (code, display_name, sort_order, created_at) VALUES ('MEMBER', '会员用户', 1, '2026-08-22 01:00:53.799391+08');
INSERT INTO public.identity_role (code, display_name, sort_order, created_at) VALUES ('COMPANION', '陪玩', 2, '2026-08-22 01:00:53.799391+08');
INSERT INTO public.identity_role (code, display_name, sort_order, created_at) VALUES ('CUSTOMER_SERVICE', '客服', 3, '2026-08-22 01:00:53.799391+08');
INSERT INTO public.identity_role (code, display_name, sort_order, created_at) VALUES ('ASSESSOR', '考核官', 4, '2026-08-22 01:00:53.799391+08');
INSERT INTO public.identity_role (code, display_name, sort_order, created_at) VALUES ('ADMIN', '管理员', 5, '2026-08-22 01:00:53.799391+08');


--
-- Data for Name: identity_role_permission; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: identity_tag_definition; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.identity_tag_definition (id, category, label, sort_order, enabled) VALUES (1, 'GAME_TYPE', '王者荣耀', 1, true);
INSERT INTO public.identity_tag_definition (id, category, label, sort_order, enabled) VALUES (2, 'GAME_TYPE', '英雄联盟', 2, true);
INSERT INTO public.identity_tag_definition (id, category, label, sort_order, enabled) VALUES (3, 'GAME_TYPE', '和平精英', 3, true);
INSERT INTO public.identity_tag_definition (id, category, label, sort_order, enabled) VALUES (4, 'GAME_TYPE', '原神', 4, true);
INSERT INTO public.identity_tag_definition (id, category, label, sort_order, enabled) VALUES (5, 'GAME_TYPE', 'CS2', 5, true);
INSERT INTO public.identity_tag_definition (id, category, label, sort_order, enabled) VALUES (6, 'GAME_TYPE', '其他', 99, true);


--
-- Data for Name: identity_user_role; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.identity_user_role (user_id, role) VALUES (1, 'ADMIN');
INSERT INTO public.identity_user_role (user_id, role) VALUES (2, 'MEMBER');
INSERT INTO public.identity_user_role (user_id, role) VALUES (3, 'COMPANION');
INSERT INTO public.identity_user_role (user_id, role) VALUES (4, 'CUSTOMER_SERVICE');
INSERT INTO public.identity_user_role (user_id, role) VALUES (5, 'ASSESSOR');


--
-- Data for Name: identity_user_tag; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: leaderboard_companion_stat; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: leaderboard_patron_stat; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: membership_account; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: relationship_intimacy_record; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- PostgreSQL database dump complete
--


-- ============================================================================
-- 种子数据：行政区划（国家 + 一级行政区），内容与 deploy/sql/V15 完全一致
-- ============================================================================

-- pg_dump 在文件开头把 search_path 清空（安全惯例，逼自己的 CREATE TABLE/INSERT
-- 都用 public.xxx 全限定名）；这段是手工拼接进来的种子数据，表名没加 public. 前缀，
-- 不恢复 search_path 会报"关系不存在"。
SET search_path = public;

-- Country level
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('AL', NULL, 'COUNTRY', '阿尔巴尼亚', 'Albania', 1),
('AD', NULL, 'COUNTRY', '安道尔', 'Andorra', 2),
('AT', NULL, 'COUNTRY', '奥地利', 'Austria', 3),
('BY', NULL, 'COUNTRY', '白俄罗斯', 'Belarus', 4),
('BE', NULL, 'COUNTRY', '比利时', 'Belgium', 5),
('BA', NULL, 'COUNTRY', '波斯尼亚和黑塞哥维那', 'Bosnia and Herzegovina', 6),
('BG', NULL, 'COUNTRY', '保加利亚', 'Bulgaria', 7),
('HR', NULL, 'COUNTRY', '克罗地亚', 'Croatia', 8),
('CZ', NULL, 'COUNTRY', '捷克', 'Czechia', 9),
('DK', NULL, 'COUNTRY', '丹麦', 'Denmark', 10),
('EE', NULL, 'COUNTRY', '爱沙尼亚', 'Estonia', 11),
('FI', NULL, 'COUNTRY', '芬兰', 'Finland', 12),
('FR', NULL, 'COUNTRY', '法国', 'France', 13),
('DE', NULL, 'COUNTRY', '德国', 'Germany', 14),
('GR', NULL, 'COUNTRY', '希腊', 'Greece', 15),
('VA', NULL, 'COUNTRY', '梵蒂冈', 'Holy See', 16),
('HU', NULL, 'COUNTRY', '匈牙利', 'Hungary', 17),
('IS', NULL, 'COUNTRY', '冰岛', 'Iceland', 18),
('IE', NULL, 'COUNTRY', '爱尔兰', 'Ireland', 19),
('IT', NULL, 'COUNTRY', '意大利', 'Italy', 20),
('XK', NULL, 'COUNTRY', '科索沃', 'Kosovo', 21),
('LV', NULL, 'COUNTRY', '拉脱维亚', 'Latvia', 22),
('LI', NULL, 'COUNTRY', '列支敦士登', 'Liechtenstein', 23),
('LT', NULL, 'COUNTRY', '立陶宛', 'Lithuania', 24),
('LU', NULL, 'COUNTRY', '卢森堡', 'Luxembourg', 25),
('MT', NULL, 'COUNTRY', '马耳他', 'Malta', 26),
('MD', NULL, 'COUNTRY', '摩尔多瓦', 'Moldova', 27),
('MC', NULL, 'COUNTRY', '摩纳哥', 'Monaco', 28),
('ME', NULL, 'COUNTRY', '黑山', 'Montenegro', 29),
('NL', NULL, 'COUNTRY', '荷兰', 'Netherlands', 30),
('MK', NULL, 'COUNTRY', '北马其顿', 'North Macedonia', 31),
('NO', NULL, 'COUNTRY', '挪威', 'Norway', 32),
('PL', NULL, 'COUNTRY', '波兰', 'Poland', 33),
('PT', NULL, 'COUNTRY', '葡萄牙', 'Portugal', 34),
('RO', NULL, 'COUNTRY', '罗马尼亚', 'Romania', 35),
('RU', NULL, 'COUNTRY', '俄罗斯', 'Russia', 36),
('SM', NULL, 'COUNTRY', '圣马力诺', 'San Marino', 37),
('RS', NULL, 'COUNTRY', '塞尔维亚', 'Serbia', 38),
('SK', NULL, 'COUNTRY', '斯洛伐克', 'Slovakia', 39),
('SI', NULL, 'COUNTRY', '斯洛文尼亚', 'Slovenia', 40),
('ES', NULL, 'COUNTRY', '西班牙', 'Spain', 41),
('SE', NULL, 'COUNTRY', '瑞典', 'Sweden', 42),
('CH', NULL, 'COUNTRY', '瑞士', 'Switzerland', 43),
('UA', NULL, 'COUNTRY', '乌克兰', 'Ukraine', 44),
('GB', NULL, 'COUNTRY', '英国', 'United Kingdom', 45);

-- Subdivision level

-- Denmark
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('DK-84', 'DK', 'SUBDIVISION', '首都大区', 'Capital Region', 1),
('DK-82', 'DK', 'SUBDIVISION', '中日德兰大区', 'Central Denmark Region', 2),
('DK-81', 'DK', 'SUBDIVISION', '北日德兰大区', 'North Denmark Region', 3),
('DK-83', 'DK', 'SUBDIVISION', '南丹麦大区', 'Region of Southern Denmark', 4),
('DK-85', 'DK', 'SUBDIVISION', '西兰大区', 'Region Zealand', 5);

-- Estonia
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('EE-37', 'EE', 'SUBDIVISION', '哈尔尤县', 'Harju County', 1),
('EE-39', 'EE', 'SUBDIVISION', '希尤马县', 'Hiiu County', 2),
('EE-45', 'EE', 'SUBDIVISION', '伊达-维鲁县', 'Ida-Viru County', 3),
('EE-52', 'EE', 'SUBDIVISION', '亚尔瓦县', 'Järva County', 4),
('EE-50', 'EE', 'SUBDIVISION', '约盖瓦县', 'Jõgeva County', 5),
('EE-56', 'EE', 'SUBDIVISION', '莱内县', 'Lääne County', 6),
('EE-60', 'EE', 'SUBDIVISION', '西维鲁县', 'Lääne-Viru County', 7),
('EE-68', 'EE', 'SUBDIVISION', '派尔努县', 'Pärnu County', 8),
('EE-64', 'EE', 'SUBDIVISION', '波尔瓦县', 'Põlva County', 9),
('EE-71', 'EE', 'SUBDIVISION', '拉普拉县', 'Rapla County', 10),
('EE-74', 'EE', 'SUBDIVISION', '萨雷县', 'Saare County', 11),
('EE-79', 'EE', 'SUBDIVISION', '塔尔图县', 'Tartu County', 12),
('EE-81', 'EE', 'SUBDIVISION', '瓦尔加县', 'Valga County', 13),
('EE-84', 'EE', 'SUBDIVISION', '维良迪县', 'Viljandi County', 14),
('EE-87', 'EE', 'SUBDIVISION', '沃鲁县', 'Võru County', 15);

-- Finland
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('FI-01', 'FI', 'SUBDIVISION', '奥兰', 'Åland', 1),
('FI-08', 'FI', 'SUBDIVISION', '中芬兰', 'Central Finland', 2),
('FI-07', 'FI', 'SUBDIVISION', '中博滕', 'Central Ostrobothnia', 3),
('FI-05', 'FI', 'SUBDIVISION', '凯努', 'Kainuu', 4),
('FI-06', 'FI', 'SUBDIVISION', '坎塔海梅', 'Kanta-Häme', 5),
('FI-09', 'FI', 'SUBDIVISION', '屈门拉克索', 'Kymenlaakso', 6),
('FI-10', 'FI', 'SUBDIVISION', '拉普兰', 'Lapland', 7),
('FI-13', 'FI', 'SUBDIVISION', '北卡累利阿', 'North Karelia', 8),
('FI-14', 'FI', 'SUBDIVISION', '北博滕', 'North Ostrobothnia', 9),
('FI-15', 'FI', 'SUBDIVISION', '北萨沃', 'North Savo', 10),
('FI-12', 'FI', 'SUBDIVISION', '博滕', 'Ostrobothnia', 11),
('FI-16', 'FI', 'SUBDIVISION', '派耶特海梅', 'Päijät-Häme', 12),
('FI-11', 'FI', 'SUBDIVISION', '皮尔卡拉', 'Pirkanmaa', 13),
('FI-17', 'FI', 'SUBDIVISION', '萨塔昆塔', 'Satakunta', 14),
('FI-02', 'FI', 'SUBDIVISION', '南卡累利阿', 'South Karelia', 15),
('FI-03', 'FI', 'SUBDIVISION', '南博滕', 'South Ostrobothnia', 16),
('FI-04', 'FI', 'SUBDIVISION', '南萨沃', 'South Savo', 17),
('FI-19', 'FI', 'SUBDIVISION', '西南芬兰', 'Southwest Finland', 18),
('FI-18', 'FI', 'SUBDIVISION', '乌西马', 'Uusimaa', 19);

-- Iceland
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('IS-1', 'IS', 'SUBDIVISION', '首都大区', 'Capital Region', 1),
('IS-7', 'IS', 'SUBDIVISION', '东部大区', 'Eastern Region', 2),
('IS-6', 'IS', 'SUBDIVISION', '东北大区', 'Northeastern Region', 3),
('IS-5', 'IS', 'SUBDIVISION', '西北大区', 'Northwestern Region', 4),
('IS-2', 'IS', 'SUBDIVISION', '南部半岛大区', 'Southern Peninsula', 5),
('IS-8', 'IS', 'SUBDIVISION', '南部大区', 'Southern Region', 6),
('IS-3', 'IS', 'SUBDIVISION', '西部大区', 'Western Region', 7),
('IS-4', 'IS', 'SUBDIVISION', '西峡湾大区', 'Westfjords', 8);

-- Ireland
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('IE-C', 'IE', 'SUBDIVISION', '康诺特省', 'Connaught', 1),
('IE-L', 'IE', 'SUBDIVISION', '伦斯特省', 'Leinster', 2),
('IE-M', 'IE', 'SUBDIVISION', '芒斯特省', 'Munster', 3),
('IE-U', 'IE', 'SUBDIVISION', '阿尔斯特省', 'Ulster', 4);

-- Latvia
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('LV-011', 'LV', 'SUBDIVISION', '阿达日区', 'Ādaži Municipality', 1),
('LV-002', 'LV', 'SUBDIVISION', '艾兹克劳克莱区', 'Aizkraukle Municipality', 2),
('LV-007', 'LV', 'SUBDIVISION', '阿卢克斯内区', 'Alūksne Municipality', 3),
('LV-111', 'LV', 'SUBDIVISION', '上道加瓦区', 'Augšdaugava Municipality', 4),
('LV-015', 'LV', 'SUBDIVISION', '巴尔维区', 'Balvi Municipality', 5),
('LV-016', 'LV', 'SUBDIVISION', '包斯卡区', 'Bauska Municipality', 6),
('LV-022', 'LV', 'SUBDIVISION', '采西斯区', 'Cēsis Municipality', 7),
('LV-DGV', 'LV', 'SUBDIVISION', '陶格夫匹尔斯市', 'Daugavpils (City)', 8),
('LV-112', 'LV', 'SUBDIVISION', '南库尔泽梅区', 'Dienvidkurzeme Municipality', 9),
('LV-026', 'LV', 'SUBDIVISION', '多贝莱区', 'Dobele Municipality', 10),
('LV-033', 'LV', 'SUBDIVISION', '古尔别内区', 'Gulbene Municipality', 11),
('LV-JEL', 'LV', 'SUBDIVISION', '叶尔加瓦市', 'Jelgava (City)', 12),
('LV-041', 'LV', 'SUBDIVISION', '叶尔加瓦区', 'Jelgava Municipality', 13),
('LV-042', 'LV', 'SUBDIVISION', '叶卡布皮尔斯区', 'Jēkabpils Municipality', 14),
('LV-JUR', 'LV', 'SUBDIVISION', '尤尔马拉市', 'Jūrmala (City)', 15),
('LV-047', 'LV', 'SUBDIVISION', '克拉斯拉瓦区', 'Krāslava Municipality', 16),
('LV-050', 'LV', 'SUBDIVISION', '库尔迪加区', 'Kuldīga Municipality', 17),
('LV-052', 'LV', 'SUBDIVISION', '凯卡瓦区', 'Ķekava Municipality', 18),
('LV-LPX', 'LV', 'SUBDIVISION', '利耶帕亚市', 'Liepāja (City)', 19),
('LV-054', 'LV', 'SUBDIVISION', '林巴日区', 'Limbaži Municipality', 20),
('LV-056', 'LV', 'SUBDIVISION', '利瓦尼区', 'Līvāni Municipality', 21),
('LV-058', 'LV', 'SUBDIVISION', '卢扎区', 'Ludza Municipality', 22),
('LV-059', 'LV', 'SUBDIVISION', '马多纳区', 'Madona Municipality', 23),
('LV-062', 'LV', 'SUBDIVISION', '马鲁佩区', 'Mārupe Municipality', 24),
('LV-067', 'LV', 'SUBDIVISION', '奥格雷区', 'Ogre Municipality', 25),
('LV-068', 'LV', 'SUBDIVISION', '奥莱内区', 'Olaine Municipality', 26),
('LV-073', 'LV', 'SUBDIVISION', '普雷利区', 'Preiļi Municipality', 27),
('LV-REZ', 'LV', 'SUBDIVISION', '雷泽克内市', 'Rēzekne (City)', 28),
('LV-077', 'LV', 'SUBDIVISION', '雷泽克内区', 'Rēzekne Municipality', 29),
('LV-RIX', 'LV', 'SUBDIVISION', '里加市', 'Rīga (City)', 30),
('LV-080', 'LV', 'SUBDIVISION', '罗帕日区', 'Ropaži Municipality', 31),
('LV-087', 'LV', 'SUBDIVISION', '萨拉斯皮尔斯区', 'Salaspils Municipality', 32),
('LV-088', 'LV', 'SUBDIVISION', '萨尔杜斯区', 'Saldus Municipality', 33),
('LV-089', 'LV', 'SUBDIVISION', '绍尔克拉斯特区', 'Saulkrasti Municipality', 34),
('LV-091', 'LV', 'SUBDIVISION', '西古尔达区', 'Sigulda Municipality', 35),
('LV-094', 'LV', 'SUBDIVISION', '斯米尔泰内区', 'Smiltene Municipality', 36),
('LV-097', 'LV', 'SUBDIVISION', '塔尔西区', 'Talsi Municipality', 37),
('LV-099', 'LV', 'SUBDIVISION', '图库姆区', 'Tukums Municipality', 38),
('LV-101', 'LV', 'SUBDIVISION', '瓦尔卡区', 'Valka Municipality', 39),
('LV-113', 'LV', 'SUBDIVISION', '瓦尔米耶拉区', 'Valmiera Municipality', 40),
('LV-102', 'LV', 'SUBDIVISION', '瓦拉克拉尼区', 'Varakļāni Municipality', 41),
('LV-VEN', 'LV', 'SUBDIVISION', '文茨皮尔斯市', 'Ventspils (City)', 42),
('LV-106', 'LV', 'SUBDIVISION', '文茨皮尔斯区', 'Ventspils Municipality', 43);

-- Lithuania
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('LT-AL', 'LT', 'SUBDIVISION', '阿利图斯县', 'Alytus County', 1),
('LT-KU', 'LT', 'SUBDIVISION', '考纳斯县', 'Kaunas County', 2),
('LT-KL', 'LT', 'SUBDIVISION', '克莱佩达县', 'Klaipėda County', 3),
('LT-MR', 'LT', 'SUBDIVISION', '马里扬波莱县', 'Marijampolė County', 4),
('LT-PN', 'LT', 'SUBDIVISION', '帕涅韦日斯县', 'Panevėžys County', 5),
('LT-SA', 'LT', 'SUBDIVISION', '希奥利艾县', 'Šiauliai County', 6),
('LT-TA', 'LT', 'SUBDIVISION', '陶拉盖县', 'Tauragė County', 7),
('LT-TE', 'LT', 'SUBDIVISION', '泰尔希艾县', 'Telšiai County', 8),
('LT-UT', 'LT', 'SUBDIVISION', '乌田纳县', 'Utena County', 9),
('LT-VL', 'LT', 'SUBDIVISION', '维尔纽斯县', 'Vilnius County', 10);

-- Norway
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('NO-42', 'NO', 'SUBDIVISION', '阿格德尔', 'Agder', 1),
('NO-32', 'NO', 'SUBDIVISION', '阿克什胡斯', 'Akershus', 2),
('NO-33', 'NO', 'SUBDIVISION', '布斯克吕', 'Buskerud', 3),
('NO-56', 'NO', 'SUBDIVISION', '芬马克', 'Finnmark', 4),
('NO-34', 'NO', 'SUBDIVISION', '内陆', 'Innlandet', 5),
('NO-15', 'NO', 'SUBDIVISION', '默勒-鲁姆斯达尔', 'Møre og Romsdal', 6),
('NO-18', 'NO', 'SUBDIVISION', '北兰', 'Nordland', 7),
('NO-03', 'NO', 'SUBDIVISION', '奥斯陆', 'Oslo', 8),
('NO-11', 'NO', 'SUBDIVISION', '罗加兰', 'Rogaland', 9),
('NO-40', 'NO', 'SUBDIVISION', '泰勒马克', 'Telemark', 10),
('NO-55', 'NO', 'SUBDIVISION', '特罗姆斯', 'Troms', 11),
('NO-50', 'NO', 'SUBDIVISION', '特伦德拉格', 'Trøndelag', 12),
('NO-39', 'NO', 'SUBDIVISION', '韦斯特福尔', 'Vestfold', 13),
('NO-46', 'NO', 'SUBDIVISION', '韦斯特兰', 'Vestland', 14),
('NO-31', 'NO', 'SUBDIVISION', '东福尔', 'Østfold', 15);

-- Sweden
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('SE-K', 'SE', 'SUBDIVISION', '布莱金厄省', 'Blekinge County', 1),
('SE-W', 'SE', 'SUBDIVISION', '达拉纳省', 'Dalarna County', 2),
('SE-I', 'SE', 'SUBDIVISION', '哥特兰省', 'Gotland County', 3),
('SE-X', 'SE', 'SUBDIVISION', '耶夫勒堡省', 'Gävleborg County', 4),
('SE-N', 'SE', 'SUBDIVISION', '哈兰省', 'Halland County', 5),
('SE-Z', 'SE', 'SUBDIVISION', '耶姆特兰省', 'Jämtland County', 6),
('SE-F', 'SE', 'SUBDIVISION', '延雪平省', 'Jönköping County', 7),
('SE-H', 'SE', 'SUBDIVISION', '卡尔马省', 'Kalmar County', 8),
('SE-G', 'SE', 'SUBDIVISION', '克鲁努贝里省', 'Kronoberg County', 9),
('SE-BD', 'SE', 'SUBDIVISION', '北博滕省', 'Norrbotten County', 10),
('SE-M', 'SE', 'SUBDIVISION', '斯科讷省', 'Scania County', 11),
('SE-AB', 'SE', 'SUBDIVISION', '斯德哥尔摩省', 'Stockholm County', 12),
('SE-D', 'SE', 'SUBDIVISION', '南曼兰省', 'Södermanland County', 13),
('SE-C', 'SE', 'SUBDIVISION', '乌普萨拉省', 'Uppsala County', 14),
('SE-S', 'SE', 'SUBDIVISION', '韦姆兰省', 'Värmland County', 15),
('SE-AC', 'SE', 'SUBDIVISION', '西博滕省', 'Västerbotten County', 16),
('SE-Y', 'SE', 'SUBDIVISION', '西诺尔兰省', 'Västernorrland County', 17),
('SE-U', 'SE', 'SUBDIVISION', '西曼兰省', 'Västmanland County', 18),
('SE-O', 'SE', 'SUBDIVISION', '西约特兰省', 'Västra Götaland County', 19),
('SE-T', 'SE', 'SUBDIVISION', '厄勒布鲁省', 'Örebro County', 20),
('SE-E', 'SE', 'SUBDIVISION', '东约特兰省', 'Östergötland County', 21);

-- United Kingdom
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('GB-ENG', 'GB', 'SUBDIVISION', '英格兰', 'England', 1),
('GB-NIR', 'GB', 'SUBDIVISION', '北爱尔兰', 'Northern Ireland', 2),
('GB-SCT', 'GB', 'SUBDIVISION', '苏格兰', 'Scotland', 3),
('GB-WLS', 'GB', 'SUBDIVISION', '威尔士', 'Wales', 4);

-- Austria
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('AT-1', 'AT', 'SUBDIVISION', '布尔根兰州', 'Burgenland', 1),
('AT-2', 'AT', 'SUBDIVISION', '克恩顿州', 'Carinthia', 2),
('AT-3', 'AT', 'SUBDIVISION', '下奥地利州', 'Lower Austria', 3),
('AT-5', 'AT', 'SUBDIVISION', '萨尔茨堡州', 'Salzburg', 4),
('AT-6', 'AT', 'SUBDIVISION', '施蒂里亚州', 'Styria', 5),
('AT-7', 'AT', 'SUBDIVISION', '蒂罗尔州', 'Tyrol', 6),
('AT-4', 'AT', 'SUBDIVISION', '上奥地利州', 'Upper Austria', 7),
('AT-9', 'AT', 'SUBDIVISION', '维也纳', 'Vienna', 8),
('AT-8', 'AT', 'SUBDIVISION', '福拉尔贝格州', 'Vorarlberg', 9);

-- Belgium
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('BE-BRU', 'BE', 'SUBDIVISION', '布鲁塞尔首都大区', 'Brussels-Capital Region', 1),
('BE-VLG', 'BE', 'SUBDIVISION', '弗拉芒大区', 'Flanders', 2),
('BE-WAL', 'BE', 'SUBDIVISION', '瓦隆大区', 'Wallonia', 3);

-- France
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('FR-ARA', 'FR', 'SUBDIVISION', '奥弗涅-罗讷-阿尔卑斯大区', 'Auvergne-Rhône-Alpes', 1),
('FR-BFC', 'FR', 'SUBDIVISION', '勃艮第-弗朗什-孔泰大区', 'Bourgogne-Franche-Comté', 2),
('FR-BRE', 'FR', 'SUBDIVISION', '布列塔尼大区', 'Bretagne', 3),
('FR-CVL', 'FR', 'SUBDIVISION', '中央-卢瓦尔河谷大区', 'Centre-Val de Loire', 4),
('FR-20R', 'FR', 'SUBDIVISION', '科西嘉大区', 'Corse', 5),
('FR-GES', 'FR', 'SUBDIVISION', '大东部大区', 'Grand Est', 6),
('FR-971', 'FR', 'SUBDIVISION', '瓜德罗普大区', 'Guadeloupe', 7),
('FR-973', 'FR', 'SUBDIVISION', '圭亚那大区', 'Guyane', 8),
('FR-HDF', 'FR', 'SUBDIVISION', '上法兰西大区', 'Hauts-de-France', 9),
('FR-IDF', 'FR', 'SUBDIVISION', '法兰西岛大区', 'Île-de-France', 10),
('FR-974', 'FR', 'SUBDIVISION', '留尼汪大区', 'La Réunion', 11),
('FR-972', 'FR', 'SUBDIVISION', '马提尼克大区', 'Martinique', 12),
('FR-976', 'FR', 'SUBDIVISION', '马约特大区', 'Mayotte', 13),
('FR-NOR', 'FR', 'SUBDIVISION', '诺曼底大区', 'Normandie', 14),
('FR-NAQ', 'FR', 'SUBDIVISION', '新阿基坦大区', 'Nouvelle-Aquitaine', 15),
('FR-OCC', 'FR', 'SUBDIVISION', '奥克西塔尼大区', 'Occitanie', 16),
('FR-PDL', 'FR', 'SUBDIVISION', '卢瓦尔河地区大区', 'Pays de la Loire', 17),
('FR-PAC', 'FR', 'SUBDIVISION', '普罗旺斯-阿尔卑斯-蓝色海岸大区', 'Provence-Alpes-Côte d''Azur', 18);

-- Germany
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('DE-BW', 'DE', 'SUBDIVISION', '巴登-符腾堡州', 'Baden-Württemberg', 1),
('DE-BY', 'DE', 'SUBDIVISION', '巴伐利亚州', 'Bavaria', 2),
('DE-BE', 'DE', 'SUBDIVISION', '柏林', 'Berlin', 3),
('DE-BB', 'DE', 'SUBDIVISION', '勃兰登堡州', 'Brandenburg', 4),
('DE-HB', 'DE', 'SUBDIVISION', '不来梅州', 'Bremen', 5),
('DE-HH', 'DE', 'SUBDIVISION', '汉堡', 'Hamburg', 6),
('DE-HE', 'DE', 'SUBDIVISION', '黑森州', 'Hesse', 7),
('DE-NI', 'DE', 'SUBDIVISION', '下萨克森州', 'Lower Saxony', 8),
('DE-MV', 'DE', 'SUBDIVISION', '梅克伦堡-前波美拉尼亚州', 'Mecklenburg-Western Pomerania', 9),
('DE-NW', 'DE', 'SUBDIVISION', '北莱茵-威斯特法伦州', 'North Rhine-Westphalia', 10),
('DE-RP', 'DE', 'SUBDIVISION', '莱茵兰-普法尔茨州', 'Rhineland-Palatinate', 11),
('DE-SL', 'DE', 'SUBDIVISION', '萨尔州', 'Saarland', 12),
('DE-SN', 'DE', 'SUBDIVISION', '萨克森州', 'Saxony', 13),
('DE-ST', 'DE', 'SUBDIVISION', '萨克森-安哈尔特州', 'Saxony-Anhalt', 14),
('DE-SH', 'DE', 'SUBDIVISION', '石勒苏益格-荷尔斯泰因州', 'Schleswig-Holstein', 15),
('DE-TH', 'DE', 'SUBDIVISION', '图林根州', 'Thuringia', 16);

-- Liechtenstein
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('LI-01', 'LI', 'SUBDIVISION', '巴尔查斯', 'Balzers', 1),
('LI-02', 'LI', 'SUBDIVISION', '埃申', 'Eschen', 2),
('LI-03', 'LI', 'SUBDIVISION', '加姆普林', 'Gamprin', 3),
('LI-04', 'LI', 'SUBDIVISION', '毛伦', 'Mauren', 4),
('LI-05', 'LI', 'SUBDIVISION', '普兰肯', 'Planken', 5),
('LI-06', 'LI', 'SUBDIVISION', '鲁格尔', 'Ruggell', 6),
('LI-07', 'LI', 'SUBDIVISION', '沙恩', 'Schaan', 7),
('LI-08', 'LI', 'SUBDIVISION', '舍伦贝格', 'Schellenberg', 8),
('LI-09', 'LI', 'SUBDIVISION', '特里森', 'Triesen', 9),
('LI-10', 'LI', 'SUBDIVISION', '特里森贝格', 'Triesenberg', 10),
('LI-11', 'LI', 'SUBDIVISION', '瓦杜兹', 'Vaduz', 11);

-- Luxembourg
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('LU-CA', 'LU', 'SUBDIVISION', '卡佩伦县', 'Capellen', 1),
('LU-CL', 'LU', 'SUBDIVISION', '克莱沃县', 'Clervaux', 2),
('LU-DI', 'LU', 'SUBDIVISION', '迪基希县', 'Diekirch', 3),
('LU-EC', 'LU', 'SUBDIVISION', '埃希特纳赫县', 'Echternach', 4),
('LU-ES', 'LU', 'SUBDIVISION', '埃施苏尔阿尔泽特县', 'Esch-sur-Alzette', 5),
('LU-GR', 'LU', 'SUBDIVISION', '格雷文马赫县', 'Grevenmacher', 6),
('LU-LU', 'LU', 'SUBDIVISION', '卢森堡县', 'Luxembourg', 7),
('LU-ME', 'LU', 'SUBDIVISION', '梅尔施县', 'Mersch', 8),
('LU-RD', 'LU', 'SUBDIVISION', '雷当日县', 'Redange', 9),
('LU-RM', 'LU', 'SUBDIVISION', '雷米希县', 'Remich', 10),
('LU-VD', 'LU', 'SUBDIVISION', '维安登县', 'Vianden', 11),
('LU-WI', 'LU', 'SUBDIVISION', '维尔茨县', 'Wiltz', 12);

-- Netherlands (European provinces only; Caribbean special municipalities excluded as out of scope)
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('NL-DR', 'NL', 'SUBDIVISION', '德伦特省', 'Drenthe', 1),
('NL-FL', 'NL', 'SUBDIVISION', '弗莱福兰省', 'Flevoland', 2),
('NL-FR', 'NL', 'SUBDIVISION', '弗里斯兰省', 'Friesland', 3),
('NL-GE', 'NL', 'SUBDIVISION', '海尔德兰省', 'Gelderland', 4),
('NL-GR', 'NL', 'SUBDIVISION', '格罗宁根省', 'Groningen', 5),
('NL-LI', 'NL', 'SUBDIVISION', '林堡省', 'Limburg', 6),
('NL-NB', 'NL', 'SUBDIVISION', '北布拉班特省', 'North Brabant', 7),
('NL-NH', 'NL', 'SUBDIVISION', '北荷兰省', 'North Holland', 8),
('NL-OV', 'NL', 'SUBDIVISION', '上艾瑟尔省', 'Overijssel', 9),
('NL-ZH', 'NL', 'SUBDIVISION', '南荷兰省', 'South Holland', 10),
('NL-UT', 'NL', 'SUBDIVISION', '乌得勒支省', 'Utrecht', 11),
('NL-ZE', 'NL', 'SUBDIVISION', '泽兰省', 'Zeeland', 12);

-- Switzerland
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('CH-AG', 'CH', 'SUBDIVISION', '阿尔高州', 'Aargau', 1),
('CH-AR', 'CH', 'SUBDIVISION', '阿彭策尔外罗登州', 'Appenzell Ausserrhoden', 2),
('CH-AI', 'CH', 'SUBDIVISION', '阿彭策尔内罗登州', 'Appenzell Innerrhoden', 3),
('CH-BL', 'CH', 'SUBDIVISION', '巴塞尔乡村州', 'Basel-Landschaft', 4),
('CH-BS', 'CH', 'SUBDIVISION', '巴塞尔城市州', 'Basel-Stadt', 5),
('CH-BE', 'CH', 'SUBDIVISION', '伯尔尼州', 'Bern', 6),
('CH-FR', 'CH', 'SUBDIVISION', '弗里堡州', 'Fribourg', 7),
('CH-GE', 'CH', 'SUBDIVISION', '日内瓦州', 'Genève', 8),
('CH-GL', 'CH', 'SUBDIVISION', '格拉鲁斯州', 'Glarus', 9),
('CH-GR', 'CH', 'SUBDIVISION', '格劳宾登州', 'Graubünden', 10),
('CH-JU', 'CH', 'SUBDIVISION', '汝拉州', 'Jura', 11),
('CH-LU', 'CH', 'SUBDIVISION', '卢塞恩州', 'Luzern', 12),
('CH-NE', 'CH', 'SUBDIVISION', '纳沙泰尔州', 'Neuchâtel', 13),
('CH-NW', 'CH', 'SUBDIVISION', '下瓦尔登州', 'Nidwalden', 14),
('CH-OW', 'CH', 'SUBDIVISION', '上瓦尔登州', 'Obwalden', 15),
('CH-SG', 'CH', 'SUBDIVISION', '圣加仑州', 'Sankt Gallen', 16),
('CH-SH', 'CH', 'SUBDIVISION', '沙夫豪森州', 'Schaffhausen', 17),
('CH-SZ', 'CH', 'SUBDIVISION', '施维茨州', 'Schwyz', 18),
('CH-SO', 'CH', 'SUBDIVISION', '索洛图恩州', 'Solothurn', 19),
('CH-TG', 'CH', 'SUBDIVISION', '图尔高州', 'Thurgau', 20),
('CH-TI', 'CH', 'SUBDIVISION', '提契诺州', 'Ticino', 21),
('CH-UR', 'CH', 'SUBDIVISION', '乌里州', 'Uri', 22),
('CH-VS', 'CH', 'SUBDIVISION', '瓦莱州', 'Valais', 23),
('CH-VD', 'CH', 'SUBDIVISION', '沃州', 'Vaud', 24),
('CH-ZG', 'CH', 'SUBDIVISION', '楚格州', 'Zug', 25),
('CH-ZH', 'CH', 'SUBDIVISION', '苏黎世州', 'Zürich', 26);

-- Holy See: no first-level administrative subdivisions
-- Monaco: no first-level administrative subdivisions

-- Albania
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('AL-01', 'AL', 'SUBDIVISION', '培拉特州', 'Berat County', 1),
('AL-09', 'AL', 'SUBDIVISION', '迪勃拉州', 'Dibër County', 2),
('AL-02', 'AL', 'SUBDIVISION', '都拉斯州', 'Durrës County', 3),
('AL-03', 'AL', 'SUBDIVISION', '爱尔巴桑州', 'Elbasan County', 4),
('AL-04', 'AL', 'SUBDIVISION', '费里州', 'Fier County', 5),
('AL-05', 'AL', 'SUBDIVISION', '吉罗卡斯特州', 'Gjirokastër County', 6),
('AL-06', 'AL', 'SUBDIVISION', '科尔察州', 'Korçë County', 7),
('AL-07', 'AL', 'SUBDIVISION', '库克斯州', 'Kukës County', 8),
('AL-08', 'AL', 'SUBDIVISION', '莱什州', 'Lezhë County', 9),
('AL-10', 'AL', 'SUBDIVISION', '什科德尔州', 'Shkodër County', 10),
('AL-11', 'AL', 'SUBDIVISION', '地拉那州', 'Tiranë County', 11),
('AL-12', 'AL', 'SUBDIVISION', '发罗拉州', 'Vlorë County', 12);

-- Andorra
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('AD-07', 'AD', 'SUBDIVISION', '安道尔城教区', 'Andorra la Vella', 1),
('AD-02', 'AD', 'SUBDIVISION', '卡尼略教区', 'Canillo', 2),
('AD-03', 'AD', 'SUBDIVISION', '恩坎普教区', 'Encamp', 3),
('AD-08', 'AD', 'SUBDIVISION', '埃斯卡尔德斯-恩戈尔达尼教区', 'Escaldes-Engordany', 4),
('AD-04', 'AD', 'SUBDIVISION', '拉马萨那教区', 'La Massana', 5),
('AD-05', 'AD', 'SUBDIVISION', '奥尔迪诺教区', 'Ordino', 6),
('AD-06', 'AD', 'SUBDIVISION', '圣胡利娅-德洛里亚教区', 'Sant Julià de Lòria', 7);

-- Bosnia and Herzegovina
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('BA-BRC', 'BA', 'SUBDIVISION', '布尔奇科特区', 'Brčko District', 1),
('BA-BIH', 'BA', 'SUBDIVISION', '波黑联邦', 'Federation of Bosnia and Herzegovina', 2),
('BA-SRP', 'BA', 'SUBDIVISION', '塞族共和国', 'Republika Srpska', 3);

-- Croatia
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('HR-01', 'HR', 'SUBDIVISION', '萨格勒布县', 'Zagreb County', 1),
('HR-02', 'HR', 'SUBDIVISION', '克拉皮纳-扎戈列县', 'Krapina-Zagorje County', 2),
('HR-03', 'HR', 'SUBDIVISION', '锡萨克-莫斯拉维纳县', 'Sisak-Moslavina County', 3),
('HR-04', 'HR', 'SUBDIVISION', '卡尔洛瓦茨县', 'Karlovac County', 4),
('HR-05', 'HR', 'SUBDIVISION', '瓦拉日丁县', 'Varaždin County', 5),
('HR-06', 'HR', 'SUBDIVISION', '科普里夫尼察-克里热夫齐县', 'Koprivnica-Križevci County', 6),
('HR-07', 'HR', 'SUBDIVISION', '别洛瓦尔-比洛戈拉县', 'Bjelovar-Bilogora County', 7),
('HR-08', 'HR', 'SUBDIVISION', '滨海-戈尔斯基科塔尔县', 'Primorje-Gorski Kotar County', 8),
('HR-09', 'HR', 'SUBDIVISION', '利卡-塞尼县', 'Lika-Senj County', 9),
('HR-10', 'HR', 'SUBDIVISION', '维罗维蒂察-波德拉维纳县', 'Virovitica-Podravina County', 10),
('HR-11', 'HR', 'SUBDIVISION', '波热加-斯拉沃尼亚县', 'Požega-Slavonia County', 11),
('HR-12', 'HR', 'SUBDIVISION', '布罗德-波萨维纳县', 'Brod-Posavina County', 12),
('HR-13', 'HR', 'SUBDIVISION', '扎达尔县', 'Zadar County', 13),
('HR-14', 'HR', 'SUBDIVISION', '奥西耶克-巴拉尼亚县', 'Osijek-Baranja County', 14),
('HR-15', 'HR', 'SUBDIVISION', '希贝尼克-克宁县', 'Šibenik-Knin County', 15),
('HR-16', 'HR', 'SUBDIVISION', '武科瓦尔-斯里耶姆县', 'Vukovar-Srijem County', 16),
('HR-17', 'HR', 'SUBDIVISION', '斯普利特-达尔马提亚县', 'Split-Dalmatia County', 17),
('HR-18', 'HR', 'SUBDIVISION', '伊斯特拉县', 'Istria County', 18),
('HR-19', 'HR', 'SUBDIVISION', '杜布罗夫尼克-内雷特瓦县', 'Dubrovnik-Neretva County', 19),
('HR-20', 'HR', 'SUBDIVISION', '梅吉穆列县', 'Međimurje County', 20),
('HR-21', 'HR', 'SUBDIVISION', '萨格勒布市', 'Zagreb City', 21);

-- Greece
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('GR-A', 'GR', 'SUBDIVISION', '东马其顿和色雷斯大区', 'Eastern Macedonia and Thrace', 1),
('GR-B', 'GR', 'SUBDIVISION', '中马其顿大区', 'Central Macedonia', 2),
('GR-C', 'GR', 'SUBDIVISION', '西马其顿大区', 'Western Macedonia', 3),
('GR-D', 'GR', 'SUBDIVISION', '伊庇鲁斯大区', 'Epirus', 4),
('GR-E', 'GR', 'SUBDIVISION', '塞萨利大区', 'Thessaly', 5),
('GR-F', 'GR', 'SUBDIVISION', '伊奥尼亚群岛大区', 'Ionian Islands', 6),
('GR-G', 'GR', 'SUBDIVISION', '西希腊大区', 'Western Greece', 7),
('GR-H', 'GR', 'SUBDIVISION', '中希腊大区', 'Central Greece', 8),
('GR-I', 'GR', 'SUBDIVISION', '阿提卡大区', 'Attica', 9),
('GR-J', 'GR', 'SUBDIVISION', '伯罗奔尼撒大区', 'Peloponnese', 10),
('GR-K', 'GR', 'SUBDIVISION', '北爱琴大区', 'North Aegean', 11),
('GR-L', 'GR', 'SUBDIVISION', '南爱琴大区', 'South Aegean', 12),
('GR-M', 'GR', 'SUBDIVISION', '克里特大区', 'Crete', 13);

-- Italy
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('IT-65', 'IT', 'SUBDIVISION', '阿布鲁佐大区', 'Abruzzo', 1),
('IT-77', 'IT', 'SUBDIVISION', '巴西利卡塔大区', 'Basilicata', 2),
('IT-78', 'IT', 'SUBDIVISION', '卡拉布里亚大区', 'Calabria', 3),
('IT-72', 'IT', 'SUBDIVISION', '坎帕尼亚大区', 'Campania', 4),
('IT-45', 'IT', 'SUBDIVISION', '艾米利亚-罗马涅大区', 'Emilia-Romagna', 5),
('IT-36', 'IT', 'SUBDIVISION', '弗留利-威尼斯朱利亚大区', 'Friuli Venezia Giulia', 6),
('IT-62', 'IT', 'SUBDIVISION', '拉齐奥大区', 'Lazio', 7),
('IT-42', 'IT', 'SUBDIVISION', '利古里亚大区', 'Liguria', 8),
('IT-25', 'IT', 'SUBDIVISION', '伦巴第大区', 'Lombardy', 9),
('IT-57', 'IT', 'SUBDIVISION', '马尔凯大区', 'Marche', 10),
('IT-67', 'IT', 'SUBDIVISION', '莫利塞大区', 'Molise', 11),
('IT-21', 'IT', 'SUBDIVISION', '皮埃蒙特大区', 'Piedmont', 12),
('IT-75', 'IT', 'SUBDIVISION', '普利亚大区', 'Apulia', 13),
('IT-88', 'IT', 'SUBDIVISION', '撒丁大区', 'Sardinia', 14),
('IT-82', 'IT', 'SUBDIVISION', '西西里大区', 'Sicily', 15),
('IT-52', 'IT', 'SUBDIVISION', '托斯卡纳大区', 'Tuscany', 16),
('IT-32', 'IT', 'SUBDIVISION', '特伦蒂诺-上阿迪杰大区', 'Trentino-South Tyrol', 17),
('IT-55', 'IT', 'SUBDIVISION', '翁布里亚大区', 'Umbria', 18),
('IT-23', 'IT', 'SUBDIVISION', '瓦莱达奥斯塔大区', 'Aosta Valley', 19),
('IT-34', 'IT', 'SUBDIVISION', '威尼托大区', 'Veneto', 20);

-- Kosovo (no official ISO 3166-2 subdivision codes exist; XK-xx codes below are constructed for internal consistency, not an ISO standard)
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('XK-FE', 'XK', 'SUBDIVISION', '费里扎伊区', 'Ferizaj District', 1),
('XK-GJK', 'XK', 'SUBDIVISION', '加科瓦区', 'Gjakova District', 2),
('XK-GJL', 'XK', 'SUBDIVISION', '吉拉恩区', 'Gjilan District', 3),
('XK-MI', 'XK', 'SUBDIVISION', '米特罗维察区', 'Mitrovica District', 4),
('XK-PE', 'XK', 'SUBDIVISION', '佩奇区', 'Peja District', 5),
('XK-PR', 'XK', 'SUBDIVISION', '普里什蒂纳区', 'Pristina District', 6),
('XK-PZ', 'XK', 'SUBDIVISION', '普里兹伦区', 'Prizren District', 7);

-- Malta
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('MT-01', 'MT', 'SUBDIVISION', '阿塔德', 'Attard', 1),
('MT-02', 'MT', 'SUBDIVISION', '巴尔赞', 'Balzan', 2),
('MT-03', 'MT', 'SUBDIVISION', '比尔古', 'Birgu', 3),
('MT-04', 'MT', 'SUBDIVISION', '比尔基卡拉', 'Birkirkara', 4),
('MT-05', 'MT', 'SUBDIVISION', '比尔泽布贾', 'Birżebbuġa', 5),
('MT-06', 'MT', 'SUBDIVISION', '博尔姆拉', 'Bormla', 6),
('MT-07', 'MT', 'SUBDIVISION', '丁利', 'Dingli', 7),
('MT-08', 'MT', 'SUBDIVISION', '弗古拉', 'Fgura', 8),
('MT-09', 'MT', 'SUBDIVISION', '弗洛里亚纳', 'Floriana', 9),
('MT-10', 'MT', 'SUBDIVISION', '丰塔纳', 'Fontana', 10),
('MT-11', 'MT', 'SUBDIVISION', '古贾', 'Gudja', 11),
('MT-12', 'MT', 'SUBDIVISION', '吉拉', 'Gżira', 12),
('MT-13', 'MT', 'SUBDIVISION', '艾因西莱姆', 'Għajnsielem', 13),
('MT-14', 'MT', 'SUBDIVISION', '加尔布', 'Għarb', 14),
('MT-15', 'MT', 'SUBDIVISION', '加尔古尔', 'Għargħur', 15),
('MT-16', 'MT', 'SUBDIVISION', '加斯里', 'Għasri', 16),
('MT-17', 'MT', 'SUBDIVISION', '加沙克', 'Għaxaq', 17),
('MT-18', 'MT', 'SUBDIVISION', '哈姆伦', 'Ħamrun', 18),
('MT-19', 'MT', 'SUBDIVISION', '伊克林', 'Iklin', 19),
('MT-20', 'MT', 'SUBDIVISION', '伊斯拉', 'Isla', 20),
('MT-21', 'MT', 'SUBDIVISION', '卡尔卡拉', 'Kalkara', 21),
('MT-22', 'MT', 'SUBDIVISION', '凯尔切姆', 'Kerċem', 22),
('MT-23', 'MT', 'SUBDIVISION', '基尔科普', 'Kirkop', 23),
('MT-24', 'MT', 'SUBDIVISION', '利亚', 'Lija', 24),
('MT-25', 'MT', 'SUBDIVISION', '卢卡', 'Luqa', 25),
('MT-26', 'MT', 'SUBDIVISION', '马尔萨', 'Marsa', 26),
('MT-27', 'MT', 'SUBDIVISION', '马尔萨斯卡拉', 'Marsaskala', 27),
('MT-28', 'MT', 'SUBDIVISION', '马尔萨什洛克', 'Marsaxlokk', 28),
('MT-29', 'MT', 'SUBDIVISION', '姆迪纳', 'Mdina', 29),
('MT-30', 'MT', 'SUBDIVISION', '梅利哈', 'Mellieħa', 30),
('MT-31', 'MT', 'SUBDIVISION', '姆加尔', 'Mġarr', 31),
('MT-32', 'MT', 'SUBDIVISION', '莫斯塔', 'Mosta', 32),
('MT-33', 'MT', 'SUBDIVISION', '姆卡巴', 'Mqabba', 33),
('MT-34', 'MT', 'SUBDIVISION', '姆西达', 'Msida', 34),
('MT-35', 'MT', 'SUBDIVISION', '姆塔尔法', 'Mtarfa', 35),
('MT-36', 'MT', 'SUBDIVISION', '蒙沙尔', 'Munxar', 36),
('MT-37', 'MT', 'SUBDIVISION', '纳杜尔', 'Nadur', 37),
('MT-38', 'MT', 'SUBDIVISION', '纳沙尔', 'Naxxar', 38),
('MT-39', 'MT', 'SUBDIVISION', '保拉', 'Paola', 39),
('MT-40', 'MT', 'SUBDIVISION', '彭布罗克', 'Pembroke', 40),
('MT-41', 'MT', 'SUBDIVISION', '皮耶塔', 'Pietà', 41),
('MT-42', 'MT', 'SUBDIVISION', '卡拉', 'Qala', 42),
('MT-43', 'MT', 'SUBDIVISION', '科尔米', 'Qormi', 43),
('MT-44', 'MT', 'SUBDIVISION', '克伦迪', 'Qrendi', 44),
('MT-45', 'MT', 'SUBDIVISION', '拉巴特(戈佐)', 'Rabat Gozo', 45),
('MT-46', 'MT', 'SUBDIVISION', '拉巴特(马耳他)', 'Rabat Malta', 46),
('MT-47', 'MT', 'SUBDIVISION', '萨菲', 'Safi', 47),
('MT-48', 'MT', 'SUBDIVISION', '圣朱利安', 'Saint Julian''s', 48),
('MT-49', 'MT', 'SUBDIVISION', '圣约翰', 'Saint John', 49),
('MT-50', 'MT', 'SUBDIVISION', '圣劳伦斯', 'Saint Lawrence', 50),
('MT-51', 'MT', 'SUBDIVISION', '圣保罗湾', 'Saint Paul''s Bay', 51),
('MT-52', 'MT', 'SUBDIVISION', '桑纳特', 'Sannat', 52),
('MT-53', 'MT', 'SUBDIVISION', '圣露西亚', 'Saint Lucia''s', 53),
('MT-54', 'MT', 'SUBDIVISION', '圣维内拉', 'Santa Venera', 54),
('MT-55', 'MT', 'SUBDIVISION', '西吉维', 'Siġġiewi', 55),
('MT-56', 'MT', 'SUBDIVISION', '斯利马', 'Sliema', 56),
('MT-57', 'MT', 'SUBDIVISION', '斯维耶基', 'Swieqi', 57),
('MT-58', 'MT', 'SUBDIVISION', '塔什比耶克斯', 'Ta'' Xbiex', 58),
('MT-59', 'MT', 'SUBDIVISION', '塔尔申', 'Tarxien', 59),
('MT-60', 'MT', 'SUBDIVISION', '瓦莱塔', 'Valletta', 60),
('MT-61', 'MT', 'SUBDIVISION', '沙格拉', 'Xagħra', 61),
('MT-62', 'MT', 'SUBDIVISION', '谢乌基亚', 'Xewkija', 62),
('MT-63', 'MT', 'SUBDIVISION', '沙伊拉', 'Xgħajra', 63),
('MT-64', 'MT', 'SUBDIVISION', '扎巴尔', 'Żabbar', 64),
('MT-65', 'MT', 'SUBDIVISION', '泽布吉(戈佐)', 'Żebbuġ Gozo', 65),
('MT-66', 'MT', 'SUBDIVISION', '泽布吉(马耳他)', 'Żebbuġ Malta', 66),
('MT-67', 'MT', 'SUBDIVISION', '泽伊敦', 'Żejtun', 67),
('MT-68', 'MT', 'SUBDIVISION', '祖列克', 'Żurrieq', 68);

-- Montenegro
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('ME-01', 'ME', 'SUBDIVISION', '安德里耶维察', 'Andrijevica', 1),
('ME-02', 'ME', 'SUBDIVISION', '巴尔', 'Bar', 2),
('ME-03', 'ME', 'SUBDIVISION', '别拉内', 'Berane', 3),
('ME-04', 'ME', 'SUBDIVISION', '比耶洛波列', 'Bijelo Polje', 4),
('ME-05', 'ME', 'SUBDIVISION', '布德瓦', 'Budva', 5),
('ME-06', 'ME', 'SUBDIVISION', '采蒂涅', 'Cetinje', 6),
('ME-07', 'ME', 'SUBDIVISION', '达尼洛夫格勒', 'Danilovgrad', 7),
('ME-08', 'ME', 'SUBDIVISION', '黑采格诺维', 'Herceg-Novi', 8),
('ME-09', 'ME', 'SUBDIVISION', '科拉欣', 'Kolašin', 9),
('ME-10', 'ME', 'SUBDIVISION', '科托尔', 'Kotor', 10),
('ME-11', 'ME', 'SUBDIVISION', '莫伊科瓦茨', 'Mojkovac', 11),
('ME-12', 'ME', 'SUBDIVISION', '尼克希奇', 'Nikšić', 12),
('ME-13', 'ME', 'SUBDIVISION', '普拉夫', 'Plav', 13),
('ME-14', 'ME', 'SUBDIVISION', '普列夫利亚', 'Pljevlja', 14),
('ME-15', 'ME', 'SUBDIVISION', '普卢日内', 'Plužine', 15),
('ME-16', 'ME', 'SUBDIVISION', '波德戈里察', 'Podgorica', 16),
('ME-17', 'ME', 'SUBDIVISION', '罗扎耶', 'Rožaje', 17),
('ME-18', 'ME', 'SUBDIVISION', '沙夫尼克', 'Šavnik', 18),
('ME-19', 'ME', 'SUBDIVISION', '蒂瓦特', 'Tivat', 19),
('ME-20', 'ME', 'SUBDIVISION', '乌尔齐尼', 'Ulcinj', 20),
('ME-21', 'ME', 'SUBDIVISION', '扎布利亚克', 'Žabljak', 21),
('ME-22', 'ME', 'SUBDIVISION', '古西涅', 'Gusinje', 22),
('ME-23', 'ME', 'SUBDIVISION', '佩特尼察', 'Petnjica', 23),
('ME-24', 'ME', 'SUBDIVISION', '图济', 'Tuzi', 24),
('ME-25', 'ME', 'SUBDIVISION', '泽塔', 'Zeta', 25);

-- Portugal
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('PT-01', 'PT', 'SUBDIVISION', '阿威罗区', 'Aveiro', 1),
('PT-02', 'PT', 'SUBDIVISION', '贝雅区', 'Beja', 2),
('PT-03', 'PT', 'SUBDIVISION', '布拉加区', 'Braga', 3),
('PT-04', 'PT', 'SUBDIVISION', '布拉干萨区', 'Bragança', 4),
('PT-05', 'PT', 'SUBDIVISION', '卡斯特洛布兰科区', 'Castelo Branco', 5),
('PT-06', 'PT', 'SUBDIVISION', '科英布拉区', 'Coimbra', 6),
('PT-07', 'PT', 'SUBDIVISION', '埃武拉区', 'Évora', 7),
('PT-08', 'PT', 'SUBDIVISION', '法鲁区', 'Faro', 8),
('PT-09', 'PT', 'SUBDIVISION', '瓜达区', 'Guarda', 9),
('PT-10', 'PT', 'SUBDIVISION', '莱里亚区', 'Leiria', 10),
('PT-11', 'PT', 'SUBDIVISION', '里斯本区', 'Lisbon', 11),
('PT-12', 'PT', 'SUBDIVISION', '波塔莱格雷区', 'Portalegre', 12),
('PT-13', 'PT', 'SUBDIVISION', '波尔图区', 'Porto', 13),
('PT-14', 'PT', 'SUBDIVISION', '圣塔伦区', 'Santarém', 14),
('PT-15', 'PT', 'SUBDIVISION', '塞图巴尔区', 'Setúbal', 15),
('PT-16', 'PT', 'SUBDIVISION', '维亚纳堡区', 'Viana do Castelo', 16),
('PT-17', 'PT', 'SUBDIVISION', '维拉雷亚尔区', 'Vila Real', 17),
('PT-18', 'PT', 'SUBDIVISION', '维塞乌区', 'Viseu', 18),
('PT-20', 'PT', 'SUBDIVISION', '亚速尔群岛自治区', 'Azores', 19),
('PT-30', 'PT', 'SUBDIVISION', '马德拉群岛自治区', 'Madeira', 20);

-- San Marino
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('SM-01', 'SM', 'SUBDIVISION', '阿夸维瓦', 'Acquaviva', 1),
('SM-02', 'SM', 'SUBDIVISION', '基耶萨努奥瓦', 'Chiesanuova', 2),
('SM-03', 'SM', 'SUBDIVISION', '多马尼亚诺', 'Domagnano', 3),
('SM-04', 'SM', 'SUBDIVISION', '法埃塔诺', 'Faetano', 4),
('SM-05', 'SM', 'SUBDIVISION', '菲奥伦蒂诺', 'Fiorentino', 5),
('SM-06', 'SM', 'SUBDIVISION', '博尔戈马焦雷', 'Borgo Maggiore', 6),
('SM-07', 'SM', 'SUBDIVISION', '圣马力诺市', 'Città di San Marino', 7),
('SM-08', 'SM', 'SUBDIVISION', '蒙泰贾尔迪诺', 'Montegiardino', 8),
('SM-09', 'SM', 'SUBDIVISION', '塞拉瓦莱', 'Serravalle', 9);

-- Serbia (RS-KM Kosovo and Metohija autonomous province and its constituent districts are
-- excluded here since Kosovo is modeled as an independent country in this dataset)
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('RS-00', 'RS', 'SUBDIVISION', '贝尔格莱德市', 'City of Belgrade', 1),
('RS-01', 'RS', 'SUBDIVISION', '北巴奇卡区', 'North Bačka District', 2),
('RS-02', 'RS', 'SUBDIVISION', '中巴纳特区', 'Central Banat District', 3),
('RS-03', 'RS', 'SUBDIVISION', '北巴纳特区', 'North Banat District', 4),
('RS-04', 'RS', 'SUBDIVISION', '南巴纳特区', 'South Banat District', 5),
('RS-05', 'RS', 'SUBDIVISION', '西巴奇卡区', 'West Bačka District', 6),
('RS-06', 'RS', 'SUBDIVISION', '南巴奇卡区', 'South Bačka District', 7),
('RS-07', 'RS', 'SUBDIVISION', '斯雷姆区', 'Srem District', 8),
('RS-08', 'RS', 'SUBDIVISION', '马奇瓦区', 'Mačva District', 9),
('RS-09', 'RS', 'SUBDIVISION', '科卢巴拉区', 'Kolubara District', 10),
('RS-10', 'RS', 'SUBDIVISION', '波杜纳夫列区', 'Podunavlje District', 11),
('RS-11', 'RS', 'SUBDIVISION', '布拉尼切沃区', 'Braničevo District', 12),
('RS-12', 'RS', 'SUBDIVISION', '舒马迪亚区', 'Šumadija District', 13),
('RS-13', 'RS', 'SUBDIVISION', '波莫拉维区', 'Pomoravlje District', 14),
('RS-14', 'RS', 'SUBDIVISION', '博尔区', 'Bor District', 15),
('RS-15', 'RS', 'SUBDIVISION', '扎耶查尔区', 'Zaječar District', 16),
('RS-16', 'RS', 'SUBDIVISION', '兹拉蒂博尔区', 'Zlatibor District', 17),
('RS-17', 'RS', 'SUBDIVISION', '莫拉维察区', 'Moravica District', 18),
('RS-18', 'RS', 'SUBDIVISION', '拉什卡区', 'Raška District', 19),
('RS-19', 'RS', 'SUBDIVISION', '拉西纳区', 'Rasina District', 20),
('RS-20', 'RS', 'SUBDIVISION', '尼沙瓦区', 'Nišava District', 21),
('RS-21', 'RS', 'SUBDIVISION', '托普利察区', 'Toplica District', 22),
('RS-22', 'RS', 'SUBDIVISION', '皮罗特区', 'Pirot District', 23),
('RS-23', 'RS', 'SUBDIVISION', '亚布拉尼察区', 'Jablanica District', 24),
('RS-24', 'RS', 'SUBDIVISION', '普契尼亚区', 'Pčinja District', 25),
('RS-VO', 'RS', 'SUBDIVISION', '伏伊伏丁那自治省', 'Vojvodina', 26);

-- Spain
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('ES-AN', 'ES', 'SUBDIVISION', '安达卢西亚自治区', 'Andalusia', 1),
('ES-AR', 'ES', 'SUBDIVISION', '阿拉贡自治区', 'Aragon', 2),
('ES-AS', 'ES', 'SUBDIVISION', '阿斯图里亚斯自治区', 'Principality of Asturias', 3),
('ES-IB', 'ES', 'SUBDIVISION', '巴利阿里群岛自治区', 'Balearic Islands', 4),
('ES-PV', 'ES', 'SUBDIVISION', '巴斯克自治区', 'Basque Country', 5),
('ES-CN', 'ES', 'SUBDIVISION', '加那利群岛自治区', 'Canary Islands', 6),
('ES-CB', 'ES', 'SUBDIVISION', '坎塔布里亚自治区', 'Cantabria', 7),
('ES-CL', 'ES', 'SUBDIVISION', '卡斯蒂利亚-莱昂自治区', 'Castile and León', 8),
('ES-CM', 'ES', 'SUBDIVISION', '卡斯蒂利亚-拉曼恰自治区', 'Castile-La Mancha', 9),
('ES-CT', 'ES', 'SUBDIVISION', '加泰罗尼亚自治区', 'Catalonia', 10),
('ES-CE', 'ES', 'SUBDIVISION', '休达自治市', 'Ceuta', 11),
('ES-EX', 'ES', 'SUBDIVISION', '埃斯特雷马杜拉自治区', 'Extremadura', 12),
('ES-GA', 'ES', 'SUBDIVISION', '加利西亚自治区', 'Galicia', 13),
('ES-RI', 'ES', 'SUBDIVISION', '拉里奥哈自治区', 'La Rioja', 14),
('ES-MD', 'ES', 'SUBDIVISION', '马德里自治区', 'Community of Madrid', 15),
('ES-ML', 'ES', 'SUBDIVISION', '梅利利亚自治市', 'Melilla', 16),
('ES-MC', 'ES', 'SUBDIVISION', '穆尔西亚自治区', 'Region of Murcia', 17),
('ES-NC', 'ES', 'SUBDIVISION', '纳瓦拉自治区', 'Chartered Community of Navarre', 18),
('ES-VC', 'ES', 'SUBDIVISION', '瓦伦西亚自治区', 'Valencian Community', 19);

-- North Macedonia
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('MK-801', 'MK', 'SUBDIVISION', '阿埃罗德鲁姆', 'Aerodrom', 1),
('MK-802', 'MK', 'SUBDIVISION', '阿拉钦诺沃', 'Aračinovo', 2),
('MK-201', 'MK', 'SUBDIVISION', '贝罗沃', 'Berovo', 3),
('MK-501', 'MK', 'SUBDIVISION', '比托拉', 'Bitola', 4),
('MK-401', 'MK', 'SUBDIVISION', '博格丹齐', 'Bogdanci', 5),
('MK-601', 'MK', 'SUBDIVISION', '博戈维涅', 'Bogovinje', 6),
('MK-402', 'MK', 'SUBDIVISION', '博西洛沃', 'Bosilovo', 7),
('MK-602', 'MK', 'SUBDIVISION', '布尔韦尼察', 'Brvenica', 8),
('MK-803', 'MK', 'SUBDIVISION', '布特尔', 'Butel', 9),
('MK-814', 'MK', 'SUBDIVISION', '中央区', 'Centar', 10),
('MK-313', 'MK', 'SUBDIVISION', '中央茹帕', 'Centar Župa', 11),
('MK-815', 'MK', 'SUBDIVISION', '恰伊尔', 'Čair', 12),
('MK-109', 'MK', 'SUBDIVISION', '恰什卡', 'Čaška', 13),
('MK-210', 'MK', 'SUBDIVISION', '切希诺沃-奥布莱舍沃', 'Češinovo-Obleševo', 14),
('MK-816', 'MK', 'SUBDIVISION', '丘切尔-桑德沃', 'Čučer-Sandevo', 15),
('MK-303', 'MK', 'SUBDIVISION', '德巴尔', 'Debar', 16),
('MK-304', 'MK', 'SUBDIVISION', '德布尔察', 'Debrca', 17),
('MK-203', 'MK', 'SUBDIVISION', '德尔切沃', 'Delčevo', 18),
('MK-502', 'MK', 'SUBDIVISION', '德米尔希萨尔', 'Demir Hisar', 19),
('MK-103', 'MK', 'SUBDIVISION', '德米尔卡皮亚', 'Demir Kapija', 20),
('MK-406', 'MK', 'SUBDIVISION', '多伊兰', 'Dojran', 21),
('MK-503', 'MK', 'SUBDIVISION', '多尔内尼', 'Dolneni', 22),
('MK-804', 'MK', 'SUBDIVISION', '加齐巴巴', 'Gazi Baba', 23),
('MK-405', 'MK', 'SUBDIVISION', '格夫盖利亚', 'Gevgelija', 24),
('MK-805', 'MK', 'SUBDIVISION', '乔尔切彼得罗夫', 'Gjorče Petrov', 25),
('MK-604', 'MK', 'SUBDIVISION', '戈斯蒂瓦尔', 'Gostivar', 26),
('MK-102', 'MK', 'SUBDIVISION', '格拉茨科', 'Gradsko', 27),
('MK-807', 'MK', 'SUBDIVISION', '伊林登', 'Ilinden', 28),
('MK-606', 'MK', 'SUBDIVISION', '耶古诺夫策', 'Jegunovce', 29),
('MK-205', 'MK', 'SUBDIVISION', '卡尔宾齐', 'Karbinci', 30),
('MK-808', 'MK', 'SUBDIVISION', '卡尔波什', 'Karpoš', 31),
('MK-104', 'MK', 'SUBDIVISION', '卡瓦达尔齐', 'Kavadarci', 32),
('MK-307', 'MK', 'SUBDIVISION', '基切沃', 'Kičevo', 33),
('MK-809', 'MK', 'SUBDIVISION', '基塞拉沃达', 'Kisela Voda', 34),
('MK-206', 'MK', 'SUBDIVISION', '科查尼', 'Kočani', 35),
('MK-407', 'MK', 'SUBDIVISION', '孔切', 'Konče', 36),
('MK-701', 'MK', 'SUBDIVISION', '克拉托沃', 'Kratovo', 37),
('MK-702', 'MK', 'SUBDIVISION', '克里瓦帕兰卡', 'Kriva Palanka', 38),
('MK-504', 'MK', 'SUBDIVISION', '克里沃加什塔尼', 'Krivogaštani', 39),
('MK-505', 'MK', 'SUBDIVISION', '克鲁舍沃', 'Kruševo', 40),
('MK-703', 'MK', 'SUBDIVISION', '库马诺沃', 'Kumanovo', 41),
('MK-704', 'MK', 'SUBDIVISION', '利普科沃', 'Lipkovo', 42),
('MK-105', 'MK', 'SUBDIVISION', '洛佐沃', 'Lozovo', 43),
('MK-207', 'MK', 'SUBDIVISION', '马其顿卡缅尼察', 'Makedonska Kamenica', 44),
('MK-308', 'MK', 'SUBDIVISION', '马其顿布罗德', 'Makedonski Brod', 45),
('MK-607', 'MK', 'SUBDIVISION', '马夫罗沃-罗斯图舍', 'Mavrovo i Rostuše', 46),
('MK-506', 'MK', 'SUBDIVISION', '莫吉拉', 'Mogila', 47),
('MK-106', 'MK', 'SUBDIVISION', '内戈蒂诺', 'Negotino', 48),
('MK-507', 'MK', 'SUBDIVISION', '诺瓦齐', 'Novaci', 49),
('MK-408', 'MK', 'SUBDIVISION', '诺沃塞洛', 'Novo Selo', 50),
('MK-310', 'MK', 'SUBDIVISION', '奥赫里德', 'Ohrid', 51),
('MK-208', 'MK', 'SUBDIVISION', '佩切沃', 'Pehčevo', 52),
('MK-810', 'MK', 'SUBDIVISION', '彼得罗韦茨', 'Petrovec', 53),
('MK-311', 'MK', 'SUBDIVISION', '普拉斯尼察', 'Plasnica', 54),
('MK-508', 'MK', 'SUBDIVISION', '普里莱普', 'Prilep', 55),
('MK-209', 'MK', 'SUBDIVISION', '普罗比什蒂普', 'Probištip', 56),
('MK-409', 'MK', 'SUBDIVISION', '拉多维什', 'Radoviš', 57),
('MK-705', 'MK', 'SUBDIVISION', '兰科夫策', 'Rankovce', 58),
('MK-509', 'MK', 'SUBDIVISION', '雷森', 'Resen', 59),
('MK-107', 'MK', 'SUBDIVISION', '罗索曼', 'Rosoman', 60),
('MK-811', 'MK', 'SUBDIVISION', '萨拉伊', 'Saraj', 61),
('MK-812', 'MK', 'SUBDIVISION', '索皮什特', 'Sopište', 62),
('MK-706', 'MK', 'SUBDIVISION', '老纳戈里恰内', 'Staro Nagoričane', 63),
('MK-312', 'MK', 'SUBDIVISION', '斯特鲁加', 'Struga', 64),
('MK-410', 'MK', 'SUBDIVISION', '斯特鲁米察', 'Strumica', 65),
('MK-813', 'MK', 'SUBDIVISION', '斯图代尼察尼', 'Studeničani', 66),
('MK-108', 'MK', 'SUBDIVISION', '圣尼古拉', 'Sveti Nikole', 67),
('MK-211', 'MK', 'SUBDIVISION', '什蒂普', 'Štip', 68),
('MK-817', 'MK', 'SUBDIVISION', '舒托奥里扎里', 'Šuto Orizari', 69),
('MK-608', 'MK', 'SUBDIVISION', '特阿尔采', 'Tearce', 70),
('MK-609', 'MK', 'SUBDIVISION', '泰托沃', 'Tetovo', 71),
('MK-403', 'MK', 'SUBDIVISION', '瓦兰多沃', 'Valandovo', 72),
('MK-404', 'MK', 'SUBDIVISION', '瓦西列沃', 'Vasilevo', 73),
('MK-101', 'MK', 'SUBDIVISION', '维莱斯', 'Veles', 74),
('MK-301', 'MK', 'SUBDIVISION', '韦夫察尼', 'Vevčani', 75),
('MK-202', 'MK', 'SUBDIVISION', '维尼察', 'Vinica', 76),
('MK-603', 'MK', 'SUBDIVISION', '夫拉普奇什特', 'Vrapčište', 77),
('MK-806', 'MK', 'SUBDIVISION', '泽列尼科沃', 'Zelenikovo', 78),
('MK-204', 'MK', 'SUBDIVISION', '兹尔诺夫齐', 'Zrnovci', 79),
('MK-605', 'MK', 'SUBDIVISION', '热利诺', 'Želino', 80);

-- Belarus
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('BY-BR', 'BY', 'SUBDIVISION', '布列斯特州', 'Brest Region', 1),
('BY-HO', 'BY', 'SUBDIVISION', '戈梅利州', 'Gomel Region', 2),
('BY-HR', 'BY', 'SUBDIVISION', '格罗德诺州', 'Grodno Region', 3),
('BY-MI', 'BY', 'SUBDIVISION', '明斯克州', 'Minsk Region', 4),
('BY-HM', 'BY', 'SUBDIVISION', '明斯克市', 'Minsk City', 5),
('BY-MA', 'BY', 'SUBDIVISION', '莫吉廖夫州', 'Mogilev Region', 6),
('BY-VI', 'BY', 'SUBDIVISION', '维捷布斯克州', 'Vitebsk Region', 7);

-- Bulgaria
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('BG-01', 'BG', 'SUBDIVISION', '布拉戈耶夫格勒州', 'Blagoevgrad Province', 1),
('BG-02', 'BG', 'SUBDIVISION', '布尔加斯州', 'Burgas Province', 2),
('BG-03', 'BG', 'SUBDIVISION', '瓦尔纳州', 'Varna Province', 3),
('BG-04', 'BG', 'SUBDIVISION', '大特尔诺沃州', 'Veliko Tarnovo Province', 4),
('BG-05', 'BG', 'SUBDIVISION', '维丁州', 'Vidin Province', 5),
('BG-06', 'BG', 'SUBDIVISION', '弗拉察州', 'Vratsa Province', 6),
('BG-07', 'BG', 'SUBDIVISION', '加布罗沃州', 'Gabrovo Province', 7),
('BG-08', 'BG', 'SUBDIVISION', '多布里奇州', 'Dobrich Province', 8),
('BG-09', 'BG', 'SUBDIVISION', '卡尔贾利州', 'Kardzhali Province', 9),
('BG-10', 'BG', 'SUBDIVISION', '丘斯滕迪尔州', 'Kyustendil Province', 10),
('BG-11', 'BG', 'SUBDIVISION', '洛维奇州', 'Lovech Province', 11),
('BG-12', 'BG', 'SUBDIVISION', '蒙塔纳州', 'Montana Province', 12),
('BG-13', 'BG', 'SUBDIVISION', '帕扎尔吉克州', 'Pazardzhik Province', 13),
('BG-14', 'BG', 'SUBDIVISION', '佩尔尼克州', 'Pernik Province', 14),
('BG-15', 'BG', 'SUBDIVISION', '普列文州', 'Pleven Province', 15),
('BG-16', 'BG', 'SUBDIVISION', '普罗夫迪夫州', 'Plovdiv Province', 16),
('BG-17', 'BG', 'SUBDIVISION', '拉兹格勒州', 'Razgrad Province', 17),
('BG-18', 'BG', 'SUBDIVISION', '鲁塞州', 'Ruse Province', 18),
('BG-19', 'BG', 'SUBDIVISION', '西利斯特拉州', 'Silistra Province', 19),
('BG-20', 'BG', 'SUBDIVISION', '斯利文州', 'Sliven Province', 20),
('BG-21', 'BG', 'SUBDIVISION', '斯莫梁州', 'Smolyan Province', 21),
('BG-22', 'BG', 'SUBDIVISION', '索非亚市', 'Sofia City Province', 22),
('BG-23', 'BG', 'SUBDIVISION', '索非亚州', 'Sofia Province', 23),
('BG-24', 'BG', 'SUBDIVISION', '旧扎戈拉州', 'Stara Zagora Province', 24),
('BG-25', 'BG', 'SUBDIVISION', '塔尔戈维什特州', 'Targovishte Province', 25),
('BG-26', 'BG', 'SUBDIVISION', '哈斯科沃州', 'Haskovo Province', 26),
('BG-27', 'BG', 'SUBDIVISION', '舒门州', 'Shumen Province', 27),
('BG-28', 'BG', 'SUBDIVISION', '亚姆博尔州', 'Yambol Province', 28);

-- Czechia
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('CZ-31', 'CZ', 'SUBDIVISION', '南波希米亚州', 'South Bohemia', 1),
('CZ-64', 'CZ', 'SUBDIVISION', '南摩拉维亚州', 'South Moravia', 2),
('CZ-41', 'CZ', 'SUBDIVISION', '卡罗维发利州', 'Karlovy Vary', 3),
('CZ-52', 'CZ', 'SUBDIVISION', '赫拉德茨-克拉洛韦州', 'Hradec Králové', 4),
('CZ-51', 'CZ', 'SUBDIVISION', '利贝雷茨州', 'Liberec', 5),
('CZ-80', 'CZ', 'SUBDIVISION', '摩拉维亚-西里西亚州', 'Moravia-Silesia', 6),
('CZ-71', 'CZ', 'SUBDIVISION', '奥洛穆茨州', 'Olomouc', 7),
('CZ-53', 'CZ', 'SUBDIVISION', '帕尔杜比采州', 'Pardubice', 8),
('CZ-32', 'CZ', 'SUBDIVISION', '比尔森州', 'Plzeň', 9),
('CZ-10', 'CZ', 'SUBDIVISION', '布拉格', 'Prague', 10),
('CZ-20', 'CZ', 'SUBDIVISION', '中波希米亚州', 'Central Bohemia', 11),
('CZ-42', 'CZ', 'SUBDIVISION', '乌斯季州', 'Ústí nad Labem', 12),
('CZ-63', 'CZ', 'SUBDIVISION', '维索基纳州', 'Vysočina', 13),
('CZ-72', 'CZ', 'SUBDIVISION', '兹林州', 'Zlín', 14);

-- Hungary
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('HU-BK', 'HU', 'SUBDIVISION', '巴奇-基什孔州', 'Bács-Kiskun', 1),
('HU-BA', 'HU', 'SUBDIVISION', '巴兰尼亚州', 'Baranya', 2),
('HU-BE', 'HU', 'SUBDIVISION', '贝凯什州', 'Békés', 3),
('HU-BZ', 'HU', 'SUBDIVISION', '鲍尔绍德-奥包乌伊-曾普伦州', 'Borsod-Abaúj-Zemplén', 4),
('HU-CS', 'HU', 'SUBDIVISION', '琼格拉德-琼纳德州', 'Csongrád-Csanád', 5),
('HU-FE', 'HU', 'SUBDIVISION', '费耶尔州', 'Fejér', 6),
('HU-GS', 'HU', 'SUBDIVISION', '杰尔-莫雄-肖普朗州', 'Győr-Moson-Sopron', 7),
('HU-HB', 'HU', 'SUBDIVISION', '豪伊杜-比豪尔州', 'Hajdú-Bihar', 8),
('HU-HE', 'HU', 'SUBDIVISION', '赫维什州', 'Heves', 9),
('HU-JN', 'HU', 'SUBDIVISION', '雅斯-大库讷-索尔诺克州', 'Jász-Nagykun-Szolnok', 10),
('HU-KE', 'HU', 'SUBDIVISION', '科马罗姆-埃斯泰尔戈姆州', 'Komárom-Esztergom', 11),
('HU-NO', 'HU', 'SUBDIVISION', '诺格拉德州', 'Nógrád', 12),
('HU-PE', 'HU', 'SUBDIVISION', '佩斯州', 'Pest', 13),
('HU-SO', 'HU', 'SUBDIVISION', '绍莫吉州', 'Somogy', 14),
('HU-SZ', 'HU', 'SUBDIVISION', '萨博尔奇-索特马尔-贝雷格州', 'Szabolcs-Szatmár-Bereg', 15),
('HU-TO', 'HU', 'SUBDIVISION', '托尔瑙州', 'Tolna', 16),
('HU-VA', 'HU', 'SUBDIVISION', '瓦什州', 'Vas', 17),
('HU-VE', 'HU', 'SUBDIVISION', '维斯普雷姆州', 'Veszprém', 18),
('HU-ZA', 'HU', 'SUBDIVISION', '佐洛州', 'Zala', 19),
('HU-BU', 'HU', 'SUBDIVISION', '布达佩斯', 'Budapest', 20);

-- Moldova
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('MD-AN', 'MD', 'SUBDIVISION', '阿内尼伊诺伊区', 'Anenii Noi', 1),
('MD-BS', 'MD', 'SUBDIVISION', '巴萨拉比亚斯卡区', 'Basarabeasca', 2),
('MD-BR', 'MD', 'SUBDIVISION', '布里切尼区', 'Briceni', 3),
('MD-CA', 'MD', 'SUBDIVISION', '卡胡尔区', 'Cahul', 4),
('MD-CT', 'MD', 'SUBDIVISION', '坎特米尔区', 'Cantemir', 5),
('MD-CL', 'MD', 'SUBDIVISION', '克勒拉希区', 'Călărași', 6),
('MD-CS', 'MD', 'SUBDIVISION', '克乌沙尼区', 'Căușeni', 7),
('MD-CM', 'MD', 'SUBDIVISION', '奇米什利亚区', 'Cimișlia', 8),
('MD-CR', 'MD', 'SUBDIVISION', '克留列尼区', 'Criuleni', 9),
('MD-DO', 'MD', 'SUBDIVISION', '东杜舍尼区', 'Dondușeni', 10),
('MD-DR', 'MD', 'SUBDIVISION', '德罗基亚区', 'Drochia', 11),
('MD-DU', 'MD', 'SUBDIVISION', '杜巴萨里区', 'Dubăsari', 12),
('MD-ED', 'MD', 'SUBDIVISION', '叶迪内茨区', 'Edineț', 13),
('MD-FA', 'MD', 'SUBDIVISION', '弗勒雷什蒂区', 'Fălești', 14),
('MD-FL', 'MD', 'SUBDIVISION', '弗洛雷什蒂区', 'Florești', 15),
('MD-GL', 'MD', 'SUBDIVISION', '格洛代尼区', 'Glodeni', 16),
('MD-HI', 'MD', 'SUBDIVISION', '亨切什蒂区', 'Hîncești', 17),
('MD-IA', 'MD', 'SUBDIVISION', '亚洛韦尼区', 'Ialoveni', 18),
('MD-LE', 'MD', 'SUBDIVISION', '列奥瓦区', 'Leova', 19),
('MD-NI', 'MD', 'SUBDIVISION', '尼斯波雷尼区', 'Nisporeni', 20),
('MD-OC', 'MD', 'SUBDIVISION', '奥克尼察区', 'Ocnița', 21),
('MD-OR', 'MD', 'SUBDIVISION', '奥尔海区', 'Orhei', 22),
('MD-RE', 'MD', 'SUBDIVISION', '雷济纳区', 'Rezina', 23),
('MD-RI', 'MD', 'SUBDIVISION', '勒什卡尼区', 'Rîșcani', 24),
('MD-SI', 'MD', 'SUBDIVISION', '森杰雷伊区', 'Sîngerei', 25),
('MD-SO', 'MD', 'SUBDIVISION', '索罗卡区', 'Soroca', 26),
('MD-ST', 'MD', 'SUBDIVISION', '斯特勒谢尼区', 'Strășeni', 27),
('MD-SD', 'MD', 'SUBDIVISION', '绍尔德内什蒂区', 'Șoldănești', 28),
('MD-SV', 'MD', 'SUBDIVISION', '斯特凡沃德区', 'Ștefan Vodă', 29),
('MD-TA', 'MD', 'SUBDIVISION', '塔拉克利亚区', 'Taraclia', 30),
('MD-TE', 'MD', 'SUBDIVISION', '泰列内什蒂区', 'Telenești', 31),
('MD-UN', 'MD', 'SUBDIVISION', '温格尼区', 'Ungheni', 32),
('MD-BA', 'MD', 'SUBDIVISION', '贝尔齐市', 'Bălți', 33),
('MD-BD', 'MD', 'SUBDIVISION', '本德尔市', 'Bender', 34),
('MD-CU', 'MD', 'SUBDIVISION', '基希讷乌市', 'Chișinău', 35),
('MD-GA', 'MD', 'SUBDIVISION', '加告兹自治区', 'Găgăuzia', 36),
('MD-SN', 'MD', 'SUBDIVISION', '德涅斯特河左岸区', 'Stînga Nistrului', 37);

-- Poland
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('PL-02', 'PL', 'SUBDIVISION', '下西里西亚省', 'Lower Silesia', 1),
('PL-04', 'PL', 'SUBDIVISION', '库亚维-滨海省', 'Kuyavia-Pomerania', 2),
('PL-06', 'PL', 'SUBDIVISION', '卢布林省', 'Lublin', 3),
('PL-08', 'PL', 'SUBDIVISION', '卢布斯卡省', 'Lubusz', 4),
('PL-10', 'PL', 'SUBDIVISION', '罗兹省', 'Łódź', 5),
('PL-12', 'PL', 'SUBDIVISION', '小波兰省', 'Lesser Poland', 6),
('PL-14', 'PL', 'SUBDIVISION', '马佐夫舍省', 'Mazovia', 7),
('PL-16', 'PL', 'SUBDIVISION', '奥波莱省', 'Opole', 8),
('PL-18', 'PL', 'SUBDIVISION', '喀尔巴阡山省', 'Subcarpathia', 9),
('PL-20', 'PL', 'SUBDIVISION', '波德拉谢省', 'Podlaskie', 10),
('PL-22', 'PL', 'SUBDIVISION', '滨海省', 'Pomerania', 11),
('PL-24', 'PL', 'SUBDIVISION', '西里西亚省', 'Silesia', 12),
('PL-26', 'PL', 'SUBDIVISION', '圣十字省', 'Holy Cross', 13),
('PL-28', 'PL', 'SUBDIVISION', '瓦尔米亚-马祖里省', 'Warmia-Masuria', 14),
('PL-30', 'PL', 'SUBDIVISION', '大波兰省', 'Greater Poland', 15),
('PL-32', 'PL', 'SUBDIVISION', '西滨海省', 'West Pomerania', 16);

-- Romania
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('RO-AB', 'RO', 'SUBDIVISION', '阿尔巴县', 'Alba', 1),
('RO-AR', 'RO', 'SUBDIVISION', '阿拉德县', 'Arad', 2),
('RO-AG', 'RO', 'SUBDIVISION', '阿尔杰什县', 'Argeș', 3),
('RO-BC', 'RO', 'SUBDIVISION', '巴克乌县', 'Bacău', 4),
('RO-BH', 'RO', 'SUBDIVISION', '比霍尔县', 'Bihor', 5),
('RO-BN', 'RO', 'SUBDIVISION', '比斯特里察-讷瑟乌德县', 'Bistrița-Năsăud', 6),
('RO-BT', 'RO', 'SUBDIVISION', '博托沙尼县', 'Botoșani', 7),
('RO-BV', 'RO', 'SUBDIVISION', '布拉索夫县', 'Brașov', 8),
('RO-BR', 'RO', 'SUBDIVISION', '布勒伊拉县', 'Brăila', 9),
('RO-B', 'RO', 'SUBDIVISION', '布加勒斯特市', 'București', 10),
('RO-BZ', 'RO', 'SUBDIVISION', '布泽乌县', 'Buzău', 11),
('RO-CS', 'RO', 'SUBDIVISION', '卡拉什-塞维林县', 'Caraș-Severin', 12),
('RO-CL', 'RO', 'SUBDIVISION', '克勒拉希县', 'Călărași', 13),
('RO-CJ', 'RO', 'SUBDIVISION', '克卢日县', 'Cluj', 14),
('RO-CT', 'RO', 'SUBDIVISION', '康斯坦察县', 'Constanța', 15),
('RO-CV', 'RO', 'SUBDIVISION', '科瓦斯纳县', 'Covasna', 16),
('RO-DB', 'RO', 'SUBDIVISION', '登博维察县', 'Dâmbovița', 17),
('RO-DJ', 'RO', 'SUBDIVISION', '多尔日县', 'Dolj', 18),
('RO-GL', 'RO', 'SUBDIVISION', '加拉茨县', 'Galați', 19),
('RO-GR', 'RO', 'SUBDIVISION', '久尔久县', 'Giurgiu', 20),
('RO-GJ', 'RO', 'SUBDIVISION', '戈尔日县', 'Gorj', 21),
('RO-HR', 'RO', 'SUBDIVISION', '哈尔吉塔县', 'Harghita', 22),
('RO-HD', 'RO', 'SUBDIVISION', '胡内多阿拉县', 'Hunedoara', 23),
('RO-IL', 'RO', 'SUBDIVISION', '亚洛米察县', 'Ialomița', 24),
('RO-IS', 'RO', 'SUBDIVISION', '雅西县', 'Iași', 25),
('RO-IF', 'RO', 'SUBDIVISION', '伊尔福夫县', 'Ilfov', 26),
('RO-MM', 'RO', 'SUBDIVISION', '马拉穆列什县', 'Maramureș', 27),
('RO-MH', 'RO', 'SUBDIVISION', '梅赫丁茨县', 'Mehedinți', 28),
('RO-MS', 'RO', 'SUBDIVISION', '穆列什县', 'Mureș', 29),
('RO-NT', 'RO', 'SUBDIVISION', '讷姆茨县', 'Neamț', 30),
('RO-OT', 'RO', 'SUBDIVISION', '奥尔特县', 'Olt', 31),
('RO-PH', 'RO', 'SUBDIVISION', '普拉霍瓦县', 'Prahova', 32),
('RO-SM', 'RO', 'SUBDIVISION', '萨图马雷县', 'Satu Mare', 33),
('RO-SJ', 'RO', 'SUBDIVISION', '萨拉日县', 'Sălaj', 34),
('RO-SB', 'RO', 'SUBDIVISION', '锡比乌县', 'Sibiu', 35),
('RO-SV', 'RO', 'SUBDIVISION', '苏恰瓦县', 'Suceava', 36),
('RO-TR', 'RO', 'SUBDIVISION', '特莱奥尔曼县', 'Teleorman', 37),
('RO-TM', 'RO', 'SUBDIVISION', '蒂米什县', 'Timiș', 38),
('RO-TL', 'RO', 'SUBDIVISION', '图尔恰县', 'Tulcea', 39),
('RO-VS', 'RO', 'SUBDIVISION', '瓦斯卢伊县', 'Vaslui', 40),
('RO-VL', 'RO', 'SUBDIVISION', '瓦尔恰县', 'Vâlcea', 41),
('RO-VN', 'RO', 'SUBDIVISION', '弗兰恰县', 'Vrancea', 42);

-- Russia
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('RU-AD', 'RU', 'SUBDIVISION', '阿迪格共和国', 'Adygea Republic', 1),
('RU-AL', 'RU', 'SUBDIVISION', '阿尔泰共和国', 'Altai Republic', 2),
('RU-BA', 'RU', 'SUBDIVISION', '巴什科尔托斯坦共和国', 'Bashkortostan Republic', 3),
('RU-BU', 'RU', 'SUBDIVISION', '布里亚特共和国', 'Buryatia Republic', 4),
('RU-CE', 'RU', 'SUBDIVISION', '车臣共和国', 'Chechnya Republic', 5),
('RU-CU', 'RU', 'SUBDIVISION', '楚瓦什共和国', 'Chuvashia Republic', 6),
('RU-DA', 'RU', 'SUBDIVISION', '达吉斯坦共和国', 'Dagestan Republic', 7),
('RU-IN', 'RU', 'SUBDIVISION', '印古什共和国', 'Ingushetia Republic', 8),
('RU-KB', 'RU', 'SUBDIVISION', '卡巴尔达-巴尔卡尔共和国', 'Kabardino-Balkaria Republic', 9),
('RU-KL', 'RU', 'SUBDIVISION', '卡尔梅克共和国', 'Kalmykia Republic', 10),
('RU-KR', 'RU', 'SUBDIVISION', '卡累利阿共和国', 'Karelia Republic', 11),
('RU-KK', 'RU', 'SUBDIVISION', '哈卡斯共和国', 'Khakassia Republic', 12),
('RU-KO', 'RU', 'SUBDIVISION', '科米共和国', 'Komi Republic', 13),
('RU-KC', 'RU', 'SUBDIVISION', '卡拉恰伊-切尔克斯共和国', 'Karachay-Cherkessia Republic', 14),
('RU-ME', 'RU', 'SUBDIVISION', '马里埃尔共和国', 'Mari El Republic', 15),
('RU-MO', 'RU', 'SUBDIVISION', '莫尔多瓦共和国', 'Mordovia Republic', 16),
('RU-SA', 'RU', 'SUBDIVISION', '萨哈共和国', 'Sakha (Yakutia) Republic', 17),
('RU-SE', 'RU', 'SUBDIVISION', '北奥塞梯共和国', 'North Ossetia Republic', 18),
('RU-TA', 'RU', 'SUBDIVISION', '鞑靼斯坦共和国', 'Tatarstan Republic', 19),
('RU-TY', 'RU', 'SUBDIVISION', '图瓦共和国', 'Tuva Republic', 20),
('RU-UD', 'RU', 'SUBDIVISION', '乌德穆尔特共和国', 'Udmurtia Republic', 21),
('RU-MOW', 'RU', 'SUBDIVISION', '莫斯科市', 'Moscow City', 22),
('RU-SPE', 'RU', 'SUBDIVISION', '圣彼得堡市', 'Saint Petersburg', 23),
('RU-YEV', 'RU', 'SUBDIVISION', '犹太自治州', 'Jewish Autonomous Oblast', 24),
('RU-CHU', 'RU', 'SUBDIVISION', '楚科奇自治区', 'Chukotka Autonomous Okrug', 25),
('RU-KHM', 'RU', 'SUBDIVISION', '汉特-曼西自治区', 'Khanty-Mansi Autonomous Okrug', 26),
('RU-NEN', 'RU', 'SUBDIVISION', '涅涅茨自治区', 'Nenets Autonomous Okrug', 27),
('RU-YAN', 'RU', 'SUBDIVISION', '亚马尔-涅涅茨自治区', 'Yamalo-Nenets Autonomous Okrug', 28),
('RU-ALT', 'RU', 'SUBDIVISION', '阿尔泰边疆区', 'Altai Krai', 29),
('RU-KAM', 'RU', 'SUBDIVISION', '堪察加边疆区', 'Kamchatka Krai', 30),
('RU-KDA', 'RU', 'SUBDIVISION', '克拉斯诺达尔边疆区', 'Krasnodar Krai', 31),
('RU-KYA', 'RU', 'SUBDIVISION', '克拉斯诺亚尔斯克边疆区', 'Krasnoyarsk Krai', 32),
('RU-KHA', 'RU', 'SUBDIVISION', '哈巴罗夫斯克边疆区', 'Khabarovsk Krai', 33),
('RU-PER', 'RU', 'SUBDIVISION', '彼尔姆边疆区', 'Perm Krai', 34),
('RU-PRI', 'RU', 'SUBDIVISION', '滨海边疆区', 'Primorsky Krai', 35),
('RU-STA', 'RU', 'SUBDIVISION', '斯塔夫罗波尔边疆区', 'Stavropol Krai', 36),
('RU-ZAB', 'RU', 'SUBDIVISION', '外贝加尔边疆区', 'Zabaykalsky Krai', 37),
('RU-AMU', 'RU', 'SUBDIVISION', '阿穆尔州', 'Amur Oblast', 38),
('RU-ARK', 'RU', 'SUBDIVISION', '阿尔汉格尔斯克州', 'Arkhangelsk Oblast', 39),
('RU-AST', 'RU', 'SUBDIVISION', '阿斯特拉罕州', 'Astrakhan Oblast', 40),
('RU-BEL', 'RU', 'SUBDIVISION', '别尔哥罗德州', 'Belgorod Oblast', 41),
('RU-BRY', 'RU', 'SUBDIVISION', '布良斯克州', 'Bryansk Oblast', 42),
('RU-CHE', 'RU', 'SUBDIVISION', '车里雅宾斯克州', 'Chelyabinsk Oblast', 43),
('RU-IVA', 'RU', 'SUBDIVISION', '伊万诺沃州', 'Ivanovo Oblast', 44),
('RU-IRK', 'RU', 'SUBDIVISION', '伊尔库茨克州', 'Irkutsk Oblast', 45),
('RU-KGD', 'RU', 'SUBDIVISION', '加里宁格勒州', 'Kaliningrad Oblast', 46),
('RU-KLU', 'RU', 'SUBDIVISION', '卡卢加州', 'Kaluga Oblast', 47),
('RU-KEM', 'RU', 'SUBDIVISION', '克麦罗沃州', 'Kemerovo Oblast', 48),
('RU-KIR', 'RU', 'SUBDIVISION', '基洛夫州', 'Kirov Oblast', 49),
('RU-KOS', 'RU', 'SUBDIVISION', '科斯特罗马州', 'Kostroma Oblast', 50),
('RU-KGN', 'RU', 'SUBDIVISION', '库尔干州', 'Kurgan Oblast', 51),
('RU-KRS', 'RU', 'SUBDIVISION', '库尔斯克州', 'Kursk Oblast', 52),
('RU-LEN', 'RU', 'SUBDIVISION', '列宁格勒州', 'Leningrad Oblast', 53),
('RU-LIP', 'RU', 'SUBDIVISION', '利佩茨克州', 'Lipetsk Oblast', 54),
('RU-MAG', 'RU', 'SUBDIVISION', '马加丹州', 'Magadan Oblast', 55),
('RU-MOS', 'RU', 'SUBDIVISION', '莫斯科州', 'Moscow Oblast', 56),
('RU-MUR', 'RU', 'SUBDIVISION', '摩尔曼斯克州', 'Murmansk Oblast', 57),
('RU-NIZ', 'RU', 'SUBDIVISION', '下诺夫哥罗德州', 'Nizhny Novgorod Oblast', 58),
('RU-NGR', 'RU', 'SUBDIVISION', '诺夫哥罗德州', 'Novgorod Oblast', 59),
('RU-NVS', 'RU', 'SUBDIVISION', '新西伯利亚州', 'Novosibirsk Oblast', 60),
('RU-OMS', 'RU', 'SUBDIVISION', '鄂木斯克州', 'Omsk Oblast', 61),
('RU-ORE', 'RU', 'SUBDIVISION', '奥伦堡州', 'Orenburg Oblast', 62),
('RU-ORL', 'RU', 'SUBDIVISION', '奥廖尔州', 'Oryol Oblast', 63),
('RU-PNZ', 'RU', 'SUBDIVISION', '奔萨州', 'Penza Oblast', 64),
('RU-PSK', 'RU', 'SUBDIVISION', '普斯科夫州', 'Pskov Oblast', 65),
('RU-ROS', 'RU', 'SUBDIVISION', '罗斯托夫州', 'Rostov Oblast', 66),
('RU-RYA', 'RU', 'SUBDIVISION', '梁赞州', 'Ryazan Oblast', 67),
('RU-SAK', 'RU', 'SUBDIVISION', '萨哈林州', 'Sakhalin Oblast', 68),
('RU-SAM', 'RU', 'SUBDIVISION', '萨马拉州', 'Samara Oblast', 69),
('RU-SAR', 'RU', 'SUBDIVISION', '萨拉托夫州', 'Saratov Oblast', 70),
('RU-SMO', 'RU', 'SUBDIVISION', '斯摩棱斯克州', 'Smolensk Oblast', 71),
('RU-SVE', 'RU', 'SUBDIVISION', '斯维尔德洛夫斯克州', 'Sverdlovsk Oblast', 72),
('RU-TAM', 'RU', 'SUBDIVISION', '坦波夫州', 'Tambov Oblast', 73),
('RU-TOM', 'RU', 'SUBDIVISION', '托木斯克州', 'Tomsk Oblast', 74),
('RU-TUL', 'RU', 'SUBDIVISION', '图拉州', 'Tula Oblast', 75),
('RU-TVE', 'RU', 'SUBDIVISION', '特维尔州', 'Tver Oblast', 76),
('RU-TYU', 'RU', 'SUBDIVISION', '秋明州', 'Tyumen Oblast', 77),
('RU-ULY', 'RU', 'SUBDIVISION', '乌里扬诺夫斯克州', 'Ulyanovsk Oblast', 78),
('RU-VLA', 'RU', 'SUBDIVISION', '弗拉基米尔州', 'Vladimir Oblast', 79),
('RU-VGG', 'RU', 'SUBDIVISION', '伏尔加格勒州', 'Volgograd Oblast', 80),
('RU-VLG', 'RU', 'SUBDIVISION', '沃洛格达州', 'Vologda Oblast', 81),
('RU-VOR', 'RU', 'SUBDIVISION', '沃罗涅日州', 'Voronezh Oblast', 82),
('RU-YAR', 'RU', 'SUBDIVISION', '雅罗斯拉夫尔州', 'Yaroslavl Oblast', 83);

-- Slovakia
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('SK-BC', 'SK', 'SUBDIVISION', '班斯卡-比斯特里察州', 'Banská Bystrica Region', 1),
('SK-BL', 'SK', 'SUBDIVISION', '布拉迪斯拉发州', 'Bratislava Region', 2),
('SK-KI', 'SK', 'SUBDIVISION', '科希策州', 'Košice Region', 3),
('SK-NI', 'SK', 'SUBDIVISION', '尼特拉州', 'Nitra Region', 4),
('SK-PV', 'SK', 'SUBDIVISION', '普雷绍夫州', 'Prešov Region', 5),
('SK-TC', 'SK', 'SUBDIVISION', '特伦钦州', 'Trenčín Region', 6),
('SK-TA', 'SK', 'SUBDIVISION', '特尔纳瓦州', 'Trnava Region', 7),
('SK-ZI', 'SK', 'SUBDIVISION', '日利纳州', 'Žilina Region', 8);

-- Ukraine
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('UA-43', 'UA', 'SUBDIVISION', '克里米亚自治共和国', 'Crimea', 1),
('UA-71', 'UA', 'SUBDIVISION', '切尔卡瑟州', 'Cherkasy Oblast', 2),
('UA-74', 'UA', 'SUBDIVISION', '切尔尼戈夫州', 'Chernihiv Oblast', 3),
('UA-77', 'UA', 'SUBDIVISION', '切尔诺夫策州', 'Chernivtsi Oblast', 4),
('UA-12', 'UA', 'SUBDIVISION', '第聂伯罗彼得罗夫斯克州', 'Dnipropetrovsk Oblast', 5),
('UA-14', 'UA', 'SUBDIVISION', '顿涅茨克州', 'Donetsk Oblast', 6),
('UA-26', 'UA', 'SUBDIVISION', '伊万诺-弗兰科夫斯克州', 'Ivano-Frankivsk Oblast', 7),
('UA-63', 'UA', 'SUBDIVISION', '哈尔科夫州', 'Kharkiv Oblast', 8),
('UA-65', 'UA', 'SUBDIVISION', '赫尔松州', 'Kherson Oblast', 9),
('UA-68', 'UA', 'SUBDIVISION', '赫梅利尼茨基州', 'Khmelnytskyi Oblast', 10),
('UA-35', 'UA', 'SUBDIVISION', '基洛沃格勒州', 'Kirovohrad Oblast', 11),
('UA-30', 'UA', 'SUBDIVISION', '基辅市', 'Kyiv City', 12),
('UA-32', 'UA', 'SUBDIVISION', '基辅州', 'Kyiv Oblast', 13),
('UA-09', 'UA', 'SUBDIVISION', '卢甘斯克州', 'Luhansk Oblast', 14),
('UA-46', 'UA', 'SUBDIVISION', '利沃夫州', 'Lviv Oblast', 15),
('UA-48', 'UA', 'SUBDIVISION', '尼古拉耶夫州', 'Mykolaiv Oblast', 16),
('UA-51', 'UA', 'SUBDIVISION', '敖德萨州', 'Odesa Oblast', 17),
('UA-53', 'UA', 'SUBDIVISION', '波尔塔瓦州', 'Poltava Oblast', 18),
('UA-56', 'UA', 'SUBDIVISION', '罗夫诺州', 'Rivne Oblast', 19),
('UA-40', 'UA', 'SUBDIVISION', '塞瓦斯托波尔市', 'Sevastopol', 20),
('UA-59', 'UA', 'SUBDIVISION', '苏梅州', 'Sumy Oblast', 21),
('UA-61', 'UA', 'SUBDIVISION', '捷尔诺波尔州', 'Ternopil Oblast', 22),
('UA-05', 'UA', 'SUBDIVISION', '文尼察州', 'Vinnytsia Oblast', 23),
('UA-07', 'UA', 'SUBDIVISION', '沃伦州', 'Volyn Oblast', 24),
('UA-21', 'UA', 'SUBDIVISION', '外喀尔巴阡州', 'Transcarpathia Oblast', 25),
('UA-23', 'UA', 'SUBDIVISION', '扎波罗热州', 'Zaporizhzhia Oblast', 26),
('UA-18', 'UA', 'SUBDIVISION', '日托米尔州', 'Zhytomyr Oblast', 27);

-- Slovenia (Slovenia has no legally established regional tier of government; the 212 municipalities
-- (občine) are the sole and therefore first-level ISO 3166-2 subdivision for the country)
INSERT INTO common_region (code, parent_code, level, name_zh, name_en, sort_order) VALUES
('SI-001', 'SI', 'SUBDIVISION', '艾多夫希齐纳', 'Ajdovščina', 1),
('SI-002', 'SI', 'SUBDIVISION', '贝尔廷齐', 'Beltinci', 2),
('SI-003', 'SI', 'SUBDIVISION', '布莱德', 'Bled', 3),
('SI-004', 'SI', 'SUBDIVISION', '博欣', 'Bohinj', 4),
('SI-005', 'SI', 'SUBDIVISION', '博罗夫尼察', 'Borovnica', 5),
('SI-006', 'SI', 'SUBDIVISION', '博韦茨', 'Bovec', 6),
('SI-007', 'SI', 'SUBDIVISION', '布尔达', 'Brda', 7),
('SI-008', 'SI', 'SUBDIVISION', '布雷佐维察', 'Brezovica', 8),
('SI-009', 'SI', 'SUBDIVISION', '布雷日采', 'Brežice', 9),
('SI-010', 'SI', 'SUBDIVISION', '蒂希纳', 'Tišina', 10),
('SI-011', 'SI', 'SUBDIVISION', '采列', 'Celje', 11),
('SI-012', 'SI', 'SUBDIVISION', '上克拉尼斯卡采尔克列', 'Cerklje na Gorenjskem', 12),
('SI-013', 'SI', 'SUBDIVISION', '采尔克尼察', 'Cerknica', 13),
('SI-014', 'SI', 'SUBDIVISION', '采尔克诺', 'Cerkno', 14),
('SI-015', 'SI', 'SUBDIVISION', '采伦绍夫齐', 'Črenšovci', 15),
('SI-016', 'SI', 'SUBDIVISION', '科罗什卡采尔纳', 'Črna na Koroškem', 16),
('SI-017', 'SI', 'SUBDIVISION', '采尔诺梅利', 'Črnomelj', 17),
('SI-018', 'SI', 'SUBDIVISION', '德斯特尔尼克', 'Destrnik', 18),
('SI-019', 'SI', 'SUBDIVISION', '迪瓦查', 'Divača', 19),
('SI-020', 'SI', 'SUBDIVISION', '多布雷波列', 'Dobrepolje', 20),
('SI-021', 'SI', 'SUBDIVISION', '多布罗瓦-波尔霍夫格拉德茨', 'Dobrova-Polhov Gradec', 21),
('SI-022', 'SI', 'SUBDIVISION', '卢布尔雅那近多尔', 'Dol pri Ljubljani', 22),
('SI-023', 'SI', 'SUBDIVISION', '多姆扎莱', 'Domžale', 23),
('SI-024', 'SI', 'SUBDIVISION', '多尔纳瓦', 'Dornava', 24),
('SI-025', 'SI', 'SUBDIVISION', '德拉沃格勒', 'Dravograd', 25),
('SI-026', 'SI', 'SUBDIVISION', '杜普莱克', 'Duplek', 26),
('SI-027', 'SI', 'SUBDIVISION', '戈伦尼亚瓦斯-波利亚内', 'Gorenja vas-Poljane', 27),
('SI-028', 'SI', 'SUBDIVISION', '戈里什尼察', 'Gorišnica', 28),
('SI-029', 'SI', 'SUBDIVISION', '上拉德戈纳', 'Gornja Radgona', 29),
('SI-030', 'SI', 'SUBDIVISION', '戈尔尼格拉德', 'Gornji Grad', 30),
('SI-031', 'SI', 'SUBDIVISION', '戈尔尼彼得罗夫齐', 'Gornji Petrovci', 31),
('SI-032', 'SI', 'SUBDIVISION', '格罗苏普列', 'Grosuplje', 32),
('SI-033', 'SI', 'SUBDIVISION', '沙洛夫齐', 'Šalovci', 33),
('SI-034', 'SI', 'SUBDIVISION', '赫拉斯特尼克', 'Hrastnik', 34),
('SI-035', 'SI', 'SUBDIVISION', '赫尔佩列-科齐纳', 'Hrpelje-Kozina', 35),
('SI-036', 'SI', 'SUBDIVISION', '伊德里亚', 'Idrija', 36),
('SI-037', 'SI', 'SUBDIVISION', '伊格', 'Ig', 37),
('SI-038', 'SI', 'SUBDIVISION', '伊利尔斯卡比斯特里察', 'Ilirska Bistrica', 38),
('SI-039', 'SI', 'SUBDIVISION', '伊万奇纳戈里察', 'Ivančna Gorica', 39),
('SI-040', 'SI', 'SUBDIVISION', '伊佐拉', 'Izola', 40),
('SI-041', 'SI', 'SUBDIVISION', '耶塞尼采', 'Jesenice', 41),
('SI-042', 'SI', 'SUBDIVISION', '尤尔申齐', 'Juršinci', 42),
('SI-043', 'SI', 'SUBDIVISION', '卡姆尼克', 'Kamnik', 43),
('SI-044', 'SI', 'SUBDIVISION', '索查河畔卡纳尔', 'Kanal ob Soči', 44),
('SI-045', 'SI', 'SUBDIVISION', '基德里切沃', 'Kidričevo', 45),
('SI-046', 'SI', 'SUBDIVISION', '科巴里德', 'Kobarid', 46),
('SI-047', 'SI', 'SUBDIVISION', '科比列', 'Kobilje', 47),
('SI-048', 'SI', 'SUBDIVISION', '科切夫耶', 'Kočevje', 48),
('SI-049', 'SI', 'SUBDIVISION', '科门', 'Komen', 49),
('SI-050', 'SI', 'SUBDIVISION', '科佩尔', 'Koper', 50),
('SI-051', 'SI', 'SUBDIVISION', '科兹耶', 'Kozje', 51),
('SI-052', 'SI', 'SUBDIVISION', '克拉尼', 'Kranj', 52),
('SI-053', 'SI', 'SUBDIVISION', '克拉尼斯卡戈拉', 'Kranjska Gora', 53),
('SI-054', 'SI', 'SUBDIVISION', '克尔什科', 'Krško', 54),
('SI-055', 'SI', 'SUBDIVISION', '孔戈塔', 'Kungota', 55),
('SI-056', 'SI', 'SUBDIVISION', '库兹马', 'Kuzma', 56),
('SI-057', 'SI', 'SUBDIVISION', '拉什科', 'Laško', 57),
('SI-058', 'SI', 'SUBDIVISION', '莱纳特', 'Lenart', 58),
('SI-059', 'SI', 'SUBDIVISION', '伦达瓦', 'Lendava', 59),
('SI-060', 'SI', 'SUBDIVISION', '利蒂亚', 'Litija', 60),
('SI-061', 'SI', 'SUBDIVISION', '卢布尔雅那', 'Ljubljana', 61),
('SI-062', 'SI', 'SUBDIVISION', '卢布诺', 'Ljubno', 62),
('SI-063', 'SI', 'SUBDIVISION', '卢托梅尔', 'Ljutomer', 63),
('SI-064', 'SI', 'SUBDIVISION', '洛加特茨', 'Logatec', 64),
('SI-065', 'SI', 'SUBDIVISION', '洛什卡多利纳', 'Loška dolina', 65),
('SI-066', 'SI', 'SUBDIVISION', '洛什基波托克', 'Loški Potok', 66),
('SI-067', 'SI', 'SUBDIVISION', '卢切', 'Luče', 67),
('SI-068', 'SI', 'SUBDIVISION', '卢科维察', 'Lukovica', 68),
('SI-069', 'SI', 'SUBDIVISION', '马伊什佩尔克', 'Majšperk', 69),
('SI-070', 'SI', 'SUBDIVISION', '马里博尔', 'Maribor', 70),
('SI-071', 'SI', 'SUBDIVISION', '梅德沃德', 'Medvode', 71),
('SI-072', 'SI', 'SUBDIVISION', '门格什', 'Mengeš', 72),
('SI-073', 'SI', 'SUBDIVISION', '梅特利卡', 'Metlika', 73),
('SI-074', 'SI', 'SUBDIVISION', '梅日察', 'Mežica', 74),
('SI-075', 'SI', 'SUBDIVISION', '米伦-科斯坦耶维察', 'Miren-Kostanjevica', 75),
('SI-076', 'SI', 'SUBDIVISION', '米斯林尼亚', 'Mislinja', 76),
('SI-077', 'SI', 'SUBDIVISION', '莫拉夫切', 'Moravče', 77),
('SI-078', 'SI', 'SUBDIVISION', '莫拉夫斯克托普利采', 'Moravske Toplice', 78),
('SI-079', 'SI', 'SUBDIVISION', '莫齐尔耶', 'Mozirje', 79),
('SI-080', 'SI', 'SUBDIVISION', '摩尔斯卡索博塔', 'Murska Sobota', 80),
('SI-081', 'SI', 'SUBDIVISION', '穆塔', 'Muta', 81),
('SI-082', 'SI', 'SUBDIVISION', '纳克洛', 'Naklo', 82),
('SI-083', 'SI', 'SUBDIVISION', '纳扎里耶', 'Nazarje', 83),
('SI-084', 'SI', 'SUBDIVISION', '新戈里察', 'Nova Gorica', 84),
('SI-085', 'SI', 'SUBDIVISION', '新梅斯托', 'Novo Mesto', 85),
('SI-086', 'SI', 'SUBDIVISION', '奥德兰齐', 'Odranci', 86),
('SI-087', 'SI', 'SUBDIVISION', '奥尔莫日', 'Ormož', 87),
('SI-088', 'SI', 'SUBDIVISION', '奥西尔尼察', 'Osilnica', 88),
('SI-089', 'SI', 'SUBDIVISION', '佩斯尼察', 'Pesnica', 89),
('SI-090', 'SI', 'SUBDIVISION', '皮兰', 'Piran', 90),
('SI-091', 'SI', 'SUBDIVISION', '皮夫卡', 'Pivka', 91),
('SI-092', 'SI', 'SUBDIVISION', '波德切特尔特克', 'Podčetrtek', 92),
('SI-093', 'SI', 'SUBDIVISION', '波德韦尔卡', 'Podvelka', 93),
('SI-094', 'SI', 'SUBDIVISION', '波斯托伊纳', 'Postojna', 94),
('SI-095', 'SI', 'SUBDIVISION', '普雷德德沃尔', 'Preddvor', 95),
('SI-096', 'SI', 'SUBDIVISION', '普图伊', 'Ptuj', 96),
('SI-097', 'SI', 'SUBDIVISION', '普措齐', 'Puconci', 97),
('SI-098', 'SI', 'SUBDIVISION', '拉切-弗拉姆', 'Rače-Fram', 98),
('SI-099', 'SI', 'SUBDIVISION', '拉德切', 'Radeče', 99),
('SI-100', 'SI', 'SUBDIVISION', '拉登齐', 'Radenci', 100),
('SI-101', 'SI', 'SUBDIVISION', '德拉瓦河畔拉德列', 'Radlje ob Dravi', 101),
('SI-102', 'SI', 'SUBDIVISION', '拉多夫利察', 'Radovljica', 102),
('SI-103', 'SI', 'SUBDIVISION', '科罗什卡拉夫内', 'Ravne na Koroškem', 103),
('SI-104', 'SI', 'SUBDIVISION', '里布尼察', 'Ribnica', 104),
('SI-105', 'SI', 'SUBDIVISION', '罗加绍夫齐', 'Rogašovci', 105),
('SI-106', 'SI', 'SUBDIVISION', '罗加什卡斯拉蒂纳', 'Rogaška Slatina', 106),
('SI-107', 'SI', 'SUBDIVISION', '罗加特茨', 'Rogatec', 107),
('SI-108', 'SI', 'SUBDIVISION', '鲁舍', 'Ruše', 108),
('SI-109', 'SI', 'SUBDIVISION', '塞米奇', 'Semič', 109),
('SI-110', 'SI', 'SUBDIVISION', '塞夫尼察', 'Sevnica', 110),
('SI-111', 'SI', 'SUBDIVISION', '塞扎纳', 'Sežana', 111),
('SI-112', 'SI', 'SUBDIVISION', '斯洛文格拉德茨', 'Slovenj Gradec', 112),
('SI-113', 'SI', 'SUBDIVISION', '斯洛文斯卡比斯特里察', 'Slovenska Bistrica', 113),
('SI-114', 'SI', 'SUBDIVISION', '斯洛文斯克孔尼采', 'Slovenske Konjice', 114),
('SI-115', 'SI', 'SUBDIVISION', '斯塔尔谢', 'Starše', 115),
('SI-116', 'SI', 'SUBDIVISION', '什查夫尼察河畔圣尤里', 'Sveti Jurij ob Ščavnici', 116),
('SI-117', 'SI', 'SUBDIVISION', '申楚尔', 'Šenčur', 117),
('SI-118', 'SI', 'SUBDIVISION', '申蒂利', 'Šentilj', 118),
('SI-119', 'SI', 'SUBDIVISION', '申特尔内伊', 'Šentjernej', 119),
('SI-120', 'SI', 'SUBDIVISION', '申图尔', 'Šentjur', 120),
('SI-121', 'SI', 'SUBDIVISION', '什科茨扬', 'Škocjan', 121),
('SI-122', 'SI', 'SUBDIVISION', '什科菲亚洛卡', 'Škofja Loka', 122),
('SI-123', 'SI', 'SUBDIVISION', '什科夫利察', 'Škofljica', 123),
('SI-124', 'SI', 'SUBDIVISION', '耶尔沙近什马里耶', 'Šmarje pri Jelšah', 124),
('SI-125', 'SI', 'SUBDIVISION', '帕基河畔什马尔特诺', 'Šmartno ob Paki', 125),
('SI-126', 'SI', 'SUBDIVISION', '绍什坦', 'Šoštanj', 126),
('SI-127', 'SI', 'SUBDIVISION', '什托雷', 'Štore', 127),
('SI-128', 'SI', 'SUBDIVISION', '托尔明', 'Tolmin', 128),
('SI-129', 'SI', 'SUBDIVISION', '特尔博夫列', 'Trbovlje', 129),
('SI-130', 'SI', 'SUBDIVISION', '特雷布涅', 'Trebnje', 130),
('SI-131', 'SI', 'SUBDIVISION', '特尔日奇', 'Tržič', 131),
('SI-132', 'SI', 'SUBDIVISION', '图尔尼谢', 'Turnišče', 132),
('SI-133', 'SI', 'SUBDIVISION', '韦莱涅', 'Velenje', 133),
('SI-134', 'SI', 'SUBDIVISION', '大拉什切', 'Velike Lašče', 134),
('SI-135', 'SI', 'SUBDIVISION', '维德姆', 'Videm', 135),
('SI-136', 'SI', 'SUBDIVISION', '维帕瓦', 'Vipava', 136),
('SI-137', 'SI', 'SUBDIVISION', '维坦耶', 'Vitanje', 137),
('SI-138', 'SI', 'SUBDIVISION', '沃迪采', 'Vodice', 138),
('SI-139', 'SI', 'SUBDIVISION', '沃伊尼克', 'Vojnik', 139),
('SI-140', 'SI', 'SUBDIVISION', '弗尔尼卡', 'Vrhnika', 140),
('SI-141', 'SI', 'SUBDIVISION', '武泽尼察', 'Vuzenica', 141),
('SI-142', 'SI', 'SUBDIVISION', '萨瓦河畔扎戈列', 'Zagorje ob Savi', 142),
('SI-143', 'SI', 'SUBDIVISION', '扎夫尔奇', 'Zavrč', 143),
('SI-144', 'SI', 'SUBDIVISION', '兹雷切', 'Zreče', 144),
('SI-146', 'SI', 'SUBDIVISION', '热莱兹尼基', 'Železniki', 145),
('SI-147', 'SI', 'SUBDIVISION', '日里', 'Žiri', 146),
('SI-148', 'SI', 'SUBDIVISION', '贝内迪克特', 'Benedikt', 147),
('SI-149', 'SI', 'SUBDIVISION', '索特拉河畔比斯特里察', 'Bistrica ob Sotli', 148),
('SI-150', 'SI', 'SUBDIVISION', '布洛克', 'Bloke', 149),
('SI-151', 'SI', 'SUBDIVISION', '布拉斯洛夫切', 'Braslovče', 150),
('SI-152', 'SI', 'SUBDIVISION', '灿科瓦', 'Cankova', 151),
('SI-153', 'SI', 'SUBDIVISION', '采尔克韦尼亚克', 'Cerkvenjak', 152),
('SI-154', 'SI', 'SUBDIVISION', '多布耶', 'Dobje', 153),
('SI-155', 'SI', 'SUBDIVISION', '多布尔纳', 'Dobrna', 154),
('SI-156', 'SI', 'SUBDIVISION', '多布罗夫尼克', 'Dobrovnik', 155),
('SI-157', 'SI', 'SUBDIVISION', '多伦斯克托普利采', 'Dolenjske Toplice', 156),
('SI-158', 'SI', 'SUBDIVISION', '格拉德', 'Grad', 157),
('SI-159', 'SI', 'SUBDIVISION', '海迪纳', 'Hajdina', 158),
('SI-160', 'SI', 'SUBDIVISION', '霍切-斯利夫尼察', 'Hoče-Slivnica', 159),
('SI-161', 'SI', 'SUBDIVISION', '霍多什', 'Hodoš', 160),
('SI-162', 'SI', 'SUBDIVISION', '霍尔尤尔', 'Horjul', 161),
('SI-163', 'SI', 'SUBDIVISION', '耶泽尔斯科', 'Jezersko', 162),
('SI-164', 'SI', 'SUBDIVISION', '科门达', 'Komenda', 163),
('SI-165', 'SI', 'SUBDIVISION', '科斯特尔', 'Kostel', 164),
('SI-166', 'SI', 'SUBDIVISION', '克里热夫齐', 'Križevci', 165),
('SI-167', 'SI', 'SUBDIVISION', '波霍尔山洛夫伦茨', 'Lovrenc na Pohorju', 166),
('SI-168', 'SI', 'SUBDIVISION', '马尔科夫齐', 'Markovci', 167),
('SI-169', 'SI', 'SUBDIVISION', '德拉夫河平原米克拉夫日', 'Miklavž na Dravskem polju', 168),
('SI-170', 'SI', 'SUBDIVISION', '米尔纳佩奇', 'Mirna Peč', 169),
('SI-171', 'SI', 'SUBDIVISION', '奥普洛特尼察', 'Oplotnica', 170),
('SI-172', 'SI', 'SUBDIVISION', '波德莱赫尼克', 'Podlehnik', 171),
('SI-173', 'SI', 'SUBDIVISION', '波尔泽拉', 'Polzela', 172),
('SI-174', 'SI', 'SUBDIVISION', '普雷博尔德', 'Prebold', 173),
('SI-175', 'SI', 'SUBDIVISION', '普雷瓦列', 'Prevalje', 174),
('SI-176', 'SI', 'SUBDIVISION', '拉兹克里日耶', 'Razkrižje', 175),
('SI-177', 'SI', 'SUBDIVISION', '波霍尔山里布尼察', 'Ribnica na Pohorju', 176),
('SI-178', 'SI', 'SUBDIVISION', '德拉瓦河畔塞尔尼察', 'Selnica ob Dravi', 177),
('SI-179', 'SI', 'SUBDIVISION', '索德拉日察', 'Sodražica', 178),
('SI-180', 'SI', 'SUBDIVISION', '索尔恰瓦', 'Solčava', 179),
('SI-181', 'SI', 'SUBDIVISION', '圣安娜', 'Sveta Ana', 180),
('SI-182', 'SI', 'SUBDIVISION', '斯洛文尼亚山地圣安德拉日', 'Sveti Andraž v Slovenskih goricah', 181),
('SI-183', 'SI', 'SUBDIVISION', '申佩特尔-弗尔托伊巴', 'Šempeter-Vrtojba', 182),
('SI-184', 'SI', 'SUBDIVISION', '塔博尔', 'Tabor', 183),
('SI-185', 'SI', 'SUBDIVISION', '特尔诺夫斯卡瓦斯', 'Trnovska Vas', 184),
('SI-186', 'SI', 'SUBDIVISION', '特尔津', 'Trzin', 185),
('SI-187', 'SI', 'SUBDIVISION', '大波拉纳', 'Velika Polana', 186),
('SI-188', 'SI', 'SUBDIVISION', '韦尔泽伊', 'Veržej', 187),
('SI-189', 'SI', 'SUBDIVISION', '弗兰斯科', 'Vransko', 188),
('SI-190', 'SI', 'SUBDIVISION', '扎莱茨', 'Žalec', 189),
('SI-191', 'SI', 'SUBDIVISION', '热塔莱', 'Žetale', 190),
('SI-192', 'SI', 'SUBDIVISION', '日罗夫尼察', 'Žirovnica', 191),
('SI-193', 'SI', 'SUBDIVISION', '茹热姆贝克', 'Žužemberk', 192),
('SI-194', 'SI', 'SUBDIVISION', '利蒂亚什马尔特诺', 'Šmartno pri Litiji', 193),
('SI-195', 'SI', 'SUBDIVISION', '阿帕切', 'Apače', 194),
('SI-196', 'SI', 'SUBDIVISION', '齐尔库拉内', 'Cirkulane', 195),
('SI-197', 'SI', 'SUBDIVISION', '克尔卡河畔科斯坦耶维察', 'Kostanjevica na Krki', 196),
('SI-198', 'SI', 'SUBDIVISION', '马科莱', 'Makole', 197),
('SI-199', 'SI', 'SUBDIVISION', '莫克罗诺格-特雷贝尔诺', 'Mokronog-Trebelno', 198),
('SI-200', 'SI', 'SUBDIVISION', '波利查内', 'Poljčane', 199),
('SI-201', 'SI', 'SUBDIVISION', '伦切-沃格尔斯科', 'Renče-Vogrsko', 200),
('SI-202', 'SI', 'SUBDIVISION', '德拉瓦河畔斯雷迪谢', 'Središče ob Dravi', 201),
('SI-203', 'SI', 'SUBDIVISION', '斯特拉扎', 'Straža', 202),
('SI-204', 'SI', 'SUBDIVISION', '斯洛文尼亚山地圣三一', 'Sveta Trojica v Slovenskih goricah', 203),
('SI-205', 'SI', 'SUBDIVISION', '圣托马斯', 'Sveti Tomaž', 204),
('SI-206', 'SI', 'SUBDIVISION', '什马尔耶什克托普利采', 'Šmarješke Toplice', 205),
('SI-207', 'SI', 'SUBDIVISION', '戈尔耶', 'Gorje', 206),
('SI-208', 'SI', 'SUBDIVISION', '洛格-德拉戈梅尔', 'Log-Dragomer', 207),
('SI-209', 'SI', 'SUBDIVISION', '萨维尼亚河畔雷奇察', 'Rečica ob Savinji', 208),
('SI-210', 'SI', 'SUBDIVISION', '斯洛文尼亚山地圣尤里', 'Sveti Jurij v Slovenskih goricah', 209),
('SI-211', 'SI', 'SUBDIVISION', '申特鲁佩特', 'Šentrupert', 210),
('SI-212', 'SI', 'SUBDIVISION', '米尔纳', 'Mirna', 211),
('SI-213', 'SI', 'SUBDIVISION', '安卡兰', 'Ankaran', 212);
