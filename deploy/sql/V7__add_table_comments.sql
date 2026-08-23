-- 补全现有表/列的 COMMENT，此前的迁移脚本（V1-V6）都没写。
-- 从这个版本开始，新建表的迁移脚本必须同步写 COMMENT ON TABLE/COLUMN，不再事后补。

COMMENT ON TABLE booking_order IS '预约撮合订单（Booking 限界上下文）';
COMMENT ON COLUMN booking_order.id IS '主键ID';
COMMENT ON COLUMN booking_order.patron_id IS '下单老板用户ID';
COMMENT ON COLUMN booking_order.companion_id IS '陪玩用户ID';
COMMENT ON COLUMN booking_order.sku_id IS '服务SKU ID';
COMMENT ON COLUMN booking_order.time_range_start IS '预约时段开始时间';
COMMENT ON COLUMN booking_order.time_range_end IS '预约时段结束时间';
COMMENT ON COLUMN booking_order.price_snapshot IS '下单时的价格快照';
COMMENT ON COLUMN booking_order.status IS '订单状态：DRAFT/PENDING_PAYMENT/PAID/MATCHING/ACCEPTED/IN_SERVICE/COMPLETED/CANCELLED/EXPIRED/DISPUTED/REFUNDED';
COMMENT ON COLUMN booking_order.version IS '乐观锁版本号';
COMMENT ON COLUMN booking_order.created_at IS '创建时间';
COMMENT ON COLUMN booking_order.updated_at IS '更新时间';

COMMENT ON TABLE identity_user IS '用户账号（Identity & Access 限界上下文），手机号/邮箱二选一注册';
COMMENT ON COLUMN identity_user.id IS '主键ID';
COMMENT ON COLUMN identity_user.phone IS '手机号，唯一（可为空，与 email 二选一）';
COMMENT ON COLUMN identity_user.email IS '邮箱，唯一（可为空，与 phone 二选一）';
COMMENT ON COLUMN identity_user.password_hash IS '密码哈希（BCrypt），为空表示只能用验证码登录，不接受密码登录';
COMMENT ON COLUMN identity_user.status IS '账号状态：ACTIVE/DISABLED/BANNED';
COMMENT ON COLUMN identity_user.membership_tier_level IS '会员等级数值（挂点字段，梯度由 MembershipTier 接口实现决定，见 mystikos-common）';
COMMENT ON COLUMN identity_user.membership_tier_code IS '会员等级编码（挂点字段，同上）';
COMMENT ON COLUMN identity_user.created_at IS '创建时间';
COMMENT ON COLUMN identity_user.nickname IS '昵称（老板侧资料，S2）';
COMMENT ON COLUMN identity_user.privacy_anonymous IS '是否匿名上榜（老板侧隐私设置，S2）';

COMMENT ON TABLE identity_user_role IS '用户-角色关联（一个用户可同时拥有多个角色）';
COMMENT ON COLUMN identity_user_role.user_id IS '用户ID';
COMMENT ON COLUMN identity_user_role.role IS '角色编码，对应 identity_role.code / Java 枚举 Role';

COMMENT ON TABLE identity_role_permission IS '角色-权限编码关联；权限编码由业务后续定义，框架不预置';
COMMENT ON COLUMN identity_role_permission.role IS '角色编码';
COMMENT ON COLUMN identity_role_permission.permission_code IS '权限编码';

COMMENT ON TABLE identity_role IS 'Role 枚举的数据库镜像，只用于给 identity_user_role/identity_role_permission 提供外键完整性；展示名/排序仍以 Java 枚举为权威来源';
COMMENT ON COLUMN identity_role.code IS '角色编码，主键，对应 Java 枚举 Role';
COMMENT ON COLUMN identity_role.display_name IS '角色展示名';
COMMENT ON COLUMN identity_role.sort_order IS '展示排序';
COMMENT ON COLUMN identity_role.created_at IS '创建时间';

COMMENT ON TABLE identity_oauth_binding IS '第三方登录绑定（一个用户可绑多个 provider，同一 provider 账号只能绑一个用户）';
COMMENT ON COLUMN identity_oauth_binding.id IS '主键ID';
COMMENT ON COLUMN identity_oauth_binding.user_id IS '绑定到的用户ID';
COMMENT ON COLUMN identity_oauth_binding.provider IS '第三方登录提供商编码，如 discord';
COMMENT ON COLUMN identity_oauth_binding.provider_user_id IS '第三方平台上的用户ID';
COMMENT ON COLUMN identity_oauth_binding.bound_at IS '绑定时间';

COMMENT ON TABLE identity_refresh_token IS 'Refresh Token（只存哈希，可主动吊销，不是无状态 JWT）';
COMMENT ON COLUMN identity_refresh_token.id IS '主键ID';
COMMENT ON COLUMN identity_refresh_token.user_id IS '所属用户ID';
COMMENT ON COLUMN identity_refresh_token.token_hash IS 'Refresh Token 的 SHA-256 哈希，不存明文';
COMMENT ON COLUMN identity_refresh_token.expires_at IS '过期时间';
COMMENT ON COLUMN identity_refresh_token.revoked_at IS '吊销时间，非空即已失效（登出/换设备触发）';
